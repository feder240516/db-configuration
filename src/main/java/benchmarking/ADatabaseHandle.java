package benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ai.libs.jaicore.components.api.IComponentInstance;

public abstract class ADatabaseHandle implements IDatabase {

	List<String> queries = Arrays.asList("","");
	protected int MAX_CONNECTION_RETRIES = 3;
	protected int MAX_ALLOWED_PORTS;
	//protected int firstPort;
	protected int[] portsToUse;
	protected HashMap<Integer, Boolean> _usedPorts; // var to check which ports are being used
	protected int _nextPortOffset;
	protected HashMap<Integer, Process> processes = new HashMap<>();
	protected HashMap<Integer, Connection> connections = new HashMap<>();
	
	/**
	 * Create a database handle allowing to use specified port and next 9 ports.
	 * @param firstPort the first port to use
	 */
	public ADatabaseHandle(int firstPort) {
		this(firstPort, 10);
	}
	
	/**
	 * Create a database handle allowing to use specified port and the next specified ports. 
	 * @param firstPort The first port to be used
	 * @param numberOfPorts the number of ports including firstPort that can be used
	 */
	public ADatabaseHandle(int firstPort, int numberOfPorts) {
		if(numberOfPorts <= 0) throw new IllegalArgumentException("Number of ports used must be a positive integer");
		portsToUse = new int[numberOfPorts];
		for(int i = 0; i < numberOfPorts; ++i) {
			portsToUse[i] = i + firstPort;
		}
		initHandler();		
	}
	
	public ADatabaseHandle(int[] portsToUse) {
		this.portsToUse = portsToUse;
		initHandler();
	}
	
	
	private void initHandler() {
		if(portsToUse == null || portsToUse.length == 0) throw new IllegalArgumentException("Database Handle cannot be initialized without an array of ports to be used.");
		_nextPortOffset = 0;
		MAX_ALLOWED_PORTS = portsToUse.length;
		_usedPorts = new HashMap<>();
		for(int port: portsToUse) {
			_usedPorts.put(port, false);
		}
	}
	
	
	protected abstract String[] getStartCommand(IComponentInstance component, int port);
	protected abstract String getDbDirectory();
	protected abstract void createAndFillDatabase(int port);
	protected abstract void setupInitedDB(IComponentInstance component, int port);
	public abstract void stopServer(int port);
	protected abstract String getQueryCommand(int numTest);
	protected final int useNextAvailablePort() {
		int triedPorts = 0;
		int nextPort = -1;
		boolean foundPort = false;
		while(triedPorts < MAX_ALLOWED_PORTS && !foundPort) {
			nextPort = portsToUse[_nextPortOffset];
			if (!_usedPorts.get(nextPort)) {
				_usedPorts.put(nextPort, true);
				foundPort = true;
			}
			triedPorts++;
			_nextPortOffset = (_nextPortOffset + 1) % MAX_ALLOWED_PORTS;
		}
		if(!foundPort) throw new RuntimeException("There are no free ports to use");
		return nextPort;
	}
	
	protected final void freePort(int port) {
		_usedPorts.put(port, false);
	}
	
	public int initiateServer(IComponentInstance component) {
		System.out.println("Starting server");
		//
		int port = useNextAvailablePort();
		String[] comandoArray = getStartCommand(component, port);
		//
		ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
		processBuilder.directory(new File(getDbDirectory()));
		try {
			//processBuilder.redirectInput();
			//processBuilder.redirectError();			
			Process process = processBuilder.start();
			InputStream inStream = process.getInputStream();
			InputStream errStream = process.getErrorStream();

			inStream.close();
			errStream.close();
			
			
			Connection conn = null;
			// tries multiple connections to database
			for(int i = 0; i < MAX_CONNECTION_RETRIES && conn == null; ++i) {
				System.out.println("Trying to establish a connection...");
				// wait 5 seconds to allow server to initiate
				TimeUnit.SECONDS.sleep(5);
				conn = getConnection(port);
			}
			if (conn != null && !conn.isClosed()) {
				createAndFillDatabase(port);
				setupInitedDB(component, port);
				System.out.println("Server has been inited");
			} else {
				throw new SQLException(String.format("Could not connect to database after %d tries",MAX_CONNECTION_RETRIES));
			}
			
			//process.waitFor();
			//System.out.println(String.format("Exit value is %d", process.exitValue()));
			//System.out.println(String.format("Exit value is %d", process.()));
			
		} catch (IOException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return port;
	}
	
	@Override
	public double benchmarkQuery(int numTest, int port) {
		double score = Double.MAX_VALUE;
		try {
			
			Connection conn = getConnection(port);
			if (conn != null) {
				switch(numTest) {
				case 1:
					PreparedStatement ps = conn.prepareStatement(getQueryCommand(numTest));
					//ps = conn.prepareStatement("select * from worker");
					Date before = new Date();
					ps.execute();
					Date after = new Date();
					score = after.getTime() - before.getTime();
					break;
				default:
					throw new AssertionError("Invalid test number");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return score;
	}

	@Override
	public Connection getConnection(int port) {
		Connection conn = null;
		try {
			conn = connections.get(port);
			if (conn == null || conn.isClosed()) {
				String dbUrl = getConnectionString(port);
				conn = DriverManager.getConnection(dbUrl);
			}
		} catch (SQLException e1) {
			System.out.println(String.format("Error in port %d", port));
			conn = null;
		}
		return conn;
		//
	}
	
	protected abstract String getConnectionString (int port);

}

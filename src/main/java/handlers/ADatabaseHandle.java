package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.healthmarketscience.sqlbuilder.Query;

import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

public abstract class ADatabaseHandle implements IDatabase {

	List<String> queries = Arrays.asList("","");
	protected int MAX_CONNECTION_RETRIES = 3;
	protected int MAX_ALLOWED_PORTS;
	protected int allowedThreads;
	protected int[] portsToUse;
	protected HashMap<Integer, Boolean> _usedPorts; // var to check which ports are being used
	protected int _nextPortOffset;
	protected HashMap<Integer, Process> processes = new HashMap<>();
	protected HashMap<Integer, Connection> connections = new HashMap<>();
	protected TestDescription testDescription;
	protected Semaphore semaphore;
	
	/**
	 * Create a database handle allowing to use specified port and next 9 ports.
	 * @param firstPort the first port to use
	 */
	public ADatabaseHandle(int firstPort, int allowedThreads, TestDescription testDescription) {
		this(firstPort, 10, allowedThreads, testDescription);
	}
	
	/**
	 * Create a database handle allowing to use specified port and the next specified ports. 
	 * @param firstPort The first port to be used
	 * @param numberOfPorts the number of ports including firstPort that can be used
	 */
	public ADatabaseHandle(int firstPort, int numberOfPorts, int allowedThreads, TestDescription testDescription) {
		if(numberOfPorts <= 0) throw new IllegalArgumentException("Number of ports used must be a positive integer");
		portsToUse = new int[numberOfPorts];
		for(int i = 0; i < numberOfPorts; ++i) {
			portsToUse[i] = i + firstPort;
		}
		initHandler(allowedThreads, testDescription);		
	}
	
	public ADatabaseHandle(int[] portsToUse, int allowedThreads, TestDescription testDescription) {
		this.portsToUse = portsToUse;
		initHandler(allowedThreads, testDescription);
	}
	
	private void initHandler(int allowedThreads, TestDescription testDescription) {
		if(portsToUse == null || portsToUse.length == 0) throw new IllegalArgumentException("Database Handle cannot be initialized without an array of ports to be used.");
		if(allowedThreads <= 0) throw new IllegalArgumentException("allowedThreads must be a positive value");
		if(testDescription == null) throw new NullPointerException("You must provide a testDescription");
		this.allowedThreads = allowedThreads;
		this.testDescription = testDescription;
		_nextPortOffset = 0;
		MAX_ALLOWED_PORTS = portsToUse.length;
		_usedPorts = new HashMap<>();
		for(int port: portsToUse) {
			_usedPorts.put(port, false);
		}
		this.semaphore = new Semaphore(allowedThreads, true);
	}
	
	protected abstract String[] getStartCommand(IComponentInstance component, int port);
	public abstract void stopServer(int port);
	protected abstract String getDbDirectory(int port);
	protected abstract void createAndFillDatabase(int port);
	protected abstract void setupInitedDB(IComponentInstance component, int port);
	protected abstract String getQueryCommand(int numTest);
	
	protected final synchronized int useNextAvailablePort() {
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
	
	public final void freePort(int port) {
		stopServer(port);
		_usedPorts.put(port, false);
	}
	
	protected final boolean existsConnection(int port) {
		Connection conn = connections.get(port);
		try {
			return (conn != null && !conn.isClosed());
		} catch (SQLException e) {
			return false;
		}
	}
	
	public int initiateServer(IComponentInstance component) throws IOException, SQLException, InterruptedException {
		
			int port = useNextAvailablePort();	
			System.out.println("Starting server on port " + port);
			String[] comandoArray = getStartCommand(component, port);
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.directory(new File(getDbDirectory(port)));
			
			Process process = null;
			while(process == null || !process.isAlive()) {
				try {
					process = processBuilder.start();
					processes.put(port, process);
					InputStream inStream = process.getInputStream();
					InputStream errStream = process.getErrorStream();
					inStream.close();
					errStream.close();
					Connection conn = null;
					// tries multiple connections to database
					for(int i = 0; i < MAX_CONNECTION_RETRIES && conn == null; ++i) {
						TimeUnit.SECONDS.sleep(5); // wait 5 seconds to allow server to initiate
						//System.out.println("Trying to establish a connection... on port " + port);
						conn = getConnection(port);
					}
					if (conn != null && !conn.isClosed()) {
						setupInitedDB(component, port);
						//System.out.println("Server has been inited on port " + port);
					} else {
						throw new SQLException(String.format("Could not connect to database after %d tries",MAX_CONNECTION_RETRIES));
					}
				} catch (IOException | SQLException | InterruptedException e) {
					System.out.println("Could not connect to port " + port + " ... Restarting process");
					// throw e;
					// e.printStackTrace();
				}
			}
		
		return port;
	}
	
	@Override
	public double benchmarkQuery(IComponentInstance instance) throws InterruptedException {
		semaphore.acquire();
		int port = 0;
		try {
			port = initiateServer(instance);
		} catch (IOException | SQLException | InterruptedException e1) {
			e1.printStackTrace();
			port = 0;
		}
		double score = 0;
		if (port == 0) {
			System.out.println("Server could not be intited");
			score = Double.MAX_VALUE;
		}else {
			try (Connection conn = getConnection(port)) {
				if (conn != null) {
					for(Entry<Integer, List<Query>> entry: testDescription.queries.entrySet()) {
						for(Query q: entry.getValue()) {
							PreparedStatement ps = conn.prepareStatement(q.toString());
							Date before = new Date();
							ps.execute();
							Date after = new Date();
							score += after.getTime() - before.getTime();
							ps.close();
							
							
							System.out.println("A query was executed");
							System.out.println(String.format("Score %f for port %d",score,port));
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				score = Double.MAX_VALUE;
			}
			freePort(port);
		}
		semaphore.release();
		return score;
	}
	
	public final void destroyHandler() {
		for (int port: portsToUse) {
			if(processes.get(port) != null) {
				stopServer(port);
			}
		}
	}

	@Override
	public Connection getConnection(int port) {
		Connection conn = null;
		//while(conn == null) {
			try {
				String dbUrl = getConnectionString(port);
				conn = DriverManager.getConnection(dbUrl);	
			} catch (SQLException e1) {
				//e1.printStackTrace();
				System.out.println(String.format("Could not connect to port %d", port));
				conn = null;
			}
		//}
		return conn;
	}
	
	protected abstract String getConnectionString (int port);

}

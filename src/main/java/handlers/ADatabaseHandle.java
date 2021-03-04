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
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.healthmarketscience.sqlbuilder.Query;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import helpers.TestDescription;
import managers.PortManager;
import scala.NotImplementedError;

public abstract class ADatabaseHandle implements IDatabase {

	//List<String> queries = Arrays.asList("","");
	
	//protected int MAX_ALLOWED_PORTS;
	//protected int allowedThreads;
	//protected int[] portsToUse;
	//protected HashMap<Integer, Boolean> _usedPorts; // var to check which ports are being used
	//protected int _nextPortOffset;
	//protected HashMap<Integer, Process> processes = new HashMap<>();
	//protected HashMap<Integer, Connection> connections = new HashMap<>();
	//protected TestDescription testDescription;
	//protected Semaphore semaphore;
	protected int MAX_CONNECTION_RETRIES = 3;
	protected Process process;
	//protected Connection connection;
	protected IComponentInstance componentInstance;
	protected String createdInstancePath;
	protected int port;
	
	public ADatabaseHandle(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		this.componentInstance = ci;
		this.port = PortManager.getInstance().acquireAnyPort();
		createDBInstance();
		createAndFillDatabase();
		//initHandler();
	}
	
	/*private void initHandler() throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		initiateServer(); // ! Posiblemente haya que pasarlo a metodo benchmark
	}*/
	
	protected abstract String[] getStartCommand();
	public abstract void stopServer();
	protected abstract void createAndFillDatabase();
	protected abstract void setupInitedDB();
	protected abstract String getConnectionString ();
	protected abstract String getInstancesPath();
	protected abstract String getBasePath();
	protected abstract String getDbDirectory(); // unused
	
	// ! ya no hace falta que retorne el puerto
	public void initiateServer() throws IOException, SQLException, InterruptedException, UnavailablePortsException {
			System.out.println("Starting server on port " + port);
			String[] comandoArray = getStartCommand();
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.directory(new File(createdInstancePath));
			process = null;
			while(process == null || !process.isAlive()) {
				Connection conn = null;
				try {
					this.process = processBuilder.start();
					InputStream inStream = process.getInputStream();
					InputStream errStream = process.getErrorStream();
					inStream.close();
					errStream.close();
					// tries multiple connections to database
					for(int i = 0; i < MAX_CONNECTION_RETRIES && conn == null; ++i) {
						TimeUnit.SECONDS.sleep(5); // wait 5 seconds to allow server to initiate
						//System.out.println("Trying to establish a connection... on port " + port);
						conn = getConnection();
					}
					if (conn != null && !conn.isClosed()) {
						setupInitedDB();
						//System.out.println("Server has been inited on port " + port);
					} else {
						throw new SQLException(String.format("Could not connect to database after %d tries",MAX_CONNECTION_RETRIES));
					}
					conn.close();
				} catch (IOException | SQLException | InterruptedException e) {
					System.out.println("Could not connect to port " + port + " ... Restarting process");
					// throw e;
					// e.printStackTrace();
				} finally {
					if (conn != null && !conn.isClosed()) { conn.close(); }
				}
			}
		
		// return port; // ! ya no debería retornar port
	}
	
	public void createDBInstance() throws IOException {
		File dataDir = new File(getBasePath());
		createdInstancePath = getInstancesPath() + "/" + UUID.randomUUID();
		File destDir = new File(createdInstancePath);
	    FileUtils.copyDirectory(dataDir, destDir);
	    System.out.println("The instance " + createdInstancePath + " on port " + port + " was created");
	}
	
	public double benchmarkQuery(Query query) throws InterruptedException, SQLException {
		try (Connection conn = getConnection()){
			PreparedStatement ps = conn.prepareStatement(query.toString());
			Date before = new Date();
			ps.execute();
			Date after = new Date();
			double score = after.getTime() - before.getTime();
			ps.close();
			System.out.println("A query was executed");
			System.out.println(String.format("Score %f for port %d",score,port));
			return score;
		}	
	}
	
	protected Connection getConnection() {
		Connection conn = null;
		//while(conn == null) {
			try {
				String dbUrl = getConnectionString();
				conn = DriverManager.getConnection(dbUrl);	
			} catch (SQLException e1) {
				System.out.println(String.format("Could not connect to port %d", port));
				conn = null;
			}
		//}
		return conn;
	}
	
	/**
	 * Delete instance from disk and free port
	 */
	public void cleanup() {
		throw new NotImplementedError();
	}
}

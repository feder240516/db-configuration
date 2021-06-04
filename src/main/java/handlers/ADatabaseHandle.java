package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jooq.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import managers.PortManager;
import managers.db.parameters.IDatabaseParameterManager;

public abstract class ADatabaseHandle implements IDatabase {
	protected int MAX_CONNECTION_RETRIES = 5;
	protected Process process;
	protected IComponentInstance componentInstance;
	protected String createdInstancePath;
	protected int port;
	protected IDatabaseParameterManager databaseParameterManager;
	protected UUID ID;
	protected boolean shouldPrintResults;
	protected static boolean driversInitialized = false; 
	protected final Logger logger = LoggerFactory.getLogger(ADatabaseHandle.class);

	
	public static void initializeDrivers() throws ClassNotFoundException {
		if (!driversInitialized) {
			Class.forName ("org.mariadb.jdbc.Driver");
			Class.forName ("org.postgresql.Driver");
			Class.forName ("org.hsqldb.jdbcDriver");
			Class.forName ("org.apache.derby.client.ClientAutoloadedDriver");
			driversInitialized = true;
		}
	}
	
	public ADatabaseHandle(IComponentInstance ci, IDatabaseParameterManager databaseParameterManager) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		try{
			initializeDrivers();
			this.componentInstance = ci;
			this.databaseParameterManager = databaseParameterManager;
			this.ID = UUID.randomUUID();
			this.port = PortManager.getInstance().acquireAnyPort();
			this.shouldPrintResults = false;
			createdInstancePath = getInstancesPath() + "/" + ID;
			System.out.println("To create");
			createDBInstance();
			System.out.println("To initiate");
			initiateServer();
			System.out.println("To setup");
			setupInitedDB();
			createAndFillDatabase();
			System.out.println("To stop");
			stopServer();
			TimeUnit.SECONDS.sleep(5);
		}catch(UnavailablePortsException | IOException | SQLException | InterruptedException | ClassNotFoundException e) {
			e.printStackTrace(); 
			cleanup();
			throw e;
		}
		//initHandler();
	}
	
	public void printResultsAfterExecution(boolean print) {
		this.shouldPrintResults = print;
	}
	
	public UUID getUUID() {
		return ID;
	}
	
	/*private void initHandler() throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		initiateServer(); // ! Posiblemente haya que pasarlo a metodo benchmark
	}*/
	
	protected abstract String[] getStartCommand();
	protected String getStartCommandJoint() {return null;}
	public abstract void stopServer();
	protected abstract void createAndFillDatabase();
	protected void setupInitedDB() throws SQLException {
		Map<String, String> params = componentInstance.getParameterValues();		
		try (Connection conn = getConnection()){
			
			for(String param: params.keySet()) {
				try {
					String configurationQuery = databaseParameterManager.getCommand(param, params.get(param));
					if (configurationQuery != null) {
						PreparedStatement ps = conn.prepareStatement(configurationQuery);
						ps.execute();
						ps.close();
					}
				} catch(SQLException e) {
					System.err.println(e.getMessage());
				}
			}
			
		}
		
	}
	protected abstract String getConnectionString ();
	protected abstract String getInstancesPath();
	protected abstract String getBasePath();
	protected abstract String getDbDirectory(); // unused
	
	// ! ya no hace falta que retorne el puerto
	public void initiateServer() throws IOException, SQLException, InterruptedException, UnavailablePortsException {
			String[] comandoArray = getStartCommand();	
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			//processBuilder.redirectErrorStream();
			
			processBuilder.directory(new File(createdInstancePath));
			/* if (SystemUtils.IS_OS_LINUX) {
				processBuilder.redirectOutput(new File("/home/ailibs/output.txt"));
				processBuilder.redirectError(new File("/home/ailibs/error.txt"));
			} */
			process = null;
			Connection conn = null;
			try {
				if (process == null) {
					this.process = processBuilder.start();
					InputStream inStream = process.getInputStream();
					InputStream errStream = process.getErrorStream();
					if (!SystemUtils.IS_OS_LINUX) {
						inStream.close();
						errStream.close();
					}
				} else {
					
				}
				// tries multiple connections to database
				conn = resillientGetConnection(MAX_CONNECTION_RETRIES);
				if (conn != null && !conn.isClosed()) {
					//setupInitedDB();
				} else {
					throw new SQLException(String.format("Could not connect to database after %d tries",MAX_CONNECTION_RETRIES));
				}
				conn.close();
			} catch (IOException | SQLException | InterruptedException e) {
				System.err.println("Could not connect to port " + port);
				throw e;
			} finally {
				if (conn != null && !conn.isClosed()) { conn.close(); }
			}
		
		
		// return port; // ! should not return port anymore
	}
	
	public void createDBInstance() throws IOException {
		//System.out.println(getBasePath());
		System.out.println("To copy");
		File dataDir = new File(getBasePath());
		File destDir = new File(createdInstancePath);
		System.out.println("Prepared to copy");
	    FileUtils.copyDirectory(dataDir, destDir);
	    System.out.println("The instance " + createdInstancePath + " on port " + port + " was created");
	}
	
	public void printResultSet(PreparedStatement ps) throws SQLException {
		ResultSet resultSet = ps.getResultSet();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		for (int i = 1; i <= columnsNumber; i++) {
	        if (i > 1) System.out.print(",  ");
	        String columnName = rsmd.getColumnName(i);
	        System.out.print(columnName);
	    }
		System.out.println();
		while(resultSet.next()) {
			for (int i = 1; i <= columnsNumber; i++) {
		        if (i > 1) System.out.print(",  ");
		        String columnValue = resultSet.getString(i);
		        System.out.print(columnValue);
		    }
		    System.out.println();
		}
	}
	
	public double benchmarkQuery(String query) throws InterruptedException, SQLException {
		try (Connection conn = getConnection()){
			PreparedStatement ps = conn.prepareStatement(query);
			Date before = new Date();
			boolean resultType = ps.execute();
			if(resultType && shouldPrintResults) {
				printResultSet(ps);
			}
			Date after = new Date();
			double score = after.getTime() - before.getTime();
			ps.close();
			System.out.println(String.format("Score %f for port %d",score,port));
			return score;
		}	
	}
	
	protected Connection resillientGetConnection(int retries) throws InterruptedException {
		Connection conn = null;
		for(int i = 0; i < retries && conn == null; ++i) {
			//if (i > 0) { System.out.println("Retrying..."); }
			TimeUnit.SECONDS.sleep(5); // wait 5 seconds to allow server to initiate
			conn = getConnection();
		}
		return conn;
	}
	
	protected Connection getConnection() {
		Connection conn = null;
		try {
			String dbUrl = getConnectionString();
			conn = DriverManager.getConnection(dbUrl);	
		} catch (SQLException e1) {
			conn = null;
		}
		return conn;
	}
	
	/**
	 * Delete instance from disk and free port
	 */
	public void cleanup() {
		PortManager.getInstance().releasePort(port);
		for(int i = 0; i < 3; ++i) {
			try {	
				FileUtils.deleteDirectory(new File(createdInstancePath));
				i = 3;
			} catch(Exception | Error e) {
				if(i == 2) { e.printStackTrace(); } 
				else try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e1) {}
			}
		}
		
		//throw new NotImplementedError();
	}
}

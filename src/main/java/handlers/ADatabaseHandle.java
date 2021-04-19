package handlers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jooq.Query;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import managers.PortManager;
import managers.db.parameters.IDatabaseParameterManager;

public abstract class ADatabaseHandle implements IDatabase {
	protected int MAX_CONNECTION_RETRIES = 3;
	protected Process process;
	protected IComponentInstance componentInstance;
	protected String createdInstancePath;
	protected int port;
	protected IDatabaseParameterManager databaseParameterManager;
	protected UUID ID;
	protected boolean shouldPrintResults;
	
	public ADatabaseHandle(IComponentInstance ci, IDatabaseParameterManager databaseParameterManager) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		this.componentInstance = ci;
		this.databaseParameterManager = databaseParameterManager;
		this.ID = UUID.randomUUID();
		this.port = PortManager.getInstance().acquireAnyPort();
		this.shouldPrintResults = false;
		createDBInstance();
		initiateServer();
		setupInitedDB();
		createAndFillDatabase();
		stopServer();
		TimeUnit.SECONDS.sleep(5);
		//initHandler();
	}
	
	public UUID getUUID() {
		return ID;
	}
	
	/*private void initHandler() throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		initiateServer(); // ! Posiblemente haya que pasarlo a metodo benchmark
	}*/
	
	protected abstract String[] getStartCommand();
	public abstract void stopServer();
	protected abstract void createAndFillDatabase();
	protected void setupInitedDB() throws SQLException {
		Map<String, String> params = componentInstance.getParameterValues();		
		try (Connection conn = getConnection()){
			
			for(String param: params.keySet()) {
				String configurationQuery = databaseParameterManager.getCommand(param, params.get(param));
				if (configurationQuery != null) {
					PreparedStatement ps = conn.prepareStatement(configurationQuery);
					ps.execute();
					ps.close();
				}
			}
			//ps.execute();
			
			
			System.out.println("Parameters were applied successfully");
		}
		
	}
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
			System.out.println("createdInstancePath: " + createdInstancePath);
			process = null;
			Connection conn = null;
			try {
				if (process == null) {
					this.process = processBuilder.start();
					InputStream inStream = process.getInputStream();
					InputStream errStream = process.getErrorStream();
					inStream.close();
					errStream.close();
					
					System.out.println("Server has been inited");
				} else {
					System.out.println("Retry for process");
				}
				// tries multiple connections to database
				conn = resillientGetConnection(MAX_CONNECTION_RETRIES);
				if (conn != null && !conn.isClosed()) {
					//setupInitedDB();
					//System.out.println("Server has been inited on port " + port);
				} else {
					throw new SQLException(String.format("Could not connect to database after %d tries",MAX_CONNECTION_RETRIES));
				}
				conn.close();
			} catch (IOException | SQLException | InterruptedException e) {
				System.out.println("Could not connect to port " + port);
				throw e;
			} finally {
				System.out.println("finally executed");
				if (conn != null && !conn.isClosed()) { conn.close(); }
			}
		
		
		// return port; // ! ya no debería retornar port
	}
	
	public void createDBInstance() throws IOException {
		System.out.println(getBasePath());
		File dataDir = new File(getBasePath());
		createdInstancePath = getInstancesPath() + "/" + ID;
		File destDir = new File(createdInstancePath);
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
		while(resultSet.next()) {
			for (int i = 1; i <= columnsNumber; i++) {
		        if (i > 1) System.out.print(",  ");
		        String columnValue = resultSet.getString(i);
		        System.out.print(columnValue);
		    }
		    System.out.println("");
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
			System.out.println("A query was executed");
			System.out.println(String.format("Score %f for port %d",score,port));
			return score;
		}	
	}
	
	protected Connection resillientGetConnection(int retries) throws InterruptedException {
		Connection conn = null;
		for(int i = 0; i < retries && conn == null; ++i) {
			if (i > 0) { System.out.println("Retrying..."); }
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
			System.out.println(String.format("Could not connect to port %d", port));
			conn = null;
		}
		return conn;
	}
	
	/**
	 * Delete instance from disk and free port
	 */
	public void cleanup() {
		try {
			PortManager.getInstance().releasePort(port);
			FileUtils.deleteDirectory(new File(createdInstancePath));
		} catch(Exception | Error e) {
			e.printStackTrace();
		}
		//throw new NotImplementedError();
	}
}

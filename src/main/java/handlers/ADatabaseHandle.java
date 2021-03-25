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
import java.util.Map;
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
import helpers.TestResult;
import managers.PortManager;
import managers.db.parameters.IDatabaseParameterManager;
import scala.NotImplementedError;
import services.CSVService;

public abstract class ADatabaseHandle implements IDatabase {
	protected int MAX_CONNECTION_RETRIES = 3;
	protected Process process;
	protected IComponentInstance componentInstance;
	protected String createdInstancePath;
	protected int port;
	protected IDatabaseParameterManager databaseParameterManager;
	protected UUID ID;
	
	public ADatabaseHandle(IComponentInstance ci, IDatabaseParameterManager databaseParameterManager) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		this.componentInstance = ci;
		this.databaseParameterManager = databaseParameterManager;
		this.ID = UUID.randomUUID();
		this.port = PortManager.getInstance().acquireAnyPort();
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
			while(process == null /*|| !process.isAlive()*/) {
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
					for(int i = 0; i < MAX_CONNECTION_RETRIES && conn == null; ++i) {
						TimeUnit.SECONDS.sleep(5); // wait 5 seconds to allow server to initiate
						//System.out.println("Trying to establish a connection... on port " + port);
						conn = getConnection();
					}
					if (conn != null && !conn.isClosed()) {
						//setupInitedDB();
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
		System.out.println(getBasePath());
		File dataDir = new File(getBasePath());
		createdInstancePath = getInstancesPath() + "/" + ID;
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
	
	protected Connection resillientGetConnection(int retries) throws InterruptedException {
		Connection conn = null;
		for(int i = 0; i < retries && conn == null; ++i) {
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

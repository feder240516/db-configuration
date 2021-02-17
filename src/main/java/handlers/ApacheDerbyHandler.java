package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

public class ApacheDerbyHandler extends ADatabaseHandle {
	
	public static final String DATABASE_PAGE_SIZE = "DATABASE_PAGE_SIZE";
	
	// HashMap<Integer, String> dbNames;
	HashMap<Integer, String> directories = new HashMap<>();
	String instancesPath = System.getenv("DERBY_HOME") + "/db/instances";
	String baseDataPath = System.getenv("DERBY_HOME") + "/db/data";
	
	public ApacheDerbyHandler(int allowedThreads, TestDescription testDescription) {
		super(1527,allowedThreads,testDescription);
		initHandler();
	}
	
	public ApacheDerbyHandler(int[] portsToUse, int allowedThreads, TestDescription testDescription) {
		super(portsToUse,allowedThreads,testDescription);
		initHandler();
	}
	
	public ApacheDerbyHandler(int port, int numberOfPorts,int allowedThreads, TestDescription testDescription) {
		super(port,numberOfPorts,allowedThreads,testDescription);
		initHandler();
	}
	
	private void initHandler() {
		// dbNames = new HashMap<>();
		initInstances(this.portsToUse);
	}
	
	protected void initInstances(int[] portsToUse) {
		File instancesDir = new File(instancesPath);
		
		if (!instancesDir.exists()) instancesDir.mkdirs();
		
		String[] instances = instancesDir.list();
		int numberOfInstances = instances.length;
		
		for(int i = 0; i < numberOfInstances; i++){
			for(int port: portsToUse) {
				if(!directories.containsKey(port)) { 
					directories.put(port, instancesDir.getPath() + "/" + instances[i]); 
					break;
				}
			}
		}
		
		for(int port: portsToUse) {
			if(!directories.containsKey(port)) { 
				createDBInstance(port);
			}
		}
	}
	
	public void createDBInstance(int port) {
		File dataDir = new File(baseDataPath);
		
		String instancePath = instancesPath + "/" + UUID.randomUUID();
		File destDir = new File(instancePath);
		
		try {
		    FileUtils.copyDirectory(dataDir, destDir);
		    directories.put(port, instancePath);
		    System.out.println("The instance " + instancePath + " on port " + port + " was created");
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	@Override
	protected void createAndFillDatabase(int port) {}
	
	private void setDatabasePageSize(IComponentInstance component, int port) {
		String dbPageSizeStr = component.getParameterValue(DATABASE_PAGE_SIZE);
		if (dbPageSizeStr == null) return;
		Connection conn = getConnection(port);
		
		try(CallableStatement cs = 
				  conn.prepareCall("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)");){

			cs.setString(1, "derby.storage.pageSize"); 
			cs.setString(2, dbPageSizeStr); 
			cs.execute(); 
			cs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 
	}

	@Override
	protected String[] getStartCommand(IComponentInstance component, int port) {
		String derbyHome = System.getenv("DERBY_HOME");
		System.out.println(String.format("Running in port %d", port));
		if (derbyHome == null || derbyHome.equals("")) throw new RuntimeException("Environment Var DERBY_HOME must be configured to test apache derby");
		String[] comandoArray = {derbyHome + "/bin/startNetworkServer.bat", "-p", String.valueOf(port)};
		return comandoArray;
	}

	@Override
	protected String getDbDirectory(int port) {
		//String derbyHome = System.getenv("DERBY_HOME");
		return directories.get(port);
	}

	@Override
	protected void setupInitedDB(IComponentInstance component, int port) {
		setDatabasePageSize(component, port);
	}

	@Override
	public void stopServer(int port) {
		System.out.println("Stopping server");
		Connection conn = getConnection(port);
		try {
			if (conn != null && !conn.isClosed()) conn.close();
			String derbyHome = System.getenv("DERBY_HOME");
			String[] comandoArray = {derbyHome + "/bin/stopNetworkServer.bat", "-p", String.valueOf(port)};
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();
			
		} catch (IOException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getQueryCommand(int numTest) {
		switch(numTest) {
		case 1:
			return "select * from restaurants join workers on restaurants.id = workers.restid";
		default:
			throw new RuntimeException("Test inexistente");
		}
	}

	private String generateDbName() {
		StringBuilder sb = new StringBuilder("BENCHMARK_DB_");
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int numChars = chars.length();
		
		for (int i = 0; i < 20; ++i) {
			int selected = (int) Math.floor(Math.random() * numChars);
			sb.append(chars.charAt(selected));
		}
		
		String dbName = sb.toString();
		
		return dbName;
	}
	
	@Override
	protected String getConnectionString (int port) {
		String directory = directories.get(port);
		String dbUrl = String.format("jdbc:derby://localhost:%d/%s", port, "employees");
		return dbUrl;
	}
	
}

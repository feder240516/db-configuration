package handlers;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

public class PostgreSQLHandle extends ADatabaseHandle {
public static final String DATABASE_PAGE_SIZE = "DATABASE_PAGE_SIZE";
	
	// HashMap<Integer, String> dbNames;
	HashMap<Integer, String> directories = new HashMap<>();
	String instancesPath = "D:\\Bibliotecas\\Documents\\_Programming_Assets\\Postgresql\\instances";
	String baseDataPath = "D:\\Bibliotecas\\Documents\\_Programming_Assets\\Postgresql\\data";
	
	public PostgreSQLHandle(int[] portsToUse, TestDescription testDescription, int allowedThreads) {
		super(portsToUse,allowedThreads,testDescription);
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

	@Override
	protected String[] getStartCommand(IComponentInstance component, int port) {
		String postgresqlHome = System.getenv("POSTGRESQL_HOME");
		System.out.println(String.format("Running in port %d", port));
		if (postgresqlHome == null || postgresqlHome.equals("")) throw new RuntimeException("Environment Var postgresqlHome must be configured to test PostgreSQL");
		String[] comandoArray = {"\"" + postgresqlHome + "/bin/pg_ctl" + "\"", "-D", directories.get(port), "-l", directories.get(port) + "/log.txt", "-o", String.format("\"-F -p %d\"", port), "start"};
		return comandoArray;
	}

	@Override
	protected String getDbDirectory(int port) {
		return directories.get(port);
	}

	@Override
	protected void setupInitedDB(IComponentInstance component, int port) {
		
	}

	@Override
	public void stopServer(int port) {
		System.out.println("Stopping server");
		Connection conn = getConnection(port);
		try {
			if (conn != null && !conn.isClosed()) conn.close();
			String postgresqlHome = System.getenv("POSTGRESQL_HOME");
			String[] comandoArray = {postgresqlHome + "/bin/pg_ctl", "-D", directories.get(port), "-l", directories.get(port) + "/log.txt", String.format("-o \"-F -p %d\"", port), "stop"};
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
	
	@Override
	protected String getConnectionString (int port) {
		String directory = directories.get(port);
		String dbUrl = String.format("jdbc:postgresql://localhost:%d/%s?user=%s&password=%s", port, "employees", "feder", "root");
		return dbUrl;
	}
}

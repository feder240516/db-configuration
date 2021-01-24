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




import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

public class ApacheDerbyHandler extends ADatabaseHandle {
	
	public static final String DATABASE_PAGE_SIZE = "DATABASE_PAGE_SIZE";
	
	HashMap<Integer, String> dbNames;
	HashMap<Integer, String> directories;
	
	public ApacheDerbyHandler(TestDescription testDescription) {
		super(1527,testDescription);
		dbNames = new HashMap<>();
	}
	
	public ApacheDerbyHandler(int port, int numberOfPorts, TestDescription testDescription) {
		super(port,numberOfPorts,testDescription);
		dbNames = new HashMap<>();
	}
	
	@Override
	protected void createAndFillDatabase(int port) {
		try {
			dbNames.put(port, generateDbName());
			System.out.println(String.format("Using database %s for apache derby in port %d",dbNames.get(port),port));
			Connection conn = getConnection(port);
			int NUM_RESTAURANTS = 10;
			int NUM_WORKERS = 1000;
			PreparedStatement ps;
			conn.prepareStatement("create table restaurants(id int primary key, name varchar(255))").execute();
			conn.prepareStatement("create table workers(id int primary key, name varchar(255), restId int REFERENCES restaurants(id))").execute();
			ps = conn.prepareStatement("insert into restaurants values(?, ?)");
			for (int i = 0; i < NUM_RESTAURANTS; ++i) {
				
				ps.setInt(1, i+1);
				ps.setString(2, "a");
				ps.execute();
			}
			ps = conn.prepareStatement("insert into workers values(?, ?, ?)");
			for (int i = 0; i < NUM_WORKERS; ++i) {
				
				ps.setInt(1, i+1);
				ps.setString(2, "b");
				ps.setInt(3, ((i+1) % NUM_RESTAURANTS)+1);
				ps.execute();
				if (i%10000 == 0) System.out.println(i);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setDatabasePageSize(IComponentInstance component, int port) {
		String dbPageSizeStr = component.getParameterValue(DATABASE_PAGE_SIZE);
		if (dbPageSizeStr == null) return;
		Connection conn = getConnection(port);
		//if (size <= 0) return;
		
		try(CallableStatement cs = 
				  conn.prepareCall("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)");){

			cs.setString(1, "derby.storage.pageSize"); 
			cs.setString(2, dbPageSizeStr); 
			cs.execute(); 
			cs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	
	

	// * need to fix
	/*
	@Override
	public Connection getConnection() {
		try {
			if (conn == null || conn.isClosed()) {
				String dbUrl = String.format("jdbc:derby://localhost:1527/%s;create=true", dbName);
				conn = DriverManager.getConnection(dbUrl);
				System.out.println(String.format("Conectado a db %s", dbName));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			conn = null;
		}
		return conn;
		//
		
	}*/

	@Override
	protected String[] getStartCommand(IComponentInstance component, int port) {
		String derbyHome = System.getenv("DERBY_HOME");
		System.out.println(String.format("Running in port %d", port));
		if (derbyHome == null || derbyHome.equals("")) throw new RuntimeException("Environment Var DERBY_HOME must be configured to test apache derby");
		//String[] comandoArray = {"java", "-jar", derbyHome + "\\lib\\derbyrun.jar", "server", "start"};
		String[] comandoArray = {derbyHome + "/bin/startNetworkServer.bat", "-p", String.valueOf(port)};
		return comandoArray;
	}

	@Override
	protected String getDbDirectory(int port) {
		String derbyHome = System.getenv("DERBY_HOME");
		return derbyHome + "/bin";
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//process.destroy();
		
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
		String dbName = dbNames.get(port);
		String dbUrl = String.format("jdbc:derby://localhost:%d/%s;create=true", port, dbName);
		return dbUrl;
	}
	
}

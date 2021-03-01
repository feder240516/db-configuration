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
import exceptions.UnavailablePortsException;
import helpers.TestDescription;

public class ApacheDerbyHandler extends ADatabaseHandle {
	
	public static final String DATABASE_PAGE_SIZE = "DATABASE_PAGE_SIZE";
	
	// HashMap<Integer, String> dbNames;
	HashMap<Integer, String> directories = new HashMap<>();
	String instancesPath = System.getenv("DERBY_HOME") + "/db/instances";
	String baseDataPath = System.getenv("DERBY_HOME") + "/db/data";
	
	/*public ApacheDerbyHandler(int[] portsToUse, TestDescription testDescription, int allowedThreads) {
		super(portsToUse,allowedThreads,testDescription);
		initHandler();
	}*/
	
	public ApacheDerbyHandler(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		super(ci);
	}
	
	@Override
	protected void createAndFillDatabase() {}
	
	private void setDatabasePageSize() {
		String dbPageSizeStr = componentInstance.getParameterValue(DATABASE_PAGE_SIZE);
		if (dbPageSizeStr == null) return;
		try(Connection conn = getConnection();
				CallableStatement cs = 
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
	protected String[] getStartCommand() {
		String derbyHome = System.getenv("DERBY_HOME");
		System.out.println(String.format("Running in port %d", port));
		if (derbyHome == null || derbyHome.equals("")) throw new RuntimeException("Environment Var DERBY_HOME must be configured to test apache derby");
		String[] comandoArray = {derbyHome + "/bin/startNetworkServer.bat", "-p", String.valueOf(port)};
		return comandoArray;
	}

	@Override
	protected String getDbDirectory() {
		//String derbyHome = System.getenv("DERBY_HOME");
		return directories.get(port);
	}

	@Override
	protected void setupInitedDB() {
		setDatabasePageSize();
	}

	@Override
	public void stopServer() {
		System.out.println("Stopping server");
		try {
			String derbyHome = System.getenv("DERBY_HOME");
			String[] comandoArray = {derbyHome + "/bin/stopNetworkServer.bat", "-p", String.valueOf(port)};
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
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
	protected String getConnectionString () {
		String directory = directories.get(port);
		String dbUrl = String.format("jdbc:derby://localhost:%d/%s", port, "db");
		return dbUrl;
	}

	@Override
	protected String getInstancesPath() {
		return instancesPath;
	}

	@Override
	protected String getBasePath() {
		// TODO Auto-generated method stub
		return baseDataPath;
	}
	
}

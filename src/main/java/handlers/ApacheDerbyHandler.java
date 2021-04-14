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
import managers.db.parameters.ApacheDerbyParameterManager;

public class ApacheDerbyHandler extends ADatabaseHandle {
	
	// HashMap<Integer, String> dbNames;
	static String instancesPath = System.getenv("DERBY_HOME") + "/db/instances";
	static String baseDataPath = System.getenv("DERBY_HOME") + "/db/data";
	
	/*public ApacheDerbyHandler(int[] portsToUse, TestDescription testDescription, int allowedThreads) {
		super(portsToUse,allowedThreads,testDescription);
		initHandler();
	}*/
	
	public ApacheDerbyHandler(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		super(ci, new ApacheDerbyParameterManager());
	}
	
	@Override
	protected void createAndFillDatabase() {}

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
		return null;
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
		String dbUrl = String.format("jdbc:derby://localhost:%d/%s", port, "employees");
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

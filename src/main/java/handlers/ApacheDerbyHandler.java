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
import managers.PropertiesManager;
import managers.db.parameters.ApacheDerbyParameterManager;

public class ApacheDerbyHandler extends ADatabaseHandle {
	
	// HashMap<Integer, String> dbNames;
	
	/*public ApacheDerbyHandler(int[] portsToUse, TestDescription testDescription, int allowedThreads) {
		super(portsToUse,allowedThreads,testDescription);
		initHandler();
	}*/
	
	public ApacheDerbyHandler(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci, new ApacheDerbyParameterManager());
	}
	
	@Override
	protected void createAndFillDatabase() {}

	@Override
	protected String[] getStartCommand() {
		String derbyHome = getDerbyLocation();
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
			String derbyHome = getDerbyLocation();
			String[] comandoArray = {derbyHome + "/bin/stopNetworkServer.bat", "-p", String.valueOf(port)}; // $DERBY_HOME/bin/stopNetworkServer.bat -p 9901
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getConnectionString () {
		String dbUrl = String.format("jdbc:derby://localhost:%d/%s", port, "employees");
		return dbUrl;
	}
	
	protected String getDerbyLocation() {
		return PropertiesManager.getInstance().getProperty("derby.location");
	}

	@Override
	protected String getInstancesPath() {
		return PropertiesManager.getInstance().getProperty("derby.instances.location");
	}

	@Override
	protected String getBasePath() {
		return PropertiesManager.getInstance().getProperty("derby.base.location");
	}
	
}

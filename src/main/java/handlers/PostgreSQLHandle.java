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
import exceptions.UnavailablePortsException;
import helpers.TestDescription;

public class PostgreSQLHandle extends ADatabaseHandle {
	static String instancesPath = "D:\\Bibliotecas\\Documents\\_Programming_Assets\\Postgresql\\instances";
	static String baseDataPath = "D:\\Bibliotecas\\Documents\\_Programming_Assets\\Postgresql\\data";
	
	public PostgreSQLHandle(IComponentInstance componentInstance) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		super(componentInstance);
	}
	
	@Override
	protected void createAndFillDatabase() {}

	@Override
	protected String[] getStartCommand() {
		String postgresqlHome = System.getenv("POSTGRESQL_HOME");
		System.out.println(String.format("Running in port %d", port));
		if (postgresqlHome == null || postgresqlHome.equals("")) throw new RuntimeException("Environment Var postgresqlHome must be configured to test PostgreSQL");
		String[] comandoArray = {"\"" + postgresqlHome + "/bin/pg_ctl" + "\"", "-D", createdInstancePath, "-l", createdInstancePath + "/log.txt", "-o", String.format("\"-F -p %d\"", port), "start"};
		return comandoArray;
	}

	@Override
	protected String getDbDirectory() {
		return null;
	}

	@Override
	protected void setupInitedDB() {
		// TODO: Search for parameter configurations
	}

	@Override
	public void stopServer() {
		System.out.println("Stopping server");
		try {
			String postgresqlHome = System.getenv("POSTGRESQL_HOME");
			String[] comandoArray = {postgresqlHome + "/bin/pg_ctl", "-D", createdInstancePath, "-l", createdInstancePath + "/log.txt", String.format("-o \"-F -p %d\"", port), "stop"};
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getConnectionString () {
		String dbUrl = String.format("jdbc:postgresql://localhost:%d/%s?user=%s&password=%s", port, "employees", "feder", "root");
		return dbUrl;
	}

	@Override
	protected String getInstancesPath() {
		return instancesPath;
	}

	@Override
	protected String getBasePath() {
		return baseDataPath;
	}
}

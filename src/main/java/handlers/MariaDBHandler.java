package handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

public class MariaDBHandler extends ADatabaseHandle {

	HashMap<Integer, String> directories = new HashMap<>();
	
	public MariaDBHandler(TestDescription testDescription, int allowedThreads) {
		super(new int[]{3306, 3307, 3308}, allowedThreads, testDescription);
		directories.put(3306, "C:/Users/WIN/Desktop/MariaDB_Instances/instance1");
		directories.put(3307, "C:/Users/WIN/Desktop/MariaDB_Instances/instance2");
		directories.put(3308, "C:/Users/WIN/Desktop/MariaDB_Instances/instance3");
	}
	
	public boolean executeCommand(String cmdLine) {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("cmd.exe", "/c", cmdLine);
		
		boolean success;
		try {
			pb.start();
			success = true;
		}catch (Exception e){
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	
	private boolean ApplyParameters(IComponentInstance component, int port) {
		Map<String, String> parameters = component.getParameterValues();
		String sqlBase = "";
		
		boolean success;
		for(String keyParam : parameters.keySet()) {
			String valueParam = parameters.get(keyParam);
			
			sqlBase += "SET " + keyParam + " = " + valueParam + "; ";
		}
		
		Connection conn = getConnection(port);
		try {
			//PreparedStatement ps = conn.prepareStatement(sqlBase);
			Statement ps = conn.createStatement();
			ps.executeQuery(sqlBase);
			//ps.executeQuery();
			ps.close();
			//conn.close();
			
			success = true;
		}catch(SQLException e) {
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}

	@Override
	protected String[] getStartCommand(IComponentInstance component, int port) {
		String[] cmdStart = {"cmd.exe", "/c", "mysqld --defaults-file=.cnf --query-cache-type=0 --query-cache-size=0"};
		return cmdStart;
	}

	@Override
	protected void createAndFillDatabase(int port) {}

	@Override
	protected void setupInitedDB(IComponentInstance component, int port) {
		boolean isSuccessful = ApplyParameters(component, port);
		String msg = (isSuccessful) ? "Se aplicaron los parametros": "No se aplicaron los parametros";
		System.out.println(msg);
	}

	@Override
	public void stopServer(int port) {
		System.out.println("Stopping server");
		
		String cmdLine = String.format("mysqladmin -u root --password= --port=%d shutdown", port);
		
		Connection conn = getConnection(port);
		try {
			if (conn != null && !conn.isClosed()) conn.close();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("cmd.exe", "/c", cmdLine);
			processBuilder.start().waitFor();
			
			String msg = String.format("The server on port %d was stopped successfully", port);
			System.out.println(msg);
			
		} catch (IOException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getQueryCommand(int numTest) {
		switch(numTest) {
		case 1:
			return "SELECT employees.first_name, employees.hire_date, "
					+ "dept_emp.from_date, dept_emp.to_date "
					+ "FROM employees INNER JOIN dept_emp "
					+ "ON employees.emp_no =dept_emp.emp_no;";
		default:
			throw new RuntimeException("Test inexistente");
		}
	}

	@Override
	protected String getConnectionString(int port) {
		String dbName = "employees";
		String user = "root";
		String password = "";
		
		String dbUrl = String.format("jdbc:mariadb://localhost:%d/%s?user=%s&password=%s", port, dbName, user, password);
		return dbUrl;
	}

	@Override
	protected String getDbDirectory(int port) {
		return directories.get(port);
	}

}

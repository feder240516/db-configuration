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

public class MariaDBHandler extends ADatabaseHandle {

	HashMap<Integer, String> directories = new HashMap<>();
	
	public MariaDBHandler() {
		super(new int[]{3306, 3307, 3308});
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
	
	/*
	@Override
	public void initiateServer(IComponentInstance component) {
		String cmdStart = "mysqld";//"net start mariadb";
		boolean isSuccessful = ExecuteCommand(cmdStart);
		
		String msg = (isSuccessful) ? "Se inicio MariaDB": "No se inicio MariaDB";
		System.out.println(msg);

		isSuccessful = ApplyParameters(component);
		msg = (isSuccessful) ? "Se aplicaron los parametros": "No se aplicaron los parametros";
		System.out.println(msg);
	}*/

	/*
	@Override
	public void stopServer() {
		process.destroy();
		System.out.println("Se detuvo MariaDB");*/
		
		/*
		String cmdStop = "mysqladmin -u root --password=12345 shutdown";//"net stop mariadb";
		boolean isSuccessful = ExecuteCommand(cmdStop);
	
		String msg = (isSuccessful) ? "Se detuvo MariaDB": "No se detuvo MariaDB";
		System.out.println(msg);*/
	//}

	/*
	@Override
	public double benchmarkQuery(int numTest) {
		String sqlBase = "SELECT employees.first_name, employees.hire_date, "
				+ "dept_emp.from_date, dept_emp.to_date "
				+ "FROM employees INNER JOIN dept_emp "
				+ "ON employees.emp_no =dept_emp.emp_no;";
		
		Connection conn = getConnection();
		Date startDate = new Date();
		Date endDate = new Date();
		
		try {
			PreparedStatement ps = conn.prepareStatement(sqlBase);
			
			startDate = new Date();
			ps.executeQuery();
			endDate = new Date();
			
			ps.close();
			conn.close();
			
			System.out.println("Benchmark query was executed");
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("Benchmark query was NOT executed");
		}
		
		return Long.valueOf(endDate.getTime() - startDate.getTime()).doubleValue()/1000;
	}*/
	
	/*
	public Connection getConnection() {
		String db = "employees";
		String url = "jdbc:mariadb://localhost:3306/" + db;
		String user = "root";
		String password = "1234";
		
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Connection to MariaDB successful");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Connection to MariaDB unsuccessful");
		}
		return conn;
	}*/
	
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

	/*
	@Override
	public int initiateServer(IComponentInstance component) {
		System.out.println("Starting server...");
		
		int port = useNextAvailablePort();
		//String[] comandoArray = getStartCommand(component, port);
		System.out.println("Starting server on port: " + port);
		
		String cmdLine = "mysqld --defaults-file=.cnf";
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(new File(directories.get(port)));
		
		processBuilder.command("cmd.exe", "/c", cmdLine);
		
		try {			
			Process process = processBuilder.start();
			InputStream inStream = process.getInputStream();
			InputStream errStream = process.getErrorStream();
			
			inStream.close();
			errStream.close();
			
			Connection conn = null;
			// tries multiple connections to database
			for(int i = 0; i < MAX_CONNECTION_RETRIES && conn == null; ++i) {
				System.out.println("Trying to establish a connection...");
				// wait 5 seconds to allow server to initiate
				TimeUnit.SECONDS.sleep(5);
				conn = getConnection(port);
			}
			
			if (conn != null && !conn.isClosed()) {
				setupInitedDB(component, port);
				String msg = String.format("Server on port %d has been inited", port);
				System.out.println(msg);
			}else {
				throw new SQLException(String.format("Could not connect to database after %d tries", MAX_CONNECTION_RETRIES));
			}
			
		}catch(IOException | InterruptedException | SQLException  e) {
			e.printStackTrace();
		}
		
		return port;
	}*/

	@Override
	protected String[] getStartCommand(IComponentInstance component, int port) {
		//String directory = directories.get(port);
		String[] cmdStart = {"cmd.exe", "/c", "mysqld --defaults-file=.cnf --query-cache-type=0 --query-cache-size=0"};
		return cmdStart;
	}

	@Override
	protected void createAndFillDatabase(int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setupInitedDB(IComponentInstance component, int port) {
		// TODO Auto-generated method stub
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
			//String[] cmdStop = {cmdLine};
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("cmd.exe", "/c", cmdLine);
			processBuilder.start().waitFor();
			
			String msg = String.format("The server on port %d was stopped successfully", port);
			System.out.println(msg);
			
		} catch (IOException | SQLException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected String getQueryCommand(int numTest) {
		// TODO Auto-generated method stub
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

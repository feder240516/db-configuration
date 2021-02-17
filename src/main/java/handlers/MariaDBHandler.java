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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import java.util.UUID;

import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

import org.apache.commons.io.FileUtils;

public class MariaDBHandler extends ADatabaseHandle {

	String instancesPath = "C:/Users/WIN/Desktop/MariaDB_Handler/instances";
	String baseDataPath = "C:/Users/WIN/Desktop/MariaDB_Handler/data";
	HashMap<Integer, String> directories = new HashMap<>();
	
	public MariaDBHandler(int[] portsToUse, TestDescription testDescription, int allowedThreads) {
		super(portsToUse, allowedThreads, testDescription);
		
		initInstances(portsToUse);
		
		System.out.println("Available instances: ");
		directories.entrySet().forEach(entry->{
		    System.out.println(entry.getKey() + " " + entry.getValue());  
		});
	}
	
	public void initInstances(int[] portsToUse) {
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
		    FileUtils.copyDirectoryToDirectory(dataDir, destDir);
		    directories.put(port, instancePath);
		    System.out.println("The instance " + instancePath + " on port " + port + " was created");
		} catch (IOException e) {
		    e.printStackTrace();
		}
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
			PreparedStatement ps = conn.prepareStatement(sqlBase);
			ps.executeQuery();
			ps.close();
			
			success = true;
		}catch(SQLException e) {
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}

	@Override
	protected String[] getStartCommand(IComponentInstance component, int port) {
		//String[] cmdStart = {"cmd.exe", "/c", "mysqld --defaults-file=.cnf --query-cache-type=0 --query-cache-size=0"};
		String extraPath = "bin\\mysqld";
		String MariaDBHome = System.getenv("MARIADB_HOME");
		
		String dataDir = ".\\data";
		String socketPath = ".\\mysql.sock";
		
		String mariadbPath = String.format("\"%s%s\"", MariaDBHome, extraPath);
		
		String[] cmdStart = {"cmd.exe", "/c", String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath)};
		System.out.println("Start command on port " + port + ": " + String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath));
		return cmdStart;
	}

	@Override
	protected void createAndFillDatabase(int port) {}

	@Override
	protected void setupInitedDB(IComponentInstance component, int port) {
		boolean isSuccessful = ApplyParameters(component, port);
		String msg = (isSuccessful) ? "Parameters were applied on port " + port: "Parameter were NOT applied " + port;
		System.out.println(msg);
	}

	@Override
	public void stopServer(int port) {
		System.out.println("Stopping server on port " + port);
		String extraPath = "bin\\mysqladmin";
		String MariaDBHome = System.getenv("MARIADB_HOME");
		
		String mariadbPath = String.format("\"%s%s\"", MariaDBHome, extraPath);
		
		String[] cmdStop = {"cmd.exe", "/c", String.format("%s -u root --password= --port=%d shutdown", mariadbPath, port)};
		//String cmdLine = String.format("mysqladmin -u root --password= --port=%d shutdown", port);
		
		Connection conn = getConnection(port);
		try {
			if (conn != null && !conn.isClosed()) conn.close();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(cmdStop);
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

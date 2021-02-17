package handlers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

public class MySQLHandler extends ADatabaseHandle{

	String instancesPath = "C:/Users/WIN/Desktop/MySQLHandler/instances";
	String baseDataPath = "C:/Users/WIN/Desktop/MySQLHandler/data";
	HashMap<Integer, String> directories = new HashMap<>();
	
	public MySQLHandler(int[] portsToUse, TestDescription testDescription, int allowedThreads) {
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
		String extraPath = "bin\\mysqld";
		String MYSQLHome = System.getenv("MYSQL_HOME");
		
		String dataDir = ".\\data";
		String mysqldPath = String.format("\"%s%s\"", MYSQLHome, extraPath);
		
		String[] cmdStart = {"cmd.exe", "/c", String.format("%s --datadir=%s --port=%s", mysqldPath, dataDir, port)};
		System.out.println("Start command on port " + port + ": " + String.format("%s --datadir=%s --port=%s", mysqldPath, dataDir, port));
		return cmdStart;
	}

	@Override
	public void stopServer(int port) {
		System.out.println("Stopping server on port " + port);
		String extraPath = "bin\\mysqladmin";
		String MYSQLHome = System.getenv("MYSQL_HOME");
		
		String mysqldPath = String.format("\"%s%s\"", MYSQLHome, extraPath);
		
		String[] cmdStop = {"cmd.exe", "/c", String.format("%s -u root --password= --port=%d shutdown", mysqldPath, port)};
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
	protected String getDbDirectory(int port) {
		System.out.println(directories.get(port));
		return directories.get(port);
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
		
		String dbUrl = String.format("jdbc:mysql://localhost:%d/%s?user=%s&password=%s", port, dbName, user, password);
		return dbUrl;
	}

}

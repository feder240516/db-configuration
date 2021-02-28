package handlers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;

import ai.libs.jaicore.components.api.IComponentInstance;
import helpers.TestDescription;

public class HSQLDBHandle extends ADatabaseHandle {

	String instancesPath = "C:/Users/WIN/Desktop/HSQLDB_Instances/instances";
	String baseDataPath = "C:/Users/WIN/Desktop/HSQLDB_Instances/data";
	HashMap<Integer, String> directories = new HashMap<>();
	
	public HSQLDBHandle(int[] portsToUse, TestDescription testDescription, int allowedThreads) {
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
	
	@Override
	protected String[] getStartCommand(IComponentInstance component, int port) {
		String extraPath = "lib\\hsqldb.jar";
		String HSQLDBHome = System.getenv("HSQLDB_HOME");
		
		String dataDir = ".\\data\\db";
		
		String hsqldbdbPath = String.format("\"%s%s\"", HSQLDBHome, extraPath);
		
		String[] cmdStart = {"cmd.exe", "/c", String.format("java -cp %s org.hsqldb.Server -database.0 file:%s -port %s", hsqldbdbPath, dataDir, port)};
		System.out.println("Start command on port " + port + ": " + String.format("java -cp %s org.hsqldb.Server -database.0 file:%s -port %s", hsqldbdbPath, dataDir, port));
		return cmdStart;
	}

	@Override
	public void stopServer(int port) {
		System.out.println("Trying to stop server");
		String sqlBase = "SHUTDOWN;";
	
		Connection conn = getConnection(port);
		System.out.println("Connected to try and stop server");
		try {
			Statement statement = conn.createStatement();
            statement.execute(sqlBase);
            statement.close();
			
			String msg = String.format("The server on port %d was stopped successfully", port);
			System.out.println(msg);
		}catch(SQLException e) {
			e.printStackTrace();
			String msg = String.format("The server on port %d was NOT stopped successfully", port);
			System.out.println(msg);
		}
	}

	@Override
	protected String getDbDirectory(int port) {
		return directories.get(port);
	}

	@Override
	protected void createAndFillDatabase(int port) {
		
		
	}

	@Override
	protected void setupInitedDB(IComponentInstance component, int port) {
		
		
	}

	@Override
	protected String getQueryCommand(int numTest) {
		
		return null;
	}

	@Override
	protected String getConnectionString(int port) {
		String dbName = "";
		String user = "sa";
		String password = "";
		
		String dbUrl = String.format("jdbc:hsqldb:hsql://localhost:%s/?user=%s&password=%s", port, user, password);
		return dbUrl;
	}
	
	public void turnOffInstance(int port) {
		String sqlBase = "SHUTDOWN;";
		
		Connection conn = getConnection(port);
		try {
			Statement statement = conn.createStatement();
            statement.execute(sqlBase);
            statement.close();
			
			String msg = String.format("The server on port %d was stopped successfully", port);
		}catch(SQLException e) {
			e.printStackTrace();
			String msg = String.format("The server on port %d was NOT stopped successfully", port);
		}
	}
	
	/*Server dbServer = null;
	public void startDBServer() {
	    HsqlProperties props = new HsqlProperties();
	    props.setProperty("server.database.0", "file:" + "C:/Users/WIN/Desktop/SQL/db1");
	    props.setProperty("server.dbname.0", "newdb");
	    props.setProperty("server.port", "9139");
	    this.dbServer = new org.hsqldb.Server();
	    try {
	    	this.dbServer.setProperties(props);
	    } catch (Exception e) {
	        return;
	    }
	    this.dbServer.start();
	}
	
	
	public void stopDBServer() {
		this.dbServer.stop();
	}
	
	public Connection connectionTest() {
		Connection con = null;
	      
	      try {
	         Class.forName("org.hsqldb.jdbc.JDBCDriver");
	         con = DriverManager.getConnection("jdbc:hsqldb:file://localhost:9139/newdb;readonly=true,hsqldb.lock_file=false,user=SA,password=");
	         // con = DriverManager.getConnection("jdbc:hsqldb:file://localhost:9138/employees;ifexists=true?user=SA&password=");
	         if (con!= null){
	            System.out.println("Connection created successfully");
	         }else{
	            System.out.println("Problem with creating connection");
	         }
	      
	      }  catch (Exception e) {
	         e.printStackTrace(System.out);
	      }
	      
	      return con;
	}*/
}

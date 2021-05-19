package handlers;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import managers.db.parameters.HSQLDBParameterManager;

public class HSQLDBHandle extends ADatabaseHandle {

	static String instancesPath = "C:/Users/WIN/Desktop/HSQLDB_Instances/instances";
	static String baseDataPath = "C:/Users/WIN/Desktop/HSQLDB_Instances/data";
	
	//Map<String, String> shortcutCommands = new HashMap<>();
	
	public HSQLDBHandle(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci, new HSQLDBParameterManager());
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
	protected String[] getStartCommand() {
		String extraPath = "lib\\hsqldb.jar";
		String HSQLDBHome = System.getenv("HSQLDB_HOME");
		
		String dataDir = createdInstancePath + "\\db";
		
		String hsqldbdbPath = String.format("\"%s%s\"", HSQLDBHome, extraPath);
		
		String[] cmdStart = {"cmd.exe", "/c", String.format("java -cp %s org.hsqldb.Server -database.0 file:%s -port %s", hsqldbdbPath, dataDir, port)};
		System.out.println("Start command on port " + port + ": " + String.format("java -cp %s org.hsqldb.Server -database.0 file:%s -port %s", hsqldbdbPath, dataDir, port));
		return cmdStart;
	}

	@Override
	public void stopServer() {
		System.out.println("Trying to stop server");
		String sqlBase = "SHUTDOWN;";
	
		
		try(Connection conn = getConnection();) {
			if(conn != null) {
				System.out.println("Connected to try and stop server");
				
				Statement statement = conn.createStatement();
	            statement.execute(sqlBase);
	            statement.close();
				
				String msg = String.format("The server on port %d was stopped successfully", port);
				System.out.println(msg);
			} else {
				System.out.println("No connection was made");
			}
		}catch(SQLException e) {
			e.printStackTrace();
			String msg = String.format("The server on port %d was NOT stopped successfully", port);
			System.out.println(msg);
		}
	}

	@Override
	protected String getDbDirectory() {
		return null;
	}

	@Override
	protected void createAndFillDatabase() {}

	@Override
	protected String getConnectionString() {
		String dbName = "";
		String user = "sa";
		String password = "";
		
		String dbUrl = String.format("jdbc:hsqldb:hsql://localhost:%s/?user=%s&password=%s", port, user, password);
		return dbUrl;
	}
	
	public void turnOffInstance(int port) {
		String sqlBase = "SHUTDOWN;";
		
		try(Connection conn = getConnection();) {
			Statement statement = conn.createStatement();
            statement.execute(sqlBase);
            statement.close();
			
			String msg = String.format("The server on port %d was stopped successfully", port);
		}catch(SQLException e) {
			e.printStackTrace();
			String msg = String.format("The server on port %d was NOT stopped successfully", port);
		}
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

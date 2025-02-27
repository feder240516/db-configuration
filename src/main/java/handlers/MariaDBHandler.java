package handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import managers.db.parameters.MariaDBParameterManager;

public class MariaDBHandler extends ADatabaseHandle {

	static String instancesPath = "C:/Users/WIN/Desktop/MariaDB_Handler/instances";
	static String baseDataPath = "C:/Users/WIN/Desktop/MariaDB_Handler/data";
	
	public MariaDBHandler(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci, new MariaDBParameterManager());
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
		String extraPath = "bin\\mysqld";
		String MariaDBHome = System.getenv("MARIADB_HOME");
		
		String dataDir = createdInstancePath;
		String socketPath = createdInstancePath + ".\\mysql.sock";
		
		String mariadbPath = String.format("\"%s%s\"", MariaDBHome, extraPath);
		
		String[] cmdStart = {"cmd.exe", "/c", String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath)};
		// System.out.println("Start command on port " + port + ": " + String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath));
		return cmdStart;
	}

	@Override
	protected void createAndFillDatabase() {}

	@Override
	public void stopServer() {
		String extraPath = "bin\\mysqladmin";
		String MariaDBHome = System.getenv("MARIADB_HOME");
		
		String mariadbPath = String.format("\"%s%s\"", MariaDBHome, extraPath);
		
		String[] cmdStop = {"cmd.exe", "/c", String.format("%s -u root --password= --port=%d shutdown", mariadbPath, port)};
		
		try(Connection conn = getConnection();) {
			if (conn != null && !conn.isClosed()) conn.close();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(cmdStop);
			processBuilder.start().waitFor();
			
			TimeUnit.SECONDS.sleep(5);
			
		} catch (IOException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getConnectionString() {
		String dbName = "employees";
		String user = "root";
		String password = "";
		
		String dbUrl = String.format("jdbc:mariadb://localhost:%d/%s?user=%s&password=%s", port, dbName, user, password);
		return dbUrl;
	}

	@Override
	protected String getDbDirectory() {
		return null;
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

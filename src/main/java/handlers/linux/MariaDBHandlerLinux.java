package handlers.linux;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ADatabaseHandle;
import handlers.MariaDBHandler;
import managers.db.parameters.MariaDBParameterManager;

public class MariaDBHandlerLinux extends MariaDBHandler {

	static String instancesPath = "D:/Bibliotecas/Documents/_Programming_Assets/MariaDB/instances";
	static String baseDataPath = "D:/Bibliotecas/Documents/_Programming_Assets/MariaDB/data";
	
	public MariaDBHandlerLinux(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci);
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
		String extraPath = "\\bin\\mysqld";
		String MariaDBHome = System.getenv("MARIADB_HOME");
		
		String dataDir = createdInstancePath;
		String socketPath = createdInstancePath + "\\mysql.sock";
		
		String mariadbPath = String.format("\"%s%s\"", MariaDBHome, extraPath);
		
		String[] cmdStart = {"cmd.exe", "/c", String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath)};
		System.out.println("Start command on port " + port + ": " + String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath));
		return cmdStart;
	}

	@Override
	protected void createAndFillDatabase() {}

	@Override
	public void stopServer() {
		System.out.println("Stopping server on port " + port);
		String extraPath = "\\bin\\mysqladmin";
		String MariaDBHome = System.getenv("MARIADB_HOME");
		
		String mariadbPath = String.format("\"%s%s\"", MariaDBHome, extraPath);
		
		String[] cmdStop = {"cmd.exe", "/c", String.format("%s -u root --password=root --port=%d shutdown", mariadbPath, port)};
		System.out.println(String.format("%s -u root --password=root --port=%d shutdown", mariadbPath, port));
		
		try(Connection conn = getConnection();) {
			if (conn != null && !conn.isClosed()) conn.close();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(cmdStop);
			processBuilder.start().waitFor();
			
			String msg = String.format("The server on port %d was stopped successfully", port);
			System.out.println(msg);
			
			TimeUnit.SECONDS.sleep(5);
			
		} catch (IOException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getConnectionString() {
		String dbName = "employees";
		String user = "root";
		String password = "root";
		
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

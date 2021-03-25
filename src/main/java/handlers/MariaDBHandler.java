package handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;

public class MariaDBHandler extends ADatabaseHandle {

	static String instancesPath = "D:/Bibliotecas/Documents/_Programming_Assets/MariaDB/instances";
	static String baseDataPath = "D:/Bibliotecas/Documents/_Programming_Assets/MariaDB/data";
	
	public MariaDBHandler(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
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
	
	private boolean ApplyParameters(IComponentInstance component) {
		/*Map<String, String> parameters = component.getParameterValues();
		String sqlBase = "";
		
		boolean success;
		for(String keyParam : parameters.keySet()) {
			String valueParam = parameters.get(keyParam);
			
			sqlBase += "SET " + keyParam + " = " + valueParam + "; ";
		}
		
		try(Connection conn = getConnection();) {
			PreparedStatement ps = conn.prepareStatement(sqlBase);
			ps.executeQuery();
			ps.close();
			
			success = true;
		}catch(SQLException e) {
			e.printStackTrace();
			success = false;
		}
		*/
		return true;
	}

	@Override
	protected String[] getStartCommand() {
		String extraPath = "\\bin\\mysqld";
		String MariaDBHome = System.getenv("MARIADB_HOME");
		
		String dataDir = /*createdInstancePath +*/ createdInstancePath;
		String socketPath = /*createdInstancePath +*/ createdInstancePath + "\\mysql.sock";
		
		String mariadbPath = String.format("\"%s%s\"", MariaDBHome, extraPath);
		
		String[] cmdStart = {"cmd.exe", "/c", String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath)};
		System.out.println("Start command on port " + port + ": " + String.format("%s --datadir=%s --port=%s --socket=%s --query-cache-type=0 --query-cache-size=0", mariadbPath, dataDir, port, socketPath));
		return cmdStart;
	}

	@Override
	protected void createAndFillDatabase() {}

	@Override
	protected void setupInitedDB() {
		boolean isSuccessful = ApplyParameters(componentInstance);
		String msg = (isSuccessful) ? "Parameters were applied on port " + port: "Parameter were NOT applied " + port;
		System.out.println(msg);
	}

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

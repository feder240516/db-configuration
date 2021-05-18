package handlers.linux;

import java.io.File;
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
import managers.PropertiesManager;
import managers.db.parameters.MariaDBParameterManager;

public class MariaDBHandlerLinux extends MariaDBHandler {
	
	public MariaDBHandlerLinux(IComponentInstance ci) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci);
	}
	
	@Override
	public void createDBInstance() throws IOException {
		String baseDir = getBasePath();
		String instancesDir = getInstancesPath();
		String[] copyCommandArr = new String[] {"bash", "-c", 
				String.format("sudo cp -rf %1$s %2$s/%3$s"
						+ "&& sudo chmod -R 777 %2$s/%3$s", baseDir, instancesDir, ID.toString())};
		System.out.println(String.format("Testing instance %s", ID.toString()));
		ProcessBuilder processBuilder = new ProcessBuilder(copyCommandArr);
		Process copyProcess = processBuilder.start();
		System.out.println(String.valueOf(copyProcess.getInputStream().readAllBytes()));
		try {
			copyProcess.waitFor();
			System.out.println("Finished copying");
		} catch (InterruptedException e) {
			throw new IOException("Couldn't create new PostgreSQL instance");
		}
	}

	@Override
	protected String[] getStartCommand() {
		String MariaDBHome = PropertiesManager.getInstance().getProperty("mariadb.location");
		String[] cmdStart = {"sudo", MariaDBHome + "/mysqld", "--datadir=" + createdInstancePath,
				"--port=" + port, String.format("--socket=%s/mysql.sock", createdInstancePath), 
				"--query-cache-type=0", "--query-cache-size=0", "--log_bin"};
		//String[] cmdStart = {"bash", "-c", String.format("sudo -u postgres pg_ctlcluster 13 %s -o \"-F -p %d\" start", ID.toString(), port)};
		System.out.println("Start command on port " + port + ": " + String.join(" ",cmdStart));
		//System.out.println("Start command on port " + port + ": " + String.format("sudo %s/mysqld --datadir=%s --port=%s --socket=%s/mysql.sock --query-cache-type=0 --query-cache-size=0", MariaDBHome, createdInstancePath, port, createdInstancePath));
		return cmdStart;
	}

	@Override
	public void stopServer() {
		System.out.println("Stopping server on port " + port);
		String MariaDBHome = PropertiesManager.getInstance().getProperty("mariadb.location");
		
		//String[] cmdStart = {"bash", "-c", String.format("sudo %s/bin/mysqld --datadir=%s --port=%s --socket=%s/mysql.sock --query-cache-type=0 --query-cache-size=0", MariaDBHome, createdInstancePath, port, createdInstancePath)};
		String[] cmdStop = {"sudo", "mysqladmin", "--port="+port, "--protocol", "tcp", "shutdown"};
		System.out.println("deleting command: " + String.join(" ", cmdStop));
		//System.out.println(String.format("%s -u root --password=root --port=%d shutdown", MariaDBHome, port));
		
		try(Connection conn = getConnection();) {
			if (conn != null && !conn.isClosed()) conn.close();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(cmdStop);
			processBuilder.redirectOutput(new File("/home/ailibs/outputDelete.txt"));
			processBuilder.redirectError(new File("/home/ailibs/errorDelete.txt"));
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
		String user = PropertiesManager.getInstance().getProperty("mariadb.user");
		String password = PropertiesManager.getInstance().getProperty("mariadb.password");
		
		String dbUrl = String.format("jdbc:mariadb://localhost:%d/%s?user=%s&password=%s", port, dbName, user, password);
		System.out.println(" ### CONN STRING MARIA ###");
		System.out.println(dbUrl);
		return dbUrl;
	}

	@Override
	protected String getInstancesPath() {
		return PropertiesManager.getInstance().getProperty("mariadb.instances.location");
	}

	@Override
	protected String getBasePath() {
		return PropertiesManager.getInstance().getProperty("mariadb.data.location");
	}

}

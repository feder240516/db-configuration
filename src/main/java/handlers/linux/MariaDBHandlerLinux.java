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
		System.out.println("To copy");
		String baseDir = getBasePath();
		String instancesDir = getInstancesPath();
		String[] copyCommandArr = new String[] {"/bin/bash", "-c", 
				String.format("cp -rf %1$s %2$s/%3$s"
						+ "&& chmod -R 777 %2$s/%3$s", baseDir, instancesDir, ID.toString())};
		/*String[] copyCommandArr = new String[] {
				"cp", "-rf", baseDir, instancesDir, ID.toString()
		};*/
		ProcessBuilder processBuilder = new ProcessBuilder(copyCommandArr);
		System.out.println("Prepared to copy");
		System.out.println(String.join("+", processBuilder.command()));
		Process copyProcess = processBuilder.start();
		try {
			copyProcess.waitFor();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			throw new IOException("Couldn't create new MariaDB instance");
		}
		System.out.println("The instance " + createdInstancePath + " on port " + port + " was created");
	}

	@Override
	protected String[] getStartCommand() {
		String MariaDBHome = PropertiesManager.getInstance().getProperty("mariadb.location");
		String[] cmdStart = {"mysqld", "--datadir=" + createdInstancePath,
				"--port=" + port, String.format("--socket=%s/mysql.sock", createdInstancePath),
				String.format("--pid-file=%s/mysqld.pid", createdInstancePath),
				"--skip-grant-tables"};
		return cmdStart;
	}

	@Override
	public void stopServer() {
		String MariaDBHome = PropertiesManager.getInstance().getProperty("mariadb.location");
		
		//String[] cmdStart = {"/bin/bash", "-c", String.format("%s/bin/mysqld --datadir=%s --port=%s --socket=%s/mysql.sock --query-cache-type=0 --query-cache-size=0", MariaDBHome, createdInstancePath, port, createdInstancePath)};
		String[] cmdStop = {"mysqladmin", "--port="+port, "--protocol", "tcp", "shutdown"};
		
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
		String user = PropertiesManager.getInstance().getProperty("mariadb.user");
		String password = PropertiesManager.getInstance().getProperty("mariadb.password");
		
		String dbUrl = String.format("jdbc:mariadb://localhost:%d/%s?user=%s&password=%s", port, dbName, user, password);
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

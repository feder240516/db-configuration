package handlers.linux;

import java.io.IOException;
import java.sql.SQLException;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.PostgreSQLHandle;
import managers.PropertiesManager;

public class PostgreSQLHandleLinux extends PostgreSQLHandle {

	public PostgreSQLHandleLinux(IComponentInstance componentInstance)
			throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(componentInstance);
	}
	
	@Override
	public void createDBInstance() throws IOException {
		String[] copyCommandArr = new String[] {"/bin/bash", "-c", 
				String.format("sudo -u postgres /usr/bin/pg_createcluster 13 %1$s"
						+ "&& sudo -u postgres rm -rf /var/lib/postgresql/13/%1$s"
						+ "&& sudo -u postgres cp -rf /var/lib/postgresql/13/data /var/lib/postgresql/13/%1$s", ID.toString())};
		ProcessBuilder processBuilder = new ProcessBuilder(copyCommandArr);
		Process copyProcess = processBuilder.start();
		try {
			copyProcess.waitFor();
		} catch (InterruptedException e) {
			throw new IOException("Couldn't create new PostgreSQL instance");
		}
		System.out.println("The instance " + createdInstancePath + " on port " + port + " was created");
	}
	
	@Override
	protected String[] getStartCommand() {
		String postgresqlHome = PropertiesManager.getInstance().getProperty("postgres.location");
		String postgresqlLog = PropertiesManager.getInstance().getProperty("postgres.log.location");
		if (postgresqlHome == null || postgresqlHome.equals("")) throw new RuntimeException("Connector location not specified");
		String[] comandoArray = {"/bin/bash", "-c", String.format("sudo -u postgres pg_ctlcluster 13 %s -o \"-F -p %d\" start", ID.toString(), port)};
		return comandoArray;
	}

	@Override
	public void stopServer() {
		try {
			String postgresqlHome = PropertiesManager.getInstance().getProperty("postgres.location");
			String[] comandoArray = {"/bin/bash", "-c", String.format("sudo -u postgres pg_ctlcluster 13 %s stop", ID.toString(), port)};
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getConnectionString () {
		String user = PropertiesManager.getInstance().getProperty("postgres.user");
		String pass = PropertiesManager.getInstance().getProperty("postgres.password");
		String dbUrl = String.format("jdbc:postgresql://localhost:%d/?user=%s&password=%s", port, user, pass);
		return dbUrl;
	}
	
	@Override
	protected String getInstancesPath() {
		return PropertiesManager.getInstance().getProperty("postgres.instances.location");
	}

	@Override
	protected String getBasePath() {
		return PropertiesManager.getInstance().getProperty("postgres.data.location");
	}

}

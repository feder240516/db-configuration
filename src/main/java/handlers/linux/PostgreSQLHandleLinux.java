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
		String[] copyCommandArr = new String[] {"bash", "-c", 
				String.format("/usr/bin/pg_createcluster 13 %1$s"
						+ "&& rm -rf /var/lib/postgresql/13/%1$s"
						+ "&& sudo -u postgres cp -rf /var/lib/postgresql/13/data /var/lib/postgresql/13/%1$s", ID.toString())};
		System.out.println(String.format("Testing instance %s", ID.toString()));
		ProcessBuilder processBuilder = new ProcessBuilder(copyCommandArr);
		Process copyProcess = processBuilder.start();
		System.out.println(String.valueOf(copyProcess.getInputStream().readAllBytes()));
		try {
			copyProcess.waitFor();
		} catch (InterruptedException e) {
			throw new IOException("Couldn't create new PostgreSQL instance");
		}
	}
	
	@Override
	protected String[] getStartCommand() {
		String postgresqlHome = PropertiesManager.getInstance().getProperty("postgres.location");
		System.out.println(String.format("Running in port %d", port));
		if (postgresqlHome == null || postgresqlHome.equals("")) throw new RuntimeException("Connector location not specified");
		String[] comandoArray = {postgresqlHome + "/bin/pg_ctl", "-D", createdInstancePath, "-l", createdInstancePath + "/log.txt", "-o", String.format("\"-F -p %d\"", port), "start"};
		return comandoArray;
	}

	@Override
	public void stopServer() {
		System.out.println("Stopping server");
		try {
			String postgresqlHome = PropertiesManager.getInstance().getProperty("postgres.location");
			String[] comandoArray = {postgresqlHome + "/bin/pg_ctl", "-D", createdInstancePath, "-l", createdInstancePath + "/log.txt", "stop"};
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
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

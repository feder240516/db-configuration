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
				String.format("sudo -u postgres /usr/bin/pg_createcluster 13 %1$s"
						+ "&& sudo -u postgres rm -rf /var/lib/postgresql/13/%1$s"
						+ "&& sudo -u postgres cp -rf /var/lib/postgresql/13/data /var/lib/postgresql/13/%1$s"
						+ "&& sudo -u postgres rm -rf /etc/postgresql/13/%1$s"
						+ "&& sudo -u postgres cp -rf /etc/postgresql/13/data /etc/postgresql/13/%1$s", ID.toString())};
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
		String postgresqlLog = PropertiesManager.getInstance().getProperty("postgres.log.location");
		System.out.println(String.format("Running in port %d", port));
		if (postgresqlHome == null || postgresqlHome.equals("")) throw new RuntimeException("Connector location not specified");
		String[] comandoArray = {"bash", "-c", String.format("sudo -u postgres pg_ctlcluster 13 %s -o \"-F -p %d\" start", ID.toString(), port)};
		//String[] comandoArray = {postgresqlHome + "/bin/pg_ctl", "-D", createdInstancePath, "-l", postgresqlLog + "/postgresql-13-" + ID.toString() + ".log", "-o", String.format("\"-F -p %d\"", port), "start"};
		for (String comando: comandoArray) {
			System.out.print(comando + " ");
		}
		System.out.println();
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
	protected String getConnectionString () {
		String user = PropertiesManager.getInstance().getProperty("postgres.user");
		String pass = PropertiesManager.getInstance().getProperty("postgres.password");
		String dbUrl = String.format("jdbc:postgresql://localhost:%d/%s?user=%s&password=%s", port, "employees", user, pass);
		System.out.println(" #### CONNECTION STRING ####");
		System.out.println(dbUrl);
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

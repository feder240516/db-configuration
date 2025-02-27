package handlers.linux;

import java.io.IOException;
import java.sql.SQLException;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ApacheDerbyHandler;
import managers.PropertiesManager;

public class ApacheDerbyHandlerLinux extends ApacheDerbyHandler {

	public ApacheDerbyHandlerLinux(IComponentInstance ci)
			throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci);
	}
	
	/*@Override
	public void createDBInstance() throws IOException {
		String derbyData = PropertiesManager.getInstance().getProperty("derby.data.location");
		String derbyInstances = PropertiesManager.getInstance().getProperty("derby.instances.location");
		String[] copyCommandArr = new String[] {"/bin/bash", "-c", 
				String.format("sudo cp -rf %1$s %2$s", derbyData)};
		System.out.println(String.format("Testing instance %s", ID.toString()));
		ProcessBuilder processBuilder = new ProcessBuilder(copyCommandArr);
		Process copyProcess = processBuilder.start();
		System.out.println(String.valueOf(copyProcess.getInputStream().readAllBytes()));
		try {
			copyProcess.waitFor();
		} catch (InterruptedException e) {
			throw new IOException("Couldn't create new PostgreSQL instance");
		}
	}*/
	
	@Override
	protected String[] getStartCommand() {
		String derbyHome = PropertiesManager.getInstance().getProperty("derby.location");
		if (derbyHome == null || derbyHome.equals("")) throw new RuntimeException("Connector location not specified");
		String[] comandoArray = {/*"sudo",*/ "java", "-jar", derbyHome + "/lib/derbyrun.jar", "server", "start", "-p", String.valueOf(port)};
		//String[] comandoArray = {postgresqlHome + "/bin/pg_ctl", "-D", createdInstancePath, "-l", postgresqlLog + "/postgresql-13-" + ID.toString() + ".log", "-o", String.format("\"-F -p %d\"", port), "start"};
		return comandoArray;
	}

	@Override
	public void stopServer() {
		try {
			String derbyHome = PropertiesManager.getInstance().getProperty("derby.location");
			//String[] comandoArray = {"/bin/bash", "-c", String.format("sudo java -jar %s/lib/derbyrun.jar server shutdown -p %d", derbyHome, port)};
			String[] comandoArray = {"java", "-jar", String.format("%s/lib/derbyrun.jar", derbyHome), "server", "shutdown", "-p", String.valueOf(port)};
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected String getConnectionString () {
		//String user = PropertiesManager.getInstance().getProperty("postgres.user");
		//String pass = PropertiesManager.getInstance().getProperty("postgres.password");
		String dbUrl = String.format("jdbc:derby://localhost:%d/%s", port, "employees");
		return dbUrl;
	}
	
	@Override
	protected String getInstancesPath() {
		return PropertiesManager.getInstance().getProperty("derby.instances.location");
	}

	@Override
	protected String getBasePath() {
		return PropertiesManager.getInstance().getProperty("derby.data.location");
	}

}

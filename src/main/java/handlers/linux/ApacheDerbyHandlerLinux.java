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
		String[] copyCommandArr = new String[] {"bash", "-c", 
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
		//String postgresqlLog = PropertiesManager.getInstance().getProperty("postgres.log.location");
		
		System.out.println(String.format("Running in port %d", port));
		if (derbyHome == null || derbyHome.equals("")) throw new RuntimeException("Connector location not specified");
		String[] comandoArray = {"bash", "-c", String.format(/*"sudo java -jar %s/lib/derbyrun.jar server start -p %d", derbyHome, port*/"sudo sl")};
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
			String derbyHome = PropertiesManager.getInstance().getProperty("derby.location");
			String[] comandoArray = {"bash", "-c", String.format("sudo java -jar %s/lib/derbyrun.jar server shutdown -p %d", derbyHome, port)};
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
		System.out.println(" #### CONNECTION STRING ####");
		System.out.println(dbUrl);
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

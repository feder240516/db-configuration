package handlers.linux;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.HSQLDBHandle;
import managers.PropertiesManager;

public class HSQLDBHandleLinux extends HSQLDBHandle {

	public HSQLDBHandleLinux(IComponentInstance ci)
			throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci);
	}
	
	@Override
	protected String[] getStartCommand() {
		String HSQLDBHome = PropertiesManager.getInstance().getProperty("hsqldb.location");
		
		String[] cmdStart = {"bash", "-c", String.format("sudo java -cp %s/lib/hsqldb.jar org.hsqldb.Server -database.0 file:%s -port %s", HSQLDBHome, createdInstancePath, port)};
		System.out.println("Start command on port " + port + ": " + String.format("sudo java -cp %s/lib/hsqldb.jar org.hsqldb.Server -database.0 file:%s -port %s", HSQLDBHome, createdInstancePath, port));
		return cmdStart;
	}
	
	@Override
	protected String getConnectionString() {
		String dbName = "";
		String user = "sa";
		String password = "";
		
		String dbUrl = String.format("jdbc:hsqldb:hsql://localhost:%s/?user=%s&password=%s", port, user, password);
		System.out.println(" #### CONNECTION STRING ####");
		System.out.println(dbUrl);
		return dbUrl;
	}

	@Override
	protected String getInstancesPath() {
		return PropertiesManager.getInstance().getProperty("hsqldb.instances.location");
	}

	@Override
	protected String getBasePath() {
		return PropertiesManager.getInstance().getProperty("hsqldb.instances.location");
	}

}

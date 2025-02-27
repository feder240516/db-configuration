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
		String dbName = PropertiesManager.getInstance().getProperty("hsqldb.dbname");;
		String[] cmdStart = {"/bin/bash", "-c", String.format("java -cp %1$s/lib/hsqldb.jar org.hsqldb.Server -database.0 file:%3$s xdb -port %2$s", HSQLDBHome, port, dbName)};
		return cmdStart;
	}
	
	@Override
	protected String getConnectionString() {
		String dbName = PropertiesManager.getInstance().getProperty("hsqldb.dbname");
		String user = "sa";
		String password = "";
		
		String dbUrl = String.format("jdbc:hsqldb:hsql://127.0.0.1:%s/?user=%s&password=%s", port, user, password);
		return dbUrl;
	}

	@Override
	protected String getInstancesPath() {
		return PropertiesManager.getInstance().getProperty("hsqldb.instances.location");
	}

	@Override
	protected String getBasePath() {
		return PropertiesManager.getInstance().getProperty("hsqldb.data.location");
	}

}

package managers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ADatabaseHandle;
import handlers.ApacheDerbyHandler;
import handlers.HSQLDBHandle;
import handlers.MariaDBHandler;
import handlers.PostgreSQLHandle;
import helpers.Port;
import helpers.TestDescription;

public class DBSystemFactory {
	
	private static final DBSystemFactory _instance = new DBSystemFactory();
	
	public static DBSystemFactory getInstance() {
		return _instance;
	}
	
	public ADatabaseHandle createHandle(IComponentInstance componentInstance, TestDescription test) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		String componentName = componentInstance.getComponent().getName();
		switch(componentName) {
		case "MariaDB":
			return new MariaDBHandler(componentInstance);
		case "ApacheDerby":
			return new ApacheDerbyHandler(componentInstance);
		case "HSQLDB":
			return new HSQLDBHandle(componentInstance);
		case "PostgreSQL":
			return new PostgreSQLHandle(componentInstance);
		default:
			throw new IllegalArgumentException("IComponent DB name is not supported");
		}
	}
}

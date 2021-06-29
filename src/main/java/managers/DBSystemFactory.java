package managers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ADatabaseHandle;
import handlers.ApacheDerbyHandler;
import handlers.HSQLDBHandle;
import handlers.MariaDBHandler;
import handlers.PostgreSQLHandle;
import handlers.linux.ApacheDerbyHandlerLinux;
import handlers.linux.HSQLDBHandleLinux;
import handlers.linux.MariaDBHandlerLinux;
import handlers.linux.PostgreSQLHandleLinux;
import helpers.Port;
import helpers.TestDescription;

public class DBSystemFactory {
	
	private static final DBSystemFactory _instance = new DBSystemFactory();
	
	public static DBSystemFactory getInstance() {
		return _instance;
	}
	
	public ADatabaseHandle createHandle(IComponentInstance componentInstance) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		String componentName = componentInstance.getComponent().getName();
		switch(componentName) {
		case "MariaDB":
			return SystemUtils.IS_OS_LINUX ? new MariaDBHandlerLinux(componentInstance) : new MariaDBHandler(componentInstance);
		case "ApacheDerby":
			return SystemUtils.IS_OS_LINUX ? new ApacheDerbyHandlerLinux(componentInstance) : new ApacheDerbyHandler(componentInstance);
		case "HSQLDB":
			return SystemUtils.IS_OS_LINUX ? new HSQLDBHandleLinux(componentInstance) : new HSQLDBHandle(componentInstance);
		case "PostgreSQL":
			return SystemUtils.IS_OS_LINUX ?  new PostgreSQLHandleLinux(componentInstance) : new PostgreSQLHandle(componentInstance);
		default:
			throw new IllegalArgumentException("IComponent DB name is not supported");
		}
	}
}

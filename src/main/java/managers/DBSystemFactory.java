package managers;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.components.api.IComponentInstance;
import handlers.ADatabaseHandle;
import handlers.ApacheDerbyHandler;
import handlers.MariaDBHandler;
import handlers.MySQLHandler;
import handlers.PostgreSQLHandle;
import helpers.Port;
import helpers.TestDescription;

public class DBSystemFactory {
	
	public static ADatabaseHandle createHandle(IComponentInstance componentInstance, TestDescription test) {
		String componentName = componentInstance.getComponent().getName();
		switch(componentName) {
		case "MariaDB":
			return new MariaDBHandler(new int[] {3306,3307,3308}, test, 1);
		case "ApacheDerby":
			return new ApacheDerbyHandler(new int[] {1527,1528,1529}, test, 1);
		case "MySQL":
			return new MySQLHandler(new int[] {3312,3313,3314}, test, 1);
		case "PostgreSQL":
			return new PostgreSQLHandle(new int[] {5423,5424,5425}, test, 1);
		default:
			throw new IllegalArgumentException("IComponent DB name is not supported");
		}
		
	}
}

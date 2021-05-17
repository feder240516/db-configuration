package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import exceptions.UnavailablePortsException;
import managers.DBSystemFactory;
import managers.PortManager;

public class MainDrivers {
	
	public static List<IComponentInstance> getOneInstanceForDBSystem() {
		IComponent compMaria 		= new Component("MariaDB");
		IComponent compDerby 		= new Component("ApacheDerby");
		IComponent compPosgreSQL 	= new Component("PostgreSQL");
		IComponent compHSQL 		= new Component("HSQLDB");
		List<IComponentInstance> componentInstances = new ArrayList<>();
		IComponentInstance maria = new ComponentInstance(compMaria, new HashMap<>(), new HashMap<>());
		IComponentInstance derby = new ComponentInstance(compDerby, new HashMap<>(), new HashMap<>());
		IComponentInstance postgres = new ComponentInstance(compPosgreSQL, new HashMap<>(), new HashMap<>());
		IComponentInstance hsql = new ComponentInstance(compHSQL, new HashMap<>(), new HashMap<>());
		componentInstances.add(maria);
		componentInstances.add(derby);
		componentInstances.add(postgres);
		componentInstances.add(hsql);
		return componentInstances;
	}
	
	public static void main(String[] args) {
		PortManager.getInstance().setupAvailablePorts(new int[]{9905,9906,9907,9908,9909});
		DBSystemFactory _instance = DBSystemFactory.getInstance();
		List<IComponentInstance> componentInstanceExamples = getOneInstanceForDBSystem();
		String[] drivers = new String[] {"org.mariadb.jdbc.Driver", 
										"org.postgresql.Driver", 
										"org.hsqldb.jdbcDriver", 
										"org.apache.derby.client.ClientAutoloadedDriver"};
		for(String d: drivers) {
			try {
				Class.forName (d);
				System.out.println(String.format("Cargado exitosamente %s", d));
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		/*for(IComponentInstance ci: componentInstanceExamples) {
			try {
				_instance.createHandle(ci);
			} catch (ClassNotFoundException | UnavailablePortsException | IOException | SQLException
					| InterruptedException e) {
				e.printStackTrace();
				System.out.println(String.format("Couldn't instantiate db %s", ci.getComponent().getName()));
			}
		}*/
	}
}

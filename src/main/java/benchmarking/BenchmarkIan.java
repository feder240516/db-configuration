package benchmarking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.serialization.ComponentSerialization;

public class BenchmarkIan {
	
	public static void main(String[] args) {
		BenchmarkIan benchmark = new BenchmarkIan();
		
		IComponent comp = new Component("MariaDB");
		
		Map<String, String> parameterValues = new HashMap<>();
		parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "47");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		
		ComponentSerialization serializer = new ComponentSerialization();
		System.out.println(serializer.serialize(i1));
		
		/*
		DBHandlerIan dbHandler = benchmark.launchDatabase();
		benchmark.benchmark(i1);
		dbHandler.Stop();
		*/
		
		/*
		MariaDBHandler mariaDBHandler = new MariaDBHandler();
		mariaDBHandler.initiateServer(i1);
		System.out.println(mariaDBHandler.benchmarkQuery(0));
		mariaDBHandler.stopServer();
		*/
		
		MariaDBHandler mariaDBHandler = new MariaDBHandler();
		mariaDBHandler.initiateServer(i1);
		System.out.println(mariaDBHandler.benchmarkQuery(0));
		mariaDBHandler.stopServer();
	}
	
}



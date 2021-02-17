package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import handlers.ADatabaseHandle;
import handlers.ApacheDerbyHandler;
import handlers.MariaDBHandler;
import handlers.MySQLHandler;
import helpers.TestDescription;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
	    DbSpec spec = new DbSpec();
	    DbSchema schema = spec.addDefaultSchema();
	 
	    DbTable employeeTable = schema.addTable("employees");
	    DbColumn empNoCol = employeeTable.addColumn("emp_no", "int(11)", null);
	    DbColumn empBirthCol = employeeTable.addColumn("birth_date", "date", null);
	    DbColumn empfNameCol = employeeTable.addColumn("first_name", "varchar(14)", null);
	    DbColumn emplNameCol = employeeTable.addColumn("last_name", "varchar(16)", null);
	    DbColumn empGenderCol = employeeTable.addColumn("gender", "enum('M','F')", null);
	    DbColumn empHireCol = employeeTable.addColumn("hire_date", "date", null);
	 
	 
		SelectQuery selectQuery = new SelectQuery()
	      .addColumns(empNoCol, empBirthCol, empfNameCol, emplNameCol, empGenderCol, empHireCol)
	      .validate();
		
		TestDescription td = new TestDescription(null);
		td.addQuery(1, selectQuery);
		td.addQuery(1, selectQuery);
		td.addQuery(1, selectQuery);
		
		IComponent comp = new Component("MariaDB");
		Map<String, String> parameterValues = new HashMap<>();
		parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "45");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		

		IComponent comp2 = new Component("MySQL");
		Map<String, String> parameterValues2 = new HashMap<>();
		parameterValues2.put("OPTIMIZER_SEARCH_DEPTH", "45");
		Map<String, List<IComponentInstance>> reqInterfaces2 = new HashMap<>(); 
		IComponentInstance i2 = new ComponentInstance(comp2, parameterValues2, reqInterfaces2);
		
		MySQLHandler handler = new MySQLHandler(new int[]{3313, 3314, 3315, 3316}, td, 4);
		Double score = handler.benchmarkQuery(i1);
		System.out.println("Score: " + score);
		
		/*MariaDBHandler handler = new MariaDBHandler(new int[]{3307, 3308, 3309}, td, 3);
		Double score = handler.benchmarkQuery(i1);
		System.out.println("Score: " + score);*/
		

		
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(20);
		List<Callable<Double>> taskList = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			Callable<Double> task = new Callable<Double>() {
				public Double call() {
					try {
						return handler.benchmarkQuery(i1);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return Double.MAX_VALUE;
					}
					
				}
			};
	        taskList.add(task);
		}
		
        List<Future<Double>> resultList = null;
 
        try {
            resultList = executor.invokeAll(taskList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        
        for(Future<Double> result: resultList) {
        	System.out.println("Score: " + result.get());
        }
	}

}

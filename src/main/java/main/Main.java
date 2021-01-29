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
import handlers.MariaDBHandler;
import helpers.TestDescription;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
	    // create default schema
	    DbSpec spec = new DbSpec();
	    DbSchema schema = spec.addDefaultSchema();
	 
	    // add table with basic customer info
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
		
		//InsertQuery insertQuery = new InsertQuery(custOrderJoin);
		
		TestDescription td = new TestDescription(null);
		td.addQuery(1, selectQuery);
		td.addQuery(1, selectQuery);
		td.addQuery(1, selectQuery);
		//td.print();
		
		IComponent comp = new Component("MariaDB");
		Map<String, String> parameterValues = new HashMap<>();
		parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "45");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		
		MariaDBHandler handler = new MariaDBHandler(new int[]{3306, 3307, 3308, 3309}, td, 4);
		
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
		
		/*try {
			double value1 = handler.benchmarkQuery(i1);
			double value2 = handler.benchmarkQuery(i1);
			double value3 = handler.benchmarkQuery(i1);
			double value4 = handler.benchmarkQuery(i1);
			double value5 = handler.benchmarkQuery(i1);
			double value6 = handler.benchmarkQuery(i1);
			System.out.println("Score: " + value1);
			System.out.println("Score: " + value2);
			System.out.println("Score: " + value3);
			System.out.println("Score: " + value4);
			System.out.println("Score: " + value5);
			System.out.println("Score: " + value6);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		
	}

}

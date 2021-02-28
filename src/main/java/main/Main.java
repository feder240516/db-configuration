package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
import handlers.HSQLDBHandle;
import handlers.MariaDBHandler;
import handlers.MySQLHandler;
import handlers.PostgreSQLHandle;
import handlers.PostgreSQLHandle;
import helpers.TestDescription;
import managers.Benchmarker;

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
		
		IComponent comp3 = new Component("PostgreSQL");
		Map<String, String> parameterValues3 = new HashMap<>();
		parameterValues2.put("OPTIMIZER_SEARCH_DEPTH", "45");
		Map<String, List<IComponentInstance>> reqInterfaces3 = new HashMap<>(); 
		IComponentInstance i3 = new ComponentInstance(comp3, parameterValues3, reqInterfaces3);
		
		HSQLDBHandle handler = new HSQLDBHandle(new int[]{9902, 9903, 9904}, td, 3);
	
		Benchmarker benchmarker = new Benchmarker(td, 10);
		
		double results = benchmarker.benchmark(i3);
		//System.out.println(String.format("Mean of test: %f",stats.getMean()));
	}

}

package main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static void main(String[] args) {
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
		
		MariaDBHandler handler = new MariaDBHandler(new int[]{3306, 3307, 3308, 3309}, td, 3);
		try {
			double value = handler.benchmarkQuery(i1);
			System.out.println("Score: " + value);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		
	}

}

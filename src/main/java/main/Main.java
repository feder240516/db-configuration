package main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
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
import org.jooq.DatePart;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.ExtractExpression;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.InsertSelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery.JoinType;
import com.healthmarketscience.sqlbuilder.SqlObject;
import com.healthmarketscience.sqlbuilder.Subquery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Function;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ADatabaseHandle;
import handlers.ApacheDerbyHandler;
import handlers.HSQLDBHandle;
import handlers.MariaDBHandler;
import handlers.MySQLHandler;
import handlers.PostgreSQLHandle;
import handlers.PostgreSQLHandle;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.PortManager;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException, UnavailablePortsException, IOException, SQLException {
	    DbSpec spec = new DbSpec();
	    DbSchema schema = spec.addDefaultSchema();
	 
	    DbTable employeesTable = schema.addTable("employees");
	    DbColumn employees_empNoCol = employeesTable.addColumn("emp_no", "int(11)", null);
	    DbColumn employees_empBirthCol = employeesTable.addColumn("birth_date", "date", null);
	    DbColumn employees_empfNameCol = employeesTable.addColumn("first_name", "varchar(14)", null);
	    DbColumn employees_emplNameCol = employeesTable.addColumn("last_name", "varchar(16)", null);
	    DbColumn employees_empGenderCol = employeesTable.addColumn("gender", "enum('M','F')", null);
	    DbColumn employees_empHireCol = employeesTable.addColumn("hire_date", "date", null);
	 
	    DbTable salariesTable = schema.addTable("salaries");
	    DbColumn salaries_empNoCol = salariesTable.addColumn("emp_no", "int(11)", null);
	    DbColumn salaries_salaryCol = salariesTable.addColumn("salary", "int(11)", null);
	    DbColumn salaries_fromDateCol = salariesTable.addColumn("from_date", "date", null);
	    DbColumn salaries_toDateCol = salariesTable.addColumn("to_date", "date", null);
	    
	    DbTable titlesTable = schema.addTable("titles");
	    DbColumn titles_empNoCol = titlesTable.addColumn("emp_no", "int(11)", null);
	    DbColumn titles_titleCol = titlesTable.addColumn("title", "varchar(50)", null);
	    DbColumn titles_fromDateCol = titlesTable.addColumn("from_date", "date", null);
	    DbColumn titles_toDateCol = titlesTable.addColumn("to_date", "date", null);
	    
	    DbTable deptEmpTable = schema.addTable("dept_emp");
	    DbColumn deptEmp_empNoCol = deptEmpTable.addColumn("emp_no", "int(11)", null);
	    DbColumn deptEmp_deptNoCol = deptEmpTable.addColumn("dept_no", "char(4)", null);
	    DbColumn deptEmp_fromDateCol = deptEmpTable.addColumn("from_date", "date", null);
	    DbColumn deptEmp_toDateCol = deptEmpTable.addColumn("to_date", "date", null);
	    
	    DbTable deptTable = schema.addTable("departments");
	    DbColumn dept_empNoCol = deptTable.addColumn("emp_no", "int(11)", null);
	    DbColumn dept_nameCol = deptTable.addColumn("dept_name", "varchar(40)", null);
	    
	    DbTable deptMgTable = schema.addTable("dept_manager");
	    DbColumn deptMg_empNoCol = deptMgTable.addColumn("emp_no", "int(11)", null);
	    DbColumn deptMg_deptNoCol = deptMgTable.addColumn("dept_no", "char(4)", null);
	    DbColumn deptMg_fromDateCol = deptMgTable.addColumn("from_date", "date", null);
	    DbColumn deptMg_toDateCol = deptMgTable.addColumn("to_date", "date", null);
	    
	    SelectQuery selectSalaries = new SelectQuery()
	    		.addColumns(employees_empNoCol, employees_empfNameCol, employees_emplNameCol, salaries_salaryCol)
	    		.addJoin(JoinType.INNER, employeesTable, salariesTable, BinaryCondition.equalTo(employees_empNoCol, salaries_empNoCol))
	    		.addCondition(BinaryCondition.equalTo(new ExtractExpression(DatePart.YEAR, salaries_toDateCol), 9999))
	    		.validate();
	    System.out.println(selectSalaries.toString()); 
	    
	    
	    SelectQuery selectAvgSalaryTitles = new SelectQuery()
	    		.addCustomColumns(titles_titleCol, FunctionCall.avg().addColumnParams(salaries_salaryCol))
	    		.addJoin(JoinType.INNER, salariesTable, titlesTable, BinaryCondition.equalTo(salaries_empNoCol, titles_empNoCol))
	    		.addCondition(BinaryCondition.equalTo(new ExtractExpression(DatePart.YEAR, salaries_toDateCol), 9999))
	    		.addGroupings(titles_titleCol).validate();
	    		
	    System.out.println("Select AVG query new: " + selectAvgSalaryTitles);
	    
	    SelectQuery selectAvgSalaryTitlesGender = new SelectQuery()
	    		.addCustomColumns(titles_titleCol, employees_empGenderCol, FunctionCall.avg().addColumnParams(salaries_salaryCol))
	    		.addJoin(JoinType.INNER, salariesTable, titlesTable, BinaryCondition.equalTo(salaries_empNoCol, titles_empNoCol))
	    		.addJoin(JoinType.INNER, salariesTable, employeesTable, BinaryCondition.equalTo(salaries_empNoCol, employees_empNoCol))
	    		.addCondition(BinaryCondition.equalTo(new ExtractExpression(DatePart.YEAR, salaries_toDateCol), 9999))
	    		.addGroupings(employees_empGenderCol, titles_titleCol).validate();
	    
	    System.out.println("Select AVG Gender Title query new: " + selectAvgSalaryTitlesGender);
	    
	    /*UpdateQuery updateSalaries = new UpdateQuery(salariesTable)
	    		.addSetClause(salaries_salaryCol, new CustomSql(String.format("%s + (%s*20/100)", salaries_salaryCol, salaries_salaryCol)))
	    		.addCondition(BinaryCondition.equalTo(titles_titleCol, "Staff"))
	    		.addCommonTableExpression("hello").validate();
	    
	    System.out.println("UpdateQuery: " + updateSalaries);*/
	    		
	    		
	    		
	    
	    /*SelectQuery selectSalaries2 = new SelectQuery().addCustomColumns(FunctionCall.max().addColumnParams(employees_empNoCol));
	    System.out.println(selectSalaries2.toString());
	    InsertQuery insertEmployee = new InsertQuery(employeesTable)
	    		.addColumn(employees_empNoCol, 0)
	    		.addColumn(employees_empBirthCol, "2000-12-18")
	    		.addColumn(employees_empfNameCol, "Federico")
	    		.addColumn(employees_emplNameCol, "Reina")
	    		.addColumn(employees_empGenderCol, 'M')
	    		.addColumn(employees_empHireCol, new Date(System.currentTimeMillis()))
	    		.validate();*/
	    
	    String birthDate = "2000-12-18";
	    String fName = "Federico";
	    String lName = "Reina";
	    char gender = 'M';
	    Date hireDate = new Date(System.currentTimeMillis());
	    
	    InsertSelectQuery insertEmployee2 = new InsertSelectQuery(employeesTable)
	    		.addColumns(employees_empNoCol, employees_empBirthCol, employees_empfNameCol, employees_emplNameCol, employees_empGenderCol, employees_empHireCol)
	    		.setSelectQuery(new SelectQuery()
	    				.addCustomColumns(new CustomSql(String.format("%s%s", FunctionCall.max().addColumnParams(employees_empNoCol), "+1")), birthDate,fName, lName,gender,  new CustomSql(String.format("'%s' %s", hireDate, "FROM employees t0")))).validate();
	    
	    System.out.println("insertEmployee2: " + insertEmployee2.toString());
	    
	    /*InsertQuery insertSalary = new InsertQuery(salariesTable)
	    		.addColumn(salaries_empNoCol, 500001)
	    		.addColumn(salaries_salaryCol, 75000)
	    		.addColumn(salaries_fromDateCol, new Date(System.currentTimeMillis()))
	    		.addColumn(salaries_toDateCol, "9999-01-01")
	    		.validate();
	    
	    InsertQuery insertTitle = new InsertQuery(titlesTable)
	    		.addColumn(titles_empNoCol, 500001)
	    		.addColumn(titles_titleCol, "Senior Engineer")
	    		.addColumn(titles_fromDateCol, "2006-07-01")
	    		.addColumn(titles_toDateCol, new Date(System.currentTimeMillis()))
	    		.validate();
	    
	    InsertQuery insertDeptEmp = new InsertQuery(deptEmpTable)
	    		.addColumn(deptEmp_empNoCol, 500001)
	    		.addColumn(deptEmp_deptNoCol, "d005")
	    		.addColumn(deptEmp_fromDateCol, new Date(System.currentTimeMillis()))
	    		.addColumn(deptEmp_toDateCol, "9999-01-01")
	    		.validate();
	    
	    System.out.println(insertEmployee.toString());   
	    System.out.println(insertSalary.toString());   
	    System.out.println(insertTitle.toString());   
	    System.out.println(insertDeptEmp.toString());*/
	    
	    TestDescription td = new TestDescription(null);
	    td.addQuery(1, selectSalaries);
	    
	    IComponent comp = new Component("HSQLDB");
		Map<String, String> parameterValues = new HashMap<>();
		//parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "45");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		
		/*int[] ports = new int[3];
		ports[0] = 9901;
		ports[1] = 9902;
		ports[2] = 9903;
		PortManager.getInstance().setupAvailablePorts(ports);
		
	    Benchmarker b = new Benchmarker(td, 1);
	    
	    double score = b.benchmark(i1);
	    System.out.println(score);*/
	    
		/*SelectQuery selectQuery = new SelectQuery()
	      .addColumns(employees_empNoCol, employees_empBirthCol, employees_empfNameCol, employees_emplNameCol, employees_empGenderCol, employees_empHireCol)
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
		
		double results = benchmarker.benchmark(i3);*/
		//System.out.println(String.format("Mean of test: %f",stats.getMean()));
	}

}

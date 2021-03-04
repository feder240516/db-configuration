package managers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.jooq.DatePart;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ExtractExpression;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.Query;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery.JoinType;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import exceptions.UnavailablePortsException;
import helpers.TestDescription;

class Data {
	IComponentInstance componentInstance;
	TestDescription test;
	int maxThreads;
	
	public Data(IComponentInstance componentInstance, TestDescription test, int maxThreads) {
		this.test = test;
		this.componentInstance = componentInstance;
		this.maxThreads = maxThreads;
	}
}

class DataProvider implements ArgumentsProvider {
	Map<String, Query> queries = new HashMap<>();
	
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream.of(
		          Arguments.of((Data) new Data(createCI("MariaDB"), createTD(), 1)), 
		          Arguments.of((Data) new Data(createCI("HSQLDB"), createTD(), 1))
		      );
	}
	
	public ComponentInstance createCI(String dbms) {
		IComponent comp = new Component(dbms);
		Map<String, String> parameterValues = new HashMap<>();
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		
		int[] ports = new int[3];
		ports[0] = 9901;
		ports[1] = 9902;
		ports[2] = 9903;
		PortManager.getInstance().setupAvailablePorts(ports);
		
		return new ComponentInstance(comp, parameterValues, reqInterfaces);
	}
	
	public TestDescription createTD() {
		TestDescription td = new TestDescription(null);
		SelectQuery select = getQuery();
		System.out.println("Select query: " + select.toString());
	    td.addQuery(1, select);
	    return td;
	}
	
	public void setQueries() {
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
	    
	    InsertQuery insertEmployee = new InsertQuery(employeesTable)
	    		.addColumn(employees_empNoCol, 500001)
	    		.addColumn(employees_empBirthCol, "2000-12-18")
	    		.addColumn(employees_empfNameCol, "Federico")
	    		.addColumn(employees_emplNameCol, "Reina")
	    		.addColumn(employees_empGenderCol, 'M')
	    		.addColumn(employees_empHireCol, new Date(System.currentTimeMillis()))
	    		.validate();
	    
	    
	}
	
	public SelectQuery getQuery() {
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

	    return selectSalaries;
	}
}


class BenchmarkerTest {
	
	@ParameterizedTest
	@ArgumentsSource(DataProvider.class)
	void testBenchmark(Data data){
		Benchmarker b = new Benchmarker(data.test, data.maxThreads);
	    double score;
		try {
			score = b.benchmark(data.componentInstance);
		} catch (InterruptedException | ExecutionException | UnavailablePortsException | IOException | SQLException e) {
			score = Double.MAX_VALUE;
			e.printStackTrace();
		}
	    System.out.println("Score obtained: " + score);
	    assertFalse(score == Double.MAX_VALUE, "Query was not executed");
	}
}

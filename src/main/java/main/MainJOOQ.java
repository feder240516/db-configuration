package main;

import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.ExtractExpression;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertSelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery.JoinType;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.PortManager;
import services.CSVService;

import static org.jooq.impl.DSL.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jooq.*;
import org.jooq.impl.*;

public class MainJOOQ {
	
	public static List<IComponentInstance> getComponentInstanceExamples() {
		int[] ports = new int[] {9901,9902,9903,9904,9905,9906,9907,9908,9909,9910};
		PortManager.getInstance().setupAvailablePorts(ports);
		IComponent compMaria 		= new Component("MariaDB");
		IComponent compDerby 		= new Component("ApacheDerby");
		IComponent compPosgreSQL 	= new Component("PostgreSQL");
		IComponent compHSQL 		= new Component("HSQLDB");
		List<IComponentInstance> componentInstances = new ArrayList<>();

		int[] derbyPageCacheSizes = new int[] {1000,2000,4000,8000,16000};
		for(int size: derbyPageCacheSizes ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("derby.storage.pageCacheSize", String.valueOf(size));
			parameterValues.put("__instanceID", String.format("DERBY_%s_%d", "pageCacheSize", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compDerby, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] hsqdbCacheRows = new int[] {10000,25000,50000,75000,100000};
		for(int size: hsqdbCacheRows ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("hsqldb.cache_rows", String.valueOf(size));
			//parameterValues.put("hsqldb.cache_file_scale", "100");
			parameterValues.put("hsqldb.nio_data_file", "TRUE");
			parameterValues.put("hsqldb.nio_max_size", "256");
			parameterValues.put("hsqldb.result_max_memory_rows", "0");
			parameterValues.put("hsqldb.applog", "0");
			parameterValues.put("__instanceID", String.format("HSQLDB_%s_%d", "hsqldb.cache_rows", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compHSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		return componentInstances;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		DSLContext dslContext = DSL.using(SQLDialect.MARIADB);
		Query query = dslContext.select(field("BOOK.TITLE"), field("AUTHOR.FIRST_NAME"), field("AUTHOR.LAST_NAME"))
                .from(table("BOOK"))
                .join(table("AUTHOR"))
                .on(field("BOOK.AUTHOR_ID").eq(field("AUTHOR.ID")))
                .where(field("BOOK.PUBLISHED_IN").eq(1948));
		Query query2 = dslContext.select(extract(Date.from(Instant.now()), DatePart.MONTH));
		System.out.println(query2.getSQL(true));
		// StatementType.STATIC_STATEMENT
		System.out.println(query2.getBindValues());
		
		/*
		 * SelectQuery selectSalaries = new SelectQuery()
	    		.addColumns(employees_empNoCol, employees_empfNameCol, employees_emplNameCol, salaries_salaryCol)
	    		.addJoin(JoinType.INNER, employeesTable, salariesTable, BinaryCondition.equalTo(employees_empNoCol, salaries_empNoCol))
	    		.addCondition(BinaryCondition.equalTo(extractYear(i1, salaries_toDateCol), 9999))
	    		.validate();
		 */
		
		Query selectSalaries = dslContext.select(field("employees.emp_no"), 
										field("employees.first_name"), 
										field("employees.last_name"), 
										field("salaries.salary"))
								.from("employees")
								.join("salaries")
								.on(field("employees.emp_no").eq(field("salaries.emp_no")))
								.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)));
		
		
		/*
		 * SelectQuery selectAvgSalaryTitles = new SelectQuery()
	    		.addCustomColumns(titles_titleCol, FunctionCall.avg().addColumnParams(salaries_salaryCol))
	    		.addJoin(JoinType.INNER, salariesTable, titlesTable, BinaryCondition.equalTo(salaries_empNoCol, titles_empNoCol))
	    		.addCondition(BinaryCondition.equalTo(new ExtractExpression(DatePart.YEAR, salaries_toDateCol), 9999))
	    		.addGroupings(titles_titleCol).validate();
	    */
		Query selectAvgSalaryTitles = dslContext.select(field("titles.title"), 
											avg(field("salaries.salary").cast(Double.class)))
										.from("salaries")
										.join("titles")
										.on(field("salaries.emp_no").eq(field("titles.emp_no")))
										.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)))
										.groupBy(field("titles.title"));
		/*
	    
	    SelectQuery selectAvgSalaryTitlesGender = new SelectQuery()
	    		.addCustomColumns(titles_titleCol, employees_empGenderCol, FunctionCall.avg().addColumnParams(salaries_salaryCol))
	    		.addJoin(JoinType.INNER, salariesTable, titlesTable, BinaryCondition.equalTo(salaries_empNoCol, titles_empNoCol))
	    		.addJoin(JoinType.INNER, salariesTable, employeesTable, BinaryCondition.equalTo(salaries_empNoCol, employees_empNoCol))
	    		.addCondition(BinaryCondition.equalTo(new ExtractExpression(DatePart.YEAR, salaries_toDateCol), 9999))
	    		.addGroupings(employees_empGenderCol, titles_titleCol).validate();
	    
		 */
		
		Query selectAvgSalaryTitlesGender = dslContext.select(
				field("titles.title"),field("employees.gender"),avg(field("salaries.salary").cast(Double.class))
				).from("salaries")
				.join("titles").on(field("salaries.emp_no").eq(field("titles.emp_no")))
				.join("employees").on(field("salaries.emp_no").eq(field("employees.emp_no")))
				.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)))
				.groupBy(field("employees.gender"), field("titles.title"));
		
		/*
		 * InsertSelectQuery insertEmployee = new InsertSelectQuery(employeesTable)
	    		.addColumns(employees_empNoCol, employees_empBirthCol, employees_empfNameCol, employees_emplNameCol, employees_empGenderCol, employees_empHireCol)
	    		.setSelectQuery(new SelectQuery()
	    				.addCustomColumns(new CustomSql(String.format("%s%s", FunctionCall.max().addColumnParams(employees_empNoCol), "+1")), birthDate,fName, lName,gender,  new CustomSql(String.format("'%s' %s", hireDate, "FROM employees t0")))).validate();
		 */
		
		/*Query insertEmployee = dslContext.insertInto(table("employees"), field("emp_no"), field("birth_date"), field("first_name"), field("last_name"), field("gender"), field("hire_date"))
											.values(values);*/
		//select();
		System.out.println(selectSalaries.getSQL(true));
		//Query selectSalariesDerby = selectSalaries.;
		System.out.println(selectAvgSalaryTitles.getSQL(true));
		System.out.println(selectAvgSalaryTitlesGender.getSQL(true));
		
		
		/*query3.configuration().set(SQLDialect.MARIADB);
		System.out.println(query3.getSQL(true));*/
		//System.out.println(query3.configuration().set(SQLDialect.MARIADB));
		
		TestDescription td = new TestDescription(10);
	    td.addQuery(1, selectSalaries);
	    
	    int threads = 4;
	    Benchmarker benchmarker = new Benchmarker(td, threads);
	    
	    List<IComponentInstance> componentInstances = getComponentInstanceExamples();
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(threads);
		List<Callable<Double>> taskList = new ArrayList<>();
		for(IComponentInstance instance: componentInstances) {
			taskList.add(() -> {
				return benchmarker.benchmark(instance);
			});
		}
		List<Future<Double>> resultList = executor.invokeAll(taskList);
		executor.shutdown();
		for(Future<Double> result: resultList) {
			System.out.println(result.get());
		}
	    
	    CSVService.getInstance().dumpToDisk();
	}
}

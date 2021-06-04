package web;

import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import helpers.TestDescription;
import managers.Benchmarker;

public class BenchMarkerForWeb {
	private static BenchMarkerForWeb _instance;
	public Benchmarker benchmarker;
	public BenchMarkerForWeb(int threads) {
		TestDescription td = generateTestDescription();
		benchmarker = new Benchmarker(td, threads);
	}
	
	public static void CreateBenchmarker(int threads) {
		_instance = new BenchMarkerForWeb(threads);
	}
	
	public static BenchMarkerForWeb getInstance() {
		return _instance;
	}
	
	public static Benchmarker getInstanceBenchmarker() {
		return _instance.benchmarker;
	}
	
	public TestDescription generateTestDescription() {
		DSLContext dslContext = DSL.using(SQLDialect.MARIADB);
		
		Query selectSalaries = dslContext.select(field("employees.emp_no"), 
				field("employees.first_name"), 
				field("employees.last_name"), 
				field("salaries.salary"))
		.from("employees")
		.join("salaries")
		.on(field("employees.emp_no").eq(field("salaries.emp_no")))
		.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)));
		
		
		TestDescription td1 = new TestDescription("Only select salaries", 2);
	    td1.addQuery(1, selectSalaries);
	    return td1;
	}
}

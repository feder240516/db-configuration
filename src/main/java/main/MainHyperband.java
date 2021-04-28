package main;

import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.val;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.IMultiFidelityObjectEvaluator;
import ai.libs.jaicore.ml.hpo.multifidelity.MultiFidelitySoftwareConfigurationProblem;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.IHyperbandConfig;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband.HyperbandSolutionCandidate;
import exceptions.UnavailablePortsException;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.PortManager;

public class MainHyperband {
	public static void main(String[] args){
		DSLContext dslContext = DSL.using(SQLDialect.MARIADB);
		Query selectSalaries = dslContext.select(field("employees.emp_no"), 
										field("employees.first_name"), 
										field("employees.last_name"), 
										field("salaries.salary"))
								.from("employees")
								.join("salaries")
								.on(field("employees.emp_no").eq(field("salaries.emp_no")))
								.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)));
		selectSalaries.configuration().set(SQLDialect.MARIADB);
		Query selectAvgSalaryTitles = dslContext.select(field("titles.title"), 
											avg(field("salaries.salary").cast(Double.class)))
										.from("salaries")
										.join("titles")
										.on(field("salaries.emp_no").eq(field("titles.emp_no")))
										.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)))
										.groupBy(field("titles.title"));
		Query selectAvgSalaryTitlesGender = dslContext.select(
				field("titles.title"),field("employees.gender"),avg(field("salaries.salary").cast(Double.class))
				).from("salaries")
				.join("titles").on(field("salaries.emp_no").eq(field("titles.emp_no")))
				.join("employees").on(field("salaries.emp_no").eq(field("employees.emp_no")))
				.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)))
				.groupBy(field("employees.gender"), field("titles.title"));
		Query insertEmployee = dslContext.insertInto(table("employees"), field("employees.emp_no"), field("employees.birth_date"), field("first_name"), field("last_name"), field("gender"), field("hire_date"))
										.select(dslContext.select(max(field("employees.emp_no")).add(1), field("employees.birth_date"), field("first_name"), field("last_name"), field("gender"), field("hire_date")));
		//System.out.println(selectSalaries.getSQL(true));
		//System.out.println(selectAvgSalaryTitles.getSQL(true));
		//System.out.println(selectAvgSalaryTitlesGender.getSQL(true));
		
		File config = new File("C:/Users/WIN/Desktop/Semillero/db-configuration/hyperband.json");

		int[] ports = new int[] {9901,9902,9903,9904,9905,9906,9907,9908,9909,9910};
		PortManager.getInstance().setupAvailablePorts(ports);
		
		Collection<IComponent> components = new ArrayList<>();
		
		/*Component compMaria = new Component("MariaDB");
		compMaria.addParameter(new Parameter("DIV_PRECISION_INCREMENT", new NumericParameterDomain(true, 0, 30), 4));
		compMaria.addParameter(new Parameter("JOIN_CACHE_LEVEL", new NumericParameterDomain(true, 0, 8), 2));
		compMaria.addParameter(new Parameter("LOG_SLOW_RATE_LIMIT", new NumericParameterDomain(true, 1, 10000000), 1));
		compMaria.addParameter(new Parameter("LONG_QUERY_TIME", new NumericParameterDomain(true, 0, 10000000), 10));
		compMaria.addParameter(new Parameter("MAX_LENGTH_FOR_SORT_DATA", new NumericParameterDomain(true, 4, 8388608), 1024));
		compMaria.addParameter(new Parameter("MIN_EXAMINED_ROW_LIMIT", new NumericParameterDomain(true, 0, 4294967295l), 0));
		compMaria.addParameter(new Parameter("OPTIMIZER_PRUNE_LEVEL", new NumericParameterDomain(true, 0, 1), 1));
		compMaria.addParameter(new Parameter("OPTIMIZER_SEARCH_DEPTH", new NumericParameterDomain(true, 0, 62), 62));
		compMaria.addParameter(new Parameter("OPTIMIZER_USE_CONDITION_SELECTIVITY", new NumericParameterDomain(true, 1, 5), 4));
		components.add(compMaria);*/
		
		/*Component compDerby = new Component("ApacheDerby");
		compDerby.addParameter(new Parameter("derby.language.statementCacheSize", new NumericParameterDomain(true, 1, 1000), 100));
		compDerby.addParameter(new Parameter("derby.storage.initialPages", new NumericParameterDomain(true, 1, 1000), 1));
		compDerby.addParameter(new Parameter("derby.storage.pageReservedSpace", new NumericParameterDomain(true, 0, 100), 20));
		compDerby.addParameter(new Parameter("derby.storage.pageSize", new NumericParameterDomain(true, 4096, 32768), 4096));
		components.add(compDerby);*/
		
		Component compHSQL = new Component("HSQLDB");
		compHSQL.addParameter(new Parameter("hsqldb.cache_rows", new NumericParameterDomain(true, 100, 1000000), 50000));
		compHSQL.addParameter(new Parameter("hsqldb.result_max_memory_rows", new NumericParameterDomain(true, 0, 10000000), 0));
		//compHSQL.addParameter(new Parameter("hsqldb.nio_max_size", new NumericParameterDomain(true, 64, 1024), 256));
		components.add(compHSQL);
		
		/*Component compPosgreSQL = new Component("PostgreSQL");
		compPosgreSQL.addParameter(new Parameter("hash_mem_multiplier", new NumericParameterDomain(false, 1., 20.), 1));
		compPosgreSQL.addParameter(new Parameter("shared_buffers", new NumericParameterDomain(true, 16, 262144), 131072));
		compPosgreSQL.addParameter(new Parameter("work_mem", new NumericParameterDomain(true, 64, 262144), 4096));
		components.add(compPosgreSQL);*/

		
		String requiredInterface = "HSQLDB";
		TestDescription td = new TestDescription("Only select salaries", 2);
	    td.addQuery(1, selectSalaries);
	    
	    int threads = 2;
	    Benchmarker benchmarker = new Benchmarker(td, threads);
	    
		IMultiFidelityObjectEvaluator<IComponentInstance, Double> evaluator = new IMultiFidelityObjectEvaluator<IComponentInstance, Double>() {
			@Override
			public double getMaxBudget() {
				return 5.0;
			}

			@Override
			public double getMinBudget() {
				return 1.0;
			}

			@Override
			public Double evaluate(final IComponentInstance t, final double budget) throws InterruptedException, ObjectEvaluationFailedException {
					try {
						return benchmarker.benchmark(t);
					} catch (InterruptedException | ExecutionException | UnavailablePortsException | IOException
							| SQLException e) {
						e.printStackTrace();
					}
					
					return Double.MAX_VALUE;
			}
		};

		MultiFidelitySoftwareConfigurationProblem<Double> input = new MultiFidelitySoftwareConfigurationProblem<>(components, requiredInterface, evaluator);
		Hyperband hb = new Hyperband(ConfigFactory.create(IHyperbandConfig.class), input);
		
		HyperbandSolutionCandidate result;
		try {
			result = hb.call();
			
			System.out.println(result.getComponentInstance().getComponent().getName());
			System.out.println("Parameters: ");
			for (Map.Entry<String, String> entry : result.getComponentInstance().getParameterValues().entrySet()) {
			    String key = entry.getKey();
			    String value = entry.getValue();
			    System.out.println(key + " = " + value);
			}
		} catch (AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException
				| AlgorithmException e) {
			e.printStackTrace();
		}
		
	}
}

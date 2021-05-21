package main;

import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.val;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.jooq.DatePart;
import org.jooq.Query;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import benchmark.core.api.ConversionFailedException;
import benchmark.core.api.IConverter;
import benchmark.core.api.IHyperoptObjectEvaluator;
import benchmark.core.api.IOptimizer;
import benchmark.core.api.output.IOptimizationOutput;
import benchmark.core.impl.optimizer.cfg.ggp.IGeneticOptimizerConfig;
import benchmark.core.impl.optimizer.pcs.IPCSOptimizerConfig;
import exceptions.UnavailablePortsException;
import extras.BOHBOptimizer;
import extras.IPlanningOptimizationTask;
import extras.ParameterRefinementConfiguration;
import extras.PlanningOptimizationTask;
import extras.SMACOptimizer;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.PortManager;
import services.CSVService;

class SMAC {
	
	public static Collection<Component> buildComponents(boolean withCategorical) {
		Collection<Component> components = new ArrayList<Component>();
		String requiredInterface = "IDatabase";
		
		Component compMaria = new Component("MariaDB");
		compMaria.addParameter(new Parameter("DIV_PRECISION_INCREMENT", new NumericParameterDomain(true, 0, 30), 20));
		compMaria.addParameter(new Parameter("EXPENSIVE_SUBQUERY_LIMIT", new NumericParameterDomain(true, 0, 100000), 100));
		if(withCategorical) compMaria.addParameter(new Parameter("GLOBAL FLUSH", new CategoricalParameterDomain(new String[] {"OFF", "ON"}), "OFF"));
		compMaria.addParameter(new Parameter("JOIN_BUFFER_SIZE", new NumericParameterDomain(true, 128, 4194304), 262144));
		compMaria.addParameter(new Parameter("JOIN_CACHE_LEVEL", new NumericParameterDomain(true, 0, 8), 2));
		if(withCategorical) compMaria.addParameter(new Parameter("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", new CategoricalParameterDomain(new String[] {"OFF", "ON"}), "OFF"));
		compMaria.addParameter(new Parameter("LOG_SLOW_RATE_LIMIT", new NumericParameterDomain(true, 1, 10000000), 1));
		compMaria.addParameter(new Parameter("LONG_QUERY_TIME", new NumericParameterDomain(true, 1, 100000), 10));
		compMaria.addParameter(new Parameter("MAX_LENGTH_FOR_SORT_DATA", new NumericParameterDomain(true, 4, 16384), 1024));
		compMaria.addParameter(new Parameter("MIN_EXAMINED_ROW_LIMIT", new NumericParameterDomain(true, 0, 1048576), 0));
		if(withCategorical) compMaria.addParameter(new Parameter("OPTIMIZER_PRUNE_LEVEL", new CategoricalParameterDomain(new String[] {"0", "1"}), "1"));
		compMaria.addParameter(new Parameter("OPTIMIZER_SEARCH_DEPTH", new NumericParameterDomain(true, 0, 62), 62));
		compMaria.addParameter(new Parameter("OPTIMIZER_USE_CONDITION_SELECTIVITY", new NumericParameterDomain(true, 1, 5), 4));
		compMaria.addProvidedInterface(requiredInterface);
		
		
		Component compDerby = new Component("ApacheDerby");
		compDerby.addParameter(new Parameter("derby.storage.pageReservedSpace", new NumericParameterDomain(true, 0, 100), 20));
		if(withCategorical) compDerby.addParameter(new Parameter("derby.storage.pageSize", new CategoricalParameterDomain(new String[] {"4096", "8192", "16384", "32768"}), "4096"));
		if(withCategorical) compDerby.addParameter(new Parameter("derby.storage.rowLocking", new CategoricalParameterDomain(new String[] {"true", "false"}), "true"));
		compDerby.addParameter(new Parameter("derby.storage.initialPages", new NumericParameterDomain(true, 1, 1000), 1));
		compDerby.addParameter(new Parameter("derby.language.statementCacheSize", new NumericParameterDomain(true, 0, 10000), 100));
		compDerby.addParameter(new Parameter("derby.replication.logBufferSize", new NumericParameterDomain(true, 8192, 1048576), 32768));
		compDerby.addParameter(new Parameter("derby.locks.escalationThreshold", new NumericParameterDomain(true, 100, 1048576), 5000));
		compDerby.addProvidedInterface(requiredInterface);
		
		
		Component compHSQL = new Component("HSQLDB");
		compHSQL.addParameter(new Parameter("hsqldb.cache_rows", new NumericParameterDomain(true, 100, 4000000), 50000));
		if(withCategorical) compHSQL.addParameter(new Parameter("hsqldb.nio_data_file", new CategoricalParameterDomain(new String[] {"TRUE", "FALSE"}), "TRUE"));
		if(withCategorical) compHSQL.addParameter(new Parameter("hsqldb.nio_max_size", new CategoricalParameterDomain(new String[] {"64", "128", "256", "512", "1024"}), "256"));
		if(withCategorical) compHSQL.addParameter(new Parameter("hsqldb.applog", new CategoricalParameterDomain(new String[] {"0", "1", "2", "3"}), "0"));
		if(withCategorical) compHSQL.addParameter(new Parameter("hsqldb.result_max_memory_rows", new CategoricalParameterDomain(new String[] {"0", "1000", "2000", "3000", "5000", "8000", "10000"}), "0"));
		compHSQL.addProvidedInterface(requiredInterface);
		
		
		Component compPostgres = new Component("PostgreSQL");
		compPostgres.addParameter(new Parameter("work_mem", new NumericParameterDomain(true, 64, 2097151), 4096));
		compPostgres.addParameter(new Parameter("shared_buffers", new NumericParameterDomain(true, 16, 1048576), 131072));
		compPostgres.addParameter(new Parameter("temp_buffers", new NumericParameterDomain(true, 100, 131072), 1024));
		compPostgres.addParameter(new Parameter("max_prepared_transactions", new NumericParameterDomain(true, 0, 131072), 0));
		compPostgres.addParameter(new Parameter("hash_mem_multiplier", new NumericParameterDomain(true, 1, 8), 1));
		//compPostgres.addParameter(new Parameter("replacement_sort_tuples", new NumericParameterDomain(true, 10000, 400000), 150000));
		compPostgres.addProvidedInterface(requiredInterface);
		
		components.add(compDerby);
		components.add(compMaria);
		components.add(compHSQL);
		components.add(compPostgres);
		return components;
	}
	
	public static IConverter<ComponentInstance, IComponentInstance> buildConverter() {
		return new IConverter<ComponentInstance, IComponentInstance>() {
			@Override
			public IComponentInstance convert(ComponentInstance ci) throws ConversionFailedException {
				return ci;
			}
		};
	}
	
	public static double evaluateComponentInstance(IComponentInstance ci, Benchmarker benchmarker) {
		try {
        	return benchmarker.benchmark(ci);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return Double.MAX_VALUE;
	}
	
	public static IHyperoptObjectEvaluator<IComponentInstance> buildEvaluator(Benchmarker benchmarker) {
		return new IHyperoptObjectEvaluator<IComponentInstance>() {
			@Override
			public Double evaluate(IComponentInstance ci, int budget)
					throws ObjectEvaluationFailedException, InterruptedException {
				try {
					return evaluateComponentInstance(ci, benchmarker);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Double.MAX_VALUE;
			}

			@Override
			public int getMaxBudget() {
				return 999999999;
			}
		};
	}
	
	public static IOptimizer<IPlanningOptimizationTask<IComponentInstance>, IComponentInstance> buildOptimizer(int threads, String requiredInterface, Timeout globalTimeout, Timeout evalTimeout, int queryProfile) {
		int PRECAUTION_OFFSET_MS = 60 * 1000;

		TestDescription td1 = MainExecuteAnyAlgorithm.buildTestDescription(queryProfile);
		Benchmarker benchmarker = new Benchmarker(td1, threads);
		// Components
		Collection<Component> components = buildComponents(false);
		IConverter<ComponentInstance, IComponentInstance> converter = buildConverter();
		IHyperoptObjectEvaluator<IComponentInstance> evaluator = buildEvaluator(benchmarker);
		

		Map<Component, Map<Parameter, ParameterRefinementConfiguration>> parameterRefinementConfiguration = new HashMap<>();
		IGeneticOptimizerConfig gaConfig = ConfigFactory.create(IGeneticOptimizerConfig.class);

		IPlanningOptimizationTask<IComponentInstance> task = new PlanningOptimizationTask<IComponentInstance>(converter,
				evaluator, components, requiredInterface, globalTimeout, evalTimeout,
				parameterRefinementConfiguration);
		
		IPCSOptimizerConfig pcsConfig = ConfigFactory.create(IPCSOptimizerConfig.class);
		pcsConfig.setProperty(IPCSOptimizerConfig.K_CPUS, threads + "");
		SMACOptimizer<IComponentInstance> optimizer = new SMACOptimizer<IComponentInstance>("Experiment", pcsConfig, task);
		optimizer.setTimeoutPrecautionOffset(PRECAUTION_OFFSET_MS);
		return optimizer;
	}
	
	public static void run(int THREADS, int TIME_LIMIT, int queryProfile) {
		String REQUIRED_INTERFACE = "IDatabase";
		int LOCAL_TIME = 5;

		final Timeout GLOBAL_TIMEOUT = new Timeout(TIME_LIMIT, TimeUnit.MINUTES);
		final Timeout EVAL_TIMEOUT = new Timeout(LOCAL_TIME, TimeUnit.MINUTES);
		
		IOptimizer<IPlanningOptimizationTask<IComponentInstance>, IComponentInstance> opt = buildOptimizer(THREADS, REQUIRED_INTERFACE, GLOBAL_TIMEOUT, EVAL_TIMEOUT, queryProfile);
		try {
			IOptimizationOutput<IComponentInstance> result = opt.call();
			System.out.println(String.format("Final score: %f", result.getScore()));
			
		} catch (AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException
				| AlgorithmException e) {
			e.printStackTrace();
		}
	}
}

public class MainExecuteAnyAlgorithm {
	public static Query generateQuerySelectSalaries() {
		return select(field("employees.emp_no"), field("employees.first_name"), field("employees.last_name"),
				field("salaries.salary")).from("employees").join("salaries")
						.on(field("employees.emp_no").eq(field("salaries.emp_no")))
						.where(extract(field("salaries.to_date"), DatePart.YEAR).eq(val(9999)));
	}
	
	public static Query generateQuerySelectByTitle() {
		return select(field("titles.title"), 
				avg(field("salaries.salary").cast(Double.class)))
			.from("salaries")
			.join("titles")
			.on(field("salaries.emp_no").eq(field("titles.emp_no")))
			.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)))
			.groupBy(field("titles.title"));
	}
	
	public static Query generateInsertSalaries() {
		return select(field("employees.emp_no"), field("employees.first_name"), field("employees.last_name"),
				field("salaries.salary")).from("employees").join("salaries")
						.on(field("employees.emp_no").eq(field("salaries.emp_no")))
						.where(extract(field("salaries.to_date"), DatePart.YEAR).eq(val(9999)));
	}
	
	public static Query generateUpdateSalaries() {
		return select(field("employees.emp_no"), field("employees.first_name"), field("employees.last_name"),
				field("salaries.salary")).from("employees").join("salaries")
						.on(field("employees.emp_no").eq(field("salaries.emp_no")))
						.where(extract(field("salaries.to_date"), DatePart.YEAR).eq(val(9999)));
	}
	
	public static IConverter<ComponentInstance, IComponentInstance> buildConverter() {
		return new IConverter<ComponentInstance, IComponentInstance>() {
			@Override
			public IComponentInstance convert(ComponentInstance ci) throws ConversionFailedException {
				return ci;
			}
		};
	}
	
	public static double evaluateComponentInstance(IComponentInstance ci) {
		try {
        	return 42.;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 84.;
	}
	
	public static IHyperoptObjectEvaluator<IComponentInstance> buildEvaluator(Benchmarker benchmarker) {
		return new IHyperoptObjectEvaluator<IComponentInstance>() {
			@Override
			public Double evaluate(IComponentInstance ci, int budget)
					throws ObjectEvaluationFailedException, InterruptedException {
				try {
					return evaluateComponentInstance(ci);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Double.MAX_VALUE;
			}

			@Override
			public int getMaxBudget() {
				return 9999999;
			}
		};
	}
	
	public static IOptimizer<IPlanningOptimizationTask<IComponentInstance>, IComponentInstance> buildOptimizer(int threads, String requiredInterface, Timeout globalTimeout, Timeout evalTimeout, int queryProfile) {
		TestDescription td1 = buildTestDescription(queryProfile);
		Benchmarker benchmarker = new Benchmarker(td1, threads);
		// Components
		Collection<Component> components = SMAC.buildComponents(true);
		IConverter<ComponentInstance, IComponentInstance> converter = buildConverter();
		IHyperoptObjectEvaluator<IComponentInstance> evaluator = buildEvaluator(benchmarker);
		

		Map<Component, Map<Parameter, ParameterRefinementConfiguration>> parameterRefinementConfiguration = new HashMap<>();
		IGeneticOptimizerConfig gaConfig = ConfigFactory.create(IGeneticOptimizerConfig.class);

		IPlanningOptimizationTask<IComponentInstance> task = new PlanningOptimizationTask<IComponentInstance>(converter,
				evaluator, components, requiredInterface, globalTimeout, evalTimeout,
				parameterRefinementConfiguration);
		
		IPCSOptimizerConfig pcsConfig = ConfigFactory.create(IPCSOptimizerConfig.class);
		pcsConfig.setProperty(IPCSOptimizerConfig.K_CPUS, threads + "");
		
		return new SMACOptimizer<IComponentInstance>("Experiment", pcsConfig, task);
	}
	
	public static TestDescription buildTestDescription(int index) {
		Map<Integer, String> sqlnames = new HashMap<>();
		sqlnames.put(1, "SEL1");
		sqlnames.put(2, "SEL2");
		sqlnames.put(4, "INS");
		sqlnames.put(5, "UPD");
		TestDescription td = new TestDescription(sqlnames.get(index),3);
		Query query = null;
		switch (index) {
		case 1:
			query = generateQuerySelectSalaries();
			break;
		case 2:
			query = generateQuerySelectByTitle();
			break;
		case 3:
			throw new RuntimeException("Not existent");
		case 4:
			query = generateInsertSalaries();
			break;
		case 5:
			query = generateUpdateSalaries();
			break;
		default:
			throw new RuntimeException("Invalid query index");
		}
		td.addIndividualQuery(query);
		return td;
	}
	
	public static void runHASCO(int THREADS, int TIME_IN_MINUTES, int queryProfile) throws IOException {
		File newFile = new File("src/main/java/configuration/dbTestProblem.json");
		System.out.println(newFile.getCanonicalPath());
		
		TestDescription td1 = buildTestDescription(queryProfile);
		Benchmarker b = new Benchmarker(td1, THREADS);
		
		UUID executionUUID = UUID.randomUUID();
		CSVService.getInstance().setAlgorithm("HASCO");
		CSVService.getInstance().setExperimentUUID(executionUUID.toString());
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<Double>(newFile, "IDatabase", (ci) -> {
			try {
				return b.benchmark(ci);
			} catch (Exception e) {
				e.printStackTrace();
				return Double.POSITIVE_INFINITY;
			}
		} )  ;
		HASCOViaFD<Double> hasco = HASCOBuilder.get()
					.withProblem(problem)
					.withBestFirst().withRandomCompletions().withNumSamples(1)
					.withTimeout(new Timeout(TIME_IN_MINUTES, TimeUnit.MINUTES))
					.withCPUs(THREADS)
					.getAlgorithm();
		double startingTime = System.currentTimeMillis();
		CSVService.getInstance().setStartingPoint();
		hasco.registerListener(new Object() {
			double lastRecordedTime = 0;
			@Subscribe
			public void receiveSolution(final HASCOSolutionEvent<?> solutionEvent) {
				System.out.println(new ComponentSerialization().serialize(solutionEvent.getSolutionCandidate().getComponentInstance()));
				double now = System.currentTimeMillis();
				double TEN_MINUTES = 1000 * 60 * 10;
				if (now - lastRecordedTime > TEN_MINUTES) {
					lastRecordedTime = now;
					try {
						CSVService.getInstance().dumpWithVars3("PARTIAL");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		try {
			hasco.call();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (hasco.getBestScoreKnownToExist() != null) {
				System.out.println("Finished");
				System.out.println(String.format("Best candidate for %d minutes execution with %d threads: %s", TIME_IN_MINUTES, THREADS, new ComponentSerialization().serialize(hasco.getBestSeenSolution().getComponentInstance())));
				System.out.println(String.format("Time: %f", hasco.getBestScoreKnownToExist()));
				CSVService.getInstance().dumpWithVars3("COMPHASCOLETE");
			} else {
				System.out.println("Didn't work HASCO. Retrying");
			}
		}
	}
	
	public static void runBOHB(int THREADS, int TIME_IN_MINUTES, int queryProfile) {
		int NUM_TESTS = 3;
		Query selectSalaries = generateQuerySelectSalaries();

		TestDescription td1 = buildTestDescription(queryProfile);//new TestDescription("Only select salaries", NUM_TESTS);
		td1.addQuery(1, selectSalaries);

		Benchmarker benchmarker = new Benchmarker(td1, THREADS);

		// Components
		Collection<Component> components = new ArrayList<Component>();
		Component compMaria = new Component("MariaDB");
		compMaria.addParameter(new Parameter("DIV_PRECISION_INCREMENT", new NumericParameterDomain(true, 0, 30), 4));
		compMaria.addParameter(new Parameter("JOIN_CACHE_LEVEL", new NumericParameterDomain(true, 0, 8), 2));
		compMaria.addParameter(new Parameter("LOG_SLOW_RATE_LIMIT", new NumericParameterDomain(true, 1, 10000000), 1));
		compMaria.addParameter(new Parameter("LONG_QUERY_TIME", new NumericParameterDomain(true, 0, 10000000), 10));
		compMaria.addParameter(
				new Parameter("MAX_LENGTH_FOR_SORT_DATA", new NumericParameterDomain(true, 4, 8388608), 1024));
		compMaria.addParameter(
				new Parameter("MIN_EXAMINED_ROW_LIMIT", new NumericParameterDomain(true, 0, 4294967295l), 0));
		compMaria.addParameter(new Parameter("OPTIMIZER_PRUNE_LEVEL", new NumericParameterDomain(true, 0, 1), 1));
		compMaria.addParameter(new Parameter("OPTIMIZER_SEARCH_DEPTH", new NumericParameterDomain(true, 0, 62), 62));
		compMaria.addParameter(
				new Parameter("OPTIMIZER_USE_CONDITION_SELECTIVITY", new NumericParameterDomain(true, 1, 5), 4));
		String requiredInterface = "IDatabase";
		compMaria.addProvidedInterface(requiredInterface);
		components.add(compMaria);

		IConverter<ComponentInstance, IComponentInstance> converter = new IConverter<ComponentInstance, IComponentInstance>() {
			@Override
			public IComponentInstance convert(ComponentInstance ci) throws ConversionFailedException {
				return ci;
			}
		};
		
		IHyperoptObjectEvaluator<IComponentInstance> evaluator = new IHyperoptObjectEvaluator<IComponentInstance>() {
			@Override
			public Double evaluate(IComponentInstance ci, int budget)
					throws ObjectEvaluationFailedException, InterruptedException {
				try {
					return benchmarker.benchmark(ci);
				} catch (InterruptedException | ExecutionException | UnavailablePortsException | IOException
						| SQLException e) {
					e.printStackTrace();
				}

				return Double.MAX_VALUE;
			}

			@Override
			public int getMaxBudget() {
				return 9999999;
			}
		};
		
		final Timeout GLOBAL_TIMEOUT = new Timeout(5, TimeUnit.MINUTES);
		final Timeout EVAL_TIMEOUT = new Timeout(1, TimeUnit.MINUTES);

		Map<Component, Map<Parameter, ParameterRefinementConfiguration>> parameterRefinementConfiguration = new HashMap<>();
		IGeneticOptimizerConfig gaConfig = ConfigFactory.create(IGeneticOptimizerConfig.class);

		IPlanningOptimizationTask<IComponentInstance> task = new PlanningOptimizationTask<IComponentInstance>(converter,
				evaluator, components, requiredInterface, GLOBAL_TIMEOUT, EVAL_TIMEOUT,
				parameterRefinementConfiguration);
		
		IPCSOptimizerConfig pcsConfig = ConfigFactory.create(IPCSOptimizerConfig.class);
		pcsConfig.setProperty(IPCSOptimizerConfig.K_CPUS, THREADS + "");
		
		IOptimizer<IPlanningOptimizationTask<IComponentInstance>, IComponentInstance> opt = new BOHBOptimizer<IComponentInstance>("1", pcsConfig, task);
		try {
			IOptimizationOutput<IComponentInstance> result = opt.call();
		} catch (AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException
				| AlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public static void runRandom(int THREADS, int TIME_IN_MINUTES, int queryProfile) throws IOException {
		int NUM_TESTS = 3;
		File newFile = new File("src/main/java/configuration/dbTestProblem.json");
		System.out.println(newFile.getCanonicalPath());
		
		TestDescription td1 = buildTestDescription(queryProfile);
		Benchmarker b = new Benchmarker(td1, THREADS);
		
		UUID executionUUID = UUID.randomUUID();
		CSVService.getInstance().setAlgorithm("Random");
		CSVService.getInstance().setExperimentUUID(executionUUID.toString());
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<Double>(newFile, "IDatabase", (ci) -> {
			try {
				return b.benchmark(ci);
			} catch (Exception e) {
				e.printStackTrace();
				return Double.POSITIVE_INFINITY;
			}
		} )  ;
		HASCOViaFD<Double> hasco = HASCOBuilder.get()
					.withProblem(problem)
					.withBlindSearch()
					.withTimeout(new Timeout(TIME_IN_MINUTES, TimeUnit.MINUTES))
					.withCPUs(THREADS)
					.getAlgorithm();
		double startingTime = System.currentTimeMillis();
		CSVService.getInstance().setStartingPoint();
		hasco.registerListener(new Object() {
			double lastRecordedTime = 0;
			@Subscribe
			public void receiveSolution(final HASCOSolutionEvent<?> solutionEvent) {
				System.out.println(new ComponentSerialization().serialize(solutionEvent.getSolutionCandidate().getComponentInstance()));
				double now = System.currentTimeMillis();
				double TEN_MINUTES = 1000 * 60 * 10;
				if (now - lastRecordedTime > TEN_MINUTES) {
					lastRecordedTime = now;
					try {
						CSVService.getInstance().dumpWithVars3("PARTIAL");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		try {
			hasco.call();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (hasco.getBestScoreKnownToExist() != null) {
				System.out.println("Finished");
				System.out.println(String.format("Best candidate for %d minutes execution with %d threads: %s", TIME_IN_MINUTES, THREADS, new ComponentSerialization().serialize(hasco.getBestSeenSolution().getComponentInstance())));
				System.out.println(String.format("Time: %f", hasco.getBestScoreKnownToExist()));
				CSVService.getInstance().dumpWithVars3("COMPRANDOMLETE");
			} else {
				System.out.println("Didn't work HASCO. Retrying");
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length < 4) {
			System.out.println(String.format("Calling args: %s", String.join(" ", args)));
			System.err.println("Must be called with 4-6 args: algorithm: (HASCO|BOHB|SMAC|Random), queryProfile: 1-5(sel1,sel2,sel3,ins,upd), threads, timeLimit, numOfExecutions (def=10), numOfLastExecution(def=0)");
			return;
		}
		String algorithm = args[0];
		int queryProfile = Integer.valueOf(args[1]);
		int THREADS = Integer.valueOf(args[2]);
		int timeLimit = Integer.valueOf(args[3]);
		int numOfExecutions = args.length >= 5 ? Integer.valueOf(args[4]) : 10;
		int lastExecution = args.length >= 6 ? Integer.valueOf(args[5]) : 0;
		System.out.println(String.format("%d threads", THREADS));
		//int[] ports = new int[] { 9901, 9902, 9903, 9904, 9905, 9906, 9907, 9908, 9909 };
		int amountOfPorts = 1000; 
		int[] ports = new int[amountOfPorts];
		for(int i = 9900; i < 9900+amountOfPorts; ++i) {
			ports[i-9900] = i;
		}
		PortManager.getInstance().setupAvailablePorts(ports);
		for(int i = lastExecution; i < numOfExecutions; ++i) {
			CSVService.getInstance().setStartingPoint();
			try {
				switch(algorithm) {
				case "HASCO":
					runHASCO(THREADS,timeLimit,queryProfile);
					break;
				case "BOHB":
					runBOHB(THREADS,timeLimit,queryProfile);
					break;
				case "SMAC":
					SMAC.run(THREADS,timeLimit,queryProfile);
					break;
				case "Random":
					runRandom(THREADS,timeLimit,queryProfile);
					break;
				default:
					throw new RuntimeException("Invalid algorithm in args");
				}	
			} catch(Exception e) {
				if (e.getMessage().equals("Invalid algorithm in args")) {
					throw e;
				}
				System.err.println(String.format("Error: %s", e.getMessage()));
			}
			CSVService.getInstance().dumpWithVars3(String.format("%s_EXEC%d", algorithm, i));
		}
	}
}

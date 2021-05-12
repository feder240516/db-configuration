package main;

import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.val;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.python.PythonUtil;
import benchmark.core.api.ConversionFailedException;
import benchmark.core.api.IConverter;
import benchmark.core.api.IHyperoptObjectEvaluator;
import benchmark.core.api.IOptimizer;
import benchmark.core.api.output.IOptimizationOutput;
import benchmark.core.impl.optimizer.cfg.ggp.IGeneticOptimizerConfig;
import benchmark.core.impl.optimizer.pcs.IPCSOptimizerConfig;
import extras.SMACOptimizer;
import exceptions.UnavailablePortsException;
import extras.GGP;
import extras.IPlanningOptimizationTask;
import extras.ParameterRefinementConfiguration;
import extras.PlanningOptimizationTask;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.PortManager;

public class MainOptimizersSMAC {

	public static Query generateQuerySelectSalaries() {
		return select(field("employees.emp_no"), field("employees.first_name"), field("employees.last_name"),
				field("salaries.salary")).from("employees").join("salaries")
						.on(field("employees.emp_no").eq(field("salaries.emp_no")))
						.where(extract(field("salaries.to_date"), DatePart.YEAR).eq(val(9999)));
	}
	
	public static void main(String[] args) throws IOException {
		int THREADS = 2;
		int NUM_TESTS = 3;
		int TIME_IN_MINUTES = 30;
		int[] ports = new int[] { 9901, 9902, 9903, 9904, 9905, 9906, 9907, 9908, 9909 };
		PortManager.getInstance().setupAvailablePorts(ports);

		Query selectSalaries = generateQuerySelectSalaries();

		TestDescription td1 = new TestDescription("Only select salaries", NUM_TESTS);
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
		components.add(compMaria);
		String requiredInterface = "IDatabase";

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
				return 1;
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
		
		IOptimizer<IPlanningOptimizationTask<IComponentInstance>, IComponentInstance> opt = new SMACOptimizer<IComponentInstance>("Experiment", pcsConfig, task);
		try {
			IOptimizationOutput<IComponentInstance> result = opt.call();
			
			/*for (Map.Entry<String, String> entry : result.getSolutionDescription().getParameterValues().entrySet()) {
			    String key = entry.getKey();
			    String value = entry.getValue();
			    System.out.println(key + " = " + value);
			}*/
			
		} catch (AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException
				| AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

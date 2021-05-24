package managers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import static org.jooq.impl.DSL.*;
import org.jooq.*;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import exceptions.UnavailablePortsException;
import helpers.TestDescription;
import repositories.QueryRepository;

class Data {
	IComponentInstance componentInstance;
	TestDescription test;
	int maxThreads;
	
	public Data(IComponentInstance componentInstance, TestDescription test, int maxThreads) {
		this.test = test;
		this.componentInstance = componentInstance;
		this.maxThreads = maxThreads;
	}

	@Override
	public String toString() {
		return "Data [Database=" + componentInstance.getComponent().getName() + ", test=" + test.getID() + ", maxThreads=" + maxThreads + "]";
	}
}

class DataProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		String[] instances = {"PostgreSQL", "HSQLDB", "MariaDB", "ApacheDerby"};
		List<Query> queries = getQueries();
		
		List<Arguments> args = new ArrayList<Arguments>();
		for(int i = 0; i < instances.length; i++) {
			for(int j = 0; j < queries.size(); j++) {
				args.add(Arguments.of((Data) new Data(createCI(instances[i]), createTD(queries.get(j), String.format("%s - query %d", instances[i], j)), 1)));
			}
		}
		System.out.println("\n\n\n\nPrinting...\n\n\n\n");
		System.out.println(String.format("Number of instances: %d", instances.length));
		System.out.println(String.format("Number of queries: %d", queries.size()));
		//Arrays.asList(instances).stream().map(()->);
		args.stream().forEach((arg) -> {
			System.out.println(String.format("%s arg", arg.toString()));
		});
		return args.stream();
		/*Arguments.of((Data) new Data(createCI("MariaDB"), createTD(), 1)), 
		 Arguments.of((Data) new Data(createCI("HSQLDB"), createTD(), 1))*/
	}
	
	public ComponentInstance createCI(String dbms) {
		IComponent comp = new Component(dbms);
		Map<String, String> parameterValues = new HashMap<>();
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		
		int[] ports = new int[] {9901,9902,9903,9904,9905,9906,9907,9908,9909,9910,9911,9912,9913,9914,9915,9916,9917,9918};
		PortManager.getInstance().setupAvailablePorts(ports);
		
		return new ComponentInstance(comp, parameterValues, reqInterfaces);
	}
	
	public TestDescription createTD(Query query, String testID) {
		TestDescription td = new TestDescription(testID);
	    td.addQuery(1, query);
	    return td;
	}
	
	public List<Query> getQueries() {
		/*Query q1 = select(field("employees.emp_no"), 
							field("employees.first_name"), 
							field("employees.last_name"), 
							field("salaries.salary"))
					.from("employees")
					.join("salaries")
					.on(field("employees.emp_no").eq(field("salaries.emp_no")))
					.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)));*/
		//Query q1 = QueryRepository.getTestQuery1();
		//Query q2 = QueryRepository.getInsertQuery();
		
		List<Query> queries = Arrays.asList(new Query[] {QueryRepository.getUpdateQuery()});
		return queries;
	}
}


class BenchmarkerTest {
	
	@ParameterizedTest
	@ArgumentsSource(DataProvider.class)
	void testBenchmark(Data data) throws InterruptedException, ExecutionException, UnavailablePortsException, IOException, SQLException{
		Benchmarker b = new Benchmarker(data.test, data.maxThreads);
	     double	score = b.benchmark(data.componentInstance);
	    System.out.println("Score obtained: " + score);
	    assertFalse(score == Double.MAX_VALUE, "Query was not executed");
	}
}

package handlers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jooq.Query;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.*;
import org.mockito.stubbing.Answer;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import exceptions.UnavailablePortsException;
import managers.DBSystemFactory;
import managers.PortManager;
import repositories.QueryRepository;

public class PostgreSQLHandleTest {
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		PortManager.getInstance().setupAvailablePorts(new int[] {9901,9902,9903});
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void test() throws ClassNotFoundException, UnavailablePortsException, IOException, SQLException, InterruptedException {
		IComponent postgres = new Component("PostgreSQL");
		IComponentInstance postgresinst = new ComponentInstance(postgres, new HashMap<>(), new HashMap<>());
		ADatabaseHandle postgresHandle = DBSystemFactory.getInstance().createHandle(postgresinst);
		postgresHandle.printResultsAfterExecution(true);
		postgresHandle.initiateServer();
		double executionTime = postgresHandle.benchmarkQuery("select count(*) from employees");
		System.out.println(String.format("query was executed in %f miliseconds", executionTime));
		postgresHandle.stopServer();
		postgresHandle.cleanup(); 
	}
	
	@Test
	void test2() throws ClassNotFoundException, UnavailablePortsException, IOException, SQLException, InterruptedException {
		IComponent maria = new Component("PostgreSQL");
		IComponentInstance mariainst = new ComponentInstance(maria, new HashMap<>(), new HashMap<>());
		ADatabaseHandle mariaHandle = DBSystemFactory.getInstance().createHandle(mariainst);
		System.out.println("Finally connected");
		mariaHandle.initiateServer();
		mariaHandle.printResultsAfterExecution(false);
		Query query = QueryRepository.getTestQuery1();
		query.configuration().set(SQLDialect.MARIADB);
		double executionTime = mariaHandle.benchmarkQuery(query.getSQL(true));
		System.out.println(String.format("query was executed in %f miliseconds", executionTime));
		mariaHandle.stopServer();
		mariaHandle.cleanup(); 
	}
	
	@Test
	void testConcurrent() throws ClassNotFoundException, UnavailablePortsException, IOException, SQLException, InterruptedException {
		IComponent maria = new Component("PostgreSQL");
		List<IComponentInstance> componentInstances = new ArrayList<>();
		
		for(int i = 0; i < 4; ++i) {
			IComponentInstance mariainst = new ComponentInstance(maria, new HashMap<>(), new HashMap<>());
			componentInstances.add(mariainst);
		}
		
		
		int threads = 4;
		
		
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(threads);
		List<Callable<Double>> taskList = new ArrayList<>();
		for(IComponentInstance instance: componentInstances) {
			taskList.add(() -> {
				System.out.println(String.format("\r\n\r\nDESCRIPTION: %s: %s [%s]", instance.getComponent().getName(), instance.getParameterValue("__evalVar"), instance.getParameterValue("__evalVarValue")));
				ADatabaseHandle mariaHandle = DBSystemFactory.getInstance().createHandle(instance);
				System.out.println("Finally connected");
				mariaHandle.initiateServer();
				mariaHandle.printResultsAfterExecution(false);
				Query query = QueryRepository.getTestQuery1();
				query.configuration().set(SQLDialect.MARIADB);
				double executionTime = mariaHandle.benchmarkQuery(query.getSQL(true));
				System.out.println(String.format("query was executed in %f miliseconds", executionTime));
				mariaHandle.stopServer();
				mariaHandle.cleanup(); 
				return executionTime;
			});
		}
		List<Future<Double>> resultList = executor.invokeAll(taskList);
		executor.shutdown();
		executor.awaitTermination(999, TimeUnit.DAYS);
		for(Future<Double> result: resultList) {
			try {
				System.out.println(result.get());
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}

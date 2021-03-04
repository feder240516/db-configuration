package handlers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

import com.healthmarketscience.sqlbuilder.Query;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.DBSystemFactory;

@ExtendWith(MockitoExtension.class)
class ADatabaseHandleTest {
	
	@Mock
	DBSystemFactory mockDBSystemFactory;
	@Mock
	ADatabaseHandle mockDBHandle;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		
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
	
	TestDescription generateTestDescription(int numQueries) {
		TestDescription td = new TestDescription();
		for(int i = 0; i < numQueries; ++i) {
			td.addIndividualQuery(new SelectQuery());
		}
		return td;
	}
	
	void mockCreateHandleSuccess(AtomicInteger startedHandles, int maxThreads) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		Mockito.doAnswer((inv) -> {
			int newVal = startedHandles.incrementAndGet();
			System.out.println(String.format("%d semaphore seats used", newVal));
			assertTrue(newVal <= maxThreads);
			return mockDBHandle;
		}).when(mockDBSystemFactory).createHandle(ArgumentMatchers.<IComponentInstance>any(), ArgumentMatchers.<TestDescription>any());
	}
	
	void mockCleanupSuccess(AtomicInteger startedHandles, int maxThreads) {
		Mockito.doAnswer((inv)->{
			int newVal = startedHandles.decrementAndGet();
			return null;
		}).when(mockDBHandle).cleanup();
	}
	
	void mockBenchmarkQuerySuccess(AtomicInteger startedHandles, int maxThreads) throws InterruptedException, SQLException {
		Mockito.doAnswer((inv)->{
			TimeUnit.MILLISECONDS.sleep(Math.round(Math.random() * 100));
			return 42.0;
		}).when(mockDBHandle).benchmarkQuery(ArgumentMatchers.<Query>any());
	}
	
	void mockCreateHandleFailure(AtomicInteger startedHandles, int maxThreads, int errorPosition) throws UnavailablePortsException, IOException, SQLException, InterruptedException {
		Mockito.doAnswer((inv) -> {
			int newVal = startedHandles.incrementAndGet();
			assertTrue(errorPosition > newVal);
			return mockDBHandle;
		}).when(mockDBSystemFactory).createHandle(ArgumentMatchers.<IComponentInstance>any(), ArgumentMatchers.<TestDescription>any());
	}
	
	void mockBenchmarkQueryFailure(AtomicInteger startedHandles, int maxThreads, int errorPosition) throws InterruptedException, SQLException {
		Mockito.doAnswer((inv)->{
			TimeUnit.MILLISECONDS.sleep(Math.round(Math.random() * 100));
			int newVal = startedHandles.get();
			assertTrue(errorPosition > newVal);
			return 42.0;
		}).when(mockDBHandle).benchmarkQuery(ArgumentMatchers.<Query>any());
	}

	@ParameterizedTest
	@ValueSource(ints = {15,30,50,100})
	void testInitiateServer(int maxThreads) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ExecutionException {
		//if (maxThreads == 15)throw new UnavailablePortsException("erro");
		System.out.println("---------TEST START-----------");
		AtomicInteger startedHandles = new AtomicInteger(0);
		AtomicInteger counter = new AtomicInteger(0);
		
		mockCreateHandleSuccess(startedHandles, maxThreads);
		mockBenchmarkQuerySuccess(startedHandles, maxThreads);
		mockCleanupSuccess(startedHandles, maxThreads);
		
		Benchmarker benchmarker = new Benchmarker(generateTestDescription(20), maxThreads, mockDBSystemFactory);
		
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(maxThreads*5);
		List<Callable<Double>> taskList = new ArrayList<>();
		for(int i = 0; i < maxThreads*5; i++) {
			taskList.add(() -> {
				return benchmarker.benchmark(null);
			});
		}
		List<Future<Double>> resultList = executor.invokeAll(taskList);
		executor.shutdown();
		for(Future<Double> result: resultList) {
			System.out.println(result.get());
		}
		assertEquals(startedHandles.get(),0);
	}
	
	@ParameterizedTest
	@ValueSource(ints = {15,30,50,100})
	void testBenchmarkFailure (int maxThreads) throws UnavailablePortsException, IOException, SQLException, InterruptedException, ExecutionException {
		//if (maxThreads == 15)throw new UnavailablePortsException("erro");
		System.out.println("---------TEST START-----------");
		AtomicInteger startedHandles = new AtomicInteger(0);
		AtomicInteger counter = new AtomicInteger(0);
		
		mockCreateHandleFailure(startedHandles, maxThreads,maxThreads/2);
		mockBenchmarkQuerySuccess(startedHandles, maxThreads);
		mockCleanupSuccess(startedHandles, maxThreads);
		
		Benchmarker benchmarker = new Benchmarker(generateTestDescription(20), maxThreads, mockDBSystemFactory);
		
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(maxThreads*5);
		List<Callable<Double>> taskList = new ArrayList<>();
		for(int i = 0; i < maxThreads*5; i++) {
			taskList.add(() -> {
				return benchmarker.benchmark(null);
			});
		}
		List<Future<Double>> resultList = executor.invokeAll(taskList);
		executor.shutdown();
		try {
			for(Future<Double> result: resultList) {
				System.out.println(result.get());
			}
			fail("It should have failed");
		} catch (ExecutionException | AssertionError e) {
			e.printStackTrace();
		}
		//assertEquals(startedHandles.get(),0, "Empty handles");
	}

}

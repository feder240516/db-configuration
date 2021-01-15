package benchmarking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;

class TestExecution implements Callable<Double>{

	ADatabaseHandle dbHandler;
	int numTest;
	IComponentInstance component;
	
	public TestExecution(ADatabaseHandle dbHandler, IComponentInstance component, int numTest) {
		this.dbHandler = dbHandler;
		this.component = component;
		this.numTest = numTest;
	}
	
	@Override
	public Double call() {
		
		int port = dbHandler.initiateServer(component);
		double result = dbHandler.benchmarkQuery(numTest, port);
		dbHandler.freePort(port);
		return result;
	}
	
}

class TestDescription{
	IComponentInstance component;
	int testNumbers;
	int testAmounts;
	DescriptiveStatistics testResults;
	public TestDescription(IComponentInstance component, int testNumber, int testAmount) {
		this.component = component;
		testNumbers = testNumber;
		testAmounts = testAmount;
		testResults = new DescriptiveStatistics();
		
		/*for(int testNumber: testsByTestNumber.keySet()) {
			testNumbers.add(testNumber);
			testAmounts.add(testsByTestNumber.get(testNumber));
			testResults.add(new DescriptiveStatistics());
		}*/
		//this.testsByTestNumber = testsByTestNumber;
		//this.resultsStatistics = new HashMap<>();
		//for(int testNumber: testsByTestNumber.keySet()) {
        //	  resultsStatistics.put(testNumber, new DescriptiveStatistics());
		//}
	}
}

public class Benchmark {
	
	public static void benchmark(List<TestDescription> tests) {
		ADatabaseHandle dbHandler = new ApacheDerbyHandler(58000, 20);
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(20);
		List<TestExecution> taskList = new ArrayList<>();
		for(TestDescription td: tests) {
			for(int i = 0; i < td.testAmounts; ++i) {
				TestExecution task = new TestExecution(dbHandler,td.component,td.testNumbers);
	            taskList.add(task);
			}
		}
		
        //Execute all tasks and get reference to Future objects
        List<Future<Double>> resultList = null;
 
        try {
            resultList = executor.invokeAll(taskList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        
        System.out.println("\n========Printing the results======");
        
        int count = 0;
        for(TestDescription td: tests) {
			for(int j = 0; j < td.testAmounts; ++j) {
				Future<Double> future = resultList.get(count);
	            try {
	            	DescriptiveStatistics ds = td.testResults;
	                double result = future.get();
	                ds.addValue(result);
	                System.out.println(String.format("Adding value %f", result));
	            } catch (InterruptedException | ExecutionException e) {
	                e.printStackTrace();
	            }
	            count++;
			}
		}
        
        dbHandler.destroyHandler();
        
        /*for (int i = 0; i < resultList.size(); i++) {
            Future<Double> future = resultList.get(i);
            try {
                double result = future.get();
                ds.addValue(result);
                System.out.println(String.format("Adding value %f", result));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }*/
        
        /*System.out.println(String.format("min value: %f",ds.getMin()));
        System.out.println(String.format("max value: %f",ds.getMax()));
        System.out.println(String.format("std value: %f",ds.getStandardDeviation()));
        System.out.println(String.format("mean value: %f",ds.getMean()));*/
        
	}

	public static void main(String[] args) {
		IComponent component = new Component("Apache Derby");
		Map<String, String> parameterValues = new HashMap<>();
		parameterValues.put(ApacheDerbyHandler.DATABASE_PAGE_SIZE, "32768");
        Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
        IComponentInstance i1 = new ComponentInstance(component, parameterValues, reqInterfaces);

		IComponent component2 = new Component("Apache Derby");
		Map<String, String> parameterValues2 = new HashMap<>();
		parameterValues2.put(ApacheDerbyHandler.DATABASE_PAGE_SIZE, "65536");
        Map<String, List<IComponentInstance>> reqInterfaces2 = new HashMap<>(); 
        IComponentInstance i2 = new ComponentInstance(component2, parameterValues2, reqInterfaces2);
        
        List<TestDescription> testdescriptions = new ArrayList<TestDescription>();
        testdescriptions.add(new TestDescription(i1,1,4));
        testdescriptions.add(new TestDescription(i2,1,4));
        benchmark(testdescriptions);
		double score = testdescriptions.get(0).testResults.getMean();
		double score2 = testdescriptions.get(1).testResults.getMean();
		System.out.println(String.format("score for apache derby has been %f", score));
		System.out.println(String.format("score for apache derby 2 has been %f", score2));
	}

}

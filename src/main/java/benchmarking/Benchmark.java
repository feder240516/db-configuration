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
		dbHandler.stopServer(port);
		return result;
	}
	
}

class TestDescription{
	IComponentInstance component;
	Map<Integer , Integer> testsByTestNumber;
}

public class Benchmark {
	
	public static double benchmark(IComponentInstance component, int numTests) {
		/*int testNumber = 1;
		ADatabaseHandle apacheDerbyHandler = new ApacheDerbyHandler();
		ExecutorService executor = Executors.newFixedThreadPool(20);
		ArrayList<Double> results = new ArrayList<Double>();
		List<Callable> functions = new ArrayList<Callable>();
		for(int i = 0; i < numTests; ++i) {
			double val = 0;
			functions.add(new TestExecution(apacheDerbyHandler,component,testNumber));
			/*executor.execute(()->{
				val = apacheDerbyHandler.benchmarkQuery(1, 3306);
				System.out.println(String.format("Test %d in port %d was finished in %d seconds", 1, 3306, val));
				
			});*/
		/*}
		
		executor.invokeAll(functions);
		
		executor.awaitTermination(9999, TimeUnit.DAYS);
		
		executor.invokeAll(new ArrayList<Callable>());
		
		/*List<Callable<Double>> calls = new ArrayList<>();
		List<CompletableFuture<Double>> cfs = new ArrayList<>();
		for(int i = 0; i < numTests; ++i) {
			CompletableFuture.supplyAsync(new TestExecution(apacheDerbyHandler,component,5), executor);
			
		}
		
		CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]));
		
		List<Future<Double>> futures = executor.invokeAll(calls);
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
				.thenApply(ignored -> futures.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toList())
				);*/
		
		/*ThreadPoolExecutor
		
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		double score = 0;
		for(int i = 0; i < numTests; ++i) {
			int port = apacheDerbyHandler.initiateServer(component);
			score = apacheDerbyHandler.benchmarkQuery(1, port);
			ds.addValue(score);
			apacheDerbyHandler.stopServer(port);
			apacheDerbyHandler.freePort(port);
		}
		
		double [] values = ds.getValues();
		for(double d: values) {
			System.out.println(d);
		}
				
		
		ADatabaseHandle.runTests(15, 3);
		*/
		int testNumber = 1;
		ADatabaseHandle apacheDerbyHandler = new ApacheDerbyHandler(58000, 20);
		
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(20);
		 
        List<TestExecution> taskList = new ArrayList<>();
        for (int i = 0; i < numTests; i++) {
        	TestExecution task = new TestExecution(apacheDerbyHandler,component,testNumber);
            taskList.add(task);
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
        
        DescriptiveStatistics ds = new DescriptiveStatistics();
         
        for (int i = 0; i < resultList.size(); i++) {
            Future<Double> future = resultList.get(i);
            try {
                double result = future.get();
                ds.addValue(result);
                System.out.println(String.format("Adding value %f", result));
                
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println(String.format("min value: %f",ds.getMin()));
        System.out.println(String.format("max value: %f",ds.getMax()));
        System.out.println(String.format("std value: %f",ds.getStandardDeviation()));
        System.out.println(String.format("mean value: %f",ds.getMean()));
        
		return ds.getMean();
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
        
		double score = benchmark(i1,4);
		double score2 = benchmark(i2,4);
		
		System.out.println(String.format("score for apache derby has been %f", score));
		System.out.println(String.format("score for apache derby 2 has been %f", score2));
	}

}

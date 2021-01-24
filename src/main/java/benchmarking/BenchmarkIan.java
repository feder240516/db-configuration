package benchmarking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import handlers.ADatabaseHandle;
import handlers.MariaDBHandler;

public class BenchmarkIan {
	
	public static void main(String[] args) {
		BenchmarkIan benchmark = new BenchmarkIan();
		
		IComponent comp = new Component("MariaDB");
		Map<String, String> parameterValues = new HashMap<>();
		parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "45");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		//ComponentSerialization serializer = new ComponentSerialization();
		
		IComponent comp2 = new Component("MariaDB");
		Map<String, String> parameterValues2 = new HashMap<>();
		parameterValues2.put("OPTIMIZER_SEARCH_DEPTH", "25");
		Map<String, List<IComponentInstance>> reqInterfaces2 = new HashMap<>(); 
		IComponentInstance i2 = new ComponentInstance(comp, parameterValues2, reqInterfaces2);
		
		List<TestDescription> testdescriptions = new ArrayList<TestDescription>();
        testdescriptions.add(new TestDescription(i1,1,53));
        testdescriptions.add(new TestDescription(i2,1,50));
        benchmark(testdescriptions);
		double score = testdescriptions.get(0).testResults.getMean();
		double score2 = testdescriptions.get(1).testResults.getMean();
		System.out.println(String.format("score for mariadb has been %f", score));
		System.out.println(String.format("score for mariadb 2 has been %f", score2));
	}
	
	public static void benchmark(List<TestDescription> tests) {
		ADatabaseHandle dbHandler = new MariaDBHandler();
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(3);
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
            executor.shutdown();
            executor.awaitTermination(9000, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        
        
        System.out.println("\n========Printing the results======");
        
        int count = 0;
        for(TestDescription td: tests) {
        	System.out.println("----------COMPONENT----------");
			for(int j = 0; j < td.testAmounts; ++j) {
				Future<Double> future = resultList.get(count);
	            try {
	            	DescriptiveStatistics ds = td.testResults;
	                double result = future.get();
	                ds.addValue(result);
	                System.out.println(/*String.format("Adding value %f", */result + ", "/*)*/);
	            } catch (InterruptedException | ExecutionException e) {
	                e.printStackTrace();
	            }
	            count++;
			}
		}
        
        dbHandler.destroyHandler();
	}
	
	
	/*public static double benchmark(IComponentInstance component, int numTests) {
		int testNumber = 1;
		ADatabaseHandle mariaDBHandler = new MariaDBHandler();
		
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(3);
		 
        List<TestExecution> taskList = new ArrayList<>();
        for (int i = 0; i < numTests; i++) {
        	TestExecution task = new TestExecution(mariaDBHandler,component,testNumber);
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
	}*/
	
}



package benchmarking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.serialization.ComponentSerialization;

public class BenchmarkIan {
	
	public static void main(String[] args) {
		BenchmarkIan benchmark = new BenchmarkIan();
		
		IComponent comp = new Component("MariaDB");
		Map<String, String> parameterValues = new HashMap<>();
		parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "47");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		//ComponentSerialization serializer = new ComponentSerialization();
		
		IComponent comp2 = new Component("MariaDB");
		Map<String, String> parameterValues2 = new HashMap<>();
		parameterValues2.put("OPTIMIZER_SEARCH_DEPTH", "35");
		Map<String, List<IComponentInstance>> reqInterfaces2 = new HashMap<>(); 
		IComponentInstance i2 = new ComponentInstance(comp, parameterValues2, reqInterfaces2);
		
		double value1 = benchmark(i1, 3);
		double value2 = benchmark(i2, 3);
		
		System.out.println("value 1" + value1);
		System.out.println("value 2" + value2);
		
		/*int port = mariaDBHandler.initiateServer(i1);
		double value = mariaDBHandler.benchmarkQuery(1, port);
		System.out.println("Value: " + value);
		mariaDBHandler.stopServer(port);*/
	}
	
	public static double benchmark(IComponentInstance component, int numTests) {
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
	}
	
}



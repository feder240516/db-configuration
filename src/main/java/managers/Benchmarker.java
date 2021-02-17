package managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.components.api.IComponentInstance;
import handlers.ADatabaseHandle;
import helpers.TestDescription;

class TestExecutor implements Callable<Double>{

	ADatabaseHandle dbHandle;
	TestDescription test;
	IComponentInstance componentInstance;
	
	public TestExecutor(ADatabaseHandle dbHandle, TestDescription test, IComponentInstance componentInstance) {
		this.dbHandle = dbHandle;
		this.test = test;
		this.componentInstance = componentInstance;
	}
	
	@Override
	public Double call() throws Exception {
		try {
			return dbHandle.benchmarkQuery(componentInstance);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return Double.MAX_VALUE;
		}
	}
	
}

public class Benchmarker {
	public DescriptiveStatistics benchmark(IComponentInstance componentInstance, TestDescription test, int numberOfTests, int maxThreads) throws InterruptedException, ExecutionException {
		ADatabaseHandle dbHandle = DBSystemFactory.createHandle(componentInstance, test, maxThreads);
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(20);
		List<Callable<Double>> taskList = new ArrayList<>();
		for(int i = 0; i < numberOfTests; i++) {
			Callable<Double> task = new TestExecutor(dbHandle,test, componentInstance);
	        taskList.add(task);
		}
		
        List<Future<Double>> resultList = null;
        resultList = executor.invokeAll(taskList);
        executor.shutdown();
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for(Future<Double> result: resultList) {
        	double value = result.get();
        	stats.addValue(value);
        	System.out.println("Score: " + value);
        }
        
        return stats;
	}
}

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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;

public class Benchmark {
	
	public static double benchmark(IComponentInstance component, int numTests) {
		
		ADatabaseHandle apacheDerbyHandler = new ApacheDerbyHandler();
		
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		double score = 0;
		for(int i = 0; i < numTests; ++i) {
			int port = apacheDerbyHandler.initiateServer(component);
			score = apacheDerbyHandler.benchmarkQuery(1, port);
			ds.addValue(score);
			apacheDerbyHandler.stopServer(port);
			apacheDerbyHandler.freePort(port);
		}
		
		
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

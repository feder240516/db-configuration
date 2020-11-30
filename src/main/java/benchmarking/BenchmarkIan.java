package benchmarking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		
		ComponentSerialization serializer = new ComponentSerialization();
		System.out.println(serializer.serialize(i1));
		
		benchmark.launchDatabase();
	}
	
	public void launchDatabase() {
		DBHandlerIan handler = new DBHandlerIan("MariaDB");
		handler.Start();
		
		handler.Stop();
	}
	
	public double benchmark(IComponentInstance ci) {
		return 0.0;
	}
}



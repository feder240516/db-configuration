package web;

import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import helpers.TestDescription;
import web.model.IComponentInstanceGson;

public class CreateExecution implements HttpHandler {
	
	public IComponentInstance generateComponentInstance() {
		Component compMaria = new Component("MariaDB");
		compMaria.addProvidedInterface("IDatabase");
		compMaria.addParameter(new Parameter("DIV_PRECISION_INCREMENT", new NumericParameterDomain(true, 0, 30), 4));
		compMaria.addParameter(new Parameter("JOIN_CACHE_LEVEL", new NumericParameterDomain(true, 0, 8), 2));
		compMaria.addParameter(new Parameter("LOG_SLOW_RATE_LIMIT", new NumericParameterDomain(true, 1, 10000000), 1));
		compMaria.addParameter(new Parameter("LONG_QUERY_TIME", new NumericParameterDomain(true, 0, 10000000), 10));
		compMaria.addParameter(
				new Parameter("MAX_LENGTH_FOR_SORT_DATA", new NumericParameterDomain(true, 4, 8388608), 1024));
		compMaria.addParameter(
				new Parameter("MIN_EXAMINED_ROW_LIMIT", new NumericParameterDomain(true, 0, 4294967295l), 0));
		compMaria.addParameter(new Parameter("OPTIMIZER_PRUNE_LEVEL", new NumericParameterDomain(true, 0, 1), 1));
		compMaria.addParameter(new Parameter("OPTIMIZER_SEARCH_DEPTH", new NumericParameterDomain(true, 0, 62), 62));
		compMaria.addParameter(
				new Parameter("OPTIMIZER_USE_CONDITION_SELECTIVITY", new NumericParameterDomain(true, 1, 5), 4));
		String requiredInterface = "IDatabase";
		compMaria.addProvidedInterface(requiredInterface);
		
		Map<String, String> parameterValues = new HashMap<>();
		parameterValues.put("DIV_PRECISION_INCREMENT", "4");
		parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
		parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
		parameterValues.put("GLOBAL FLUSH", "OFF");
		parameterValues.put("JOIN_BUFFER_SIZE", "262144");
		parameterValues.put("JOIN_CACHE_LEVEL", "2");
		parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
		parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
		parameterValues.put("LONG_QUERY_TIME", "10");
		parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
		parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
		parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
		parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
		parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
		parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
		return i1;
	}
	
	public IComponentInstance createInstanceFromJson(String jsonStr) {
		final Gson gson = new Gson();
		IComponentInstanceGson json = gson.fromJson(jsonStr, IComponentInstanceGson.class);
		String componentName = json.getComponent().getName();
		
		Component component = new Component(componentName);
		IComponentInstance instance = new ComponentInstance(component, json.getParameterValues(), new HashMap<>());
		return instance;
	}

	@Override
	public void handle(HttpExchange he) throws IOException {
		//  // parse request
		System.out.println("conectado");
		String response = "";
		int responseCode = 200;
		he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		try(InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
			BufferedReader br = new BufferedReader(isr);
			){
				Map<String, Object> parameters = new HashMap<String, Object>();
		        
		        
		        StringBuilder bodyBuilder = new StringBuilder();
		        while(br.ready()) {
		        	bodyBuilder.append((char)br.read());
		        }
		        String body = bodyBuilder.toString();
		        System.out.println(body);
		        
		        final Gson gson = new Gson();
		        //final JsonElement properties = gson.fromJson(body, JsonElement.class);
		        //System.out.println(String.format("Props: %s", properties.toString()));
		        //final IComponentInstance i1 = generateComponentInstance();
		        //System.out.println(String.format("instance: %s", gson.fromJson(i1.toString(),JsonElement.class)));
		        //String i1Json = gson.toJson(i1);
		        //System.out.println(String.format("instance: %s", gson.toJson(i1Json)));
		        IComponentInstance i2 = createInstanceFromJson(body);
		        System.out.println(i2.toString());
		        
		        // send response
		        double result = BenchMarkerForWeb.getInstanceBenchmarker().benchmark(i2);
		        response = String.format("{\"error\": false, \"result\": %f}",result);
		} catch (Exception e) {
			e.printStackTrace();
			response = "{\"error\": true}";
			responseCode = 500;
		} finally {
			byte[] responseBytes = response.getBytes();
			he.sendResponseHeaders(responseCode, responseBytes.length);
			OutputStream os = he.getResponseBody();
			os.write(responseBytes);
			os.close();
		}
        
		
	}
	
}

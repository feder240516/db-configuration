package benchmarking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
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
		parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "47");
		Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
		
		IComponentInstance i1 = new ComponentInstance(comp, parameterValues, reqInterfaces);
		
		ComponentSerialization serializer = new ComponentSerialization();
		System.out.println(serializer.serialize(i1));
		
		/*
		DBHandlerIan dbHandler = benchmark.launchDatabase();
		benchmark.benchmark(i1);
		dbHandler.Stop();
		*/
		
		MariaDBHandler mariaDBHandler = new MariaDBHandler();
		mariaDBHandler.initiateServer(i1);
		System.out.println(mariaDBHandler.benchmarkQuery(0));
		mariaDBHandler.stopServer();
	}
	
	public DBHandlerIan launchDatabase() {
		DBHandlerIan handler = new DBHandlerIan("MariaDB");
		handler.Start();
		return handler;
	}
	
	public Connection getConnection() {
		Conexion cx = new Conexion();
		return cx.conectar();
	}
	
	public void ApplyParameter(int parameterValue) {
		String sqlBase = "SET OPTIMIZER_SEARCH_DEPTH = ?";
		
		Connection c = getConnection();

		try {
			PreparedStatement ps = c.prepareStatement(sqlBase);
			ps.setInt(1, parameterValue);
			
			ps.executeQuery();
			
			ps.close();
			c.close();
			
			System.out.println("Applied parameters successfully");
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("NOT Applied parameters");
		}
		
	}
	
	public double ExecuteQuery() {
		String sqlBase = "SELECT employees.first_name, employees.hire_date, "
							+ "dept_emp.from_date, dept_emp.to_date "
							+ "FROM employees INNER JOIN dept_emp "
							+ "ON employees.emp_no =dept_emp.emp_no;";
		
		Connection c = getConnection();
		Date startDate = new Date();
		Date endDate = new Date();
		
		System.out.println("Ejecutando query...");
		try {
			PreparedStatement ps = c.prepareStatement(sqlBase);
			
			startDate = new Date();
			ps.executeQuery();
			endDate = new Date();
			
			ps.close();
			c.close();
			System.out.println("Se ejecuto exitosamente");
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("No se ejecuto");
		}
		
		return Long.valueOf(endDate.getTime() - startDate.getTime()).doubleValue()/1000;
	}
	
	public double benchmark(IComponentInstance ci) {
		String OPTIMIZER_SEARCH_DEPTH = ci.getParameterValue("OPTIMIZER_SEARCH_DEPTH");
		ApplyParameter(Integer.parseInt(OPTIMIZER_SEARCH_DEPTH));
		
		double elapsedTime = ExecuteQuery();
		
		System.out.println("Tiempo: " + elapsedTime + " segundos");
		
		return elapsedTime;
	}
	
}



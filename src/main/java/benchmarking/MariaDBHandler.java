package benchmarking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ai.libs.jaicore.components.api.IComponentInstance;

public class MariaDBHandler implements IDatabase {

	Process process;
	public boolean ExecuteCommand(String cmdLine) {
		ProcessBuilder pb = new ProcessBuilder();
		//pb.command("cmd.exe", "/c", cmdLine);
		pb.command("mysqld.exe");
		
		boolean success;
		try {
			process = pb.start();
			System.out.println("Waiting");
			TimeUnit.SECONDS.sleep(1);
			
			//process.waitFor();
			//process.destroy();
			success = true;
		}catch (Exception e){
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	
	public boolean ApplyParameters(IComponentInstance component) {
		Map<String, String> parameters = component.getParameterValues();
		String sqlBase = "";
		
		boolean success;
		for(String keyParam : parameters.keySet()) {
			String valueParam = parameters.get(keyParam);
			
			sqlBase += "SET " + keyParam + " = " + valueParam + "; ";
		}
		
		Connection conn = getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(sqlBase);
			ps.executeQuery();
			ps.close();
			conn.close();
			
			success = true;
		}catch(SQLException e) {
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}

	@Override
	public void initiateServer(IComponentInstance component) {
		String cmdStart = "mysqld";//"net start mariadb";
		boolean isSuccessful = ExecuteCommand(cmdStart);
		
		String msg = (isSuccessful) ? "Se inicio MariaDB": "No se inicio MariaDB";
		System.out.println(msg);

		isSuccessful = ApplyParameters(component);
		msg = (isSuccessful) ? "Se aplicaron los parametros": "No se aplicaron los parametros";
		System.out.println(msg);
	}

	@Override
	public void stopServer() {
		process.destroy();
		System.out.println("Se detuvo MariaDB");
		
		/*
		String cmdStop = "mysqladmin -u root --password=12345 shutdown";//"net stop mariadb";
		boolean isSuccessful = ExecuteCommand(cmdStop);
	
		String msg = (isSuccessful) ? "Se detuvo MariaDB": "No se detuvo MariaDB";
		System.out.println(msg);*/
	}

	@Override
	public double benchmarkQuery(int numTest) {
		String sqlBase = "SELECT employees.first_name, employees.hire_date, "
				+ "dept_emp.from_date, dept_emp.to_date "
				+ "FROM employees INNER JOIN dept_emp "
				+ "ON employees.emp_no =dept_emp.emp_no;";
		
		Connection conn = getConnection();
		Date startDate = new Date();
		Date endDate = new Date();
		
		try {
			PreparedStatement ps = conn.prepareStatement(sqlBase);
			
			startDate = new Date();
			ps.executeQuery();
			endDate = new Date();
			
			ps.close();
			conn.close();
			
			System.out.println("Benchmark query was executed");
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("Benchmark query was NOT executed");
		}
		
		return Long.valueOf(endDate.getTime() - startDate.getTime()).doubleValue()/1000;
	}

	@Override
	public Connection getConnection() {
		String db = "employees";
		String url = "jdbc:mariadb://localhost:3306/" + db;
		String user = "root";
		String password = "12345";
		
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Connection to MariaDB successful");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Connection to MariaDB unsuccessful");
		}
		return conn;
	}

}

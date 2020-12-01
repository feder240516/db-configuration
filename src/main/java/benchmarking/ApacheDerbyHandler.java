package benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;




import ai.libs.jaicore.components.api.IComponentInstance;

public class ApacheDerbyHandler implements IDatabase {
	
	public static final String DATABASE_PAGE_SIZE = "DATABASE_PAGE_SIZE";
	
	Process process;
	Connection conn;
	String dbName;
	
	public ApacheDerbyHandler() {
		StringBuilder sb = new StringBuilder();
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int numChars = chars.length();
		
		for (int i = 0; i < 20; ++i) {
			int selected = (int) Math.floor(Math.random() * numChars);
			sb.append(chars.charAt(selected));
		}
		
		dbName = sb.toString();
		System.out.println(dbName);
		/*try {
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	private void createAndFillDatabase() {
		try {
			int NUM_RESTAURANTS = 1000;
			int NUM_WORKERS = 10000;
			PreparedStatement ps;
			conn.prepareStatement("create table restaurants(id int primary key, name varchar(255))").execute();
			conn.prepareStatement("create table workers(id int primary key, name varchar(255), restId int REFERENCES restaurants(id))").execute();
			ps = conn.prepareStatement("insert into restaurants values(?, ?)");
			for (int i = 0; i < NUM_RESTAURANTS; ++i) {
				
				ps.setInt(1, i+1);
				ps.setString(2, "a");
				ps.execute();
			}
			ps = conn.prepareStatement("insert into workers values(?, ?, ?)");
			for (int i = 0; i < NUM_WORKERS; ++i) {
				
				ps.setInt(1, i+1);
				ps.setString(2, "b");
				ps.setInt(3, ((i+1) % NUM_RESTAURANTS)+1);
				ps.execute();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setDatabasePageSize(IComponentInstance component) {
		String dbPageSizeStr = component.getParameterValue(DATABASE_PAGE_SIZE);
		if (dbPageSizeStr == null) return;
		//if (size <= 0) return;
		
		try(CallableStatement cs = 
				  conn.prepareCall("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)");){

			cs.setString(1, "derby.storage.pageSize"); 
			cs.setString(2, dbPageSizeStr); 
			cs.execute(); 
			cs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	
	@Override
	public void initiateServer(IComponentInstance component) {
		System.out.println("Starting server");
		
		int MAX_CONNECTION_RETRIES = 5;
		String derbyHome = System.getenv("DERBY_HOME");
		if (derbyHome == null || derbyHome.equals("")) throw new RuntimeException("Environment Var DERBY_HOME must be configured to test apache derby");
		//String[] comandoArray = {"java", "-jar", derbyHome + "\\lib\\derbyrun.jar", "server", "start"};
		String[] comandoArray = {derbyHome + "/bin/startNetworkServer.bat"};
		List<String> comandoList = Arrays.asList(comandoArray);
		ProcessBuilder processBuilder = new ProcessBuilder(comandoList);
		processBuilder.directory(new File(derbyHome + "/bin"));
		try {
			//processBuilder.redirectInput();
			//processBuilder.redirectError();			
			process = processBuilder.start();
			InputStream inStream = process.getInputStream();
			InputStream errStream = process.getErrorStream();
			BufferedReader brin = new BufferedReader(new InputStreamReader(inStream));
			BufferedReader brerr = new BufferedReader(new InputStreamReader(errStream));

			// read two lines to ensure server inited
			brin.readLine();
			brin.readLine();
			
			// closing streams to avoid server stuck due to full streams
			brin.close();
			inStream.close();
			errStream.close();
			
			// tries multiple connections to database
			for(int i = 0; i < MAX_CONNECTION_RETRIES && conn == null; ++i) {
				getConnection();
			}
			if (conn != null && !conn.isClosed()) {
				createAndFillDatabase();
				setDatabasePageSize(component);
				
			} else {
				throw new SQLException(String.format("Could not connect to apache derby after %d tries; "
						+ "ensure you have set the %DERBY_HOME% environment variable to your apache "
						+ " derby home and try again.",MAX_CONNECTION_RETRIES));
			}
			
			//process.waitFor();
			//System.out.println(String.format("Exit value is %d", process.exitValue()));
			//System.out.println(String.format("Exit value is %d", process.()));
			
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopServer() {
		System.out.println("Stopping server");
		try {
			if (conn != null && !conn.isClosed()) conn.close();
			String derbyHome = System.getenv("DERBY_HOME");
			String[] comandoArray = {derbyHome + "/bin/stopNetworkServer.bat"};
			ProcessBuilder processBuilder = new ProcessBuilder(comandoArray);
			processBuilder.start().waitFor();
			
		} catch (IOException | SQLException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//process.destroy();
		
	}

	@Override
	public double benchmarkQuery(int test) {
		System.out.println("benchmarking");
		double score = Double.MAX_VALUE;
		try {
			
			conn = getConnection();
			if (conn != null) {
				switch(test) {
				case 1:
					PreparedStatement ps = conn.prepareStatement("select * from workers, restaurants where restaurants.id = workers.restId");
					//ps = conn.prepareStatement("select * from worker");
					Date before = new Date();
					ps.execute();
					Date after = new Date();
					score = after.getTime() - before.getTime();
					break;
				default:
					throw new AssertionError("Invalid test number");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return score;
	}

	@Override
	public Connection getConnection() {
		try {
			if (conn == null || conn.isClosed()) {
				String dbUrl = String.format("jdbc:derby://localhost:1527/%s;create=true", dbName);
				conn = DriverManager.getConnection(dbUrl);
				System.out.println(String.format("Conectado a db %s", dbName));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			conn = null;
		}
		return conn;
		//
		
	}

}

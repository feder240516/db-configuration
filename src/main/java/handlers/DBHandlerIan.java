package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class DBHandlerIan {
	String db;
		
	public DBHandlerIan(String db) {
		this.db = db;
	}
		
	public void Start() {
		ProcessBuilder pb = new ProcessBuilder();
		String cmdStart = "net start mariadb";
		
		pb.command("cmd.exe", "/c", cmdStart);
		
		try {
			Process process = pb.start();
				
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			int exitCode = process.waitFor();
			System.out.println("\nExited with error code : " + exitCode);
		}catch (Exception e){
			System.out.println("whaterror");
			e.printStackTrace();
		}
	}
		
	public void Stop() {
		ProcessBuilder pb = new ProcessBuilder();
		String cmdStop = "net stop mariadb";
		
		pb.command("cmd.exe", "/c", cmdStop);
		
		try {
			Process process = pb.start();
				
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			int exitCode = process.waitFor();
			System.out.println("\nExited with error code : " + exitCode);
		}catch (Exception e){
			System.out.println("whaterror");
			e.printStackTrace();
		}
	}
}


package main;

import java.util.Scanner;

public class MainProcessBuilderTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		while (true) {
			try {
				ProcessBuilder pb = new ProcessBuilder(); 
				Scanner sc = new Scanner(System.in);
				String input = sc.nextLine();
				pb.command(input);
				Process p = pb.start();
				p.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

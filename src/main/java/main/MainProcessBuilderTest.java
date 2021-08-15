package main;

import java.util.Scanner;

public class MainProcessBuilderTest {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		while (true) {
			try {
				System.out.println("Directory: ");
				String input = sc.nextLine();
				ProcessBuilder pb = new ProcessBuilder(input); 
				System.out.println("Number of commands: ");
				int numCommands = Integer.parseInt(sc.nextLine());
				String[] commands = new String[numCommands];
				for(int i = 0; i < numCommands; ++i) {
					System.out.println(String.format("Command #%d", i + 1));
					commands[i] = sc.nextLine();
				}
				pb.command(commands);
				Process p = pb.start();
				p.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

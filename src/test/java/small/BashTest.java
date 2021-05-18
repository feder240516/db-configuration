package small;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

public class BashTest {
	@Test
	public void main() throws IOException {
		Scanner sc = new Scanner(System.in);
		String command = sc.nextLine();
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectOutput(new File("/home/ailibs/output.txt"));
		pb.start();
	}
}

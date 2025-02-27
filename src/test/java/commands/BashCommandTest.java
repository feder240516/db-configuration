package commands;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BashCommandTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() throws IOException, InterruptedException {
		String[][] commands = new String[][] {
			new String[] {
				"/bin/bash", "-c", "echo \"hello world\""	
			},
			new String[] {
				"/bin/mysqld"
			},
		};
		for (String[] command: commands) {
			ProcessBuilder pb = new ProcessBuilder(command);
			Process process = pb.start();
			process.getInputStream().transferTo(System.out);
			process.waitFor();
		}
	}

}

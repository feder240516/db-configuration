package handlers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.*;
import org.mockito.stubbing.Answer;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import exceptions.UnavailablePortsException;
import managers.DBSystemFactory;
import managers.PortManager;

public class PostgreSQLHandleTest {
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		PortManager.getInstance().setupAvailablePorts(new int[] {9901,9902,9903});
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
	void test() throws ClassNotFoundException, UnavailablePortsException, IOException, SQLException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("sudo", "sh", "-c", "echo sus > ./a.txt");
		pb.directory(new File("home/ailibs"));
		Process p = pb.start();
		p.waitFor();
		/*IComponent postgres = new Component("PostgreSQL");
		IComponentInstance postgresinst = new ComponentInstance(postgres, new HashMap<>(), new HashMap<>());
		ADatabaseHandle postgresHandle = DBSystemFactory.getInstance().createHandle(postgresinst);
		postgresHandle.initiateServer();
		System.out.println(String.format("score: %f", postgresHandle.benchmarkQuery("select count(*) from employees")));
		postgresHandle.stopServer();
		postgresHandle.cleanup();*/ 
	}
}

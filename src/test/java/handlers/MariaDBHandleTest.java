package handlers;

import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.val;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.jooq.DatePart;
import org.jooq.Query;
import org.jooq.SQLDialect;
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
import repositories.QueryRepository;

public class MariaDBHandleTest {
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
		IComponent maria = new Component("MariaDB");
		IComponentInstance mariainst = new ComponentInstance(maria, new HashMap<>(), new HashMap<>());
		ADatabaseHandle mariaHandle = DBSystemFactory.getInstance().createHandle(mariainst);
		System.out.println("Finally connected");
		mariaHandle.initiateServer();
		mariaHandle.printResultsAfterExecution(true);
		double executionTime = mariaHandle.benchmarkQuery("select count(*) from employees");
		System.out.println(String.format("query was executed in %f miliseconds", executionTime));
		mariaHandle.stopServer();
		mariaHandle.cleanup(); 
	}
	
	@Test
	void test2() throws ClassNotFoundException, UnavailablePortsException, IOException, SQLException, InterruptedException {
		IComponent maria = new Component("MariaDB");
		IComponentInstance mariainst = new ComponentInstance(maria, new HashMap<>(), new HashMap<>());
		ADatabaseHandle mariaHandle = DBSystemFactory.getInstance().createHandle(mariainst);
		System.out.println("Finally connected");
		mariaHandle.initiateServer();
		mariaHandle.printResultsAfterExecution(false);
		Query query = QueryRepository.getTestQuery1();
		query.configuration().set(SQLDialect.MARIADB);
		double executionTime = mariaHandle.benchmarkQuery(query.getSQL(true));
		System.out.println(String.format("query was executed in %f miliseconds", executionTime));
		mariaHandle.stopServer();
		mariaHandle.cleanup(); 
	}
}

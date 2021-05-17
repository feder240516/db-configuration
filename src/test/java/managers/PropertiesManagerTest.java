package managers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertiesManagerTest {

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
	void test() {
		Set<Entry<Object, Object>> properties = PropertiesManager.getInstance().getAllProperties();
		for(Entry<Object,Object> prop: properties) {
			System.out.println(String.format("%s: %s", prop.getKey(), prop.getValue()));
		}
	}

}

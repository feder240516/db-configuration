package queries;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.jooq.Query;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import repositories.QueryRepository;

class ShowAllQueries {
	
	protected final Logger logger = LoggerFactory.getLogger(ShowAllQueries.class);

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
		SQLDialect[] sqlDialects = new SQLDialect[] {
				SQLDialect.MARIADB,
				SQLDialect.HSQLDB,
				SQLDialect.POSTGRES,
				SQLDialect.DERBY
		};
		List<Query> queries = QueryRepository.getAllQueries();
		for(Query q: queries) {
			for(SQLDialect dialect: sqlDialects) {
				q.configuration().set(dialect);
				logger.info(String.format("Query in %s: %s", dialect.getName(), q.getSQL(true)));
			}
		}
	}

}

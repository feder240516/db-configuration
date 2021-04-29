package utilities;

import org.jooq.SQLDialect;

import managers.DBSystems;

public class SQLDialectConversor{  
	public static SQLDialect fromString(String dbSystem) {
		switch(dbSystem) {
		case DBSystems.APACHE_DERBY:
			return SQLDialect.DERBY;
		case DBSystems.HSQL_DB:
			return SQLDialect.HSQLDB;
		case DBSystems.MARIA_DB:
			return SQLDialect.MARIADB;
		case DBSystems.POSTGRESQL:
			return SQLDialect.POSTGRES;
		default:
			throw new IllegalArgumentException("db system is not supported");
		}
	}
}

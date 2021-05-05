package main;

import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.ExtractExpression;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertSelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery.JoinType;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.IMultiFidelityObjectEvaluator;
import ai.libs.jaicore.ml.hpo.multifidelity.MultiFidelitySoftwareConfigurationProblem;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.IHyperbandConfig;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband.HyperbandSolutionCandidate;
import exceptions.UnavailablePortsException;
import helpers.TestDescription;
import managers.Benchmarker;
import managers.PortManager;
import services.CSVService;

import static org.jooq.impl.DSL.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.jooq.*;
import org.jooq.impl.*;

public class MainJOOQ {
	
	public static List<IComponentInstance> getComponentInstanceExamples() {
		int[] ports = new int[] {9901,9902,9903,9904,9905,9906,9907,9908,9909,9910};
		PortManager.getInstance().setupAvailablePorts(ports);
		IComponent compMaria 		= new Component("MariaDB");
		IComponent compDerby 		= new Component("ApacheDerby");
		IComponent compPosgreSQL 	= new Component("PostgreSQL");
		IComponent compHSQL 		= new Component("HSQLDB");
		List<IComponentInstance> componentInstances = new ArrayList<>();
		
		// ########### APACHE DERBY ###################
		/*
		int[] derbypageReservedSpaces = new int[] {0,1,2,5,10,20,50,100};
		for(int size: derbypageReservedSpaces ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("derby.storage.pageReservedSpace", String.valueOf(size));
			parameterValues.put("derby.storage.pageSize", String.valueOf(4096));
			parameterValues.put("derby.storage.initialPages", String.valueOf(1));
			parameterValues.put("derby.language.statementCacheSize", String.valueOf(100));
			parameterValues.put("__instanceID", String.format("DERBY_%s_%d", "pageReservedSpace", size));
			parameterValues.put("__evalVar", "derby.storage.pageReservedSpace");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 20));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compDerby, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		*/

		/*int[] derbyPageSizes = new int[] {4096,8192,16384,32768};
		for(int size: derbyPageSizes ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("derby.storage.pageReservedSpace", String.valueOf(20));
			parameterValues.put("derby.storage.pageSize", String.valueOf(size));
			parameterValues.put("derby.storage.initialPages", String.valueOf(1));
			parameterValues.put("derby.language.statementCacheSize", String.valueOf(100));
			parameterValues.put("__instanceID", String.format("DERBY_%s_%d", "pageSize", size));
			parameterValues.put("__evalVar", "derby.storage.pageSize");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 4096));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compDerby, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}*/
		/*
		int[] derbyInitialPages= new int[] {1,5,10,50,100,500,1000};
		for(int size: derbyInitialPages ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("derby.storage.pageReservedSpace", String.valueOf(20));
			parameterValues.put("derby.storage.pageSize", String.valueOf(4096));
			parameterValues.put("derby.storage.initialPages", String.valueOf(size));
			parameterValues.put("derby.language.statementCacheSize", String.valueOf(100));
			parameterValues.put("__instanceID", String.format("DERBY_%s_%d", "initialPages", size));
			parameterValues.put("__evalVar", "derby.storage.initialPages");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 1));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compDerby, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] statementCacheSizes = new int[] {1,5,10,50,100,500,1000};
		for(int size: statementCacheSizes ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("derby.storage.pageReservedSpace", String.valueOf(20));
			parameterValues.put("derby.storage.pageSize", String.valueOf(4096));
			parameterValues.put("derby.storage.initialPages", String.valueOf(1));
			parameterValues.put("derby.language.statementCacheSize", String.valueOf(size));
			parameterValues.put("__instanceID", String.format("DERBY_%s_%d", "statementCacheSize", size));
			parameterValues.put("__evalVar", "derby.language.statementCacheSize");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 100));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compDerby, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBDivPrecisionIncrement = new int[] {0,4,8,12,16,20,24,28,30};
		for(int size: mariaDBDivPrecisionIncrement ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", String.valueOf(size));
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			parameterValues.put("__isDefault", String.valueOf(size == 20));
			parameterValues.put("__evalVar", "DIV_PRECISION_INCREMENT");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "divPrecisionIncrement", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		
		long[] mariaDBEQRangeIndexDiveLimit = new long[] {0,4,16,64,256,1024,4096,16384,65536,262144,1048576,4194304,8388608,33554432,134217728,536870912, 2147483648l, 4294967295l};
		for(long size: mariaDBEQRangeIndexDiveLimit ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", String.valueOf(size));
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			parameterValues.put("__isDefault", String.valueOf(size == 200));
			parameterValues.put("__evalVar", "EQ_RANGE_INDEX_DIVE_LIMIT");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "EQRangeIndexDiveLimit", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBExpensiveSubqueryLimit = new int[] {0,10,100,1000,10000,100000,1000000,10000000};
		for(int size: mariaDBExpensiveSubqueryLimit ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", String.valueOf(size));
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			parameterValues.put("__evalVar", "EXPENSIVE_SUBQUERY_LIMIT");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 100));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "expensiveSubqueryLimit", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		String[] mariaDBFlush = new String[] {"OFF","ON"};
		for(String size: mariaDBFlush ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", size);
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			parameterValues.put("__evalVar", "GLOBAL FLUSH");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == "OFF"));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "flush", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		long[] mariaDBJoinBufferSize = new long[] {128,256,1024,4096,16384,65536,262144,1048576,4194304,8388608,33554432,134217728,536870912, 2147483648l};
		for(long size: mariaDBJoinBufferSize ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", String.valueOf(size));
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "JOIN_BUFFER_SIZE");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 262144));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "joinBufferSize", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBJoinCacheLevel = new int[] {0,1,2,3,4,5,6,7,8};
		for(int size: mariaDBJoinCacheLevel ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", String.valueOf(size));
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "JOIN_CACHE_LEVEL");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 2));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "joinCacheLevel", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		String[] mariaDBLogQueriesNotUsingIndexes = new String[] {"OFF","ON"};
		for(String size: mariaDBLogQueriesNotUsingIndexes ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", size);
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "GLOBAL LOG_QUERIES_NOT_USING_INDEXES");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == "OFF"));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "logQueriesNotUsingIndexes", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBLogSlowRateLimit = new int[] {1,10,100,1000,10000,100000,1000000,10000000};
		for(int size: mariaDBLogSlowRateLimit ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", String.valueOf(size));
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "LOG_SLOW_RATE_LIMIT");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 1));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "logSlowRateLimit", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBLongQueryTime = new int[] {0,1,5,10,15,20,50,100,1000,10000,100000,1000000,10000000};
		for(int size: mariaDBLongQueryTime ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", String.valueOf(size));
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "LONG_QUERY_TIME");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 10));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "longQueryTime", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBMaxLengthForSortData = new int[] {4,16,64,256,1024,4096,16384,65536,262144,1048576,4194304,8388608};
		for(int size: mariaDBMaxLengthForSortData ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", String.valueOf(size));
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "MAX_LENGTH_FOR_SORT_DATA");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 1024));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "maxLengthForSortData", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		long[] mariaDBMaxSeeksForKey = new long[] {1, 16, 256, 4096, 65536, 1048576, 16777216, 268435456, 4294967295l};
		for(long size: mariaDBMaxSeeksForKey ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", String.valueOf(size));
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "MAX_SEEKS_FOR_KEY");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 4294967295L));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "maxSeeksForKey", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		long[] mariaDBMinExaminedRowLimit = new long[] {0, 16, 256, 4096, 65536, 1048576, 16777216, 268435456, 4294967295l};
		for(long size: mariaDBMinExaminedRowLimit ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", String.valueOf(size));
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__evalVar", "MIN_EXAMINED_ROW_LIMIT");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 0));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "minExaminedRowLimit", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBOptimizerSearchDepths = new int[] {0,1,10,20,30,40,50,60,62};
		for(int size: mariaDBOptimizerSearchDepths ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", String.valueOf(size));
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__isDefault", String.valueOf(size == 62));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "optimizerSearchDepth", size));
			parameterValues.put("__evalVar", "OPTIMIZER_SEARCH_DEPTH");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBOptimizerPruneLevel = new int[] {0,1};
		for(int size: mariaDBOptimizerPruneLevel ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", String.valueOf(size));
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", "4");
			
			parameterValues.put("__isDefault", String.valueOf(size == 1));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "optimizerPruneLevel", size));
			parameterValues.put("__evalVar", "OPTIMIZER_PRUNE_LEVEL");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] mariaDBOptimizerUseConditionSelectivity = new int[] {1,2,3,4,5};
		for(int size: mariaDBOptimizerUseConditionSelectivity ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("DIV_PRECISION_INCREMENT", "4");
			parameterValues.put("EQ_RANGE_INDEX_DIVE_LIMIT", "200");
			parameterValues.put("EXPENSIVE_SUBQUERY_LIMIT", "100");
			parameterValues.put("GLOBAL FLUSH", "OFF");
			parameterValues.put("JOIN_BUFFER_SIZE", "262144");
			parameterValues.put("JOIN_CACHE_LEVEL", "2");
			parameterValues.put("GLOBAL LOG_QUERIES_NOT_USING_INDEXES", "OFF");
			parameterValues.put("LOG_SLOW_RATE_LIMIT", "1");
			parameterValues.put("LONG_QUERY_TIME", "10");
			parameterValues.put("MAX_LENGTH_FOR_SORT_DATA", "1024");
			parameterValues.put("MAX_SEEKS_FOR_KEY", "4294967295");
			parameterValues.put("MIN_EXAMINED_ROW_LIMIT", "0");
			parameterValues.put("OPTIMIZER_PRUNE_LEVEL", "1");
			parameterValues.put("OPTIMIZER_SEARCH_DEPTH", "62");
			parameterValues.put("OPTIMIZER_USE_CONDITION_SELECTIVITY", String.valueOf(size));
			
			parameterValues.put("__isDefault", String.valueOf(size == 4));
			parameterValues.put("__instanceID", String.format("MARIADB_%s_%d", "optimizerUseConditionSelectivity", size));
			parameterValues.put("__evalVar", "OPTIMIZER_USE_CONDITION_SELECTIVITY");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compMaria, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		// ########### HSQLDB ###################
		int[] hsqdbCacheRows = new int[] {100,500,1000,5000,10000,50000,100000,500000,1000000};
		for(int size: hsqdbCacheRows ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("hsqldb.cache_rows", String.valueOf(size));
			parameterValues.put("hsqldb.nio_data_file", "TRUE");
			parameterValues.put("hsqldb.nio_max_size", "256");
			parameterValues.put("hsqldb.result_max_memory_rows", "0");
			parameterValues.put("hsqldb.applog", "0");
			parameterValues.put("__isDefault", String.valueOf(size == 50000));
			parameterValues.put("__instanceID", String.format("HSQLDB_%s_%d", "hsqldb.cache_rows", size));
			parameterValues.put("__evalVar", "hsqldb.cache_rows");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compHSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] hsqdbNioMaxSize = new int[] {64, 128, 256, 512, 1024};
		for(int size: hsqdbNioMaxSize ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("hsqldb.cache_rows", "50000");
			parameterValues.put("hsqldb.nio_data_file", "TRUE");
			parameterValues.put("hsqldb.nio_max_size", String.valueOf(size));
			parameterValues.put("hsqldb.result_max_memory_rows", "0");
			parameterValues.put("hsqldb.applog", "0");
			parameterValues.put("__isDefault", String.valueOf(size == 256));
			parameterValues.put("__instanceID", String.format("HSQLDB_%s_%d", "hsqldb.nio_max_size", size));
			parameterValues.put("__evalVar", "hsqldb.nio_max_size");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compHSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] hsqdbResultMaxMemoryRows = new int[] {0, 1001,5000,10000,50000,100000,500000,1000000,5000000,10000000};
		for(int size: hsqdbResultMaxMemoryRows ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("hsqldb.cache_rows", "50000");
			parameterValues.put("hsqldb.nio_data_file", "TRUE");
			parameterValues.put("hsqldb.nio_max_size", "256");
			parameterValues.put("hsqldb.result_max_memory_rows", String.valueOf(size));
			parameterValues.put("hsqldb.applog", "0");
			parameterValues.put("__isDefault", String.valueOf(size == 0));
			parameterValues.put("__instanceID", String.format("HSQLDB_%s_%d", "hsqldb.result_max_memory_rows", size));
			parameterValues.put("__evalVar", "hsqldb.result_max_memory_rows");
			parameterValues.put("__evalVarValue", String.valueOf(size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>(); 
			IComponentInstance i1 = new ComponentInstance(compHSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		int[] hsqdbAppLog = new int[] {0,1,2,3};
		for(int size: hsqdbAppLog ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("hsqldb.cache_rows", "50000");
			parameterValues.put("hsqldb.nio_data_file", "TRUE");
			parameterValues.put("hsqldb.nio_max_size", "256");
			parameterValues.put("hsqldb.result_max_memory_rows", "0");
			parameterValues.put("hsqldb.applog", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 0));
			parameterValues.put("__instanceID", String.format("HSQLDB_%s_%d", "hsqldb.app_log", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>();
			parameterValues.put("__evalVar", "hsqldb.app_log");
			parameterValues.put("__evalVarValue", String.valueOf(size)); 
			IComponentInstance i1 = new ComponentInstance(compHSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		// ############ POSTGRESQL #############
		int[] postgresWorkMem = new int[] {64,256,1024,4096,16384,65536,262144};
		for(int size: postgresWorkMem ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("work_mem", String.valueOf(size));
			parameterValues.put("shared_buffers", "131072");
			parameterValues.put("hash_mem_multiplier", "1");
			parameterValues.put("__isDefault", String.valueOf(size == 4096));
			parameterValues.put("__instanceID", String.format("POSTGRESQL_%s_%d", "work_mem", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>();
			parameterValues.put("__evalVar", "work_mem");
			parameterValues.put("__evalVarValue", String.valueOf(size)); 
			IComponentInstance i1 = new ComponentInstance(compPosgreSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}

		int[] postgresSharedBuffers = new int[] {16,64,256,1024,4096,16384,65536,131072,262144};
		for(int size: postgresSharedBuffers ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("work_mem", "4096");
			parameterValues.put("shared_buffers", String.valueOf(size));
			parameterValues.put("hash_mem_multiplier", "1");
			parameterValues.put("__isDefault", String.valueOf(size == 131072));
			parameterValues.put("__instanceID", String.format("POSTGRESQL_%s_%d", "shared_buffers", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>();
			parameterValues.put("__evalVar", "shared_buffers");
			parameterValues.put("__evalVarValue", String.valueOf(size)); 
			IComponentInstance i1 = new ComponentInstance(compPosgreSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		
		double[] postgresHashMemMultipliers = new double[] {1.,2.,5.,10.,20.};
		for(double size: postgresHashMemMultipliers ) {
			Map<String, String> parameterValues = new HashMap<>();
			parameterValues.put("work_mem", "4096");
			parameterValues.put("shared_buffers", "131072");
			parameterValues.put("hash_mem_multiplier", String.valueOf(size));
			parameterValues.put("__isDefault", String.valueOf(size == 1));
			parameterValues.put("__instanceID", String.format("POSTGRESQL_%s_%f", "hash_mem_multiplier", size));
			Map<String, List<IComponentInstance>> reqInterfaces = new HashMap<>();
			parameterValues.put("__evalVar", "hash_mem_multiplier");
			parameterValues.put("__evalVarValue", String.valueOf(size)); 
			IComponentInstance i1 = new ComponentInstance(compPosgreSQL, parameterValues, reqInterfaces);
			componentInstances.add(i1);
		}
		*/
		return componentInstances;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		DSLContext dslContext = DSL.using(SQLDialect.MARIADB);
		
		Query selectSalaries = dslContext.select(field("employees.emp_no"), 
										field("employees.first_name"), 
										field("employees.last_name"), 
										field("salaries.salary"))
								.from("employees")
								.join("salaries")
								.on(field("employees.emp_no").eq(field("salaries.emp_no")))
								.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)));
		selectSalaries.configuration().set(SQLDialect.MARIADB);
		System.out.println(selectSalaries.getSQL(true));
		
		Query selectAvgSalaryTitles = dslContext.select(field("titles.title"), 
											avg(field("salaries.salary").cast(Double.class)))
										.from("salaries")
										.join("titles")
										.on(field("salaries.emp_no").eq(field("titles.emp_no")))
										.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)))
										.groupBy(field("titles.title"));
		
		Query selectAvgSalaryTitlesGender = dslContext.select(
				field("titles.title"),field("employees.gender"),avg(field("salaries.salary").cast(Double.class))
				).from("salaries")
				.join("titles").on(field("salaries.emp_no").eq(field("titles.emp_no")))
				.join("employees").on(field("salaries.emp_no").eq(field("employees.emp_no")))
				.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)))
				.groupBy(field("employees.gender"), field("titles.title"));

		Query insertEmployee = dslContext.insertInto(table("employees"), field("employees.emp_no"), field("employees.birth_date"), field("first_name"), field("last_name"), field("gender"), field("hire_date"))
										.select(dslContext.select(max(field("employees.emp_no")).add(1), field("employees.birth_date"), field("first_name"), field("last_name"), field("gender"), field("hire_date")));
		
		System.out.println(selectSalaries.getSQL(true));
		System.out.println(selectAvgSalaryTitles.getSQL(true));
		System.out.println(selectAvgSalaryTitlesGender.getSQL(true));
		
		
		/*TestDescription td = new TestDescription("Only select salaries", 2);
	    td.addQuery(1, selectSalaries);
	    
	    int threads = 2;
	    Benchmarker benchmarker = new Benchmarker(td, threads);
	    
	    List<IComponentInstance> componentInstances = getComponentInstanceExamples();
		ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(threads);
		List<Callable<Double>> taskList = new ArrayList<>();
		for(IComponentInstance instance: componentInstances) {
			taskList.add(() -> {
				return benchmarker.benchmark(instance);
			});
		}
		List<Future<Double>> resultList = executor.invokeAll(taskList);
		executor.shutdown();
		for(Future<Double> result: resultList) {
			try {
				System.out.println(result.get());
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	    
	    CSVService.getInstance().dumpToDisk();*/
	}
}

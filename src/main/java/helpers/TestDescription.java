package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamType;

import utilities.SQLDialectConversor;

public class TestDescription {
	DescriptiveStatistics testResults;
	public TreeMap<Integer,List<Query>> queries;
	public List<Query> schemaBuildQueries;
	public int numberOfTests;
	public String ID;
	
	public TestDescription(String ID) {
		this(ID,1);
	}
	
	public TestDescription(String ID, int numberOfTests) {
		this(ID, numberOfTests, null);
	}
	
	public TestDescription(String ID, int numberOfTests, List<Query> schemaQueries) {
		testResults = new DescriptiveStatistics();
		queries = new TreeMap<>();
		this.ID = ID;
		this.numberOfTests = numberOfTests;
	}
	
	public void addQuery(int priority, Query query) {
		if (queries.containsKey(priority)) {
			queries.get(priority).add(query);
		} else {
			List<Query> list = new ArrayList<Query>();
			list.add(query);
			queries.put(priority, list);
		}
	}
	
	private int getMaxPriority() {
		if(queries.isEmpty()) return 0;
		else return queries.lastKey();
	}
	
	public void addIndividualQuery(Query query) {
		int newPriority = getMaxPriority() + 1;
		addQuery(newPriority, query);
	}
	
	public void addConcurrentQueries(List<Query> queries) {
		int newPriority = getMaxPriority() + 1;
		for (Query query: queries) {
			addQuery(newPriority, query);
		}
	}
	
	public synchronized Map<Integer,List<String>> generateQueries(String dbSystem){
		Map<Integer, List<String>> stringMap = new TreeMap<>();
		queries.forEach((k,v) -> {
			List<String> strings = v.stream().map(q -> {
				SQLDialect dialect = SQLDialectConversor.fromString(dbSystem);
				q.configuration().set(dialect);
				return q.getSQL(ParamType.INLINED);
			}).collect(Collectors.toList());
			stringMap.put(k, strings);
		});
		return stringMap;
	}
	
	public int getNumberOfTests() {
		return numberOfTests;
	}

	public String getID() {
		return ID;
	}

	public void print() {
		for(Entry<Integer, List<Query>> entry: queries.entrySet()) {
			System.out.println(String.format("Priority: %d", entry.getKey()));
			for(Query q: entry.getValue()) {
				System.out.println(String.format(" - %s", q.toString()));
			}
		}
	}
	
	
}

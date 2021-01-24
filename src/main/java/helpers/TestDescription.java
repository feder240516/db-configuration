package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.healthmarketscience.sqlbuilder.Query;

import ai.libs.jaicore.components.api.IComponentInstance;

public class TestDescription {
	int testAmounts;
	DescriptiveStatistics testResults;
	public TreeMap<Integer,List<Query>> queries;
	
	public TestDescription(IComponentInstance component) {
		testResults = new DescriptiveStatistics();
		queries = new TreeMap<>();
	}
	
	public void addQuery(int priority, Query query) {
		query.validate();
		if (queries.containsKey(priority)) {
			queries.get(priority).add(query);
		} else {
			List<Query> list = new ArrayList<Query>();
			list.add(query);
			queries.put(priority, list);
		}
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

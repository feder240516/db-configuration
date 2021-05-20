package managers.db.parameters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MariaDBParameterManager implements IDatabaseParameterManager{
	private Set<String> params;

	public MariaDBParameterManager() {
		super();
		params = new HashSet<>();
		
		params.add("DIV_PRECISION_INCREMENT");
		params.add("EXPENSIVE_SUBQUERY_LIMIT");
		params.add("FLUSH");
		params.add("JOIN_BUFFER_SIZE");
		params.add("JOIN_CACHE_LEVEL");
		params.add("LOG_QUERIES_NOT_USING_INDEXES");
		params.add("LOG_SLOW_RATE_LIMIT");
		params.add("LONG_QUERY_TIME");
		params.add("MAX_LENGTH_FOR_SORT_DATA");
		params.add("MAX_SEEKS_FOR_KEY");
		params.add("MIN_EXAMINED_ROW_LIMIT");
		params.add("OPTIMIZER_PRUNE_LEVEL");
		params.add("OPTIMIZER_SEARCH_DEPTH");
		params.add("OPTIMIZER_USE_CONDITION_SELECTIVITY");
	}
	
	@Override
	public String getCommand(String key, String value) {
		if(params.contains(key)) { return String.format("SET %s = %s;", key, value); }
		return null;
	}
}

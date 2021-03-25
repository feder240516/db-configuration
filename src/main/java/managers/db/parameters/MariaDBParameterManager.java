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
		params.add("OPTIMIZER_SEARCH_DEPTH");

	}
	
	@Override
	public String getCommand(String key, String value) {
		if(params.contains(key)) { return String.format("SET %s = %s;", key, value); }
		return null;
	}
}

package managers.db.parameters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HSQLDBParameterManager implements IDatabaseParameterManager{
	private Set<String> params;
	private Map<String, String> shortcuts;
	public HSQLDBParameterManager() {
		super();
		params = new HashSet<>();
		params.add("cache_rows");
		
		shortcuts = new HashMap<>();
		shortcuts.put("cache_rows", "FILES CACHE ROWS");
	}
	
	@Override
	public String getCommand(String key, String value) {
		if(params.contains(key)) { return String.format("SET %s %s;", shortcuts.get(key), value); }
		return null;
	}
}

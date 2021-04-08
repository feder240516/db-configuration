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
		params.add("hsqldb.cache_rows");
		//params.add("hsqldb.cache_file_scale");
		params.add("hsqldb.nio_data_file");
		params.add("hsqldb.nio_max_size");
		params.add("hsqldb.result_max_memory_rows");
		params.add("hsqldb.applog");
		
		shortcuts = new HashMap<>();
		shortcuts.put("hsqldb.cache_rows", "FILES CACHE ROWS");
		//shortcuts.put("hsqldb.cache_file_scale", "FILES SCALE");
		shortcuts.put("hsqldb.nio_data_file", "FILES NIO");
		shortcuts.put("hsqldb.nio_max_size", "FILES NIO SIZE");
		shortcuts.put("hsqldb.result_max_memory_rows", "DATABASE DEFAULT RESULT MEMORY ROWS");
		shortcuts.put("hsqldb.applog", "DATABASE EVENT LOG LEVEL");
	}
	
	@Override
	public String getCommand(String key, String value) {
		if(params.contains(key)) { return String.format("SET %s %s;", shortcuts.get(key), value); }
		return null;
	}
}

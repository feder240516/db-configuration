package managers.db.parameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostgreSQLParameterManager implements IDatabaseParameterManager {
	private Set<String> params;
	public PostgreSQLParameterManager() {
		super();
		params = new HashSet<>();
		params.add("max_stack_depth");
		params.add("work_mem");
		params.add("shared_buffers");
		params.add("hash_mem_multiplier");
	}
	
	@Override
	public String getCommand(String key, String value) {
		if(params.contains(key)) { return String.format("ALTER SYSTEM SET %s = %s", key, value); }
		return null;
	}
}

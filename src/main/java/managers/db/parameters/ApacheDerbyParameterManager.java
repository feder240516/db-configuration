package managers.db.parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApacheDerbyParameterManager implements IDatabaseParameterManager {
	private Set<String> params;
	private Set<String> stringParams;
	private Map<String,List<String>> enumParams;
	public ApacheDerbyParameterManager() {
		super();
		params = new HashSet<>();
		stringParams = new HashSet<>();
		enumParams = new HashMap<>();
		//params.add("derby.storage.pageCacheSize");
		stringParams.add("derby.storage.pageReservedSpace");
		enumParams.put("derby.storage.pageSize", Arrays.asList("4096","8192","16384","32768"));
		stringParams.add("derby.storage.initialPages");
		stringParams.add("derby.language.statementCacheSize");
		enumParams.put("derby.storage.rowLocking", Arrays.asList("true","false"));
		stringParams.add("derby.replication.logBufferSize");
		stringParams.add("derby.locks.escalationThreshold");
	}
	
	@Override
	public String getCommand(String key, String value) {
		if(params.contains(key)) { return String.format("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('%s', %s)", key, value); }
		if(stringParams.contains(key)) { return String.format("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('%s', '%s')", key, value); }
		if(enumParams.containsKey(key)) {
			if (enumParams.get(key).contains(value)) {
				return String.format("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('%s', '%s')", key, value);
			} else {
				throw new IllegalArgumentException("INVALID ENUM VALUE");
			}
		}
		return null;
	}

}

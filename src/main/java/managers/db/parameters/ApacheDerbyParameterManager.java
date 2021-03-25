package managers.db.parameters;

import java.util.HashSet;
import java.util.Set;

public class ApacheDerbyParameterManager implements IDatabaseParameterManager {
	private Set<String> params;
	public ApacheDerbyParameterManager() {
		super();
		params = new HashSet<>();
		params.add("derby.storage.pageCacheSize");
	}
	
	@Override
	public String getCommand(String key, String value) {
		if(params.contains(key)) { return String.format("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('%s', %s);", key, value); }
		return null;
	}

}

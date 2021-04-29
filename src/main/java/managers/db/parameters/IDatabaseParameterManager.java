package managers.db.parameters;

public interface IDatabaseParameterManager {
	public abstract String getCommand(String key, String value);
}

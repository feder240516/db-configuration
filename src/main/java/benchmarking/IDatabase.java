package benchmarking;

import java.sql.Connection;

import ai.libs.jaicore.components.api.IComponentInstance;

public interface IDatabase {
	public void initiateServer(IComponentInstance component);
	public void stopServer();
	public double benchmarkQuery(String query);
	public Connection getConnection(IComponentInstance component);
}

package benchmarking;

import java.sql.Connection;

import ai.libs.jaicore.components.api.IComponentInstance;

public interface IDatabase {
	public void initiateServer(IComponentInstance component);
	public void stopServer();
	public double benchmarkQuery(int numTest);
	public Connection getConnection(IComponentInstance component);
}

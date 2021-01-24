package handlers;

import java.sql.Connection;

import ai.libs.jaicore.components.api.IComponentInstance;

public interface IDatabase {
 	public int initiateServer(IComponentInstance component);
	public void stopServer(int port);
	public double benchmarkQuery(IComponentInstance instance) throws InterruptedException;
	public Connection getConnection(int port);
}

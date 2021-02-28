package handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import ai.libs.jaicore.components.api.IComponentInstance;

public interface IDatabase {
 	public int initiateServer(IComponentInstance component) throws IOException, SQLException, InterruptedException;
	public void stopServer(int port);
	public double benchmarkQuery(IComponentInstance instance) throws InterruptedException;
	public Connection getConnection(int port);
	public void cleanup();
}

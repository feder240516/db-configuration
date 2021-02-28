package handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;

public interface IDatabase {
 	public int initiateServer(IComponentInstance component) throws IOException, SQLException, InterruptedException, UnavailablePortsException;
	public void stopServer();
	public double benchmarkQuery(IComponentInstance instance) throws InterruptedException;
	public Connection getConnection();
	public void cleanup();
}

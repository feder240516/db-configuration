package handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.healthmarketscience.sqlbuilder.Query;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;

public interface IDatabase {
 	public void initiateServer() throws IOException, SQLException, InterruptedException, UnavailablePortsException;
	public void stopServer();
	public double benchmarkQuery(Query query) throws InterruptedException, SQLException;
	public void cleanup();
}

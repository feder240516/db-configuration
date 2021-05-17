package handlers.linux;

import java.io.IOException;
import java.sql.SQLException;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.PostgreSQLHandle;

public class PostgreSQLHandleLinux extends PostgreSQLHandle {

	public PostgreSQLHandleLinux(IComponentInstance componentInstance)
			throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(componentInstance);
	}
	
	@Override
	public void createDBInstance() throws IOException {
		String copyCommand = String.format(
				"sudo -u postgres pg_createcluster 13 %1$s &&" + 
				"rm -rf /var/lib/postgresql/13/%1$s &&" + 
				"cp -rf /var/lib/postgresql/13/data /var/lib/postgresql/13/%1$s", ID.toString());
		ProcessBuilder processBuilder = new ProcessBuilder(copyCommand);
		Process copyProcess = processBuilder.start();
		try {
			copyProcess.waitFor();
		} catch (InterruptedException e) {
			throw new IOException("Couldn't create new PostgreSQL instance");
		}
	}

}

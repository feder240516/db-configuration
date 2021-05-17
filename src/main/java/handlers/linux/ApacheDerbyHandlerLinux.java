package handlers.linux;

import java.io.IOException;
import java.sql.SQLException;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ApacheDerbyHandler;

public class ApacheDerbyHandlerLinux extends ApacheDerbyHandler {

	public ApacheDerbyHandlerLinux(IComponentInstance ci)
			throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci);
	}

}

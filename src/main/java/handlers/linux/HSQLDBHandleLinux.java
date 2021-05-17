package handlers.linux;

import java.io.IOException;
import java.sql.SQLException;

import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.HSQLDBHandle;

public class HSQLDBHandleLinux extends HSQLDBHandle {

	public HSQLDBHandleLinux(IComponentInstance ci)
			throws UnavailablePortsException, IOException, SQLException, InterruptedException, ClassNotFoundException {
		super(ci);
	}

}

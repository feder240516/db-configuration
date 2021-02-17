package managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exceptions.UnavailablePortsException;
import helpers.Port;

public class PortManager {
	
	/*private static PortManager _instance = new PortManager();
	public static PortManager getInstance() {
		return _instance;
	}*/
	
	private static Map<Integer,Port> ports = new HashMap<>();
	private static int[] portsNumbers;
	
	public static void setupAvailablePorts(int[] newPorts) {
		for(int port: newPorts) {
			if (!ports.containsKey(port)) { ports.put(port,new Port(port)); }
		}
	}
	
	public static synchronized int acquireAnyPort() throws UnavailablePortsException {
		for(Port port: ports.values()) {
			if (port.available) {
				port.available = false;
				return port.number;
			}
		}
		throw new UnavailablePortsException("There are no available ports");
	}
	
	public static synchronized void releasePort(int port) {
		ports.get(port).available = true;
	}
}

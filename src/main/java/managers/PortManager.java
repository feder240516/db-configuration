package managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import exceptions.UnavailablePortsException;
import helpers.Port;

public class PortManager {
	
	private static PortManager _instance = new PortManager();
	public static PortManager getInstance() {
		return _instance;
	}
	
	private Map<Integer,Port> ports = new HashMap<>();
	private Queue<Integer> portsQueue = new LinkedList<>();
	
	public void setupAvailablePorts(int[] newPorts) {
		for(int port: newPorts) {
			if (!ports.containsKey(port)) { 
				ports.put(port,new Port(port));
				portsQueue.add(port);
			}
		}
	}
	
	public synchronized int acquireAnyPort() throws UnavailablePortsException {
		int portNumber = portsQueue.remove();
		portsQueue.add(portNumber);
		int triedPorts = 0;
		while(!ports.get(portNumber).available && ++triedPorts < ports.size()) {
			portNumber = portsQueue.remove();
			portsQueue.add(portNumber);
		}
		throw new UnavailablePortsException("There are no available ports");
	}
	
	public synchronized void releasePort(int port) {
		ports.get(port).available = true;
	}
}

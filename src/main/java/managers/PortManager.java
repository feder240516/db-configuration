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
		if (ports.size() == 0) {
			setupAvailablePorts(new int[] {9901,9902,9903,9904,9905,9906,9907,9908,9909});
		}
		int triedPorts = 0;
		while(triedPorts++ < ports.size()) {
			int portNumber = portsQueue.remove();
			portsQueue.add(portNumber);
			Port selectedPort = ports.get(portNumber); 
			if(selectedPort.available) {
				selectedPort.available = false;
				return portNumber;
			}
		}
		throw new UnavailablePortsException("There are no available ports");
	}
	
	public synchronized void releasePort(int port) {
		ports.get(port).available = true;
	}
}

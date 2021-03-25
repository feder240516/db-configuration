package helpers;

import ai.libs.jaicore.components.api.IComponentInstance;

public class TestResult {
	private String dbInstance;
	private String componentInstanceID;
	private double time;
	private String rdms;
	
	public TestResult(String dbInstance, double time, IComponentInstance componentInstance) {
		super();
		this.dbInstance = dbInstance;
		String instanceID = componentInstance.getParameterValue("__instanceID"); 
		if (instanceID == null) {
			this.componentInstanceID = "anonymous";
		} else {
			this.componentInstanceID = instanceID;
		}
		this.time = time;
		this.rdms = componentInstance.getComponent().getName();
	}
	
	public String getDbInstance() {
		return dbInstance;
	}
	
	public double getTime() {
		return time;
	}
	
	public String getRdms() {
		return rdms;
	}

	public String getComponentInstanceID() {
		return componentInstanceID;
	}
	
}

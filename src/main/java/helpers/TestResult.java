package helpers;

import ai.libs.jaicore.components.api.IComponentInstance;

public class TestResult {
	private String dbInstance;
	private String componentInstanceID;
	private double time;
	private String rdms;
	private String variable;
	private double variableValue;
	private String queryProfileID;
	private String isDefault;
	
	public TestResult(String dbInstance, double time, IComponentInstance componentInstance, String queryProfileID) {
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
		this.variable = componentInstance.getParameterValue("__evalVar");
		if (this.variable == null) { this.variable = "NOT_SPECIFIED"; } 
		String val = componentInstance.getParameterValue("__evalVarValue");
		tryAssignVariableValue(val);
		this.queryProfileID = queryProfileID;
		this.isDefault = componentInstance.getParameterValue("__isDefault");
	}

	private void tryAssignVariableValue(String valStr) {
		if (valStr == null) {
			this.variableValue = Double.POSITIVE_INFINITY;
		}
		try {
			this.variableValue = Double.parseDouble(valStr);
		} catch (NumberFormatException e) {
			this.variableValue = -1;
		}
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

	public String getVariable() {
		return variable;
	}

	public double getVariableValue() {
		return variableValue;
	}

	public String getQueryProfileID() {
		return queryProfileID;
	}
	
	public String isDefault() {
		return isDefault;
	}
	
}

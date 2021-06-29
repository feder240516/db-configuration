package helpers;

import java.util.Map;
import java.util.Set;

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
	private String algorithm;
	private String experimentUUID;
	private Map<String, String> parameters;
	private IComponentInstance componentInstance;
	private double timestamp;
	
	public TestResult(String dbInstance, double time, IComponentInstance componentInstance, String queryProfileID) {
		super();
		this.dbInstance = dbInstance;
		this.timestamp = System.currentTimeMillis();
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
		this.parameters = componentInstance.getParameterValues();
		this.algorithm = componentInstance.getParameterValue("__algorithm");
		this.experimentUUID = componentInstance.getParameterValue("__experimentUUID");
	}

	private void tryAssignVariableValue(String valStr) {
		if (valStr == null) {
			valStr = String.valueOf(Double.POSITIVE_INFINITY);
		}
		try {
			this.variableValue = Double.parseDouble(valStr);
		} catch (NumberFormatException | NullPointerException e) {
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
	
	public Set<String> getParameterKeys() {
		return this.parameters.keySet();
	}
	
	public String getParameterValues(String key) {
		return this.parameters.get(key);
	}
	
	public double getTimestamp() {
		return this.timestamp;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getExperimentUUID() {
		return experimentUUID;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setExperimentUUID(String experimentUUID) {
		this.experimentUUID = experimentUUID;
	}
	
}

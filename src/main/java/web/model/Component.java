package web.model;

public class Component {
	private String name;
	private String[] providedInterfaces;
	private Object[] requiredInterfaces;
	private Parameter[] parameters;
	private Object[] dependencies;

	public String getName() { return name; }
	public void setName(String value) { this.name = value; }

	public String[] getProvidedInterfaces() { return providedInterfaces; }
	public void setProvidedInterfaces(String[] value) { this.providedInterfaces = value; }

	public Object[] getRequiredInterfaces() { return requiredInterfaces; }
	public void setRequiredInterfaces(Object[] value) { this.requiredInterfaces = value; }

	public Parameter[] getParameters() { return parameters; }
	public void setParameters(Parameter[] value) { this.parameters = value; }

	public Object[] getDependencies() { return dependencies; }
	public void setDependencies(Object[] value) { this.dependencies = value; }
}

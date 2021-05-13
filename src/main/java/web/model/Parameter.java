package web.model;

public class Parameter {
	private String name;
	private DefaultDomain defaultDomain;
	private long defaultValue;

	public String getName() { return name; }
	public void setName(String value) { this.name = value; }

	public DefaultDomain getDefaultDomain() { return defaultDomain; }
	public void setDefaultDomain(DefaultDomain value) { this.defaultDomain = value; }

	public long getDefaultValue() { return defaultValue; }
	public void setDefaultValue(long value) { this.defaultValue = value; }
}
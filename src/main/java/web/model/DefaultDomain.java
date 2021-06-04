package web.model;

public class DefaultDomain {
	private boolean isInteger;
	private double min;
	private double max;

	public boolean getIsInteger() { return isInteger; }
	public void setIsInteger(boolean value) { this.isInteger = value; }

	public double getMin() { return min; }
	public void setMin(double value) { this.min = value; }

	public double getMax() { return max; }
	public void setMax(double value) { this.max = value; }
}
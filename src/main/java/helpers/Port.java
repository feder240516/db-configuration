package helpers;

public class Port {
	public int number;
	public boolean available;
	
	public Port(int number) {
		this(number,true);
	}
	
	public Port(int number, boolean available) {
		this.number = number;
		this.available = available;
	}
}

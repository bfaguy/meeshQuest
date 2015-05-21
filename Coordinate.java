

public class Coordinate {

	private float x;
	private float y;
	
	// default Constructor //
	public Coordinate(float x_par, float y_par) {
		x = x_par;
		y = y_par;
	}
		
	// Copy Constructor //
	public Coordinate(Coordinate copy) {
		x = copy.getX();
		y = copy.getY();	
	}
	
	public float getX() { 
		return x;
	}
	public float getY() {
		return y;
	}
	
	
	public String toString()
	{
//		  System.out.println("Coordinate:" + "(" + x + "," + y + ")");
		  
		  // Print City in XML format //
				
	  
		return new String("Coordinate:" + "(" + x + "," + y + ")");
	}
	
	
}

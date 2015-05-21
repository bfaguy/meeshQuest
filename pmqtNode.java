//import java.awt.geom.Rectangle2D;

//import geom.Rectangle2f;

public abstract class pmqtNode extends Rectangle2D.Float {

	protected int location; // 0=root, 1=NW, 2=NE, 3=SW, 4=SE
	
	pmqtNode (int x, int y, int width, int height, int location_in) {
		super(x,y,width,height);
	
		location = location_in;
	}
		
	
	public void setLocation(int location_in) { 
		this.location = location_in; 
	}
	
	public int getLocation() {
		return location; 
	}
	
	
}


// import java.util.Comparator;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Float;
import java.awt.Rectangle;
import java.awt.Shape;

public class City extends Float implements Shape {

	private String name;
	private int radius;
	private String color;
	
	public City() {
		super();
		// TODO Auto-generated constructor stub
	}

	public City(City copy) {
		this.x = copy.x;
		this.y = copy.y;
		
		this.name = copy.getName();
		this.radius = copy.getRadius();
		this.color = copy.getColor();
	}
	
	public City(float arg0, float arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	
	public City(String name_par, float x_in, float y_in, int radius_par, String color_par) 
	{
		name = name_par;
		x = x_in;
		y = y_in;
		radius = radius_par;
		color = color_par;
	}
	
	public String getName()	{
		return name;
	}
	    
	public int getRadius() {
		return radius;
	}
	  
	public String getColor() {
		return color;
	}

	// getX and getY are implemented in parent class: Point2D.Float //
		
	public int compareTo(City otherCity)
	{
		return name.compareTo(otherCity.getName());
	}

	public String toString() {
		int x = (int)getX();
		int y = (int)getY();
		
		return "(" + Integer.toString(x) + "," + Integer.toString(y) + ")";
		
		//return getName();
	}
	
	
	// Shape Methods //////////////////////////////////////////////////////
	
	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	public Rectangle2D getBounds2D() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean contains(double x, double y) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(Point2D p) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean intersects(double rx, double ry, double rw, double rh) {

		boolean value = false;
		
		if ((x >= rx) && (x <= (rx + rw)) && (y >= ry) && ( y <= (ry + rh)) )
			value = true;
		
		return value;
	}

	public boolean intersects(Rectangle2D r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public boolean contains(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(Rectangle2D r) {
		// TODO Auto-generated method stub
		return false;
	}

	public PathIterator getPathIterator(AffineTransform at) {
		// TODO Auto-generated method stub
		return null;
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

import java.awt.geom.Line2D.Float;
import java.awt.geom.Point2D;

public class Road extends Float {
	
	protected City start;
	protected City end;
		
	// no default constructor //
	public Road(City startCity, City endCity) 
	{
		super(startCity,endCity);
				
		start = startCity;
		end = endCity;
	
	}
		
	public City getStart() {
		return start;		
	}
	
	public City getEnd() {
		return end;
	}
	
	public static double getDistance(City startCity, City endCity) {
		
		double distance = Point2D.distance(startCity.getX(),startCity.getY(),
				endCity.getX(),endCity.getY());; 
						
		return distance;
	}
	
	public String toString() {
		
		return (start.getName() + "-" + end.getName());
	}

}

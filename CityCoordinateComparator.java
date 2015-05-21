import java.util.Comparator;

public class CityCoordinateComparator implements Comparator<Coordinate> {

	public int compare (Coordinate c1, Coordinate c2) {
			
		int value = 0;
			
		if (c1.getX() < c2.getX()) {
			value = -1;
		}
		else if (c1.getX() > c2.getX()) {
			value = 1;
		}
		else if (c1.getY() < c2.getY()) {
			value = -1;
		}
		else if (c1.getY() > c2.getY()) {
			value = 1;
		}
		else { // they are the same
			value = 0;
		}
			
		return value;
		
	}
	
}


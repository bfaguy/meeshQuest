import java.awt.geom.Point2D;
import java.util.Comparator;

public class RoadDistanceComparator implements Comparator<Road> {

	double queryX;
	double queryY;
	
	RoadDistanceComparator(double x_in, double y_in) {
		queryX = x_in;
		queryY = y_in;
	}
	
	public int compare (Road left, Road right) {
	    	
	    	int comp = 0;
	    	
	    	double leftDist;
	    	double rightDist;
	    	
    		leftDist = left.ptSegDist(queryX, queryY);
    		
    		rightDist = right.ptSegDist(queryX,queryY);

	   		if ((leftDist - rightDist) < 0 ) {
				comp = -1;
			}
			else if ((leftDist - rightDist) > 0) {
				comp = 1;
			}
			else {
				comp = 0;
			}
	    	
	    	return comp;
	    }
}

import java.util.Comparator;

public class RoadComparator implements Comparator<Road> {

	public int compare (Road r1, Road r2) {
		
		int startComp = (r1.getStart().getName()).compareTo(r2.getStart().getName());
		
		if (startComp == 0) {
			startComp = (r1.getEnd().getName()).compareTo(r2.getEnd().getName());
		}
		
		return startComp;
	}
	
}

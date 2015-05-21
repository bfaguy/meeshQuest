import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.TreeSet;

import geom.Geometry;

public class PM3Validator implements PMValidator{
	
	public Geometry checkIntersection(Shape geometry, BlackN blackNode)
		throws RoadIntersectionException {
		
		// geometry passed in should always be Road //
		Road road = (Road)geometry; 
		
		TreeSet<Road> roads = blackNode.getRoads();
		
		if (roads != null) {

			String roadStart = road.getStart().getName();
			String roadEnd = road.getEnd().getName();
			
			Iterator it = roads.iterator();
			Road tempRoad;
							
			while (it.hasNext()) {
								
				tempRoad = (Road)it.next();

				String tempRoadStart = tempRoad.getStart().getName(); 
				String tempRoadEnd = tempRoad.getEnd().getName();
				
				// if intersect and 
				if (road.intersectsLine(tempRoad)) { // intersect with previous road
					
					if ((roadStart != tempRoadStart) && 
						(roadStart != tempRoadEnd)	&& 
						(roadEnd != tempRoadStart) &&
						(roadEnd != tempRoadEnd)) {

						throw new RoadIntersectionException("Road intersects previous road");										
					}
				}
			}
		}
		
		return null;
	}

	public boolean valid(BlackN node) {
		return true;
	}
	
	public String PMOrder() {
		return "3";
	}	
	
}




import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.TreeSet;

import geom.Geometry;

public class PM2Validator implements PMValidator{
	
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
		
		Road tempRoad;	
		String startName, endName, commonName;
		
		if (node.getCity() != null) {
			// has vertex -> all edges must have this vertex as endpoint //

			//System.out.println("   has city");
			commonName = node.getCity().getName();

			TreeSet<Road> roads = node.getRoads();
			
			if (roads != null) {
				// check each road to have the commond endpoint
			
				Iterator it = roads.iterator();
				while (it.hasNext()) {
					tempRoad = (Road)it.next();
					startName = tempRoad.getStart().getName();
					endName = tempRoad.getEnd().getName();
					
					// if start and end NOT commond end point -> return false
					if (!startName.equals(commonName) && !endName.equals(commonName)) {
						return false;
					}
				}
			}
			else {
				// no roads -> valid black node
				return true;
			}
		}
		else {
			// no vertex -> all edges must share common endpoint 
			
			String commonName1;
			String commonName2;
			
			TreeSet<Road> roads = node.getRoads();
			
			if (roads != null) {
				
				// find the commond end //				
				Iterator it = roads.iterator();
				tempRoad = (Road)it.next();

				commonName1 = tempRoad.getStart().getName();
				commonName2 = tempRoad.getEnd().getName();

				tempRoad = (Road)it.next();
				if (tempRoad == null) // only one road exist, of course it has common end with itself
					return true;
				else {
					if (commonName1.equals(tempRoad.getStart().getName()) ||
						commonName1.equals(tempRoad.getEnd().getName())) {
						
						commonName = commonName1;
					}
					else if (commonName2.equals(tempRoad.getStart().getName()) ||
							commonName2.equals(tempRoad.getEnd().getName())) {
						commonName = commonName2;
					}
					else {
						// no endpoints in common
						return false;
					}
				}
				
				while (it.hasNext()) {
					tempRoad = (Road)it.next();
					startName = tempRoad.getStart().getName();
					endName = tempRoad.getEnd().getName();
					
					// if start and end NOT commond end point -> return false
					if (!startName.equals(commonName) && !endName.equals(commonName)) {
						return false;
					}
				}
				
			}
			else {
				// this should never happen, but in case
				return true;
			}
			
		}
		
		return true;
	}
	

	
	
	
	public String PMOrder() {
		return "2";
	}	
	
}



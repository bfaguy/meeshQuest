import java.util.Iterator;
import java.util.TreeSet;

//import geom.Geometry;
//import java.awt.geom.Rectangle2D;
//import java.util.ArrayList;

import java.awt.Shape;


/* PM3: 
 * 	> contains 0 or 1 city
 *  > contains 0 or more roads
 */

public class BlackN extends pmqtNode {
	
	private City city;
	TreeSet<Road> roads;

	//RoadComparator RComp = new RoadComparator();
	
	// Constructors ////////////////////////////////////////////////////
	
	BlackN (Shape geometry, int x, int y, int width, int height, int location) {
		super(x,y,width,height, location);
		
		RoadComparator RComp = new RoadComparator();
		
		if (geometry instanceof City) {
			this.city = (City)geometry;	
			roads = new TreeSet<Road>(RComp);
		}
		else { // instance of Road

			roads = new TreeSet<Road>(RComp); //initial capacity is 1
			
			Road road = (Road)geometry;
			City start = (City)road.getStart();
			City end = (City)road.getEnd();
			
			if (start.getName().compareTo(end.getName()) > 0){
				roads.add(new Road(end, start));
			}
			else {
				roads.add(road);	
			}
			
			this.city = null;
		}
	}
	
	// should only be used in mergeChildren // 
	BlackN (int x, int y, int width, int height, int location) {
		super(x,y,width,height, location);
		this.city = null;
		
		RoadComparator RComp = new RoadComparator();
		this.roads = new TreeSet<Road>(RComp);
	}
	
	
	// Methods /////////////////////////////////////////////////////////
	
	public void addCity(City city_in) {
		this.city = city_in;
	}
	
	public boolean addRoad(Road road) {
		
		boolean roadAdded = false;
		
		City start = (City)road.getStart();
		City end = (City)road.getEnd();
		
		if (roads == null) { // inititate if roads is blank
			RoadComparator RComp = new RoadComparator();
			roads = new TreeSet<Road>(RComp);
		}
			
		if (start.getName().compareTo(end.getName()) > 0){
			roadAdded = roads.add(new Road(end, start));
		}
		else {
			roadAdded = roads.add(road);	
		}
		
		return roadAdded;
	}
	
	public void addRoads(TreeSet<Road> roads_in) {
		// assume all the the roads are in correct start/end order
		// because they have already been mapped 
		roads.addAll(roads_in);
	}
	
	public pmqtNode deleteCity() {
		
		// remove city //
		if (this.city != null) {
			city = null;
		}
		else { 
			System.out.println("something is wrong");
		}

		// if there is nothing else in this black Node, return white node
		if ((roads == null) || (roads.size() == 0)) {
			return new WhiteN((int)this.getX(), (int)this.getY(),
					  (int)this.getHeight(), (int)this.getWidth(),
					  this.getLocation());
		}
		else {
			return this;	
		}
		
	}
	
	public boolean deleteRoad(Road road) {
		
		boolean roadDeleted = false;
		
		City start = (City)road.getStart();
		City end = (City)road.getEnd();
		Road roadReverse = new Road(end, start); 
		
		if (roads != null) {
			if (roads.contains(road)) {
				roadDeleted = roads.remove(road);
			}
			else if (roads.contains(roadReverse)) {
				roadDeleted = roads.remove(roadReverse);
			}
		}
		else { // road doesn't exists
			if (roads == null) {
				new Debug("    ERROR: roads equal null");
			}
			else if (!roads.contains(road)) {
				new Debug("    ERROR: road not contained");
			}
			
			//System.out.println("BlackN -> deleteRoad: road not deleted");
		}
		
		return roadDeleted;
	}
	
	// Calculates the Number of roads that has target City as an endpoint //
	public int numRoadsToCity(City city) {
		
		int numRoads = 0;
		
		if (roads != null) {

			Iterator it;
			it = roads.iterator();
			Road tempRoad;
							
			while (it.hasNext()) {
				tempRoad = (Road)it.next();
				
				if ((tempRoad.getStart() == city) || (tempRoad.getEnd() == city)) {
					numRoads++;
				}
				
			}
		}
		
		return numRoads;
	}
	
	public City getCity() {
		return this.city;
	}
	
	public TreeSet<Road> getRoads() {
		return this.roads;
	}
}
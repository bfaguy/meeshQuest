import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import java.util.List;
import java.util.ArrayList;

import xml.XMLElement;

// This version is for the Road class that has Strings "start" and "end"

public class AdjacencyList extends TreeMap<City,TreeSet<Road>> {

	public AdjacencyList() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AdjacencyList(Comparator arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public AdjacencyList(Map arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public AdjacencyList(SortedMap arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void createRoads(SortedMap NameDictionary, City newCity, XMLElement output) {
		
		XMLElement roadCreated1 = new XMLElement("roadCreated", output);
		XMLElement roadCreated2 = new XMLElement("roadCreated", output);
		
		RoadComparator RComp = new RoadComparator();
		TreeSet RoadSet = new TreeSet(RComp);
				
		City oldCity = new City();
				
		if (size() >= 3) { // choose 2 roads out to the n previous vertex
			
			String closestCity[] = new String[1], farthestCity[] = new String[1];
			findNeighbors(newCity, closestCity, farthestCity);
			
			String closest = new String(closestCity[0]);
			String farthest = new String(farthestCity[0]);
			
			if (closest.compareTo(farthest) < 0) {
				oldCity = (City)NameDictionary.get(closestCity[0]);
				output.appendChild(addBidirectRoads(oldCity, newCity, roadCreated1));
				
				oldCity = (City)NameDictionary.get(farthestCity[0]);
				output.appendChild(addBidirectRoads(oldCity, newCity, roadCreated2));
			}
			else {
				oldCity = (City)NameDictionary.get(farthestCity[0]);
				output.appendChild(addBidirectRoads(oldCity, newCity, roadCreated1));
				
				oldCity = (City)NameDictionary.get(closestCity[0]);
				output.appendChild(addBidirectRoads(oldCity, newCity, roadCreated2));
			}
		}
		else if (size() == 2) {
			oldCity = firstKey();
			City oldCity2 = lastKey();
					
			output.appendChild(addBidirectRoads(oldCity, newCity, roadCreated1));
			output.appendChild(addBidirectRoads(oldCity2, newCity, roadCreated2));
		}
		else if (size() == 1) { // adjacency list has one city
			oldCity = firstKey();			
			output.appendChild(addBidirectRoads(oldCity, newCity, roadCreated1));
		}
		else if (size() == 0) { // empty List //
			put(newCity,RoadSet); // Just add the City , no roads created //
		}
	}
	
	public XMLElement addBidirectRoads (City oldCity, City newCity, XMLElement roadCreated) {
		
		addOneRoad(oldCity, newCity); // add road to new City //
		addOneRoad(newCity, oldCity); // add road to old City //
					
		roadCreated.setAttribute("start", oldCity.getName());
		roadCreated.setAttribute("end", newCity.getName());
		
		return roadCreated;
	}
	
	// for part 4 //
	public void addRoad(City startCity, City endCity) {
		addOneRoad(startCity, endCity);
		addOneRoad(endCity, startCity);
	}
	
	public void addOneRoad(City startCity, City endCity) {
		
		RoadComparator RComp = new RoadComparator();
		TreeSet<Road> endRoadSet = new TreeSet<Road>(RComp);
		
		Road road = new Road(startCity, endCity);
			
		if (containsKey(endCity)) {
			(get(endCity)).add(road);
		}
		else {
			endRoadSet.add(road);
			put(endCity,endRoadSet);
		}
	}
	
	public void findNeighbors(City newCity, String[] closestCity, String[] farthestCity) {
			
		LinkedList myList = new LinkedList();
		Iterator it;
		City tempCity = new City();
		double closest = -1.0, farthest = 0.0;
						
		myList.addAll(keySet());
		it = myList.iterator();
		
		while (it.hasNext()) {
			tempCity = (City)it.next();	
	
			if (closest != -1) {
				
				// Check closest //
				if (tempCity.distance(newCity.getX(),newCity.getY()) < closest) { 
						
					if ((farthest == closest) && 
							(closestCity[0].compareTo(farthestCity[0]) < 0 )) {
						farthestCity[0] = closestCity[0];
					}
					
					closest = tempCity.distance(newCity.getX(),newCity.getY());
					closestCity[0] = tempCity.getName();
				
				}
				// Check farthests //
				else if (tempCity.distance(newCity.getX(),newCity.getY()) > farthest) {
					farthest = tempCity.distance(newCity.getX(),newCity.getY());
					farthestCity[0] = tempCity.getName();
				}
				
				// if there's a tie btw 3 cities, choose 2 lowest asciibetical //
				else if ((farthestCity[0].compareTo(closestCity[0]) == 0) && 
						(closest == farthest)) {
					farthestCity[0] = tempCity.getName();
				}
								
			}
			else { 	// set closest and farthest for the first Iteration //
				closest = tempCity.distance(newCity.getX(),newCity.getY());
				closestCity[0] = tempCity.getName();
				
				farthest = tempCity.distance(newCity.getX(),newCity.getY());
				farthestCity[0] = tempCity.getName();
		
			}
		}
	}
	
	// for part 4 //
	public void deleteRoad(Road road) {
		
		City startCity = road.getStart();
		City endCity = road.getEnd();
		
		// find start City //
		TreeSet<Road> startRoadSet = get(startCity);
		
		// Delete road from startCity roadSet //
		if (updateRoads(startRoadSet, endCity) == true) {
			// refresh road set //
			refreshRoadSet(startCity, startRoadSet);
		}
			
		// find end City //
		TreeSet<Road> endRoadSet = get(endCity);
		
		//Delete road from endCity roadSet //
		if (updateRoads(endRoadSet, startCity) == true) {
			// refresh road set //
			refreshRoadSet(endCity, endRoadSet);
		}
	}
	
	// delete old city and all roads associated with old city from adj. list // 
	public void deleteRoads(City oldCity) {
						
		LinkedList myList = new LinkedList();
		Iterator it;
		TreeSet<Road> RoadSet = new TreeSet<Road>();
					 		
		myList.addAll(keySet()); 
		it = myList.iterator();
	
		// Goes through All Cities in Adjacency List //
		while (it.hasNext()) {
			
			City tempCity = (City)it.next();
			RoadSet = get(tempCity);

			// Delete TreeMap<City, RoadSet> Pair if tempCity = oldCity 
			if (tempCity.getName().compareTo(oldCity.getName()) == 0) {
				this.remove(oldCity);				
			}
			else if (updateRoads(RoadSet, oldCity) == true) {
				//refresh road set
				refreshRoadSet(tempCity, RoadSet);
			}
		}
	}

	private void refreshRoadSet(City city, TreeSet<Road> newRoadSet) {
		
		this.remove(city);
		
		if (newRoadSet.size() != 0) {
			this.put(city, newRoadSet);
		}
	}
	
	public boolean updateRoads(TreeSet RoadSet, City oldCity) {
		
		boolean roadsUpdated = false;
		
		LinkedList myList = new LinkedList();
		Iterator it;
				
		myList.addAll(RoadSet);
		it = myList.iterator();
		
		while (it.hasNext()) {
		
			Road tempRoad = (Road)it.next();
			
			// if Road's start city is same as old city, Delete the Road //
			if (tempRoad.getStart().getName().compareTo(oldCity.getName()) == 0) {
				
				/* commented out for part4
				// other City < deleting City //
				if (tempRoad.getEnd().getName().compareTo(oldCity.getName()) < 0) {
					roadDeleted.setAttribute("start", tempRoad.getEnd().getName());
					roadDeleted.setAttribute("end", tempRoad.getStart().getName());
				}
				
				// other City > deleting City //
				else {
					roadDeleted.setAttribute("start", tempRoad.getStart().getName());
					roadDeleted.setAttribute("end", tempRoad.getEnd().getName());
				}
				*/
				
				RoadSet.remove(tempRoad);
				roadsUpdated = true;
			}
		}
		
		return roadsUpdated;
	}
	
	public List getDestinations(City city) {
		
		List list = new ArrayList();
		LinkedList myList = new LinkedList();
		Iterator it;
		
		TreeSet RoadSet = get(city);
		
		myList.addAll(RoadSet);  // get returns a TreeSet which is a set
		it = myList.iterator();
				
		while (it.hasNext()) {
			Road tempRoad = (Road)it.next();
			list.add(tempRoad.getStart());
		}
					
		return list;
	}
	
	public double getDistance(City start, City end) {
	
		return Road.getDistance(start, end);
		
	}
	                                                

	
	
}

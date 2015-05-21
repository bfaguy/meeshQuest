
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.PriorityQueue;

import xml.XMLElement;

import java.text.*;

import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

public class DijkstraManager {
	
	private final Comparator shortestDistanceComparator = new Comparator()
    {
		public int compare(Object left, Object right)
        {
			assert left instanceof City && right instanceof City : "invalid comparison";
            return compare((City) left, (City) right);
        }
          
        private int compare(City left, City right)
        {
            // note that this trick doesn't work for huge distances, close to Integer.MAX_VALUE
            double result = getShortestDistance(left) - getShortestDistance(right);
            int value = 0;
            
            if (result < 0) {
            	value = -1;
            }
            else if (result > 0) {
            	value = 1;
            }
            
            return (result == 0) ? left.compareTo(right) : value;
        }
    };
  	
    private final AdjacencyList mapList;
    
	private final PriorityQueue unsettledNodes = 
		new PriorityQueue(10, shortestDistanceComparator);
	
	private final Set settledNodes = new HashSet();
	
	private final Map shortestDistances = new HashMap();
    private final Map predecessors = new HashMap();
    
    // Constructor //////////////////////////////////////////////////////////////
    
    public DijkstraManager(AdjacencyList AdList) {
    	this.mapList = AdList;   	
    }
        
    
    // Member Functions //////////////////////////////////////////////////////////
    
    public void execute(City start, City destination) {
    
    	init(start);
    
    	// the current City //
    	City current;
    	
    	while ((current = extractMin()) != null) {
    		
    		// current should not be settled yet // 
    		assert !isSettled(current);
    		
    		if (current == destination) break;
    	    	   		
    		markSettled(current);
    		
    		relaxNeighbors(current);	    		
    		
    	}
    	
    }
    
	private void init(City start) {
		
		settledNodes.clear();
		unsettledNodes.clear();
		
		shortestDistances.clear();
		predecessors.clear();
		
		// add starting city //
		setShortestDistance(start, 0);
		unsettledNodes.add(start);
		
	}
	
	private void setShortestDistance(City city, double distance) {

			// this crucial step ensure no duplicates will be created in the queue
	        // when an existing unsettled node is updated with a new shortest distance
	        unsettledNodes.remove(city);

	        shortestDistances.put(city, new Double(distance));
	        
			// re-balance the sorted set according to the new shortest distance found
			// (see the comparator the set was initialized with)
			unsettledNodes.add(city);        
	}
	    

	public Double getShortestDistance(City city)	{
		
		Double d = (Double) shortestDistances.get(city);
	    return (d == null) ? Double.POSITIVE_INFINITY: d.doubleValue();
	}

	
	private City extractMin() {
		
		if (unsettledNodes.isEmpty()) return null;
	
		City min = (City) unsettledNodes.poll();
		unsettledNodes.remove(min);
		
		return min;		
	}
	
	private boolean isSettled(City v) {
		return settledNodes.contains(v);
	}
	
	private void markSettled(City current) {
		settledNodes.add(current);
	}
	
	private void relaxNeighbors(City current) {
		
		Iterator i = mapList.getDestinations(current).iterator();
				
		while (i.hasNext()) {
		
			City neighborCity = (City) i.next();
			
			// skip node already settled //
			if (isSettled(neighborCity)) continue;
					
			if (getShortestDistance(neighborCity) > 
				getShortestDistance(current) + mapList.getDistance(current, neighborCity)) {
				
				setShortestDistance(neighborCity, getShortestDistance(current) +
						mapList.getDistance(current, neighborCity));
				
				setPredecessor(neighborCity, current);
			}
		
		}
	}
	
	public City getPredecessor(City city) {
		return (City) predecessors.get(city);
	}
	
	private void setPredecessor(City a, City b) {
		predecessors.put(a,b);
	}
	
	public void shortestPathSuccess (City start, City end, XMLElement success, boolean annotate)
		throws NoPathException {
						
		DecimalFormat three = new DecimalFormat("0.000");
		
		XMLElement comm = new XMLElement("command", success);
		
		if (annotate == true) {
			comm.setAttribute("name", "shortestPathAnnotated");
		}
		else {
			comm.setAttribute("name", "shortestPath");	
		}
		
		success.appendChild(comm);
		
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement startName = new XMLElement("start", param);
			startName.setAttribute("value", start.getName());
			param.appendChild(startName);
		
			XMLElement endName = new XMLElement("end", param);
			endName.setAttribute("value", end.getName());
			param.appendChild(endName);
		
		XMLElement output = new XMLElement("output", success);
		success.appendChild(output);
		
			XMLElement path; 
			if (annotate == true){
				path = new XMLElement("annotatedPath", output);
			}
			else {
				path = new XMLElement("path", output);				
			}
			
			path.setAttribute("length", three.format(getShortestDistance(end)));
			printPredecessor(end, 0, path, annotate);
			output.appendChild(path);
	}

	public City printPredecessor(City city, int hops, XMLElement path, boolean annotate) 
		throws NoPathException {
		
		City predecessor = getPredecessor(city);
				
		if (predecessor != null) {
			
			hops++;
			City prePredecessor = printPredecessor(predecessor, hops, path, annotate);
			
			if (annotate == true) {
				// find direction //
				
				String direction = findDirection(prePredecessor, predecessor, city);
				
				if (direction != null) {
					XMLElement directionN = new XMLElement(direction, path);
					path.appendChild(directionN);
				}
			}
			
			XMLElement road = new XMLElement("road", path);
			road.setAttribute("start", predecessor.getName());
			road.setAttribute("end", city.getName());
			path.appendChild(road);			
		
			return predecessor;
		}
		else {
			if (hops == 0)
				throw new NoPathException( "no path exist");
			path.setAttribute("hops", Integer.toString(hops));
			return null;
		}
	}

	
	private String findDirection(City A, City B, City C) {
		
		String returnString;
		
		if (A == null)
			returnString = null;
		else {
			
			Arc2D.Float arc = new Arc2D.Float();
			arc.setArcByTangent(A, B, C, 1);
			
			double degree = arc.getAngleExtent();
			
			if (degree >= 45)
				returnString = "right";
			else if ((degree < 45) && (degree >= -45)) {
				returnString = "straight";
			}
			else {
				returnString = "left";
			}
		}
		
		return returnString;
	}
	
}

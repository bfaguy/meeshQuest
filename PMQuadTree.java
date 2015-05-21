import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import java.awt.*;

import org.w3c.dom.Element;

import xml.XMLDocument;
import xml.XMLElement;
import xml.XMLPrintVisitor;
import drawing.CanvasPlus;

public class PMQuadTree extends AbstractMap {
			
	public class pmqtNodeComparator implements Comparator<pmqtNode> {
		
		double queryX;
		double queryY;
		
		pmqtNodeComparator(double x_in, double y_in) {
			queryX = x_in;
			queryY = y_in;
		}
		
        public int compare (pmqtNode left, pmqtNode right) {
        	
        	int comp = 0;
        	
        	double leftDist;
        	double rightDist;
        	
        	if (left instanceof BlackN) {
        		leftDist = getBlackDistance(left);
        	}        	
        	else { //instance of GrayN  		
        		leftDist = getGrayDistance(left);
        	}
        	
        	if (right instanceof BlackN) {
		 		rightDist = getBlackDistance(right);	
        	}
        	else { //instance of GrayN
        		rightDist = getGrayDistance(right);
        	}

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
        
        private double getBlackDistance(pmqtNode n) {
        	BlackN newBlackN = (BlackN) n;
    		//City blackCity = newBlackN.getCity();
        	
        	double blackDist = Point2D.distance(newBlackN.getCenterX(),
        			newBlackN.getCenterY(),queryX,queryY);
        	
        	/// new Debug("black distance:" + blackDist);
        	
    		return blackDist; 
        }		
		
        private double getGrayDistance(pmqtNode n) {
        	GrayN newGrayN = (GrayN) n;
        	
        	/*
        	new Debug("gray center x:" + newGrayN.getCenterX());
        	new Debug("gray center y:" + newGrayN.getCenterY());
        	*/
        	
        	double grayDist = Point2D.distance(newGrayN.getCenterX(),
        			newGrayN.getCenterY(),queryX,queryY); 
        	
        	
        	//new Debug("gray distance:" + grayDist);
        	
        	
    		return  grayDist;
        }		
	}
		
	// Data Memebers/////////////////////////////////////////
	
	private pmqtNode root;
	
	transient int numCities;
	
	private int spatialWidth;
	private int spatialHeight;
	
	//private int pmOrder;
	
	public static short quadrantOne = 0;
	public static short quadrantTwo = 1;
	public static short quadrantThree = 2;
	public static short quadrantFour = 3;
	
	CanvasPlus canvasMap;
	
	protected PMValidator validator;
	
	// Constructors /////////////////////////////////////////
	
	PMQuadTree(int width_in, int height_in, PMValidator validator_in) {
		
		//pmOrder = pmOrder_in;
		
		spatialWidth = width_in;
		spatialHeight = height_in;
		
		root = null;
		numCities = 0;
		
		this.validator = validator_in; 
	}

	// Methods //////////////////////////////////////////////
	
	public boolean add (Shape geometry) 
		throws RoadAlreadyMappedException, CityAlreadyMappedException, RoadIntersectionException,
		PartitionTooDeepException {

		if (geometry instanceof Road) {
			String start = ((Road)geometry).getStart().getName();
			String end = ((Road)geometry).getEnd().getName();
			new Debug("ADD ROAD: " + start + "-" + end);
		}
		else new Debug("ADD CITY: " + ((City)geometry).getName());
		
		boolean geometryNotMappedAlready = true;
			
		if (root == null) {	// initialize root //  
			if (geometry instanceof City) {
				BlackN newBlackRoot = new BlackN((City)geometry, 0, 0, spatialWidth, spatialHeight, 0);
				root = newBlackRoot;
			}
		}
		else { 
			if (root instanceof BlackN) {  // only occurs when there is 1 city (road is being added later)
				
				// transform a BlackN to GrayN //
				GrayN newGrayRoot = new GrayN(0, 0, spatialWidth, spatialHeight, 0);
				addRecursive (((BlackN)root).getCity() , newGrayRoot, null);
				
				root = newGrayRoot;	
				//newGrayRoot = addAllQuadrants(geometry, newGrayRoot);
			}
			
			//** KEY STEP: add new geometry ** //
			geometryNotMappedAlready = addRecursive (geometry, root, null);
		}
		
		if (geometryNotMappedAlready) {
			if (geometry instanceof City) {
				numCities++;	
			}

			new Debug("draw map" + drawMap());
		}
		
		return geometryNotMappedAlready;
	}
	
	private boolean addRecursive (Shape geometry, pmqtNode current, GrayN parent)
		throws RoadAlreadyMappedException, CityAlreadyMappedException, RoadIntersectionException,
		PartitionTooDeepException {
		
		/*
		if (geometry instanceof City)
			new Debug("Add recursive : " + ((City)geometry).getName());
		*/
		
		boolean geometryNotMappedAlready = true; 
		
		if (current instanceof GrayN) {
			
			GrayN currentGrayN = (GrayN) current;
			Rectangle2D.Float tempRegion;
			
			/*
			new Debug("current gray node x: " + currentGrayN.getCenterX());
			new Debug("current gray node x: " + currentGrayN.getCenterY());
			*/
			
			for (int i = quadrantOne; (i <= quadrantFour) && (geometryNotMappedAlready); i++) {
				
  		    	tempRegion = currentGrayN.getChild(i);
  		    	
  		    	/*
  		    	new Debug("temp region center x: " + tempRegion.getCenterX());
  		    	new Debug("temp region center x: " + tempRegion.getCenterY());
  		    	*/
  		    	
  		    	//new Debug("Region numbeR: " + (i+1));
  		    	
  		    	if (geometry.intersects(tempRegion)) {
  		    		
  		    		//new Debug("add: intersect region: " + (i+1));
  		    		geometryNotMappedAlready = addRecursive(geometry, currentGrayN.getChild(i), currentGrayN);
  		    	}
			}
		}
		else if (current instanceof WhiteN) {

			/*
			System.out.println("whiteN instance -> will add");
			System.out.println("     width/2: " + (parent.getWidth()/2));
			System.out.println("     height/2: " + (parent.getHeight()/2));
			*/
			
			// can throw too deep error //
			if ( ((parent.getWidth()/2) < 1) || 
				 ((parent.getHeight()/2 < 1) )) {
				
				throw new PartitionTooDeepException(" partition limit exceeed");
			}
			
			BlackN newBlackNode = new BlackN(geometry, (int)current.getX(), (int)current.getY(), 
					(int)parent.getWidth()/2, (int)parent.getHeight()/2, current.getLocation());
			
			// add black node to where white node used to be //
			parent.addChild(newBlackNode, current.getLocation()-1);	// -1 because different scale used in addchild		
		}
						
		else if (current instanceof BlackN) {
			//new Debug("BlackN instance");
			
			BlackN oldBlackNode = (BlackN) current;
			
			if (geometry instanceof City) { // if geometry is City //

				City oldCity = (City)oldBlackNode.getCity();
				City city = (City) geometry;	
				
				if (oldCity != null) { // Quadrant contains a City //
					 
					//new Debug("city is:" + city.getName());

					// if new city doesn't not here -> SPLIT //
					if (city.getName().compareTo(oldCity.getName()) != 0 ) { 
						
						// ** key step : split node ** //
						splitNode(oldBlackNode, city, parent);
					
					}
					else { // city already mapped
						geometryNotMappedAlready = false;
						throw new CityAlreadyMappedException("CITY IS ALREADY MAPPED");
					}
				}
				else { // Add new City //
					oldBlackNode.addCity(city);
				}
			}
			else if (geometry instanceof Road) {
				
				validator.checkIntersection(geometry, oldBlackNode);
				
				if (oldBlackNode.addRoad((Road)geometry)) {
					// added another road //
				}
				else { // road already mapped //
					geometryNotMappedAlready = false;
					throw new RoadAlreadyMappedException("ROAD IS ALREADY MAPPED");
				}
			}
			
			if (geometryNotMappedAlready == true) {
				// black node has changed -> check if it's still valid //
				
				//System.out.println("validating black Node");
				
				if (validator.valid(oldBlackNode)) {
					// System.out.println("      black is VALID");
				}
				else {
					//System.out.println("      black is NOT VALID: " + geometry);
					
					// splitting //
					splitUntilValid(oldBlackNode, parent);
				}
			}
		}

		return geometryNotMappedAlready;
	}
	

	private void splitUntilValid(BlackN oldBlackNode, GrayN parent) 
		throws RoadAlreadyMappedException, CityAlreadyMappedException,
		RoadIntersectionException, PartitionTooDeepException {
		
		// swap gray and old black nodes //
		GrayN splitGrayNode = new GrayN((int)oldBlackNode.getX(), (int)oldBlackNode.getY(), 
				                       	(int)oldBlackNode.getWidth(), (int)oldBlackNode.getHeight(), oldBlackNode.getLocation());
		
		
		// put all black node's contents into Gray
		if (oldBlackNode.getCity() != null) {
			addRecursive(oldBlackNode.getCity(), splitGrayNode, null);
		}
		addRoadsToGray(oldBlackNode, splitGrayNode); 
		parent.addChild(splitGrayNode, oldBlackNode.getLocation()-1); // -1 because different scale in addchild
		
	}
	
	private void splitNode(BlackN oldBlackNode, City city, GrayN parent)
		throws RoadAlreadyMappedException, CityAlreadyMappedException, RoadIntersectionException, 
		PartitionTooDeepException {

		// swap gray and old black nodes //
		GrayN splitGrayNode = new GrayN((int)oldBlackNode.getX(), (int)oldBlackNode.getY(), 
				                       	(int)oldBlackNode.getWidth(), (int)oldBlackNode.getHeight(), oldBlackNode.getLocation());
		
		// FIX: put old black node into new gray node //
		if (oldBlackNode.getCity() != null) {
			addRecursive(oldBlackNode.getCity(), splitGrayNode, null);	
		}

		// Put all OLD roads into gray //
		addRoadsToGray(oldBlackNode, splitGrayNode); 
		
		// Put current city into gray node //
		addRecursive(city, splitGrayNode, null);
		
		// add gray node to where black node used to be //
		parent.addChild(splitGrayNode, oldBlackNode.getLocation()-1); // -1 because different scale in addchild
		
	}
	
	private void addRoadsToGray(BlackN oldBlackNode, GrayN splitGrayNode) 
		throws RoadAlreadyMappedException, CityAlreadyMappedException, RoadIntersectionException,
		PartitionTooDeepException {
		
		TreeSet<Road> roads = oldBlackNode.getRoads();
		
		if (roads != null) {

			Iterator it;
			it = roads.iterator();
			Road tempRoad;
							
			while (it.hasNext()) {
				tempRoad = (Road)it.next();
				addRecursive(tempRoad, splitGrayNode, null);
			}
		}
		
	}
	
	private String drawMap() {
		
		// keydraw //
		if (true) {
			if (canvasMap == null) {
				canvasMap = new CanvasPlus("Map Canvas");
			}
		
			drawMapRecursive(root, canvasMap);
			
			canvasMap.setScaleMode(CanvasPlus.SCALE_FIT);
			canvasMap.draw();
			
			try {Thread.sleep(600); }
			catch(InterruptedException ie) {}
		}
		
		return "";
	}
	
	private String redrawMap() {
		if (true) {
			if (canvasMap == null) {
				canvasMap = new CanvasPlus("Map Canvas");
			}
		
			drawMapRecursive(root, canvasMap);
			
			canvasMap.setScaleMode(CanvasPlus.SCALE_FIT);
			canvasMap.draw();
			
			try {Thread.sleep(600); }
			catch(InterruptedException ie) {}
		}
		
		return "";
		
	}
	
	private void drawMapRecursive(pmqtNode n, CanvasPlus canvasMap) {
		
		if (n instanceof BlackN) {
			
			BlackN blackNode = (BlackN) n;
					
			if (blackNode.getCity() != null) {
				
				canvasMap.setColor(Color.red);
				canvasMap.addPoint(blackNode.getCity().getName(), 
								   (int)blackNode.getCity().getX(),
								   (int)blackNode.getCity().getY());		
				
			}
				
			if (blackNode.getRoads() != null) {
				
				TreeSet<Road> roads = blackNode.getRoads();
				Iterator it;

				it = roads.iterator();
				Road tempRoad;
								
				while (it.hasNext()) {
					
					tempRoad = (Road)it.next();
				
					canvasMap.setColor(Color.blue);
					canvasMap.addLine(tempRoad.getStart(), tempRoad.getEnd());
					
				}
				
			}
		}
		else if (n instanceof GrayN) {
		
			GrayN grayNode = (GrayN) n;
			
			int x = (int)grayNode.getCenterX();
			int y = (int)grayNode.getCenterY();
			int halfW = (int)(grayNode.getWidth()/2);
			int halfH = (int)(grayNode.getHeight()/2);
	
			canvasMap.setColor(Color.gray);
			canvasMap.addCross(x,y,halfW);
			
			// draw each child
			drawMapRecursive(grayNode.getChild(quadrantOne), canvasMap);
			drawMapRecursive(grayNode.getChild(quadrantTwo), canvasMap);
			drawMapRecursive(grayNode.getChild(quadrantThree), canvasMap);
			drawMapRecursive(grayNode.getChild(quadrantFour), canvasMap);
			
		}
	}
	
	private Point2D.Double getGuideXY(GrayN parent, int quadrant) {
		
		double x = 0;
		double y = 0;
		
		double parentX = parent.getX();
		double parentY = parent.getY();	
		
		double halfWidth = parent.getWidth() / 2;
		double halfHeight = parent.getHeight() / 2;
				
		if (quadrant == quadrantOne) {
			x = parentX;
			y = parentY + halfHeight;
		}
		else if (quadrant == quadrantTwo) {
			x = parentX + halfWidth;
			y = parentY + halfHeight;
		}
		else if (quadrant == quadrantThree) { // same
			x = parentX;
			y = parentY;
		}
		else if (quadrant == quadrantFour) {
			x = parentX + halfWidth;
			y = parentY;
		}
		else {
			new Debug("This can't be good");
		}
		
		return new Point2D.Double(x,y);
	}
	
	public static int findQuadrant(double cityX, double cityY, GrayN guide) {
		
		if (cityX < guide.getCenterX()) {
			//quadrant 3 and 1 
			if (cityY < guide.getCenterY()) {
				// quadrant 3
				return quadrantThree;
			}
			else {
				// quadrant 1
				return quadrantOne;
			}
		}
		
		else {
			// quadrant 4 and 2
			if (cityY < guide.getCenterY()) {
				// quadrant 4
				return quadrantFour;
			}
			else {
				// quadrant 2
				return quadrantTwo;
			}
		}
	}
	
	public boolean remove(Shape geometry) {
		
		if (geometry instanceof Road) {
			String start = ((Road)geometry).getStart().getName();
			String end = ((Road)geometry).getEnd().getName();
			new Debug("REMOVE ROAD: " + start + "-" + end);
		}
		else{
			new Debug("REMOVE CITY: " + ((City)geometry).getName());
		}
		
		boolean removed = false;
						
		if (root instanceof BlackN) { // only ONE CITY 
			
			if (((BlackN)root).getCity().getName().equals(((City)geometry).getName())) {
				root = null;
				removed = true;
			}
		}
		else {
			
			//** KEY STEP: remove geometry ** //
			removed = removeRecursive (geometry, root, null);
			
			if (removed == true) {	//check if root (root has to be Gray)   
				// see if children can be merged
				
				int numWhite = ((GrayN)root).numWhiteChildren();
				
				if (numWhite == 3) { // // can merge

					pmqtNode nonWhiteNode = ((GrayN)root).findNoneWhiteChild();
					
					if (nonWhiteNode instanceof BlackN) {
						root = nonWhiteNode;
					}
					else {} // if gray -> keep partition
				}
				else if (numWhite == 4) { // root has nothing left
					root = null;
				}
			}
		}
		
		if (removed) {
			
			if (geometry instanceof City) 
				numCities--;
		
			new Debug("      REMOVED!!!!!!!!!");

			canvasMap = null;
			drawMap();
		}
		else {
			new Debug("      NOT REMOVED!!!!!");
		}
		
		return removed;
	}
	
	private boolean removeRecursive (Shape geometry, pmqtNode current, GrayN parent) {

		//new Debug("remove recursive:");
		
		boolean removed = false;
		
		if (current instanceof GrayN) { 
			//new Debug("GRAY -----");
			//printANode(current);
			
			Boolean[] changedQuadrants = new Boolean[4];
			Arrays.fill(changedQuadrants, false);
			
			GrayN currentGrayN = (GrayN) current;
			Rectangle2D.Float tempRegion;

			// Check each child/Quadrant for target geometry //
			for (int i = quadrantOne; i <= quadrantFour; i++) {
				
  		    	tempRegion = currentGrayN.getChild(i);
  		    	  		    	
  		    	if (geometry.intersects(tempRegion)) {
  		    		new Debug("intersect region: " + (i+1) + " size: " + tempRegion.getWidth());
  		    		
  		    		if (removeRecursive(geometry, currentGrayN.getChild(i), currentGrayN)) {
  		    			removed = true;
  	  		    		changedQuadrants[i] = true;
  		    		}
  		    	}
			}

			// Removed? Check for Merges //
			for (int i = quadrantOne; i <= quadrantFour; i++) {
				
				if (changedQuadrants[i] == true) {
					
					tempRegion = currentGrayN.getChild(i);
					
					//new Debug("quadrant changed: " + (i+1) + " size: " + tempRegion.getWidth());
					
					pmqtNode modifiedChild = currentGrayN.getChild(i);	
					
					if (modifiedChild instanceof GrayN) {

						GrayN modifiedGrayChild = (GrayN)modifiedChild;
						
						// see if children can be merged
						//new Debug("total cities here: " + (modifiedGrayChild.totalCities());
						
						if (modifiedGrayChild.totalCities() == 1) {
							new Debug("Performing Merge-- quadrant: " + (i+1));
							
							BlackN mergedChild = modifiedGrayChild.mergeChildren();
							
							if (validator.valid(mergedChild)) {
								currentGrayN.addChild(mergedChild, i);	
							}
							else {
								//System.out.println("child not valid -> merge not concluded");
								// child not valid -> no merge -> do nothing
							}
							
						}
					}
					else if (modifiedChild instanceof BlackN) {
						//new Debug("modified child is Black");		
						
						BlackN modifiedBlackChild = (BlackN) modifiedChild;
					
						// if contains no roads and no cities
						if ((modifiedBlackChild.getCity() == null) && (modifiedBlackChild.getRoads().size() == 0)) {
							// transform black into white child							

							WhiteN transformedWhiteChild = new WhiteN((int)modifiedBlackChild.getX(),
													                  (int)modifiedBlackChild.getY(),
													                  (int)modifiedBlackChild.getWidth(),
													                  (int)modifiedBlackChild.getHeight(),
													                  (int)modifiedBlackChild.getLocation());
							
							currentGrayN.addChild(transformedWhiteChild, i);
						}

					}
					else if (modifiedChild instanceof WhiteN) {
						// leaf that just removed a geometry -> no change necessary //
						//new Debug("modified child is white");
					}  
				}
			
			} // end for loop 

		}
		else if (current instanceof WhiteN) {
			//new Debug("white");
			// do nothing //
		}
		else if (current instanceof BlackN) {
			
			new Debug("BLACK ----");
			
			BlackN oldBlackNode = (BlackN) current;
			
			if (geometry instanceof City) { // Removing City //

				City oldCity = (City)oldBlackNode.getCity();
				City city = (City) geometry;	
				
				// see if this Black Node holds targeted City (check city names) //
			 	if (oldCity != null) {
			 		
			 		//new Debug("city being deleted: " + city.getName());
			 		new Debug("numRoadsToCity: " + oldBlackNode.numRoadsToCity(city));
			 		
			 		if ((oldCity.getName().equals(city.getName())) && 
			 			(oldBlackNode.numRoadsToCity(city) == 0)) {
			 			
			 			// remove geometry and return true //
			 			parent.removeCityFromBlack(oldBlackNode.getLocation()-1);
			 			// ** -1 to offset different quadrant system //
			 			
			 			return true;
				 	}
				 	else {
				 		return false;
				 	}
			 	}
			}
			
			else { // Removing Road //
				
				// delete from Road list //
				if (oldBlackNode.deleteRoad((Road)geometry)) {
					//new Debug("A Road is deleted");
					
					removed = true; // deleted target road
				}
				else { // road already mapped //
					//throw new RoadNotMappedException("ROAD NEVER MAPPED");
				}
				
				//new Debug("removed is: " + removed);
				
			}
			
		}
		return removed;
	}
	
	private void printANode(pmqtNode current) {
		
		XMLDocument doc = new XMLDocument(null);;
		XMLElement root_results = new XMLElement("results", doc);
		doc.appendChild(root_results);
		
		printNode(current, root_results);
		
		XMLPrintVisitor.printDocument(doc, System.out);
	}
	
	private void printBlack(String nodeName, BlackN blackNode) {
		
		XMLDocument doc = new XMLDocument(null);;

		int cardinality = 0;
		
		XMLElement black = new XMLElement(nodeName, doc);
		doc.appendChild(black);
		
		black.setAttribute("x", Integer.toString((int)blackNode.getCenterX()));			
		black.setAttribute("y", Integer.toString((int)blackNode.getCenterY()));
		
		if (blackNode.getCity() != null) {

			XMLElement cityNode = new XMLElement("city", black);
			cityNode.setAttribute("name", blackNode.getCity().getName());
			cityNode.setAttribute("x", Integer.toString((int)blackNode.getCity().getX()));
			cityNode.setAttribute("y", Integer.toString((int)blackNode.getCity().getY()));
			cityNode.setAttribute("color", blackNode.getCity().getColor());
			cityNode.setAttribute("radius", Integer.toString((int)blackNode.getCity().getRadius()));
			
			black.appendChild(cityNode);
			cardinality++;
		}

		if (blackNode.getRoads() != null) {

			TreeSet<Road> roads = blackNode.getRoads();
			Iterator it;

			it = roads.iterator();
			Road tempRoad;
							
			while (it.hasNext()) {
				
				tempRoad = (Road)it.next();
				
				XMLElement roadNode = new XMLElement("road", black);
				roadNode.setAttribute("start",tempRoad.getStart().getName());
				roadNode.setAttribute("end",tempRoad.getEnd().getName());
				black.appendChild(roadNode);

				cardinality++;
			}
		}
		
		black.setAttribute("cardinality", Integer.toString(cardinality));
		
		XMLPrintVisitor.printDocument(doc, System.out);
	}
	
	
	
	public Dictionary rangeCitiesSearch(Ellipse2D.Float rangeCircle, Dictionary cityList) {
		
		// Check if circle contains entire map //
		
		/*
		Rectangle2D.Float entireMap = new Rectangle2D.Float(0, 0, spatialWidth, spatialHeight);
		if ((rangeCircle.contains((Rectangle2D)entireMap)) && !isEmpty()) {
			new Debug("ENTIRE MAP CONTAINED");
			
			cityList = NameDictionary;			
		}
		*/
		
		// Entire map NOT contained -> check each quadrant //
		if (root instanceof GrayN) {
			cityList = rangeCitiesSearchRecursive(rangeCircle, root, cityList);
		}
		else if (root instanceof BlackN) {

			// check if this city is in range
			
			// make this float?
			
			City tempCity = ((BlackN)root).getCity();
			
			double distance = Point2D.distance(tempCity.getX(),tempCity.getY(),
					rangeCircle.getCenterX(),rangeCircle.getCenterY());; 
			
			if (distance <= (rangeCircle.getWidth()/2)) {
				//new Debug("IN RANGE ------------");
								
				//City newCity = new City(tempCity);
				cityList.addCity(tempCity, false);
			}
					
		}
		
		return cityList;
	}
	
	private Dictionary rangeCitiesSearchRecursive(Ellipse2D.Float rangeCircle,
			pmqtNode current, Dictionary cityList) {
		
		//System.out.println("range Cities recursive");
		
		if (current instanceof GrayN) {
			
			// see if circle overlaps with any of the 4 children
			Rectangle2D newRectangle = (Rectangle2D) current;
			
			if (((RectangularShape)rangeCircle).intersects(newRectangle)) { //overlaps)
				
				//System.out.println("Gray Intersection!!!!!");
				
				GrayN currentGrayN = (GrayN) current;
				
				cityList = rangeCitiesSearchRecursive(rangeCircle, 
						currentGrayN.getChild(quadrantOne), cityList);
				
				cityList = rangeCitiesSearchRecursive(rangeCircle, 
						currentGrayN.getChild(quadrantTwo), cityList);
				
				cityList = rangeCitiesSearchRecursive(rangeCircle, 
						currentGrayN.getChild(quadrantThree), cityList);
				
				cityList = rangeCitiesSearchRecursive(rangeCircle, 
						currentGrayN.getChild(quadrantFour), cityList);
		
			}
			else { // circle outside the map
			}
		}
		else if (current instanceof BlackN) {
			
			City tempCity = ((BlackN)current).getCity();
			
			if (tempCity != null) {
				//System.out.println("temp city is: " + tempCity.getName());
				double distance = Point2D.distance(tempCity.getX(),tempCity.getY(),
						rangeCircle.getCenterX(),rangeCircle.getCenterY());; 
			
						/*
				System.out.println("range x: " + rangeCircle.getCenterX() + ", y: " + rangeCircle.getCenterY());
				System.out.println("distance is: " + distance);
				System.out.println("range circle is: " + (rangeCircle.getWidth()/2));
						*/
						
				if (distance <= (rangeCircle.getWidth()/2)) {
					cityList.addCity(tempCity, false);
				}	
			}
			else { } // black node without city, ignore
			
		}
		
		return cityList;
	}
	
	public Shape findNearest(Point2D.Float queryPoint, boolean findCity) {
		
		City nearestCity = new City();
		Road nearestRoad;
		
		if ((numCities == 1) && (findCity == true)) {
			nearestCity = ((BlackN)root).getCity();
		}
		else {
			//int quadrant = findQuadrant(queryPoint.getX(), queryPoint.getY(), (GrayN)root);
			
			pmqtNodeComparator myNodeComp = new pmqtNodeComparator(queryPoint.getX(),
					queryPoint.getY());
			PriorityQueue<pmqtNode> myQueue = new PriorityQueue<pmqtNode> (10, myNodeComp);
			
			GrayN grayRoot = (GrayN) root;

			myQueue = enqueue(myQueue, grayRoot);
			
			while (!myQueue.isEmpty()) {
			
				//new Debug("queue size: " + myQueue.size());
				
				pmqtNode currentNode = myQueue.poll();
 				
				if (currentNode instanceof BlackN) {
					
					BlackN currentBlackN = (BlackN) currentNode;
					
					//new Debug("A black node, should I go on?");
					//new Debug("name is: " + currentBlackN.getCity().getName());
					
					if (findCity == true) { // find nearest City
						nearestCity = currentBlackN.getCity();
						
						if (nearestCity != null) {
							return nearestCity;
						}	
					}
					else { // find nearest Road
						nearestRoad = currentBlackN.getRoads().first(); 
							
						if (nearestRoad != null) {
							return nearestRoad;
						}
					}
					
				}
				else if (currentNode instanceof GrayN) {
					
					//new Debug("Gray Node");
					
					GrayN currentGrayN = (GrayN) currentNode;
					myQueue = enqueue(myQueue, currentGrayN);
				}
				
			}
			
		}
				
		return nearestCity;
	}

	private PriorityQueue<pmqtNode> enqueue (PriorityQueue<pmqtNode> myQueue, GrayN grayBlock) {
		
		myQueue = addToQueue(myQueue, grayBlock, quadrantOne);
		myQueue = addToQueue(myQueue, grayBlock, quadrantTwo);
		myQueue = addToQueue(myQueue, grayBlock, quadrantThree);
		myQueue = addToQueue(myQueue, grayBlock, quadrantFour);

		return myQueue;
	}
	
	private PriorityQueue<pmqtNode> addToQueue (PriorityQueue<pmqtNode> myQueue, GrayN grayBlock, 
			int quadrant) {
		
		pmqtNode n = grayBlock.getChild(quadrant);
		
		if ( (n instanceof BlackN) || (n instanceof GrayN)) {
			myQueue.add(n);
		}

		return myQueue;
	}
	
	public boolean isEmpty() {
		
		//* overriding AbrstractMap.isEmpty() //
		if (root == null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void clear() {
		if (!isEmpty()) {
			root = null;
			numCities = 0;
		}
	}
	
	public void printTree(XMLElement success) {
		
		XMLElement command = new XMLElement ("command", success);
		command.setAttribute("name", "printPMQuadtree");
		success.appendChild(command);
		
		XMLElement parameters = new XMLElement ("parameters", success);
		success.appendChild(parameters);
		
		XMLElement output = new XMLElement ("output", success);
		success.appendChild(output);
		
			XMLElement quadtree = new XMLElement ("quadtree", output);
			quadtree.setAttribute("order", validator.PMOrder());
			output.appendChild(quadtree);
		
			printNode(root, quadtree);
	}
	
	private void printNode(pmqtNode n, XMLElement xmlParent) {
		
		if (n instanceof WhiteN) {
			XMLElement white = new XMLElement("white", xmlParent);
			xmlParent.appendChild(white);
		}
		else if (n instanceof BlackN) {
			
			int cardinality = 0;
			
			BlackN blackNode = (BlackN) n;
			
			XMLElement black = new XMLElement("black", xmlParent);
						
			//add on - remove later//
			/*
			black.setAttribute("x", Integer.toString((int)blackNode.getCenterX()));			
			black.setAttribute("y", Integer.toString((int)blackNode.getCenterY()));
			*/
			
			if (blackNode.getCity() != null) {

				XMLElement cityNode = new XMLElement("city", black);
				cityNode.setAttribute("name", blackNode.getCity().getName());
				cityNode.setAttribute("x", Integer.toString((int)blackNode.getCity().getX()));
				cityNode.setAttribute("y", Integer.toString((int)blackNode.getCity().getY()));
				cityNode.setAttribute("color", blackNode.getCity().getColor());
				cityNode.setAttribute("radius", Integer.toString((int)blackNode.getCity().getRadius()));
				
				black.appendChild(cityNode);
				
				cardinality++;
				
			}
			
			if (blackNode.getRoads() != null) {

				TreeSet<Road> roads = blackNode.getRoads();
				Iterator it;

				it = roads.iterator();
				Road tempRoad;
								
				while (it.hasNext()) {
					
					tempRoad = (Road)it.next();
					
					XMLElement roadNode = new XMLElement("road", black);
					roadNode.setAttribute("start",tempRoad.getStart().getName());
					roadNode.setAttribute("end",tempRoad.getEnd().getName());
					black.appendChild(roadNode);

					cardinality++;
				}
				
				
			}
			
			black.setAttribute("cardinality", Integer.toString(cardinality));
			
			xmlParent.appendChild(black);
		}
		else if (n instanceof GrayN) {
			
			GrayN grayNode = (GrayN) n;
			
			XMLElement gray = new XMLElement("gray", xmlParent);
			gray.setAttribute("x", Integer.toString((int)grayNode.getCenterX()));			
			gray.setAttribute("y", Integer.toString((int)grayNode.getCenterY()));
			
			
			printNode(grayNode.getChild(quadrantOne), gray);
			printNode(grayNode.getChild(quadrantTwo), gray);
			printNode(grayNode.getChild(quadrantThree), gray);
			printNode(grayNode.getChild(quadrantFour), gray);
			
			xmlParent.appendChild(gray);
			
		}
	}
	
	@Override
	public Set entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

}

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import xml.InvalidXMLException;
import xml.XMLDocumentType;
import xml.XMLDocumentValidator;
import xml.XMLParser;

import xml.XMLDocument;
import xml.XMLPrintVisitor;
import xml.XMLElement;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class Main {

	private PrintStream out = System.out;		
	
	private Dictionary NameDictionary;
	
	private CityCoordinateComparator coorComp;
	private Dictionary CoorDictionary;
			
	// part 4
	private BPDictionary BPNameDictionary;
	// end part4
	
	private CityNameComparator nameComp;
	private AdjacencyList AdList;	
	
	private Document d;
	private NodeList commandList; 
	private XMLDocumentValidator validator; 
	private XMLDocumentType dtd; 
	
	private XMLDocument resultsDoc;
	private Element root_results;		
	
	private boolean autoGraph;

	// part 2 //
	
	private PMQuadTree myMap;
	private int spatialHeight;
	private int spatialWidth;
	
	// part 3 //
	
	private int pmOrder;
	private int bpOrder;
	private int leafOrder;

	
	// Function Declarations /////////////////////////////////
	
	private Main() {
	
		nameComp = new CityNameComparator();
		AdList = new AdjacencyList(nameComp);
		
		d = null;
		commandList = null;
		validator = new XMLDocumentValidator(XMLDocumentValidator.FAIL_THROW_EXCEPTION);
		
		resultsDoc = new XMLDocument(null);
		root_results = resultsDoc.createElement("results");
		resultsDoc.appendChild(root_results);
		
		autoGraph = false;
			
	}

	public static void main(String[] args) {
	
		Main main = new Main();

		main.createDocument();     // Create Parsed Document // 
					
		main.processCommands();    // Process each command //

		main.printDocument();      // Prints Results //
		
	}
	
	private void createDocument() {
		
		try {
			d = XMLParser.parseDocument("my.input");
		} catch (IOException e1) {
			out.println("<undefinedError/>");
			System.exit(1);
		} catch (SAXException e1) {
			out.println("<undefinedError/>");
			System.exit(1);
		}
		
		// Validate Document //
		dtd = (XMLDocumentType)d.getDoctype();

		Element commandsElement = d.getDocumentElement();		
		fullValidate(commandsElement);
		
		commandList = d.getDocumentElement().getChildNodes(); // List of Commands //
		
		processCommandsElement();
	}
	
	private void processCommandsElement() {

		if (d.getDocumentElement().getAttribute("autoGraph").equals("true"))
			autoGraph = true;	

		spatialWidth = validateInteger(d.getDocumentElement(), "spatialWidth");
		spatialHeight = validateInteger(d.getDocumentElement(), "spatialHeight");
		
		pmOrder = validateInteger(d.getDocumentElement(), "pmOrder");
		leafOrder = validateInteger(d.getDocumentElement(), "leafOrder");
		bpOrder = validateInteger(d.getDocumentElement(), "bpOrder");
		
		if ((leafOrder <= 0) || (bpOrder <= 2)) {
			out.println("<fatalError/>");
			System.exit(1);
		}

		BPNameDictionary = new BPDictionary(bpOrder, leafOrder);
		NameDictionary = new Dictionary();
		
		coorComp = new CityCoordinateComparator();
		CoorDictionary = new Dictionary(coorComp);
		
		switch (pmOrder){
		case 3:
			myMap = new PMQuadTree(spatialWidth, spatialHeight, 
					new PM3Validator());
			break;
		case 2:
			myMap = new PMQuadTree(spatialWidth, spatialHeight, 
					new PM2Validator());
			break;
		case 1: 
			myMap = new PMQuadTree(spatialWidth, spatialHeight, 
					new PM1Validator());
			break;
		default:
			System.out.println("uh oh");
		}
		
		
	}
	
	private void processCommands() {
								
		for (int i = 0; i < commandList.getLength(); ++i) {
		
			Node command = commandList.item(i); 
			
			// Validate Command/////////////////////////////
					
			fullValidate(command);
			
			// process command /////////////////////////////////
		
			if (command.getNodeName().equals("createCity")) {
				runCreateCity(command);
			}
			else if (command.getNodeName().equals("deleteCity")) {
				runDeleteCity(command);
			}	
			else if (command.getNodeName().equals("shortestPath")) {
				runShortestPath(command, false);
			}
			else if (command.getNodeName().equals("shortestPathAnnotated")) {
				runShortestPath(command, true);
			}
			else if (command.getNodeName().equals("listCities")) {
				runListCities(command);						
			}
			else if (command.getNodeName().equals("clearAll")) {
		   		runClearAll();
		   	}
			else if (command.getNodeName().equals("mapCity")) {
				runMapCity(command);
			}
			else if (command.getNodeName().equals("unmapCity")) {
				runUnmapCity(command);
			}
			else if (command.getNodeName().equals("mapRoad")) {
				runMapRoad(command);
			}
			else if (command.getNodeName().equals("unmapRoad")) {
				runUnmapRoad(command);
			}
			else if (command.getNodeName().equals("rangeCities")) {
				runRangeCities(command);
			}
			else if (command.getNodeName().equals("rangeRoads")) {
				runRangeRoads(command);
			}
			else if (command.getNodeName().equals("nearestCity")) {
				runNearestCity(command);
			}
			else if (command.getNodeName().equals("nearestRoad")) {
				runNearestRoad(command);
			}
			else if (command.getNodeName().equals("nearestCityToRoad")) {
				runNearestCityToRoad(command);
			}
			else if (command.getNodeName().equals("printPMQuadtree")) {
				runPrintPMQuadtree();
			}
			else if (command.getNodeName().equals("printBPTree")) {
				runPrintBPTree();
			}
			else if (command.getNodeName().equals("nameRange")) {
				runNameRange(command);
			}
			else {
				out.println("<undefinedError/>");
			}
		}		
	}
		
	private void fullValidate(Node node) {
		
		try {		
			validator.valid(node,dtd);		
		} catch (InvalidXMLException e) {
			
			if (e == InvalidXMLException.UNDEFINED_ELEMENT) {
				out.println("<fatalError/>");
			}
			else if (e == InvalidXMLException.UNKNOWN_ATTRIBUTE) {
				out.println("<fatalError/>");
			}
			else if (e == InvalidXMLException.MISSING_REQUIRED_ATTRIBUTE) {
				out.println("<fatalError/>");	
			}
			else { // when the XMLDocumentValidator breaks down
				out.println("<fatalError/>");
			}
						
			System.exit(1);		
		}		
		
		String nodeName = node.getNodeName();
				
		if (nodeName.equals("createCity")) {
			validateString(node, "name");
			validateInteger((Element)node, "x");
			validateInteger((Element)node, "y");
			validateInteger((Element)node, "radius");
		}
		else if (nodeName.equals("deleteCity")) {
			validateString(node, "name");
		}
		else if (nodeName.equals("shortestPath")) {
			validateString(node, "start");
			validateString(node, "end");
		}
		else if (nodeName.equals("shortestPathAnnotated")) {
			validateString(node, "start");
			validateString(node, "end");
		} 
		else if (nodeName.equals("clearAll")) {
			// do nothing //
		}
		else if (nodeName.equals("listCities")) {
			// do nothing //
		}
		else if (nodeName.equals("commands")) {
			// do nothing //
		}
		else if (nodeName.equals("mapCity")) {
			validateString(node, "name");
		}
		else if (nodeName.equals("unmapCity")) {
			validateString(node, "name");
		}
		else if (nodeName.equals("mapRoad")) {
			validateString(node, "start");
			validateString(node, "end");
		}
		else if (nodeName.equals("unmapRoad")) {
			validateString(node, "start");
			validateString(node, "end");
		}
		else if (nodeName.equals("rangeCities")) {
			validateInteger((Element)node, "x");
			validateInteger((Element)node, "y");
			// radius can be a float
		}
		else if (nodeName.equals("rangeRoads")) {
			validateInteger((Element)node, "x");
			validateInteger((Element)node, "y");
		}
		else if (nodeName.equals("nearestCity")) {
			validateInteger((Element)node, "x");
			validateInteger((Element)node, "y");
		}
		else if (nodeName.equals("nearestRoad")) {
			validateInteger((Element)node, "x");
			validateInteger((Element)node, "y");
		}
		else if (nodeName.equals("nearestCityToRoad")) {
			validateString(node, "start");
			validateString(node, "end");
		}
		else if (nodeName.equals("printPMQuadtree")) {
			// do nothing //
		}
		else if (nodeName.equals("printBPTree")) {
			// do nothing //
		}
		else if (nodeName.equals("nameRange")) {
			validateString(node, "start");
			validateString(node, "end");
		}
		else {
			
			out.println("<undefinedError1/>" + nodeName);
			System.exit(1);
		}
		
	}
		
	private void validateString(Node node, String attrName) {
		
		String value = node.getAttributes().getNamedItem(attrName).getNodeValue();
		
		if (! value.matches("[_a-zA-Z][_a-zA-Z0-9]*")) {
			out.println("<fatalError/>");
			System.exit(1);
		}		
				
	}
	
	private int validateInteger(Element node, String attrName) {
		
		String value = null;
		
		try {
			value = node.getAttributes().getNamedItem(attrName).getNodeValue();
		}
		catch (NullPointerException exp) {
			out.println("<undefinedError/>");
			System.exit(1);
		}
		
		int intValue = 0;
		
		try {
			intValue = Integer.parseInt(value);
		}
		catch (NumberFormatException exp) {
			out.println("<fatalError/>");
			System.exit(1);
		}
		
		return intValue;
	}	
	
	private void runCreateCity(Node command) { 
	    					
		City tempCity = getCity(command);
		
		// if name already exists //
		if (NameDictionary.containsKey(tempCity.getName())) {
			createCityError("duplicateCityName", tempCity);																	
		}

		// if coordiante already exists //
		else if (CoorDictionary.containsKey(new Coordinate((int)tempCity.getX(),
				(int)tempCity.getY()))) {
			createCityError("duplicateCityCoordinates", tempCity);		
		}
		
		// Add city to Dictionaries
		else {
			
			CoorDictionary.addCity(tempCity, true);
			NameDictionary.addCity(tempCity, false);
			
			BPNameDictionary.addCity(tempCity, false);
			
			createCitySuccess(tempCity);
			
			//System.out.println("added city: " + tempCity.getName());
		}
	}
	
	private City getCity(Node command) {
		
		// Break down Command //
		String name_in = command.getAttributes().
			getNamedItem("name").getNodeValue();
		int x_in = Integer.parseInt(command.getAttributes().
			getNamedItem("x").getNodeValue());
		int y_in = Integer.parseInt(command.getAttributes().
			getNamedItem("y").getNodeValue());
		
		int radius_in =	Integer.parseInt(command.getAttributes().
			getNamedItem("radius").getNodeValue());
		String color_in = command.getAttributes().
			getNamedItem("color").getNodeValue();		
		
		return new City(name_in, x_in, y_in, radius_in, color_in);
	}
	
	private void createCityError(String errorName, City city) {
		
		XMLElement errorN = new XMLElement("error", root_results);
	    errorN.setAttribute("type", errorName);
	    root_results.appendChild(errorN);
	    
		XMLElement commandN = new XMLElement("command", errorN);
		commandN.setAttribute("name", "createCity");
		errorN.appendChild(commandN);
		
		XMLElement parametersN = new XMLElement("parameters", errorN);
		errorN.appendChild(parametersN);
		
			XMLElement param1N = new XMLElement("name", parametersN);
			param1N.setAttribute("value", city.getName());
			parametersN.appendChild(param1N);
			
			XMLElement param2N = new XMLElement("x", parametersN);
			param2N.setAttribute("value",Integer.toString((int)city.getX()));
			parametersN.appendChild(param2N);
			
			XMLElement param3N = new XMLElement("y", parametersN);
			param3N.setAttribute("value",Integer.toString((int)city.getY()));
			parametersN.appendChild(param3N);
			
			XMLElement param4N = new XMLElement("radius", parametersN);
			param4N.setAttribute("value",Integer.toString((int)city.getRadius()));
			parametersN.appendChild(param4N);
			
			XMLElement param5N = new XMLElement("color", parametersN);
			param5N.setAttribute("value",city.getColor());
			parametersN.appendChild(param5N);		
		
	}
	
	private void createCitySuccess(City city) {		
		
		XMLElement success = new XMLElement("success", root_results);
		root_results.appendChild(success);
		
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name","createCity");
		success.appendChild(comm);
		
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement name = new XMLElement("name", param);
			name.setAttribute("value",city.getName());
			param.appendChild(name);
			XMLElement x = new XMLElement("x", param);
			x.setAttribute("value",Integer.toString((int)city.getX()));
			param.appendChild(x);
			XMLElement y = new XMLElement("y", param);
			y.setAttribute("value",Integer.toString((int)city.getY()));
			param.appendChild(y);
			XMLElement radius = new XMLElement("radius", param);
			radius.setAttribute("value",Integer.toString((int)city.getRadius()));
			param.appendChild(radius);
			XMLElement color = new XMLElement("color", param);
			color.setAttribute("value",city.getColor());
			param.appendChild(color);

		
		XMLElement output = new XMLElement("output", success);
		if (autoGraph == true) {
			AdList.createRoads(NameDictionary,city,output);						
		}
		success.appendChild(output);
					
	}
		
	private void runDeleteCity(Node command) { 
		
		// Break down Command //
		String name_in = command.getAttributes().getNamedItem("name").getNodeValue();

		//System.out.println("run delete city: " + name_in);
		
		if (NameDictionary.containsKey(name_in)) {
			
			City tempCity = (City) NameDictionary.get(name_in);
			
			if (!AdList.containsKey(tempCity)) { // if city not mapped (not in adlist //
			
				CoorDictionary.deleteCity(tempCity, true);
				NameDictionary.deleteCity(tempCity, false);

				deleteCitySuccess(tempCity);
			}
			else {
				badCityError("deleteCity", "cityIsMapped", name_in);
			}
		}
		else {
			badCityError("deleteCity", "nonExistentCity", name_in);
		}
		
		if (BPNameDictionary.containsKey(name_in)) {
			City tempCity = (City) BPNameDictionary.get(name_in);
			BPNameDictionary.deleteCity(tempCity, false);	
		}
		
	}
	
	private void badCityError(String commandName, String errorName, String cityName) {
		
		XMLElement errorN = new XMLElement("error", root_results);
		errorN.setAttribute("type", errorName);
		root_results.appendChild(errorN);
		
		XMLElement commandN = new XMLElement("command", errorN);
		commandN.setAttribute("name", commandName);
		errorN.appendChild(commandN);
		
		XMLElement parametersN = new XMLElement("parameters", errorN);
		errorN.appendChild(parametersN);
		
			XMLElement param1N = new XMLElement("name", parametersN);
			param1N.setAttribute("value", cityName);
			parametersN.appendChild(param1N);
		
	}
	
	private void deleteCitySuccess(City city) {
				
		XMLElement success = new XMLElement("success", root_results);
		root_results.appendChild(success);
		
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name","deleteCity");
		success.appendChild(comm);
		
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement name = new XMLElement("name", param);
			name.setAttribute("value",city.getName());
			param.appendChild(name);
					
		XMLElement output = new XMLElement("output", success);
		
		if (autoGraph == true) {
			AdList.deleteRoads(city);						
		}
		
		if ( myMap.remove(city) == true) { // if city found -> it will be removed 
			XMLElement cityUnmapped = new XMLElement("cityUnmapped", output);
		
			cityUnmapped.setAttribute("name",city.getName());
			cityUnmapped.setAttribute("x",Integer.toString((int)city.getX()));
			cityUnmapped.setAttribute("y",Integer.toString((int)city.getY()));
			cityUnmapped.setAttribute("radius",Integer.toString(city.getRadius()));
			cityUnmapped.setAttribute("color",city.getColor());

			output.appendChild(cityUnmapped);
		}
				
		success.appendChild(output);
		
	}
		
	private void runShortestPath(Node command, boolean annotate) {
		
		String start_in = command.getAttributes().getNamedItem("start").getNodeValue();
		String end_in = command.getAttributes().getNamedItem("end").getNodeValue();
		
		DijkstraManager DManager = new DijkstraManager(AdList); 
		
		City startCity = (City)NameDictionary.get(start_in);
		City endCity = (City)NameDictionary.get(end_in);
					
		if (startCity == null) { 
			shortestPathError("nonExistentStart", start_in, end_in, annotate);
		}
		else if (endCity == null) {
			shortestPathError("nonExistentEnd", start_in, end_in, annotate);
		}
    	else if (AdList.size() == 0) {
			shortestPathError("noPathExists", start_in, end_in, annotate);
		}
    	else if (!AdList.containsKey(startCity) || !AdList.containsKey(endCity)) {
    		shortestPathError("noPathExists", start_in, end_in, annotate);
    	}
		// if all is well, execute Shortest Path Algorithm 
		else {

			boolean noPath = false;
			
			DManager.execute(startCity, endCity);
			XMLElement success = new XMLElement("success", root_results);
			
			try {
				DManager.shortestPathSuccess(startCity, endCity, success, annotate);
			} catch (NoPathException e) {
				shortestPathError("noPathExists", start_in, end_in, annotate);
				noPath = true;
			}

			if (noPath == false) { // there is a path
				root_results.appendChild(success);
			}
		}
		
	}
			
	private void shortestPathError(String errorName, String startName, String endName, 
			boolean annotate) {
			
		XMLElement errorN = new XMLElement("error", root_results);
		errorN.setAttribute("type", errorName);
		root_results.appendChild(errorN);
		
		XMLElement commandN = new XMLElement("command", errorN);
		
		if (annotate == true) {
			commandN.setAttribute("name", "shortestPathAnnotated");	
		}
		else {
			commandN.setAttribute("name", "shortestPath");
		}
		
		errorN.appendChild(commandN);
		
		XMLElement parametersN = new XMLElement("parameters", errorN);
		errorN.appendChild(parametersN);
		
			XMLElement param1N = new XMLElement("start", parametersN);
			param1N.setAttribute("value", startName);
			parametersN.appendChild(param1N);
			
			XMLElement param2N = new XMLElement("end", parametersN);
			param2N.setAttribute("value", endName);
			parametersN.appendChild(param2N);
			
	}
	
	private void runListCities(Node command) {
		
		String sortValue = command.getAttributes().getNamedItem("sortBy").getNodeValue();
	
		// Check for empty Tree //
		if (NameDictionary.isEmpty()) {
			
			XMLElement errorN = new XMLElement("error", root_results);
			errorN.setAttribute("type","noCitiesToList");
			root_results.appendChild(errorN);
			
			XMLElement commandN = new XMLElement("command", errorN);
			commandN.setAttribute("name", "listCities");
			errorN.appendChild(commandN);
			
			XMLElement parametersN = new XMLElement("parameters", errorN);
			errorN.appendChild(parametersN);
			
				XMLElement param1N = new XMLElement("sortBy", parametersN);
				if (sortValue.equals("coordinate")){
					param1N.setAttribute("value","coordinate");	
				}
				else {
					param1N.setAttribute("value","name");
				}
				parametersN.appendChild(param1N);
		}
		
		// Print All Cities if tree is not empty //
		else {
			
			XMLElement success = new XMLElement("success", root_results);
			root_results.appendChild(success);		
			
			if (sortValue.equals("coordinate")) {
				CoorDictionary.listCities(true, success);	
			}
			else {
				NameDictionary.listCities(false, success);
			}				
		}
	}
	
	private void runClearAll() {
		
		NameDictionary.clear();
   		CoorDictionary.clear();
   		AdList.clear();
   		myMap.clear();
   				
   		XMLElement success = new XMLElement("success", root_results);
		root_results.appendChild(success);
		
		XMLElement command = new XMLElement("command", success);
		command.setAttribute("name", "clearAll");
		success.appendChild(command);
		
		XMLElement parameters = new XMLElement("parameters", success);
		success.appendChild(parameters);
		
		XMLElement output = new XMLElement("output", success);
   		success.appendChild(output);
	
	}
	
	private void runMapCity(Node command) {

		// Break down Command //
		String name_in = command.getAttributes().getNamedItem("name").getNodeValue();
			
		if (NameDictionary.containsKey(name_in)) {
						
			City tempCity = (City)NameDictionary.get(name_in);

			if ((tempCity.getX() < spatialWidth) && (tempCity.getX() >= 0) &&
				(tempCity.getY() < spatialHeight) && (tempCity.getY() >= 0)) {
				
				try {
					if (myMap.add(tempCity)) { 
						// added succesfully //
						mappingSuccess(tempCity, "mapCity");
					}
				} catch (RoadAlreadyMappedException e) {
					// This shouldn't happen !! //
					// e.printStackTrace();
				} catch (CityAlreadyMappedException e) {
					badCityError("mapCity", "cityOutOfBounds", name_in);
				} catch (RoadIntersectionException e) {
					// This shouldn't happen 1! //
					// e.printStackTrace();
				} catch (PartitionTooDeepException e) {
					badCityError("mapCity", "pmPartitionLimitExceeded", name_in);
				}
			}
			else {
				
			}
		}
		else {
			badCityError("mapCity", "nameNotInDictionary", name_in);
		}
	}
	
	private void mappingSuccess(City city, String commandName) {
		
		XMLElement success = new XMLElement("success", root_results);
		root_results.appendChild(success);
		
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name",commandName);
		success.appendChild(comm);
		
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement name = new XMLElement("name", param);
			name.setAttribute("value",city.getName());
			param.appendChild(name);
					
		XMLElement output = new XMLElement("output", success);
		success.appendChild(output);
	}
		
	private void runUnmapCity(Node command) {
		
		String name_in = command.getAttributes().getNamedItem("name").getNodeValue();
		
		if (NameDictionary.containsKey(name_in)) {
			
			City tempCity = (City)NameDictionary.get(name_in);
			
			if ( myMap.remove(tempCity) == true) { // if city found -> it will be killed 
				mappingSuccess(tempCity, "unmapCity");
			}
			else {
				badCityError("unmapCity", "cityNotMapped", name_in);
			}
		}
		else {
			badCityError("unmapCity", "nameNotInDictionary", name_in);
		}
		
	}
		
	private void runMapRoad(Node command) {
		
		String startName = command.getAttributes().getNamedItem("start").getNodeValue();
		String endName = command.getAttributes().getNamedItem("end").getNodeValue();
		
		if (!NameDictionary.containsKey(startName)) {
			badRoadError("mapRoad", "startPointDoesNotExist", startName, endName);
		}
		else if (!NameDictionary.containsKey(endName)) {
			badRoadError("mapRoad", "endPointDoesNotExist", startName, endName);
		}
		else if (false) { // road intersect error
			// probably not deal with here //
		}
		else { 
		
			City startCity = (City)NameDictionary.get(startName);
			City endCity = (City)NameDictionary.get(endName);

		    if ((startCity.getX() <= spatialWidth) && (startCity.getX() >= 0) &&
				(startCity.getY() <= spatialHeight) && (startCity.getY() >= 0) && 
				(endCity.getX() <= spatialWidth) && (endCity.getX() >= 0) &&
				(endCity.getY() <= spatialHeight) && (endCity.getY() >= 0)) { 
				
		    	// everything is ok, map road
		    	
		    	// add the Start / End Cities, if they are already there -> no effect //
				
				try {
					myMap.add(startCity);
				} catch (RoadAlreadyMappedException e) { // ignore 
				} catch (CityAlreadyMappedException e) { // ignore
				} catch (RoadIntersectionException e) { // ignore
				} catch (PartitionTooDeepException e) {
					badRoadError("mapRoad", "pmPartitionLimitExceeded", startName, endName);
					// just remove start city if possible //
					myMap.remove(startCity);
					return;
				}
				
				try {
					myMap.add(endCity);
				} catch (RoadAlreadyMappedException e) { // ignore 
				} catch (CityAlreadyMappedException e) { // ignore
					//System.out.println("main - city already mapped");
				} catch (RoadIntersectionException e) { // ignore
				} catch (PartitionTooDeepException e) {
					badRoadError("mapRoad", "pmPartitionLimitExceeded", startName, endName);

					// remove start and end city if possible //
					myMap.remove(startCity);
					myMap.remove(endCity);
					return;
				}
				
				Road road = new Road(startCity, endCity);
				
				try {
					if (myMap.add(road)) {				//**KEY STEP**//
						// added succesfully //
						roadSuccess("mapRoad", startName, endName);
						
						// update AdjacencyList //
						AdList.addRoad(startCity, endCity);
					}
				} catch (RoadAlreadyMappedException e) {
					// road already mapped //
					badRoadError("mapRoad", "roadMapped", startName, endName);
					
				} catch (CityAlreadyMappedException e) {
					// shouldn't happen //
				} catch (RoadIntersectionException e) {
					// road Intersect previous road //
					badRoadError("mapRoad", "roadIntersectsAnotherRoad", startName, endName);
					myMap.remove(road);
					
					// remove start and end city if possible //
					myMap.remove(startCity);
					myMap.remove(endCity);
				} catch (PartitionTooDeepException e) {
					badRoadError("mapRoad", "pmPartitionLimitExceeded", startName, endName);
					myMap.remove(road);
					
					// remove start and end city if possible //
					myMap.remove(startCity);
					myMap.remove(endCity);
				}
				
			}
			
			else {
				// end city out of bounds //
				badRoadError("mapRoad", "roadOutOfSpatialBounds", startName, endName);
			}
		}
		
		//System.out.println("map road : adjacency list size: " + AdList.size());
	}
		
	private void runUnmapRoad(Node command) {
		
		String startName = command.getAttributes().getNamedItem("start").getNodeValue();
		String endName = command.getAttributes().getNamedItem("end").getNodeValue();
		
		if (!NameDictionary.containsKey(startName)) {
			badRoadError("unmapRoad", "startPointDoesNotExist", startName, endName);
		}
		else if (!NameDictionary.containsKey(endName)) {
			badRoadError("unmapRoad", "endPointDoesNotExist", startName, endName);
		}
		else { 
		
			City startCity = (City)NameDictionary.get(startName);
			City endCity = (City)NameDictionary.get(endName);
			Road road = new Road(startCity, endCity);
			
			if (myMap.remove(road)) {
				myMap.remove(startCity);
				myMap.remove(endCity);
				roadSuccess("unmapRoad", startName, endName);
				
				// update AdjacencyList //
				AdList.deleteRoad(road);
			}
			else {
				badRoadError("unmapRoad", "roadNotMapped", startName, endName);
			}
		}
			
		//System.out.println("unmap road : adjacency list size: " + AdList.size());
	}
	
	private void roadSuccess(String commandName, String start, String end) {
		
		XMLElement success = new XMLElement("success", root_results);
		root_results.appendChild(success);
		
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name",commandName);
		success.appendChild(comm);
		
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement startN = new XMLElement("start", param);
			startN.setAttribute("value", start);
			param.appendChild(startN);
			
			XMLElement endN = new XMLElement("end", param);
			endN.setAttribute("value", end);
			param.appendChild(endN);
			
					
		XMLElement output = new XMLElement("output", success);
		success.appendChild(output);
		
			XMLElement roadN;
			if (commandName.equals("mapRoad"))
					roadN = new XMLElement("roadCreated", param);
			else roadN = new XMLElement("roadDeleted", param);
		
			roadN.setAttribute("start", start);
			roadN.setAttribute("end", end);
			output.appendChild(roadN);
		
	}
	
	private void badRoadError(String commandName, String errorName, String startName, String endName) {
		
		XMLElement errorN = new XMLElement("error", root_results);
		errorN.setAttribute("type", errorName);
		root_results.appendChild(errorN);
		
		XMLElement commandN = new XMLElement("command", errorN);
		commandN.setAttribute("name", commandName);
		errorN.appendChild(commandN);
		
		XMLElement parametersN = new XMLElement("parameters", errorN);
		errorN.appendChild(parametersN);
		
			XMLElement param1N = new XMLElement("start", parametersN);
			param1N.setAttribute("value", startName);
			parametersN.appendChild(param1N);
			
			XMLElement param2N = new XMLElement("end", parametersN);
			param2N.setAttribute("value", endName);
			parametersN.appendChild(param2N);
		
	}
	
	private void runRangeCities(Node command) {
		
		int x = Integer.parseInt(command.getAttributes().getNamedItem("x").getNodeValue());
		int y = Integer.parseInt(command.getAttributes().getNamedItem("y").getNodeValue());
		float radius = Float.parseFloat(command.getAttributes().getNamedItem("radius").getNodeValue());
				
		Ellipse2D.Float rangeCircle = new Ellipse2D.Float(x-radius, y-radius, 
														  2*radius, 2*radius);
		
		Dictionary cityList = new Dictionary();
		cityList = myMap.rangeCitiesSearch(rangeCircle, cityList);
		
		if (!cityList.isEmpty()) { 
			rangeCitySuccess(cityList, rangeCircle);	
		}
		else { // city List empty (no cities in range)
			rangeError("rangeCities", "noCitiesExistInRange", rangeCircle);
		}
	}
	
	private void rangeCitySuccess(Dictionary cityList, Ellipse2D.Float rangeCircle) {
				
		LinkedList myList = new LinkedList();
		Iterator it;
		City tempCity = new City();
		
		myList.addAll(cityList.keySet());
		it = myList.iterator();
		
		XMLElement success = new XMLElement("success", root_results);
		root_results.appendChild(success);
	
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name","rangeCities");
		success.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement x = new XMLElement("x", param);
			x.setAttribute("value", Integer.toString((int)rangeCircle.getCenterX()));
			param.appendChild(x);
		
			XMLElement y = new XMLElement("y", param);
			y.setAttribute("value", Integer.toString((int)rangeCircle.getCenterY()));
			param.appendChild(y);
		
			XMLElement radius = new XMLElement("radius", param);
			radius.setAttribute("value", Integer.toString((int)(rangeCircle.getWidth()/2)));
			param.appendChild(radius);
		
		XMLElement output = new XMLElement("output", success);
		
			XMLElement cityListNode = new XMLElement("cityList", output);
			output.appendChild(cityListNode);
		
			while (it.hasNext()) {
				
				tempCity = (City)cityList.get(it.next());
				
				XMLElement cityNode = new XMLElement("city", cityListNode);
				cityNode.setAttribute("name",tempCity.getName());
				cityNode.setAttribute("x",Integer.toString((int)tempCity.getX()));
				cityNode.setAttribute("y",Integer.toString((int)tempCity.getY()));
				cityNode.setAttribute("radius",Integer.toString(tempCity.getRadius()));
				cityNode.setAttribute("color",tempCity.getColor());
				cityListNode.appendChild(cityNode);
			}
		
		success.appendChild(output);
		
	}
	
	private void rangeError(String commandName, String errorType,
			Ellipse2D.Float rangeCircle) {
		
		XMLElement error = new XMLElement("error", root_results);
		error.setAttribute("type", errorType);
		root_results.appendChild(error);
	
		XMLElement comm = new XMLElement("command", error);
		comm.setAttribute("name", commandName);
		error.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", error);
		error.appendChild(param);
		
			XMLElement x = new XMLElement("x", param);
			x.setAttribute("value", Integer.toString((int)rangeCircle.getCenterX()));
			param.appendChild(x);
		
			XMLElement y = new XMLElement("y", param);
			y.setAttribute("value", Integer.toString((int)rangeCircle.getCenterY()));
			param.appendChild(y);
		
			XMLElement radius = new XMLElement("radius", param);
			radius.setAttribute("value", Integer.toString((int)(rangeCircle.getWidth()/2)));
			param.appendChild(radius);
		
	}
	
	private void runRangeRoads(Node command) {
		
		int x = Integer.parseInt(command.getAttributes().getNamedItem("x").getNodeValue());
		int y = Integer.parseInt(command.getAttributes().getNamedItem("y").getNodeValue());
		float radius = Float.parseFloat(command.getAttributes().getNamedItem("radius").getNodeValue());
				
		Ellipse2D.Float rangeCircle = new Ellipse2D.Float(x-radius, y-radius, 
														  2*radius, 2*radius);
		
		if (autoGraph == true) {

			Dictionary cityList = new Dictionary();
			cityList = myMap.rangeCitiesSearch(rangeCircle, cityList);
			
			TreeSet<Road> roadList = compileRoadList(cityList, false, 0, 0);
				
			if (!roadList.isEmpty()) { 
				rangeRoadSuccess(roadList, rangeCircle);	
			}
			else { // city List empty (no cities in range)
				rangeError("rangeRoads", "noRoadsExistInRange", rangeCircle);
			}	
		}
		else {
			rangeError("rangeRoads", "noRoadsExistInRange", rangeCircle);
		}
		
	}

	private TreeSet<Road> compileRoadList(Dictionary cityList, boolean compareDistance, int x, int y) {
	
		TreeSet<Road> RoadSet;
		
		if (compareDistance) {
			RoadDistanceComparator RComp = new RoadDistanceComparator(x, y);
			RoadSet = new TreeSet<Road>(RComp);
		}
		else {
			RoadComparator RComp = new RoadComparator();
			RoadSet = new TreeSet<Road>(RComp);
		}
		
		City tempCity = new City();
		String cityName = "";
		
		//System.out.println("city list size: " + cityList.size());
		
		LinkedList myList = new LinkedList();
		Iterator it;
		myList.addAll(cityList.keySet());
		it = myList.iterator();
		
		while (it.hasNext()) {
			
			cityName = (String)it.next();	
			//System.out.println("city name: " + cityName);
			
			tempCity = (City)NameDictionary.get(cityName);
			TreeSet<Road> tempRoadSet = AdList.get(tempCity);
			
			//System.out.println("temp road set / collection size: " + tempRoadSet.size());			
			RoadSet = addRoads(tempRoadSet, RoadSet);
		}
		
		//System.out.println("road set size: " + RoadSet.size());
		
		return RoadSet;
	}
	
	private TreeSet<Road> addRoads(TreeSet<Road> adListRoadSet, 
			TreeSet<Road> RoadSet) {
		
		/*
		RoadComparator RComp = new RoadComparator();
		TreeSet<Road> returnRoadSet = new TreeSet<Road>(RComp);
		*/
		
		LinkedList myList = new LinkedList();
		Iterator it;
		myList.addAll(adListRoadSet);
		it = myList.iterator();
		
		Road tempRoad;
		
		while (it.hasNext()) {
			
			tempRoad = (Road)it.next();
		
			// System.out.println("current road: " + tempRoad.getStart().getName());
			
			if (tempRoad.getEnd().compareTo(tempRoad.getStart()) < 0) {
				Road newRoad = new Road(tempRoad.getEnd(), tempRoad.getStart());
				RoadSet.add(newRoad);
			}
			else {
				RoadSet.add(tempRoad);
			}
		}
		
		return RoadSet;
	}
	
	private void rangeRoadSuccess(TreeSet<Road> roadList, Ellipse2D.Float rangeCircle) {
		
		LinkedList myList = new LinkedList();
		Iterator it;
		Road tempRoad;
		
		myList.addAll(roadList);
		it = myList.iterator();
		
		XMLElement success = new XMLElement("success", root_results);
		root_results.appendChild(success);
	
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name","rangeRoads");
		success.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement x = new XMLElement("x", param);
			x.setAttribute("value", Integer.toString((int)rangeCircle.getCenterX()));
			param.appendChild(x);
		
			XMLElement y = new XMLElement("y", param);
			y.setAttribute("value", Integer.toString((int)rangeCircle.getCenterY()));
			param.appendChild(y);
		
			XMLElement radius = new XMLElement("radius", param);
			radius.setAttribute("value", Integer.toString((int)(rangeCircle.getWidth()/2)));
			param.appendChild(radius);
		
		XMLElement output = new XMLElement("output", success);
		
			XMLElement roadListNode = new XMLElement("roadList", output);
			output.appendChild(roadListNode);
		
			while (it.hasNext()) {
				
				tempRoad = (Road)it.next();
				
				XMLElement roadNode = new XMLElement("road", roadListNode);
				roadNode.setAttribute("start",tempRoad.getStart().getName());
				roadNode.setAttribute("end",tempRoad.getEnd().getName());
				roadListNode.appendChild(roadNode);
			}
		
		success.appendChild(output);
		
		
	}
	
	private void runNearestCity(Node command) {
		
		int x = Integer.parseInt(command.getAttributes().getNamedItem("x").getNodeValue());
		int y = Integer.parseInt(command.getAttributes().getNamedItem("y").getNodeValue());

		Point2D.Float queryPoint = new Point2D.Float(x,y);
		
		if (myMap.isEmpty()) {
			nearestError("nearestCity", x, y);		
		}
		else {
			City nearestCity = (City)myMap.findNearest(queryPoint, true);
			nearestCity = checkNearest(x, y, nearestCity);
			nearestCitySuccess(x, y, nearestCity);
		}
	}
	
	private City checkNearest(int x, int y, City city) {
		
		//System.out.println("checking nearest city: " + city.getName());
		
		City nearestCity = new City();
				
		float radius = (float)(Point2D.distance(x,y,city.getX(), city.getY())+ 0.001);
		
		Ellipse2D.Float rangeCircle = new Ellipse2D.Float(x-radius, y-radius, 
				  2*radius, 2*radius);
		
		Dictionary cityList = new Dictionary();
		cityList = myMap.rangeCitiesSearch(rangeCircle, cityList);
		
		//System.out.println("cityList size:" + cityList.size());
		
		if (cityList.size() == 1) { 
		
			nearestCity = city;	
		}
		else { // check it see which one is closest
			
			LinkedList myList = new LinkedList();
			Iterator it;
			myList.addAll(cityList.keySet());
			it = myList.iterator();
			
			String cityName = "";
			City tempCity = new City();
			float lowestRadius = radius; 
			
			while (it.hasNext()) {
						
				cityName = (String)it.next();
				tempCity = (City)NameDictionary.get(cityName);
					
				radius = (float)Point2D.distance(x,y,tempCity.getX(), tempCity.getY());;
				
				if (radius < lowestRadius ) {
					nearestCity = tempCity;
					lowestRadius = radius;
				}
				else if (radius == lowestRadius) {
					// break tie with alphabetical priority
					if (tempCity.getName().compareTo(nearestCity.getName()) < 0) {
						nearestCity = tempCity;
					}
				}
			}
		}
		
		
		
		return nearestCity;
	}
		
	private void nearestCitySuccess(int x, int y, City city) {
		
		XMLElement success= new XMLElement("success", root_results);
		root_results.appendChild(success);
	
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name", "nearestCity");
		success.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement xNode = new XMLElement("x", param);
			xNode.setAttribute("value", Integer.toString(x));
			param.appendChild(xNode);
		
			XMLElement yNode = new XMLElement("y", param);
			yNode.setAttribute("value", Integer.toString(y));
			param.appendChild(yNode);
			
		XMLElement output = new XMLElement("output", success);
		success.appendChild(output);
		
			XMLElement cityNode = new XMLElement("city", output); 
			cityNode.setAttribute("name", city.getName());
			cityNode.setAttribute("x", Integer.toString((int)city.getX()));
			cityNode.setAttribute("y", Integer.toString((int)city.getY()));
			cityNode.setAttribute("color", city.getColor());
			cityNode.setAttribute("radius", Integer.toString((int)city.getRadius()));
			output.appendChild(cityNode);
	}
	
	private void nearestError(String errorName, int x, int y) {
		
		XMLElement error = new XMLElement("error", root_results);
		error.setAttribute("type", "mapIsEmpty");
		root_results.appendChild(error);
	
		XMLElement comm = new XMLElement("command", error);
		comm.setAttribute("name", errorName);
		error.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", error);
		error.appendChild(param);
		
			XMLElement xNode = new XMLElement("x", param);
			xNode.setAttribute("value", Integer.toString(x));
			param.appendChild(xNode);
		
			XMLElement yNode = new XMLElement("y", param);
			yNode.setAttribute("value", Integer.toString(y));
			param.appendChild(yNode);
	}
	
	
	
	private void runNearestRoad(Node command) {
		
		int x = Integer.parseInt(command.getAttributes().getNamedItem("x").getNodeValue());
		int y = Integer.parseInt(command.getAttributes().getNamedItem("y").getNodeValue());

		Point2D.Float queryPoint = new Point2D.Float(x,y);
		
		if (myMap.isEmpty()) {
			nearestError("nearestRoad", x, y);		
		}
		else {
			Road nearestRoad = (Road)myMap.findNearest(queryPoint, false);
			nearestRoad = checkNearestRoad(x, y, nearestRoad);
			nearestRoadSuccess(x, y, nearestRoad);
		}
	}
	
	private Road checkNearestRoad(int x, int y, Road road) {
		
		//System.out.println("checking nearest road: " + road);
		
		Road nearestRoad = road;
		
		// Create the Range Circle // 
		Point2D.Float midPoint = findMidPoint(road);
		float radius = (float)(Point2D.distance(x,y, midPoint.getX(), midPoint.getY())+ 0.001);
		Ellipse2D.Float rangeCircle = new Ellipse2D.Float(x-radius, y-radius, 
				  2*radius, 2*radius);
		
		// Road List //
		RoadComparator RComp = new RoadComparator();
		TreeSet<Road> roadList = new TreeSet<Road>(RComp);
		
		// City List to figure out Road List //
		Dictionary cityList = new Dictionary();
		cityList = myMap.rangeCitiesSearch(rangeCircle, cityList);
		
		//System.out.println("city list size: " + cityList.size());

		if (cityList.size() != 0) {
			roadList = compileRoadList(cityList, true, x, y);
			nearestRoad = roadList.first();
		}
				
		return nearestRoad;
	}
	
	private Point2D.Float findMidPoint(Road road) {
		
		double x = ((road.getStart().getX() + road.getEnd().getX()) / 2);
		double y = ((road.getStart().getY() + road.getEnd().getY()) / 2);
		
		return new Point2D.Float((float)x,(float)y);
	}
	
	private void nearestRoadSuccess(int x, int y, Road road) {
		
		XMLElement success= new XMLElement("success", root_results);
		root_results.appendChild(success);
	
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name", "nearestRoad");
		success.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);
		
			XMLElement xNode = new XMLElement("x", param);
			xNode.setAttribute("value", Integer.toString(x));
			param.appendChild(xNode);
		
			XMLElement yNode = new XMLElement("y", param);
			yNode.setAttribute("value", Integer.toString(y));
			param.appendChild(yNode);
			
		XMLElement output = new XMLElement("output", success);
		success.appendChild(output);
		
			XMLElement roadNode = new XMLElement("road", output); 
			roadNode.setAttribute("start", road.getStart().getName());
			roadNode.setAttribute("end", road.getEnd().getName());
			output.appendChild(roadNode);
	}
	
	private void runNearestCityToRoad(Node command) {
		
		//System.out.println("nearest city to road");
		
		String startName = command.getAttributes().getNamedItem("start").getNodeValue();
		String endName = command.getAttributes().getNamedItem("end").getNodeValue();
		
		if (!NameDictionary.containsKey(startName)) {
			badRoadError("runNearestCityToRoad", "roadIsNotMapped", startName, endName);
		}
		else if (!NameDictionary.containsKey(endName)) {
			badRoadError("runNearestCityToRoad", "roadIsNotMapped", startName, endName);
		}
		else {
			
			City startCity = (City)NameDictionary.get(startName); 
			City endCity = (City)NameDictionary.get(endName);

			// check map empty //
			if (myMap.isEmpty()) {
				badRoadError("runNearestCityToRoad", "mapIsEmpty", startName, endName);
			}
			// check Adlist contains both cities //
			else if (!AdList.containsKey(startCity) || !AdList.containsKey(endCity)) {
				badRoadError("runNearestCityToRoad", "roadIsNotMapped", startName, endName);	
			}
			else if (AdList.size() == 2) {
				
				// only 2 cities and 1 road --> undefined error //
				XMLElement undefinedN = new XMLElement("undefinedError", root_results);
				root_results.appendChild(undefinedN);
			}
			
			else {
			
			}
		}
	}

	
	private void runNameRange(Node command) {
		
		//System.out.println("name ranging");
		
		String startName = command.getAttributes().getNamedItem("start").getNodeValue();
		String endName = command.getAttributes().getNamedItem("end").getNodeValue();
	
		
		BPNameDictionary.listNameRangeCities(startName, endName, root_results);
	
	}
	
	
	
	private void runPrintPMQuadtree() {
		
		if (myMap.isEmpty()) {
			
			XMLElement errorN = new XMLElement("error", root_results);
			errorN.setAttribute("type","mapIsEmpty");
			root_results.appendChild(errorN);
			
			XMLElement commandN = new XMLElement("command", errorN);
			commandN.setAttribute("name", "printPMQuadtree");
			errorN.appendChild(commandN);
			
			XMLElement parametersN = new XMLElement("parameters", errorN);
			errorN.appendChild(parametersN);
		}
		
		// Print whole PM QuadTree if it is not empty //
		else {
			
			XMLElement success = new XMLElement("success", root_results);
			root_results.appendChild(success);		
			
			myMap.printTree(success);
		}
	}
	
	
	private void runPrintBPTree() {

		if (BPNameDictionary.isEmpty()) {
			XMLElement errorN = new XMLElement("error", root_results);
			errorN.setAttribute("type","emptyBPTree");
			root_results.appendChild(errorN);
			
			XMLElement commandN = new XMLElement("command", errorN);
			commandN.setAttribute("name", "printBPTree");
			errorN.appendChild(commandN);
			
			XMLElement parametersN = new XMLElement("parameters", errorN);
			errorN.appendChild(parametersN);
		}
		else {
			XMLElement success = new XMLElement("success", root_results);
			root_results.appendChild(success);		
			
			BPNameDictionary.printTree(success);
		}
			
	}
	
	private void printDocument() {
		XMLPrintVisitor.printDocument(resultsDoc, out);
	}
			
}

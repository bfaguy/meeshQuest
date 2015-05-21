import java.util.*;
import java.util.Comparator;
import xml.XMLElement;
import org.w3c.dom.Element;

public class BPDictionary extends BPTree {
	
	public BPDictionary() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public BPDictionary(Comparator arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public BPDictionary(int bpOrder, int leafOrder) {
		super(bpOrder, leafOrder);
	}
	
	public BPDictionary(Comparator c, int bpOrder, int leafOrder) {
		super(c, bpOrder, leafOrder);
	}
	

	public void addCity(City city, Boolean coorSort) {
						
		// store City into BPDictionary //
		
		if (coorSort == true) {
			put(new Coordinate((float)city.getX(),(float)city.getY()),city);
		}
		else {
			put(city.getName(),city);	
		}
	}
	
	public void deleteCity(City city, Boolean coorSort) {
		
		// System.out.println("deleting city");
		
		// delete City from Coordinate BPDictionary //
		if (coorSort == true) {
			remove(new Coordinate((float)city.getX(),(float)city.getY()));
		}
		
		// delete City from Name BPDictionary //
		else {
			remove(city.getName());	
		}
	}
	
	public void listNameRangeCities(String startName, String endName, Element root_results) {

		if (isEmpty()) {
			nameRangeError("noCitiesExistInRange", root_results, startName, endName);
		}
		else if (startName.compareTo(endName) >= 0) {
			nameRangeError("startNotLessThanEnd", root_results, startName, endName);
		}
		else {
			nameRangeHelper(startName, endName, root_results);
		}
	}
	
	private void nameRangeHelper(String startName, String endName, Element root_results) {
		
		Iterator it = keyIterator();
		City tempCity = new City();
		String tempCityName;
		boolean cityExistInRange = false;
		
		XMLElement success = new XMLElement("success", root_results);
		
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name","nameRange");
		success.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);

			XMLElement startN = new XMLElement("start", param);
			startN.setAttribute("value", startName);
			param.appendChild(startN);
			XMLElement endN = new XMLElement("end", param);
			endN.setAttribute("value", endName);
			param.appendChild(endN);
		
		XMLElement output = new XMLElement("output", success);
		success.appendChild(output);
		
			XMLElement cityList = new XMLElement("cityList", output);
			output.appendChild(cityList);
		
		// Attach the Cities one by one //
			
		while (it.hasNext()) {
			
			tempCity = (City)get(it.next());	
			tempCityName = tempCity.getName();
			
			if ((tempCityName.compareTo(startName) >= 0) && 
				(tempCityName.compareTo(endName) <= 0)) { 
				
				// bigger than start && smaller than end
				
				XMLElement cityNode = new XMLElement("city", cityList);
				cityNode.setAttribute("name", tempCityName);
				cityNode.setAttribute("x",Integer.toString((int)tempCity.getX()));
				cityNode.setAttribute("y",Integer.toString((int)tempCity.getY()));
				cityNode.setAttribute("radius",Integer.toString(tempCity.getRadius()));
				cityNode.setAttribute("color",tempCity.getColor());
				
				cityList.appendChild(cityNode);
				
				cityExistInRange = true;
			}
			
		}
	
		if (cityExistInRange) {
			root_results.appendChild(success);
		}
		else {
			nameRangeError("noCitiesExistInRange", root_results, startName, endName);
		}
		
		
	}
	
	private void nameRangeError(String errorName, Element root_results, String startName, String endName) {
		
		XMLElement errorN = new XMLElement("error", root_results);
		errorN.setAttribute("type", errorName);
		root_results.appendChild(errorN);
		
		XMLElement commandN = new XMLElement("command", errorN);
		commandN.setAttribute("name", "nameRange");
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
	
	
	public void listCities(boolean coorSort, XMLElement success) {
		
		LinkedList myList = new LinkedList();
		Iterator it;
		City tempCity = new City();
		
		myList.addAll(keySet());
		it = myList.iterator();
		
		XMLElement comm = new XMLElement("command", success);
		comm.setAttribute("name","listCities");
		success.appendChild(comm);
				
		XMLElement param = new XMLElement("parameters", success);
		success.appendChild(param);

			XMLElement sortBy = new XMLElement("sortBy", param);
			if (coorSort == true){
				sortBy.setAttribute("value","coordinate");	
			}
			else {
				sortBy.setAttribute("value","name");
			}
			param.appendChild(sortBy);
		
		XMLElement output = new XMLElement("output", success);
		success.appendChild(output);
		
			XMLElement cityList = new XMLElement("cityList", output);
			output.appendChild(cityList);
		
		// Attach the Cities one by one //
		while (it.hasNext()) {
			
			 //using Coordinates that map to Names
			if (coorSort == true){
				tempCity = (City)get(it.next());	
			}
			
			// using Names that Map to Cities //
			else { 
				tempCity = (City)get(it.next());	
			}
							
			XMLElement cityNode = new XMLElement("city", cityList);
			cityNode.setAttribute("name",tempCity.getName());
			cityNode.setAttribute("x",Integer.toString((int)tempCity.getX()));
			cityNode.setAttribute("y",Integer.toString((int)tempCity.getY()));
			cityNode.setAttribute("radius",Integer.toString(tempCity.getRadius()));
			cityNode.setAttribute("color",tempCity.getColor());
			
			cityList.appendChild(cityNode);
		}
	
	}

	public void printTree(XMLElement success) {
		
		XMLElement command = new XMLElement ("command", success);
		command.setAttribute("name", "printBPTree");
		success.appendChild(command);
		
		XMLElement parameters = new XMLElement ("parameters", success);
		success.appendChild(parameters);
		
		XMLElement output = new XMLElement ("output", success);
		success.appendChild(output);
		
			output.appendChild(toXML());
		
	}
	
}

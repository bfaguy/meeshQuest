import java.util.*;
import java.util.Comparator;
import xml.XMLElement;

public class Dictionary extends TreeMap {
	
	public Dictionary() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Dictionary(Comparator arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void addCity(City city, Boolean coorSort) {
						
		// store City into Dictionary //
		
		if (coorSort == true) {
			put(new Coordinate((float)city.getX(),(float)city.getY()),city);
		}
		else {
			put(city.getName(),city);	
		}
	}
	
	
	public void deleteCity(City city, Boolean coorSort) {
		
		// delete City from Coordinate Dictionary //
		if (coorSort == true) {
			remove(new Coordinate((float)city.getX(),(float)city.getY()));
		}
		
		// delete City from Name Dictionary //
		else {
			remove(city.getName());	
		}
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
}

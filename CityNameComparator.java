import java.util.Comparator;

public class CityNameComparator implements Comparator<City> {

	public int compare (City c1, City c2) {
		return c1.getName().compareTo(c2.getName());
	}

}

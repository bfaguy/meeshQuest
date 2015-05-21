import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import geom.Geometry;

public interface PMValidator {

	//UnknownErrorHandler ueh = UnknownErrorHandler.instance;
	
	public Geometry checkIntersection(Shape geometry, BlackN blackNode) throws RoadIntersectionException;

	public boolean valid(BlackN blackNode);
		
	public String PMOrder();
	
}

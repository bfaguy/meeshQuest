//import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.TreeSet;

public class GrayN extends pmqtNode {
			
		ArrayList<pmqtNode> children;
		//Rectangle2D.Float[] regions;
		
		GrayN(int x, int y, int width, int height, int location) {
			
			// rectangle (int) constructor //
			super(x,y,width,height, location);
		
			children = new ArrayList<pmqtNode>(4);
			
			int halfWidth = width >> 1;
			int halfHeight = height >> 1;
			
			// inititate children //
			WhiteN quad1 = new WhiteN(x, y+halfWidth, halfWidth, halfHeight, 1);
			children.add(0, quad1);

			WhiteN quad2= new WhiteN(x+halfHeight, y+halfWidth, halfWidth, halfHeight, 2);
			children.add(1, quad2); 

			WhiteN quad3 = new WhiteN(x, y, halfWidth, halfHeight, 3);
			children.add(2, quad3); 

			WhiteN quad4 = new WhiteN(x+halfHeight, y, halfWidth, halfHeight, 4);
			children.add(3, quad4); 
		}
				
		public pmqtNode getChild(int quadrant) {
			return children.get(quadrant);
		}
				
		public void addChild(pmqtNode n, int quadrant) {
			children.remove(quadrant);
			children.add(quadrant, n);
		}
	
		public void removeChild(Shape geometry) {
			
			//new Debug("in remove child");
			if (geometry instanceof City) {
				int quadrant = PMQuadTree.findQuadrant(((City)geometry).getX(), 
													   ((City)geometry).getY(), this);
				
				//new Debug("quadrant: " + quadrant);
				
				BlackN oldBlackNode = (BlackN)getChild(quadrant);
				children.remove(quadrant);
				children.add(quadrant, new WhiteN((int)oldBlackNode.getX(), (int)oldBlackNode.getY(),
												  (int)oldBlackNode.getHeight(), (int)oldBlackNode.getWidth(),
												  oldBlackNode.getLocation()));
			}
		}
	
		public void removeCityFromBlack(int quadrant) {
			
			new Debug("quadrant: " + quadrant);
			
			BlackN oldBlackNode = (BlackN)getChild(quadrant); 

			// remove City from blackNode, return new pmqtNode
			pmqtNode newChild = oldBlackNode.deleteCity();
			
			children.remove(quadrant);
			children.add(quadrant, newChild);
		}
		
		public int numWhiteChildren() {
			int count = 0;
			
			for (int i = 0; i < 4; i++) {
				
				pmqtNode node = children.get(i);
				
				if (node instanceof WhiteN) {
					count++; 
				}
			}
			return count;
		}
		
		public int totalCities() {
			int count = 0;
			
			for (int i = 0; i < 4; i++) {
				pmqtNode node = children.get(i);
				
				if ((node instanceof BlackN) && (((BlackN)node).getCity()!= null)) {
					count++;	
				}
				else if (node instanceof GrayN) {
					count += ((GrayN)node).totalCities();
				}
			}
			
			return count;
		}
		
		
		public pmqtNode findNoneWhiteChild() {
	
			for (int i = 0; i < 4; i++) {
				if ( ! (children.get(i) instanceof WhiteN)) {
					return children.get(i);
				}
			}
			
			return null; // return null if all children are white
		}
	
		
		public BlackN mergeChildren() {

			BlackN mergedChild = new BlackN((int)x,(int)y,(int)width,(int)height,location);
			pmqtNode tempChild;
			
			City tempCity;
			TreeSet<Road> tempRoads;
			
			// go through 4 children
			for (int i = 0; i < 4; i++) {

				tempChild = children.get(i);
				
				if (tempChild instanceof BlackN) {
					tempCity = ((BlackN)tempChild).getCity();
					tempRoads = ((BlackN)tempChild).getRoads();
					
					if (tempCity != null) { // if black has city add city
						// this step shoud only happen once //
						mergedChild.addCity(tempCity);  
					}

					if ((tempRoads != null) && (tempRoads.size() != 0)) { // if roadset not null, add roadset
						new Debug("         Roads Found - quadrant: " + (i+1));
						new Debug("        temproad size: " + tempRoads.size());
						
						Road tempRoad = tempRoads.first();
						new Debug("		tempRoad is: " + tempRoad);
						
						mergedChild.addRoads(tempRoads);
					}

				}
				else if (tempChild instanceof WhiteN) {
					// do nothing 
				}	
				else if (tempChild instanceof GrayN) {
					new Debug("    ERROR: temp child shouldn't be gray");
				}
				
			
				
			}

			
		
			
			return mergedChild; 
		}
		
		
		
	}
		
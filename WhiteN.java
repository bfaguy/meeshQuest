import java.awt.geom.Rectangle2D;

public class WhiteN extends pmqtNode {

	WhiteN(int x, int y, int width, int height, int location) {
		super(x,y,width,height, location);
	}

	public void setAttributes(int x, int y, int width, int height, int location) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.location = location;
	}
}

package bfroehlich.cthulhu;

import java.awt.Point;

public enum Direction {

	North, South, East, West;
	
	public int getRotation() {
		if(this == North) {
			return 180;
		}
		else if(this == South) {
			return 0;
		}
		else if(this == East) {
			return -90;
		}
		else if(this == West) {
			return 90;
		}
		return 0;
	}
	
	public Point getPointer() {
		if(this == North) {
			return new Point(0, -1);
		}
		else if(this == South) {
			return new Point(0, 1);
		}
		else if(this == East) {
			return new Point(1, 0);
		}
		else if(this == West) {
			return new Point(-1, 0);
		}
		return null;
	}
	
	public boolean facesSideways() {
		return this == East || this == West;
	}
}
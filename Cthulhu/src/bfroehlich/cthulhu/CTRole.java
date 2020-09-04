package bfroehlich.cthulhu;

import java.awt.Color;

public enum CTRole {

	Cultist, Investigator, Hidden;
	
	public Color getColor() {
		if(this == Investigator) {
			return new Color(60, 179, 113); //medium sea green
		}
		else if(this == Cultist) {
			return new Color(178, 34, 34); //firebrick
		}
		return null;
	}
}

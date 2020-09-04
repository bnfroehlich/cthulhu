package bfroehlich.cthulhu;

import java.awt.Image;

public class SKGamePiece {
	
	protected static Image caution;

	protected SKColor color;
	
	public SKGamePiece(SKColor color) {
		this.color = color;

		if(caution == null) {
			caution = Main.loadImage("caution.png", Main.ctCardSize.width, Main.ctCardSize.height, true);
		}
	}
	
	public SKColor getColor() {
		return color;
	}
	
	public void setColor(SKColor color) {
		this.color = color;
	}
	
	public Image getImage() {
		return caution;
	}
	
	public static SKGamePiece parseString(String s) {
		SKGamePiece result = null;
		try {
			result = new SKCard(s);
			return result;
		}
		catch(Exception e) {}
		try {
			result = new SKPlayerTile(s);
			return result;
		}
		catch(Exception e) {}
		return result;
	}
}
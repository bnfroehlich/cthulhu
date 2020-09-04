package bfroehlich.cthulhu;

import java.awt.Dimension;
import java.awt.Image;
import java.util.HashMap;

public class SKPlayerTile extends SKGamePiece {
	
	private boolean darkSide;
	
	private static Image sun;
	private static Image eclipse;
	
	private static HashMap<String, Image> tileImages;
	
	public static void loadImages(Dimension size) {
		tileImages = new HashMap<String, Image>();
		String[] values = {"tile", "tiledark"};
		for(String aValue : values) {
			for(SKColor aColor : SKColor.values()) {
				String name = ("" + aColor + aValue).toLowerCase();
				Image image = Main.loadImage(name + ".png", size.width, size.height, true);
				tileImages.put(name, image);
			}
		}
	}
	
	public SKPlayerTile(String fromString) {
		super(null);
		String[] pieces = fromString.split("-");
		color = SKColor.valueOf(pieces[0]);
		Boolean isDarkside = Boolean.parseBoolean(pieces[1]);
		if(isDarkside != null && isDarkside) {
			darkSide = isDarkside;
		}
		
		if(sun == null) {
			sun = Main.loadImage("sun.png", Main.skCardSize.width, Main.skCardSize.height, true);
		}
		if(eclipse == null) {
			eclipse = Main.loadImage("eclipse.png", Main.skCardSize.width, Main.skCardSize.height, true);
		}
		
		if(tileImages == null) {
			loadImages(Main.skCardSize);
		}
	}
	
	public SKPlayerTile(SKPlayer player) {
		super(player.getColor());
		this.darkSide = player.getScore() > 0;
		
		if(sun == null) {
			sun = Main.loadImage("sun.png", Main.skCardSize.width, Main.skCardSize.height, true);
		}
		if(eclipse == null) {
			eclipse = Main.loadImage("eclipse.png", Main.skCardSize.width, Main.skCardSize.height, true);
		}
		
		if(tileImages == null) {
			loadImages(Main.skCardSize);
		}
	}
	
	public boolean isDarkSide() {
		return darkSide;
	}

	public void setDarkside(boolean darkSide) {
		this.darkSide = darkSide;
	}

	public Image getImage() {
		String name = color + "tile";
		if(darkSide) {
			name += "dark";
		}
		return tileImages.get(name.toLowerCase());
	}

	public String toString() {
		return color + "-" + darkSide;
	}
}
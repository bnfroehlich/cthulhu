package bfroehlich.cthulhu;

import java.awt.Dimension;
import java.awt.Image;
import java.util.HashMap;

public class SKCard extends SKGamePiece {

	public enum Value {
		Skull, Flower
	}
	
	private Value value;
	private boolean hidden;
	
	private static HashMap<String, Image> cardImages;
	
	public static void loadImages(Dimension size) {
		cardImages = new HashMap<String, Image>();
		String[] values = {"skull", "flower", "back"};
		for(String aValue : values) {
			for(SKColor aColor : SKColor.values()) {
				String name = ("" + aColor + aValue).toLowerCase();
				Image image = Main.loadImage(name + ".png", size.width, size.height, true);
				cardImages.put(name, image);
			}
		}
		
	}
		
	public SKCard(SKColor color, SKCard.Value value, boolean hidden) {
		super(color);
		this.value = value;
		this.hidden = hidden;
		
		if(cardImages == null) {
			loadImages(Main.skCardSize);
		}
	}
	
	public SKCard(String fromString) {
		super(null);
		String[] pieces = fromString.split("-");
		color = SKColor.valueOf(pieces[0]);
		value = Value.valueOf(pieces[1]);
		hidden = Boolean.parseBoolean(pieces[2]);
		
		if(cardImages == null) {
			loadImages(Main.skCardSize);
		}
	}
	
	public Value getValue() {
		return value;
	}
	
	public void setValue(Value value) {
		this.value = value;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public Image getImage() {
		Image image = null;
		if(hidden) {			
			image = cardImages.get((color + "back").toLowerCase());
		}
		else {
			image = cardImages.get(("" + color + value).toLowerCase());
		}
		if(image == null) {
			return caution;
		}
		return image;
	}
	
	public String getDesc() {
		if(hidden) {
			return "card";
		}
		return color + "-" + value;
	}
	
	public String toString() {
		return color + "-" + value + "-" + hidden;
	}
}
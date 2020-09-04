package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

public class CTHandAnnounceDisplay extends JLabel {
	
	private static HashMap<Dimension, Image> speechBubbleImages;	
	private static HashMap<Dimension, Image> sidewaysSpeechBubbleImages;	
	
	private Image speechBubbleImage;
	private String announce;
	private Dimension cardSize;
	
	public CTHandAnnounceDisplay(String announce, Dimension cardSize, boolean facesSideways) {
		this.announce = announce;
		this.cardSize = cardSize;
		
		Dimension prelimDim = cardSize;
		
		if(facesSideways) {
			if(sidewaysSpeechBubbleImages == null) {
				sidewaysSpeechBubbleImages = new HashMap<Dimension, Image>();
			}
			if(sidewaysSpeechBubbleImages.containsKey(prelimDim)) {
				speechBubbleImage = sidewaysSpeechBubbleImages.get(prelimDim);
			}
			else {
				Image newImage = Main.loadImage("speechbubble.png", prelimDim.width, prelimDim.height, true);
				//newImage = Main.createFlipped(Main.toBufferedImage(newImage));
				sidewaysSpeechBubbleImages.put(prelimDim, newImage);
				speechBubbleImage = newImage;
			}
		}
		else {		
			if(speechBubbleImages == null) {
				speechBubbleImages = new HashMap<Dimension, Image>();
			}
			if(speechBubbleImages.containsKey(prelimDim)) {
				speechBubbleImage = speechBubbleImages.get(prelimDim);
			}
			else {
				Image newImage = Main.loadImage("speechbubble.png", prelimDim.width, prelimDim.height, true);
				speechBubbleImages.put(prelimDim, newImage);
				speechBubbleImage = newImage;
			}
		}
		init();
	}

	private void init() {
		
		//setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK));
		
		int actualW = speechBubbleImage.getWidth(this);
		int actualH = speechBubbleImage.getHeight(this);
		
	    //System.out.println("Pilesize: " + stackW + ", " + stackH);
	    final BufferedImage finalImage = new BufferedImage(actualW, actualH,
	        BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = finalImage.createGraphics();
	    
	    g.drawImage(speechBubbleImage, 0, 0, null);

		announce = announce.replaceAll("0-", "");
	    String[] textRows = announce.split("-");
	    int[] fontSizesByNumRows = {16, 16, 16, 12, 8, 8, 8, 8, 8};
	    int fontSize = 16;
	    double vertTransMult = 1;
//   	if(announce.equals("0")) {
//			textRows = "-".split("#");
//			fontSize = 60;
//			vertTransMult = 0;
//		}
   		if(announce.length() == 1) {
   			fontSize = 50;
			vertTransMult = 0;
   		}
   		else if(announce.length() == 2) {
   			fontSize = 40;
			vertTransMult = 0;
   		}
   		else {
   			fontSize = fontSizesByNumRows[textRows.length-1];
   		}
   		
   		fontSize = fontSize*cardSize.width/Main.ctCardSize.width;
   		g.setColor(Color.BLACK);
	    g.setFont(new Font("Century Gothic", Font.BOLD, fontSize));
		int vertTranslate = actualH/2 - (int) (((double) (g.getFontMetrics().getHeight()*textRows.length)*vertTransMult)/2);
	    //System.out.println("fontsize: " + fontSize + ", revisedFontHeight: " + g.getFontMetrics().getHeight() + " rows: " + textRows.length + " bubbleH: " + actualH + " vertTranslate: " + vertTranslate);
		g.translate(actualW/2, vertTranslate);
		
		for(int i = 0; i < textRows.length; i++ ) {
			String line = textRows[i];
			g.drawString(line, -g.getFontMetrics().stringWidth(line)/2, 0);
			g.translate(0, g.getFontMetrics().getHeight());
		}
	    g.dispose();
	    
	    setIcon(new ImageIcon(finalImage));
	}
}
package bfroehlich.cthulhu;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CTCardSetPanel extends JPanel {

	private CTCardSet cardset;
	private Direction position;
	private CTGameWindow window;

	public static HashMap<String, Image> images;
	
	public static void loadImages(Dimension size) {
		images = new HashMap<String, Image>();
		CTCard.Value[] cardNames = CTCard.Value.values();
		for(CTCard.Value cardName : cardNames) {
			images.put(cardName.toString(), Main.loadImage(cardName.toString().toLowerCase() + ".png", size.width, size.height, true));
		}
		images.put("Transparent", Main.loadImage("transparent.png",size.width, size.height, true));
		images.put("Cardback", Main.loadImage("cardback.png",size.width, size.height, true));
	}
	
	public CTCardSetPanel(CTCardSet cardset, Direction position, String name, CTGameWindow window) {
		super();
		
		if(images == null) {
			loadImages(Main.ctCardSize);
		}
		
		this.cardset = cardset;
		this.position = position;
		
		if(position.facesSideways()) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}
		else {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
		
		ArrayList<CTCard> cards = cardset.getCards();
		if(cards.isEmpty()) {
			JLabel label = new JLabel(new ImageIcon(images.get("Transparent")));
			add(label);
		}
		
		for(int i = 0; i < cards.size(); i++) {
			int anI = i;
			CTCard card = cards.get(i);
			Image image = images.get(card.getValue().toString());
			if(card.isHidden()) {
				image = images.get("Cardback");
			}
			image = Main.rotate(Main.toBufferedImage(image), position.getRotation());
			
			JLabel label = new JLabel(new ImageIcon(image));
			label.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				
				public void mouseClicked(MouseEvent e) {
					window.cardClicked(name, anI);
				}
			});
			add(label);
			add(Box.createRigidArea(new Dimension(10, 10)));
		}
	}
}
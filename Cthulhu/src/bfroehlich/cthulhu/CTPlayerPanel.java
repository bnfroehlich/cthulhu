package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class CTPlayerPanel extends JPanel {

	private CTPlayer player;
	private Direction position;
	private CTGameWindow window;
	
	private static ImageIcon hourglass;
	private static Image flashlight;
	private static Image roleback;
	private static Image cultist;
	private static Image investigator;
	
	private static Dimension cardSize;
	
	public static void loadImages(Dimension size) {
		hourglass = Main.loadImageIcon("hourglass.gif", -1, -1, true);
		flashlight = Main.loadImage("flashlight.png", size.width/2, size.height/2, true);
		roleback = Main.loadImage("roleback.png", size.width, size.height, true);
		cultist = Main.loadImage("cultist.png", size.width, size.height, true);
		investigator = Main.loadImage("investigator.png", size.width, size.height, true);
		cardSize = size;
	}
	
	public CTPlayerPanel(CTPlayer player, Direction position, CTGameWindow window) {
		super();
		this.player = player;
		this.position = position;
		this.window = window;
		
		if(flashlight == null) {
			loadImages(Main.ctCardSize);
		}
		init();
	}
	
	private void init() {
		Color playerColor = player.getRole().getColor();
		if(playerColor != null) {
			setBackground(playerColor);
		}
		else {
			setBackground(UIManager.getColor(this));
		}
		
		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK);
		setBorder(border);
				
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
				
		CTCardSetPanel cardSetPanel = new CTCardSetPanel(player.getCardSet(), position, player.getName(), window);
		cardSetPanel.setOpaque(false);
		
		JPanel namePanel = new JPanel();
		namePanel.setOpaque(false);
		
		if(position == Direction.East) {
			add(cardSetPanel);
			add(namePanel);
		}
		else {
			add(namePanel);
			add(cardSetPanel);
		}
		
		if(position.facesSideways()) {
			namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
		}
		else {
			namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		}
		
		ArrayList<Component> namePanelComps = new ArrayList<Component>();
			
		Image roleImage = roleback;
		if(player.getRole() == CTRole.Cultist) {
			roleImage = cultist;
		}
		else if(player.getRole() == CTRole.Investigator) {
			roleImage = investigator;
		}
		roleImage = Main.rotate(Main.toBufferedImage(roleImage), position.getRotation());
		JLabel roleLabel = new JLabel(new ImageIcon(roleImage));
		namePanelComps.add(roleLabel);
		
		namePanelComps.add(Box.createRigidArea(new Dimension(30, 30)));
		
		if(player.isAwaitingInput()) {
			ImageIcon tImageIcon = hourglass;
			//tImage = Main.rotate(Main.toBufferedImage(tImage), position.getRotation());
			JLabel tLabel = new JLabel(tImageIcon);
			namePanelComps.add(tLabel);
			
			namePanelComps.add(Box.createRigidArea(new Dimension(30, 30)));
		}
		
		if(player.hasFlashlight()) {
			Image fImage = flashlight;
			fImage = Main.rotate(Main.toBufferedImage(fImage), position.getRotation());
			JLabel fLabel = new JLabel(new ImageIcon(fImage));
			namePanelComps.add(fLabel);
			
			namePanelComps.add(Box.createRigidArea(new Dimension(30, 30)));
		}
		
		JLabel nameLabel = new JLabel(player.getName());
		namePanelComps.add(nameLabel);

		namePanelComps.add(Box.createRigidArea(new Dimension(30, 30)));		
		
		CTHandAnnounceDisplay handDisp = new CTHandAnnounceDisplay(player.getAnnouncedHand(), cardSize, position.facesSideways());
		namePanelComps.add(handDisp);

		if(position.facesSideways()) {
			namePanel.add(Box.createRigidArea(new Dimension(20, 20)));
			for(int i = namePanelComps.size()-1; i >= 0; i--) {
				namePanel.add(namePanelComps.get(i));
			}
		}
		else {
			for(int i = 0; i < namePanelComps.size(); i++) {
				namePanel.add(namePanelComps.get(i));
			}
		}
	}
	
	public CTPlayer getPlayer() {
		return player;
	}
	
	public void setPlayer(CTPlayer newPlayer) {
		if(player == null || !newPlayer.equals(player)) {
			this.player = newPlayer;
			removeAll();
			init();
		}
	}
}
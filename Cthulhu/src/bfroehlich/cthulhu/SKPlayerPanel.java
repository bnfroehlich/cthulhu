package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class SKPlayerPanel extends JPanel {

	private SKPlayer player;
	private Direction position;
	private SKGameWindow gameWindow;
	
	private static Image transparent;
	private static ImageIcon hourglass;
	
	public static void loadImages(Dimension size) {
		transparent = Main.loadImage("transparent.png", size.width, size.height, true);
		hourglass = Main.loadImageIcon("hourglass.gif", -1, -1, true);
	}
	
	public SKPlayerPanel(SKPlayer player, Direction position, SKGameWindow gameWindow) {
		super();
		this.player = player;
		this.position = position;
		this.gameWindow = gameWindow;
		
		if(transparent == null) {
			loadImages(Main.skCardSize);
		}
		
		init();
	}

	private void init() {

		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK);
		
//		setLayout(new GridBagLayout());
//		int fill = GridBagConstraints.BOTH;
//		GridBagConstraints northConst = new GridBagConstraints();
//		northConst.gridx = 1;
//		northConst.gridy = 0;
//		northConst.fill = fill;
//		GridBagConstraints westConst = new GridBagConstraints();
//		westConst.gridx = 0;
//		westConst.gridy = 1;
//		westConst.fill = fill;
//		GridBagConstraints centerConst = new GridBagConstraints();
//		centerConst.gridx = 1;
//		centerConst.gridy = 1;
//		centerConst.fill = fill;
//		GridBagConstraints eastConst = new GridBagConstraints();
//		eastConst.gridx = 2;
//		eastConst.gridy = 1;
//		eastConst.fill = fill;		
//		GridBagConstraints southConst = new GridBagConstraints();
//		southConst.gridx = 1;
//		southConst.gridy = 2;
//		southConst.fill = fill;
//		

		if(position.facesSideways()) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
		else {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}
		JPanel handPanel = new JPanel();
		JPanel inPlayPanel = new JPanel();
		JPanel bidPanel = new JPanel();
//		
		if(position == Direction.North || position == Direction.West) {
			add(handPanel);
			add(inPlayPanel);
			if(position.facesSideways()) {
				add(Box.createRigidArea(new Dimension(30, 30)));
			}
			add(bidPanel);
			if(position.facesSideways()) {
				add(Box.createRigidArea(new Dimension(30, 30)));
			}
		}
		else {
			if(position.facesSideways()) {
				add(Box.createRigidArea(new Dimension(30, 30)));
			}
			add(bidPanel);
			if(position.facesSideways()) {
				add(Box.createRigidArea(new Dimension(30, 30)));
			}
			add(inPlayPanel);
			add(handPanel);
		}
//
//		if(position == Direction.North) {
//			add(handPanel, northConst);
//			add(inPlayPanel, centerConst);
//			add(bidPanel, southConst);
//		}
//		else if(position == Direction.South) {
//			add(bidPanel, northConst);
//			add(inPlayPanel, centerConst);
//			add(handPanel, southConst);
//		}
//		if(position == Direction.West) {
//			add(handPanel, westConst);
//			add(inPlayPanel, centerConst);
//			add(bidPanel, eastConst);
//		}
//		if(position == Direction.East) {
//			add(handPanel, eastConst);
//			add(inPlayPanel, centerConst);
//			add(bidPanel, westConst);
//		}
		
		handPanel.setBorder(border);
		if(position.facesSideways()) {
			handPanel.setLayout(new BoxLayout(handPanel, BoxLayout.Y_AXIS));
		}
		else {
			handPanel.setLayout(new BoxLayout(handPanel, BoxLayout.X_AXIS));
		}
		
		JLabel tileLabel = new JLabel();
		tileLabel.setIcon(new ImageIcon(player.getTile().getImage()));
		handPanel.add(tileLabel);
		
		handPanel.add(Box.createRigidArea(new Dimension(30, 30)));
		
		if(player.isAwaitingInput()) {
			ImageIcon tImageIcon = hourglass;
			//tImage = Main.rotate(Main.toBufferedImage(tImage), position.getRotation());
			JLabel tLabel = new JLabel(tImageIcon);
			handPanel.add(tLabel);
			
			handPanel.add(Box.createRigidArea(new Dimension(30, 30)));
		}
		
		JLabel nameLabel = new JLabel(player.getName());
		nameLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		handPanel.add(nameLabel);

		handPanel.add(Box.createRigidArea(new Dimension(30, 30)));
		
		ArrayList<SKCard> hand = player.getHand();
		for(int i = 0; i < hand.size(); i++) {
			int anI = i;
			SKCard card = hand.get(i);
			Image image = card.getImage();
			ImageIcon icon = new ImageIcon(image);
			JLabel label = new JLabel(icon);
			label.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {
					gameWindow.cardClicked(player.getName(), anI);
					gameWindow.playerClicked(player.getName());
				}
				public void mousePressed(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseClicked(MouseEvent e) {}
			});
			handPanel.add(label);
		}
		for(int i = hand.size(); i < 4; i++) {
			handPanel.add(new JLabel(new ImageIcon(transparent)));
		}

		inPlayPanel.setBorder(border);
		inPlayPanel.setLayout(new GridBagLayout());
		Direction inPlayDir = Direction.East;
		if(position.facesSideways()) {
			inPlayDir = Direction.South;
		}
		if(player.getInPlay().isEmpty()) {
			inPlayPanel.add(new JLabel(new ImageIcon(transparent)));
		}
		else {
			JPanel inPlaySubPanel = new JPanel();
			if(position.facesSideways()) {
				inPlaySubPanel.setLayout(new BoxLayout(inPlaySubPanel, BoxLayout.Y_AXIS));
			}
			else {
				inPlaySubPanel.setLayout(new BoxLayout(inPlaySubPanel, BoxLayout.X_AXIS));
			}
			inPlayPanel.add(inPlaySubPanel);
			ArrayList<SKCard> inPlay = player.getInPlay();
			ArrayList<SKCard> faceDown = new ArrayList<SKCard>();
			int faceUpBeginIndex = -1;
			for(int i = 0; i < inPlay.size(); i++) {
				if(inPlay.get(i).isHidden()) {
					faceDown.add(inPlay.get(i));
				}
				else {
					faceUpBeginIndex = i;
					break;
				}
			}
			inPlaySubPanel.add(new SKPileLabel(faceDown, inPlayDir, -1, 0.5));
			if(faceUpBeginIndex >= 0) {
				for(int i = faceUpBeginIndex; i < inPlay.size(); i++) {
					JLabel label = new JLabel(new ImageIcon(inPlay.get(i).getImage()));
					inPlaySubPanel.add(label);
				}
			}
		}
		
//		
//		ArrayList<SKCard> inPlay = player.getInPlay();
//		if(inPlay.isEmpty()) {
//			inPlaySubPanel.add(new JLabel(new ImageIcon(transparent)));
//		}
//		for(int i = 0; i < inPlay.size(); i++) {
//			SKCard card = inPlay.get(i);
//			JLabel label = new JLabel(new ImageIcon(card.getImage()));
//			label.setAlignmentY(CENTER_ALIGNMENT);
//			inPlaySubPanel.add(label);
//		}

		bidPanel.setLayout(new GridBagLayout());
		JLabel bidLabel = new JLabel();
		bidLabel.setFont(new Font("Times New Roman", Font.BOLD, 80*gameWindow.getCardSize().width/Main.skCardSize.width));
		if(player.getBid() == null || player.getBid().equals("null")) {
			bidLabel.setText(" ");
		}
		else {
			bidLabel.setText(player.getBid());
		}
		bidPanel.add(bidLabel);

		this.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				gameWindow.playerClicked(player.getName());
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
		});
	}

	public SKPlayer getPlayer() {
		return player;
	}

	public void setPlayer(SKPlayer newPlayer) {
		if(player == null || !newPlayer.equals(player)) {
			this.player = newPlayer;
			removeAll();
			init();
		}
	}
}
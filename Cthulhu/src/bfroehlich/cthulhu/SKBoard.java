package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class SKBoard extends JPanel {
	
	private SKGameWindow gameWindow;
	private SKGameState gameState;
	private String homeUsername;
	
	private JPanel center;
	private HashMap<String, SKPlayerPanel> playerPanels;
	
	public SKBoard(SKGameWindow gameWindow) {
		this.gameWindow = gameWindow;
	}
	
	public void zoomBoard(Dimension cardSize) {
		if(gameState != null && homeUsername != null) {
			initBoard(gameState, homeUsername, cardSize);
		}
	}

	public void initBoard(SKGameState gameState, String homeUsername, Dimension cardSize) {
		this.gameState = gameState;
		this.homeUsername = homeUsername;
		ArrayList<SKPlayer> players = gameState.getPlayers();
		
		this.removeAll();
		setLayout(new GridBagLayout());
		
		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK);
		
		JPanel west = new JPanel();
		//west.setBorder(border);
		GridBagConstraints westConst = new GridBagConstraints();
		westConst.gridx = 0;
		westConst.gridy = 0;
		westConst.gridheight = 3;
		add(west, westConst);
		
		JPanel north = new JPanel();
		//north.setBorder(border);
		GridBagConstraints northConst = new GridBagConstraints();
		northConst.gridx = 1;
		northConst.gridy = 0;
		add(north, northConst);
		
		center = new JPanel();
		center.setPreferredSize(new Dimension((cardSize.width+10)*8, cardSize.height*2));
		GridBagConstraints centerConst = new GridBagConstraints();
		centerConst.gridx = 1;
		centerConst.gridy = 1;
		add(center, centerConst);
		
		center.setLayout(new GridBagLayout());
		fillCenter(gameState);
//		center.setBorder(border);
//		for(SKGamePiece piece : gameState.getDiscard()) {
//			center.add(new JLabel(new ImageIcon(piece.getImage())));
//		}

		JPanel south = new JPanel();
		//south.setBorder(border);
		GridBagConstraints southConst = new GridBagConstraints();
		southConst.gridx = 1;
		southConst.gridy = 2;
		add(south, southConst);
		
		JPanel east = new JPanel();
		//east.setBorder(border);
		GridBagConstraints eastConst = new GridBagConstraints();
		eastConst.gridx = 2;
		eastConst.gridy = 0;
		eastConst.gridheight = 3;
		add(east, eastConst);
		
		//offset so that home player is always in south position
		int offset = 0; 
		for(int i = 0; i < players.size(); i++) {
			if(players.get(i).getName().equals(homeUsername)) {
				offset = i;
				break;
			}
		}
		
		//order of locations to put players in, based on number of players
		JPanel[][] orders = {{south, north},
				{south, west, north},
				{south, west, north, east},
				{south, west, north, north, east},
				{south, west, north, north, east, south},
				{south, west, north, north, north, east, south, south}};
		Direction[][] dirOrders = {{Direction.South, Direction.North},
				{Direction.South, Direction.West, Direction.North},
				{Direction.South, Direction.West, Direction.North, Direction.East},
				{Direction.South, Direction.West, Direction.North, Direction.North, Direction.East},
				{Direction.South, Direction.West, Direction.North, Direction.North, Direction.East, Direction.South},
				{Direction.South, Direction.West, Direction.North, Direction.North, Direction.North, Direction.East, Direction.South, Direction.South}};
		JPanel[] order = orders[players.size()-2];
		Direction[] dirOrder = dirOrders[players.size()-2];
		
		playerPanels = new HashMap<String, SKPlayerPanel>();
		for(int i = 0; i < players.size(); i++) {
			int index = i;
			if(players.size() == 8) {
				//swap positions of last 2 players in 8-player game (who are both in the south panel) to maintain correct clockwise positioning
				if(index == 7) {
					index = 6;
				}
				else if(index == 6) {
					index = 7;
				}
			}
			int offsetIndex = (index + offset) % players.size();
			SKPlayer player = players.get(offsetIndex);
			SKPlayerPanel playerPanel = new SKPlayerPanel(player, dirOrder[index], gameWindow);
			order[index].add(playerPanel);
			playerPanels.put(player.getName(), playerPanel);
		}
	}

	public void updateBoard(SKGameState newGameState, String homeUsername, Dimension cardSize) {
		if(true) {//gameState == null) {
			initBoard(newGameState, homeUsername, cardSize);
			return;
		}
		
		if(newGameState.getDiscard().size() != gameState.getDiscard().size()) {
			fillCenter(newGameState);
		}
		
		System.out.println("newGameState players: " + newGameState.getPlayers().size());
		for(SKPlayer newPlayer : newGameState.getPlayers()) {
			SKPlayerPanel panel = playerPanels.get(newPlayer.getName());
			if(!newPlayer.equals(panel.getPlayer())) {
				panel.setPlayer(newPlayer);
				System.out.println(newPlayer.getName() + " panel new componentCount: " + panel.getComponentCount());
			}
		}
		this.gameState = newGameState;
	}
	
	private void fillCenter(SKGameState newGameState) {
		center.removeAll();
		center.add(new SKPileLabel(newGameState.getDiscard(), Direction.East, center.getPreferredSize().width*3/4, 0));
	}
}
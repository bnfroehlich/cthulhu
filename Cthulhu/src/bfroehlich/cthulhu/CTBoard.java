package bfroehlich.cthulhu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class CTBoard extends JPanel {
	
	private CTGameWindow gameWindow;
	private CTGameState gameState;
	private String homeUsername;
	private HashMap<String, CTPlayerPanel> playerPanels;
	private JPanel center;

	public CTBoard(CTGameWindow window) {
		super();
		this.gameWindow = window;
	}
	
//	public CTGameWindow getGameWindow() {
//		return gameWindow;
//	}
//	
//	public void setGameWindow(CTGameWindow window) {
//		this.gameWindow = window;
//	}
	
	public void zoomBoard(Dimension cardSize) {
		if(gameState != null && homeUsername != null) {
			initBoard(gameState, homeUsername, cardSize);
		}
	}
	
	public void initBoard(CTGameState newGameState, String homeUsername, Dimension cardSize) {
		this.gameState = newGameState;
		this.homeUsername = homeUsername;
		
		ArrayList<CTPlayer> players = newGameState.getPlayers();
		
		removeAll();
		setLayout(new BorderLayout());
		
		FlowLayout flow = new FlowLayout(FlowLayout.CENTER, 10, 10);
		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK);
		
		center = new JPanel();
		center.setBorder(border);
		add(center, BorderLayout.CENTER);

		colorCenter(newGameState);
		fillCenter(newGameState, cardSize);

		JPanel north = new JPanel();
		north.setLayout(flow);
		//north.setBorder(border);
		add(north, BorderLayout.NORTH);

		JPanel south = new JPanel();
		south.setLayout(flow);
		//south.setBorder(border);
		add(south, BorderLayout.SOUTH);
		
		JPanel east = new JPanel();
		east.setLayout(flow);
		//east.setBorder(border);
		add(east, BorderLayout.EAST);
		
		JPanel west = new JPanel();
		west.setLayout(flow);
		//west.setBorder(border);
		add(west, BorderLayout.WEST);
		
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
				{south, west, north, north, north, east, south},
				{south, west, north, north, north, east, south, south}};
		Direction[][] dirOrders = {{Direction.South, Direction.North},
				{Direction.South, Direction.West, Direction.North},
				{Direction.South, Direction.West, Direction.North, Direction.East},
				{Direction.South, Direction.West, Direction.North, Direction.North, Direction.East},
				{Direction.South, Direction.West, Direction.North, Direction.North, Direction.East, Direction.South},
				{Direction.South, Direction.West, Direction.North, Direction.North, Direction.North, Direction.East, Direction.South},
				{Direction.South, Direction.West, Direction.North, Direction.North, Direction.North, Direction.East, Direction.South, Direction.South}};
		JPanel[] order = orders[players.size()-2];
		Direction[] dirOrder = dirOrders[players.size()-2];
		
		playerPanels = new HashMap<String, CTPlayerPanel>();
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
			CTPlayer player = players.get(offsetIndex);
			CTPlayerPanel playerPanel = new CTPlayerPanel(player, dirOrder[index], gameWindow);
			order[index].add(playerPanel);
			playerPanels.put(player.getName(), playerPanel);
		}
		
	}

	public void updateBoard(CTGameState newGameState, String homeUsername, Dimension cardSize) {
		if(gameState == null) {
			initBoard(newGameState, homeUsername, cardSize);
			return;
		}
		
		colorCenter(newGameState);
		if(newGameState.cardsInPlay() != gameState.cardsInPlay()) {
			fillCenter(newGameState, cardSize);
		}
		
		for(CTPlayer newPlayer : newGameState.getPlayers()) {
			CTPlayerPanel panel = playerPanels.get(newPlayer.getName());
			if(!newPlayer.equals(panel.getPlayer())) {
				panel.setPlayer(newPlayer);
			}
		}
		this.gameState = newGameState;
	}
	
	private void colorCenter(CTGameState newGameState) {
		if(newGameState.getWinner() != null && newGameState.getWinner().getColor() != null) {
			center.setBackground(newGameState.getWinner().getColor());;
		}
		else {
			center.setBackground(UIManager.getColor(center));
		}
	}
	
	private void fillCenter(CTGameState newGameState, Dimension cardSize) {
		center.removeAll();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		for(CTCardSet row : newGameState.getInPlay()) {
			JPanel rowPanel = new CTCardSetPanel(row, Direction.South, "null", gameWindow);
			rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
			//rowPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.RED, Color.RED));
			rowPanel.setPreferredSize(new Dimension((cardSize.width+10)*Math.max(8, newGameState.getPlayers().size()+1), cardSize.height));
			rowPanel.setOpaque(false);
			center.add(rowPanel);
		}
	}
}
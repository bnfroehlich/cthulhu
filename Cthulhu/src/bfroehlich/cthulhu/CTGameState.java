package bfroehlich.cthulhu;

import java.util.ArrayList;

public class CTGameState {

	private ArrayList<CTPlayer> players;
	private ArrayList<CTCardSet> inPlay;
	private CTCardSet discard;
	private int roundNum;
	private CTRole winner;
	
	public CTGameState(ArrayList<CTPlayer> players, ArrayList<CTCardSet> inPlay, CTCardSet discard) {
		super();
		this.players = players;
		if(inPlay == null) {
			inPlay = new ArrayList<CTCardSet>();
		}
		this.inPlay = inPlay;
		if(discard == null) {
			discard = new CTCardSet("");
		}
		this.discard = discard;
		roundNum = 0;
		winner = null;
	}
	
	public CTGameState(String s) {
		String[] pieces = s.split("#", -1);
		
		roundNum = Integer.parseInt(pieces[0].substring("round:".length()));
		
		try {
			winner = CTRole.valueOf(pieces[1].substring("winner:".length()));
		}
		catch(Exception e) {
			winner = null;
		}
		
		String[] inPlayRows = pieces[2].split(";", -1);
		inPlay = new ArrayList<CTCardSet>();
		for(String inPlayRow : inPlayRows) {
			inPlay.add(new CTCardSet(inPlayRow));
		}
		
		discard = new CTCardSet(pieces[3]);
		
		String[] playerVals = pieces[4].split(";");
		players = new ArrayList<CTPlayer>();
		for(String playerVal : playerVals) {
			try {
				players.add(new CTPlayer(playerVal));
			}
			catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("bad game state: " + s);
				throw e;
			}
		}
	}
	
	public CTGameState(CTGameState gs) {
		this(gs.toString());
	}

	public ArrayList<CTPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<CTPlayer> players) {
		this.players = players;
	}
	
	public ArrayList<String> getPlayerNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(CTPlayer player : players) {
			names.add(player.getName());
		}
		return names;
	}
	
	public CTPlayer getPlayerByName(String name) {
		CTPlayer foundPlayer = null;
		for(CTPlayer player : players) {
			if(player.getName().equals(name)) {
				foundPlayer = player;
				break;
			}
		}
		return foundPlayer;
	}

	public ArrayList<CTCardSet> getInPlay() {
		return inPlay;
	}

	public void setInPlay(ArrayList<CTCardSet> inPlay) {
		this.inPlay = inPlay;
	}
	
	public CTCardSet getDiscard() {
		return discard;
	}
	
	public int getRoundNum() {
		return roundNum;
	}
	
	public void setRoundNum(int roundNum) {
		this.roundNum = roundNum;
	}
	
	public CTRole getWinner() {
		return winner;
	}

	public void setWinner(CTRole winner) {
		this.winner = winner;
	}

	public int cluesInPlay() {
		int cluesInPlay = 0;
		for(CTCardSet set : inPlay) {
			for(CTCard card : set.getCards()) {
				if(card.getValue() == CTCard.Value.Clue) {
					cluesInPlay++;
				}
			}
		}
		return cluesInPlay;
	}

	public int cardsInPlay() {
		int cardsInPlay = 0;
		for(CTCardSet set : inPlay) {
			for(CTCard card : set.getCards()) {
				cardsInPlay++;
			}
		}
		return cardsInPlay;
	}
	
	public String toString() {
		String val = "round:" + roundNum + "#";
		
		val += "winner:" + winner + "#";
		
		for(CTCardSet row : inPlay) {
			val += row + ";";
		}
		if(!inPlay.isEmpty()) {
			val = val.substring(0, val.length() - 1);
		}
		
		val += "#";
		
		val += discard + "#";
		
		for(CTPlayer p : players) {
			val += p + ";";
		}
		if(!players.isEmpty()) {
			val = val.substring(0, val.length() - 1);
		}
		return val;
	}
}
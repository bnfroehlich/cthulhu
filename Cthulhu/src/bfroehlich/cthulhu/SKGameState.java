package bfroehlich.cthulhu;

import java.util.ArrayList;

public class SKGameState {

	private ArrayList<SKGamePiece> discard;
	private ArrayList<SKPlayer> players;
	private String winner;
	
	public SKGameState(ArrayList<SKGamePiece> discard, ArrayList<SKPlayer> players, String winner) {
		if(discard == null) {
			discard = new ArrayList<SKGamePiece>();
		}
		this.discard = discard;
		this.players = players;
		this.winner = winner;
	}
	
	public SKGameState(String s) {
		String[] pieces = s.split("#");
		
		try {

		try {
				winner = pieces[0].substring("winner:".length());
			}
			catch(Exception e) {
				winner = null;
			}
			
			discard = new ArrayList<SKGamePiece>();
			String discardVal = pieces[1].substring("discard:".length());
			if(!discardVal.isEmpty()) {
				String[] discardValPieces = discardVal.split(",");
				for(String discardValPiece : discardValPieces) {
					discard.add(SKGamePiece.parseString(discardValPiece));
				}
			}
			
			players = new ArrayList<SKPlayer>();
			String playerVal = pieces[2];
			if(!playerVal.isEmpty()) {
				String[] playerValPieces = playerVal.split(";", -1);
				for(String playerValPiece : playerValPieces) {
					players.add(new SKPlayer(playerValPiece));
				}
			}
		}
		catch(Exception e) {
			System.out.println("Bad game state: " + s);
			throw e;
		}
	}
	
	public SKGameState(SKGameState state) {
		this(state.toString());
	}

	public ArrayList<SKGamePiece> getDiscard() {
		return discard;
	}

	public void setDiscard(ArrayList<SKGamePiece> discard) {
		this.discard = discard;
	}

	public ArrayList<SKPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<SKPlayer> players) {
		this.players = players;
	}

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}
	
	public int getTotalInPlay() {
		int total = 0;
		for(SKPlayer player : players) {
			total += player.getInPlay().size();
		}
		return total;
	}
	
	public int getFlowersRevealed() {
		int flowRev = 0;
		for(SKPlayer player : players) {
			for(SKCard card : player.getInPlay()) {
				if(card.getValue() == SKCard.Value.Flower && !card.isHidden()) {
					flowRev++;
				}
			}
		}
		return flowRev;
	}
	
	public String getHighBidder() {
		int highBid = 0;
		String highBidder = null;
		for(SKPlayer player : players) {
			try {
				int bid = Integer.parseInt(player.getBid());
				if(bid > highBid) {
					highBidder = player.getName();
					highBid = bid;
				}
			}
			catch(Exception e) {}
		}
		return highBidder;
	}
	
	public int getHighBid() {
		String highBidder = getHighBidder();
		if(highBidder != null) {
			return Integer.parseInt(getPlayerByName(highBidder).getBid());
		}
		return 0;
	}
	
	public SKPlayer getPlayerByName(String name) {
		SKPlayer foundPlayer = null;
		for(SKPlayer player : players) {
			if(player.getName().equals(name)) {
				foundPlayer = player;
				break;
			}
		}
		return foundPlayer;
	}
	
	public ArrayList<String> getPlayerNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(SKPlayer player : players) {
			names.add(player.getName());
		}
		return names;
	}
	
	public String getNextPlayer(String name) {
		int index = players.indexOf(getPlayerByName(name));
		SKPlayer nextAlive = null;
		while(nextAlive == null) {
			index = (index + 1) % players.size();
			SKPlayer next = players.get(index);
			if(next.getHand().size() > 0 || next.getInPlay().size() > 0) {
				nextAlive = next;
			}
		}
		return nextAlive.getName();
	}
	
	public ArrayList<SKPlayer> playersAlive() {
		ArrayList<SKPlayer> alive = new ArrayList<SKPlayer>();
		for(SKPlayer player : players) {
			if(player.getHand().size() > 0 || player.getInPlay().size() > 0) {
				alive.add(player);
			}
		}
		return alive;
	}
	
	public String toString() {
		String val = "winner:" + winner + "#";
		val += "discard:";
		for(SKGamePiece card : discard) {
			val += card + ",";
		}
		if(!discard.isEmpty()) {
			val = val.substring(0, val.length() - 1);
		}
		val += "#";
		for(SKPlayer player : this.players) {
			val += player + ";";
		}
		if(!players.isEmpty()) {
			val = val.substring(0, val.length() - 1);
		}
		return val;
	}
}
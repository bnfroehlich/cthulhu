package bfroehlich.cthulhu;

import java.util.ArrayList;


public class SKPlayer {
	
	private String name;
	private SKColor color;
	private SKPlayerTile tile;
	private ArrayList<SKCard> hand;
	private ArrayList<SKCard> inPlay;
	private boolean awaitingInput;
	
	private String bid;
	private int score;
	
	public SKPlayer(String name, SKColor color, ArrayList<SKCard> hand) {
		this.name = name;
		this.color = color;
		this.tile = new SKPlayerTile(this);
		
		if(hand == null) {
			hand = new ArrayList<SKCard>();
		}
		this.hand = hand;
		if(inPlay == null) {
			inPlay = new ArrayList<SKCard>();
		}
	}
	
	public SKPlayer(String fromString) {
		String[] pieces = fromString.split(" ");
		name = pieces[0];
		color = SKColor.valueOf(pieces[1]);
		
		hand = new ArrayList<SKCard>();
		String handVal = pieces[2].substring("hand:".length());
		if(!handVal.isEmpty()) {
			String[] handValPieces = handVal.split(",");
			for(String handValPiece : handValPieces) {
				hand.add(new SKCard(handValPiece));
			}
		}
		
		inPlay = new ArrayList<SKCard>();
		String inPlayVal = pieces[3].substring("inplay:".length());
		if(!inPlayVal.isEmpty()) {
			String[] inPlayValPieces = inPlayVal.split(",");
			for(String inPlayValPiece : inPlayValPieces) {
				inPlay.add(new SKCard(inPlayValPiece));
			}
		}

		bid = pieces[4].substring("bid:".length());
		score = Integer.parseInt(pieces[5].substring("score:".length()));
		awaitingInput = Boolean.parseBoolean(pieces[6].substring("awaitinginput:".length()));
		
		this.tile = new SKPlayerTile(this);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SKColor getColor() {
		return color;
	}

	public ArrayList<SKCard> getHand() {
		return hand;
	}
	
	public ArrayList<SKCard> getInPlay() {
		return inPlay;
	}

	public String getBid() {
		return bid;
	}

	public void setBid(String bid) {
		this.bid = bid;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
		tile.setDarkside(score > 0);
	}
	
	public boolean isAwaitingInput() {
		return awaitingInput;
	}

	public void setAwaitingInput(boolean awaitingInput) {
		this.awaitingInput = awaitingInput;
	}

	public SKPlayerTile getTile() {
		return tile;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof SKPlayer && ((SKPlayer) obj).toString().equals(toString());
	}
	
	public int hashcode() {
		return toString().hashCode();
	}
	
	public String toString() {
		String val = name + " " + color + " hand:";
		
		for(SKCard card : hand) {
			val += card + ",";
		}
		if(!hand.isEmpty()) {
			val = val.substring(0, val.length() - 1);
		}
		
		val += " inplay:";
		for(SKCard card : inPlay) {
			val += card + ",";
		}
		if(!inPlay.isEmpty()) {
			val = val.substring(0, val.length() - 1);
		}
		
		val += " bid:" + bid + " score:" + score + " awaitinginput:" + awaitingInput;
		return val;
	}
}
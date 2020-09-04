package bfroehlich.cthulhu;

import java.util.ArrayList;

public class CTCardSet {

	private ArrayList<CTCard> cards;

	public CTCardSet(ArrayList<CTCard> cards) {
		super();
		this.cards = cards;
	}
	
	public CTCardSet(String val) {
		cards = new ArrayList<CTCard>();
		if(!val.isEmpty()) {
			String[] cardVals = val.split(",");
			for(String cardVal : cardVals) {
				cards.add(new CTCard(cardVal));
			}
		}
	}

	public ArrayList<CTCard> getCards() {
		return cards;
	}

	public void setCards(ArrayList<CTCard> cards) {
		this.cards = cards;
	}
	
	public String toString() {
		String val = "";
		for(CTCard card : cards) {
			val += card + ",";
		}
		if(val.length() > 1) {
			val = val.substring(0, val.length() - 1);
		}
		return val;
	}
}
package bfroehlich.cthulhu;

public class CTPlayer {

	private String name;
	private CTCardSet cards;
	private CTRole role;
	private boolean hasFlashlight;
	private boolean awaitingInput;
	private String announcedHand;
	
	public CTPlayer(String name, CTRole role, CTCardSet cards) {
		this.name = name;
		this.role = role;
		this.cards = cards;
		this.announcedHand = "";
	}
	
	public CTPlayer(String val) {
		String[] pieces = val.split(" ", -1);
		try {
			name = pieces[0];
			role = CTRole.valueOf(pieces[1]);
			awaitingInput = pieces[2].equals("thinking");
			hasFlashlight = pieces[3].equals("F");
			announcedHand = pieces[4];
			if(pieces.length > 5) {
				cards = new CTCardSet(pieces[5]);
			}
			else {
				cards = new CTCardSet("");
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("can't make player: " + val);
			throw e;
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public CTRole getRole() {
		return role;
	}

	public void setRole(CTRole role) {
		this.role = role;
	}

	public CTCardSet getCardSet() {
		return cards;
	}

	public void setCardSet(CTCardSet cards) {
		this.cards = cards;
	}
	
	public boolean hasFlashlight() {
		return hasFlashlight;
	}

	public void setHasFlashlight(boolean hasFlashlight) {
		this.hasFlashlight = hasFlashlight;
	}

	public boolean isAwaitingInput() {
		return awaitingInput;
	}

	public void setAwaitingInput(boolean awaitingInput) {
		this.awaitingInput = awaitingInput;
	}

	public String getAnnouncedHand() {
		return announcedHand;
	}

	public void setAnnouncedHand(String announcement) {
		this.announcedHand = announcement;
	}
	
	public boolean equals(Object obj) {
		return obj instanceof CTPlayer && ((CTPlayer) obj).toString().equals(toString());
	}
	
	public int hashcode() {
		return toString().hashCode();
	}

	public String toString() {
		String val = name + " " + role + " ";
		if(awaitingInput) {
			val += "thinking";
		}
		val += " ";
		if(hasFlashlight) {
			val += "F";
		}
		val += " " + announcedHand + " " + cards;
		return val;
	}
}
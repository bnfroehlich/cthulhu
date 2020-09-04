package bfroehlich.cthulhu;

public class CTCard {

	public enum Value {Clue, Rock, Cthulhu, EvilPresence, InsanitysGrasp, Mirage, Paranoia, PrescientVision, PrivateEye};
	
	private Value value;
	private boolean hidden;
	
	public CTCard(Value value) {
		this.value = value;
	}
	
	public CTCard(String fromString) {
		String[] pieces = fromString.split("-hidden:");
		value = Value.valueOf(pieces[0]);
		hidden = Boolean.parseBoolean(pieces[1]);
	}
	
	public Value getValue() {
		return value;
	}
	
	public void setValue(Value value) {
		this.value = value;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public String toString() {
		return value + "-hidden:" + hidden;
	}
	
}
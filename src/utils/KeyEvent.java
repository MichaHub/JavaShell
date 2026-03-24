package utils;

public class KeyEvent {

	private KeyType ktype;
	private char ch;

	public KeyEvent(KeyType ktype) {
		this.ktype = ktype;
	}

	public KeyEvent(char ch) {
		this.ktype = KeyType.CHAR;
		this.ch = ch;
	}

	public KeyEvent(KeyType ktype, char ch) {
		this.ktype = ktype;
		this.ch = ch;
	}

	public KeyType getKtype() {
		return ktype;
	}

	public void setKtype(KeyType ktype) {
		this.ktype = ktype;
	}

	public char getCh() {
		return ch;
	}

	public void setCh(char ch) {
		this.ch = ch;
	}
}

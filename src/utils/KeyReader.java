package utils;

import java.io.IOException;

public class KeyReader {

	private enum State {
		NORMAL, ESC_CONTAIN, CSI
	}

	private State state = State.NORMAL;
	private StringBuilder csiBuffer = new StringBuilder();

	public KeyEvent readKey() throws IOException {

		int code = RawConsoleInput.read(true);

		if (code >= 0xE000)
			return mapWindows(code);

		switch (state) {

		case NORMAL:
			if (code == 27) {
				state = State.ESC_CONTAIN;
				return readKey();
			}
			if (code == 10 || code == 13)
				return new KeyEvent(KeyType.ENTER);
			if (code == 8 || code == 127)
				return new KeyEvent(KeyType.BACKSPACE);
			if (code == 1)
				return new KeyEvent(KeyType.HOME);
			if (code == 5)
				return new KeyEvent(KeyType.END);
			return new KeyEvent((char) code);

		case ESC_CONTAIN:

			if (code == '[') {
				state = State.CSI;
				csiBuffer.setLength(0);
				return readKey();
			}
			state = State.NORMAL;
			return new KeyEvent(KeyType.UNKNOWN);

		case CSI:

			csiBuffer.append((char) code);
			if (code >= 64 && code <= 126) {
				KeyEvent ev = mapCSI(csiBuffer.toString());
				state = State.NORMAL;
				return ev;
			}
			return readKey();
		}
		return new KeyEvent(KeyType.UNKNOWN);
	}

	private KeyEvent mapWindows(int code) {

		switch (code) {

		case 0xE048:
			return new KeyEvent(KeyType.UP);
		case 0xE050:
			return new KeyEvent(KeyType.DOWN);
		case 0xE04B:
			return new KeyEvent(KeyType.LEFT);
		case 0xE04D:
			return new KeyEvent(KeyType.RIGHT);
		case 0xE053:
			return new KeyEvent(KeyType.DELETE);
		case 0xE047:
			return new KeyEvent(KeyType.HOME);
		case 0xE04F:
			return new KeyEvent(KeyType.END);
		}

		return new KeyEvent(KeyType.UNKNOWN);
	}

	private KeyEvent mapCSI(String seq) {

		switch (seq) {

		case "A":
			return new KeyEvent(KeyType.UP);
		case "B":
			return new KeyEvent(KeyType.DOWN);
		case "C":
			return new KeyEvent(KeyType.RIGHT);
		case "D":
			return new KeyEvent(KeyType.LEFT);
		case "3~":
			return new KeyEvent(KeyType.DELETE);
		case "H":
			return new KeyEvent(KeyType.HOME);
		case "F":
			return new KeyEvent(KeyType.END);
		}
		return new KeyEvent(KeyType.UNKNOWN);
	}

}

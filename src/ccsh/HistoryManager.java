package ccsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class HistoryManager {

	public HistoryManager() {
	}

	public void writeHistoryFile() {

		File historyFile = new File(System.getProperty("user.home"), ".ccsh_history");

		try {
			if (!historyFile.exists()) {
				historyFile.createNewFile();
			}
		} catch (IOException e) {
			System.out.println("file could not be created " + e.getMessage());
		}
		try (Writer w = new FileWriter(historyFile)) {

			for (String line : Main.getHistory()) {
				w.write(line);
				w.write("\n");
			}

		} catch (IOException e) {
			System.out.println("write error: " + e.getMessage());
		}
		Main.getHistory().clear();

	}

	public void readFromHistoryFile() {

		if (new File(System.getProperty("user.home"), ".ccsh_history").exists()) {
			try (BufferedReader historyreader = new BufferedReader(
					new FileReader(new File(System.getProperty("user.home"), ".ccsh_history")))) {

				String line;
				while ((line = historyreader.readLine()) != null) {
					Main.getHistory().add(line);
				}

			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
			}
		}
	}
}

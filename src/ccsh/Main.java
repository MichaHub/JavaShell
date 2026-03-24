package ccsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import utils.KeyEvent;
import utils.KeyReader;
import utils.RawConsoleInput;

public class Main {

	static File directory = new File(System.getProperty("user.dir"));
	static File[] filesInDirectory = directory.listFiles();
	static File path = new File(directory.getAbsolutePath());
	static LinkedList<String> history = new LinkedList<>();
	private static final String PROMPT = ">";

	public static void main(String[] args) {

		CommandExecuter cex = new CommandExecuter();
		HistoryManager hm = new HistoryManager();
		CommandData cmdData;
		List<String> commandInput;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

			String input;
			String[] command;
			String commandFirst;
			String commandArgument = "";
			String commandArgument2 = "";
			String start = "ccsh";
			while (start.equals("ccsh")) {
				System.out.println("\\");
				input = reader.readLine();
				if (input == null || input.equals("exit"))
					break;
				if (input.equals("ccsh")) {
					hm.readFromHistoryFile();
					System.out.println("% ccsh");
					KeyReader keyReader = new KeyReader();

					while (!input.equals("exit")) {
						System.out.println("ccsh>");

						String listenerline = keyListener(keyReader, history);
						RawConsoleInput.resetConsoleMode();
						input = listenerline;
						commandArgument = "";
						commandArgument2 = "";
						commandInput = null;
						cmdData = new CommandData(commandArgument, commandArgument2, commandInput, directory);
						if (input.contains("|")) {
							cex.pipecommand(cmdData, input);
							continue;
						}
						if (input.contains("curl")) {
							cex.curlcommand(cmdData, input);
							continue;
						}
						if (input == null || input.trim().isEmpty() || input.equals("exit")) {
							continue;
						}
						command = input.split("\\s+");
						commandFirst = command[0];
						if (command.length > 1) {
							commandArgument = command[1];
						}
						if (command.length > 2) {
							commandArgument2 = command[2];
						}
						cmdData = new CommandData(commandArgument, commandArgument2, commandInput, directory);
						final CommandData finalCmdData = cmdData;
						List<String> redir;
						redir = Optional.ofNullable(cex.getCe().get(commandFirst)).map(cmd -> cmd.apply(finalCmdData))
								.orElseThrow(() -> new RuntimeException("command not found."));

						if (commandFirst.equals("cd") || commandFirst.equals("mkdir")) {
							Main.directory = new File(redir.get(0));
						} else if (redir != null) {
							for (String o : redir) {
								System.out.println(o);
							}
						}

					}
					System.out.println("%");
					hm.writeHistoryFile();
				}
			}
		} catch (IOException e) {
			System.err.println("IO Error " + e.getMessage());
		}
	}

	public static File getDirectory() {
		return directory;
	}

	public static void setDirectory(File directory) {
		Main.directory = directory;
	}

	public static LinkedList<String> getHistory() {
		return history;
	}

	public static void setHistory(LinkedList<String> history) {
		Main.history = history;
	}

	private static void liveEditing(StringBuilder keyEdit, int cursor) {

		int buffer;
		System.out.print("\r");
		System.out.print("\u001B[2K");
		System.out.print("> ");
		System.out.print(keyEdit);

		buffer = 2 + cursor;
		System.out.print("\r");
		if (buffer > 0) {
			System.out.print("\u001B[" + buffer + "C");
		}
		System.out.flush();

	}

	private static String keyListener(KeyReader reader, LinkedList<String> history) throws IOException {

		StringBuilder keyEdit = new StringBuilder();
		int cursor = 0;
		int historyPosition = history.size() - 1;

//		liveEditing(keyEdit,cursor);

		while (true) {

			KeyEvent ev = reader.readKey();

			switch (ev.getKtype()) {

			case CHAR:
				keyEdit.insert(cursor, ev.getCh());
				cursor++;
				break;

			case ENTER:
				String line = keyEdit.toString();
				if (!line.isEmpty())
					history.add(line);
				return line;

			case BACKSPACE:
				if (cursor > 0) {
					keyEdit.deleteCharAt(cursor - 1);
					cursor--;
				}
				break;

			case DELETE:
				if (cursor < keyEdit.length())
					keyEdit.deleteCharAt(cursor);
				break;

			case LEFT:
				if (cursor > 0)
					cursor--;
				break;

			case RIGHT:
				if (cursor < keyEdit.length())
					cursor++;
				break;

			case HOME:
				cursor = 0;
				break;

			case END:
				cursor = keyEdit.length();
				break;

			case UP:
				if (historyPosition > 0) {
					historyPosition--;
					keyEdit.setLength(0);
					keyEdit.append(history.get(historyPosition));
					cursor = keyEdit.length();
				}
				break;

			case DOWN:
				if (historyPosition < history.size() - 1) {
					historyPosition++;
					keyEdit.setLength(0);
					keyEdit.append(history.get(historyPosition));
					cursor = keyEdit.length();
				} else {
					historyPosition = history.size();
					keyEdit.setLength(0);
					cursor = 0;
				}
				break;

			case UNKNOWN:
				break;

			}
			liveEditing(keyEdit, cursor);

		}
	}

}

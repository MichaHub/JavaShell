package ccsh;

import java.io.BufferedReader;
import java.io.Writer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.net.ssl.SSLSocketFactory;

public class CommandExecuter {

	private Map<String, Function<CommandData, List<String>>> ce = new HashMap<>();

	public Map<String, Function<CommandData, List<String>>> getCe() {
		return ce;
	}

	public void setCe(Map<String, Function<CommandData, List<String>>> ce) {
		this.ce = ce;
	}

	public CommandExecuter() {
		registerCommands();
	}

	private String buildFilePrompt(File file) {

		StringBuilder sb = new StringBuilder();
		sb.append(file.isDirectory() ? "d" : "-");
		sb.append(file.canRead() ? "r" : "-");
		sb.append(file.canWrite() ? "w" : "-");
		sb.append(file.canExecute() ? "x" : "-");
		sb.append(file.isHidden() ? "h" : "-");
		String filep = sb.toString();
		long flength = file.length();
		String fname = file.getName();
		String fmod = String.format("%1$tY-%1$tm-%1$td", file.lastModified());
		String wholeFile = String.format("%s %10d %s %-18s", filep, flength, fmod, fname);
		return wholeFile;
	}

	public List<String> lscommand(CommandData cmdData) {

		File directory;
		File[] filesInDirectory = cmdData.getDirectory().listFiles();
		if (cmdData.hasInput() && new File(cmdData.getCommandInput().get(0)).isDirectory()) {
			directory = new File(cmdData.getCommandInput().get(0));
			filesInDirectory = directory.listFiles();
		}
		List<String> output = new ArrayList<>();

		if (cmdData.getCommandArgument() == null || cmdData.getCommandArgument().isEmpty()) {

			for (File file : filesInDirectory) {

				if (!file.isHidden()) {
					output.add(buildFilePrompt(file));
				}
			}
		} else if (cmdData.getCommandArgument().equals("-a")) {
			for (File file : filesInDirectory) {
				output.add(buildFilePrompt(file));
			}

		} else {
			System.out.println("command not found!");
		}
		return output;
	}

	public List<String> pwdcommand(CommandData cmdData) {
		List<String> output = new ArrayList<>();
		output.add(cmdData.getDirectory().getAbsolutePath());
		return output;
	}

	public List<String> cdcommand(CommandData cmdData) {

		List<String> output = new ArrayList<>();

		Path newPath = Paths.get(cmdData.getDirectory().getAbsolutePath()).resolve(cmdData.getCommandArgument())
				.normalize();

		File directory = newPath.toFile();

		if (directory.exists()) {
//			System.out.println(directory.getAbsolutePath());
			output.add(directory.getAbsolutePath());
		} else {
			System.out.println(" path not found");
			output.add(cmdData.getDirectory().getAbsolutePath());
		}

		return output;
	}

	public List<String> catcommand(CommandData cmdData) {

		List<String> output = new ArrayList<>();

		if (cmdData.hasInput()) {

			if (cmdData.getCommandArgument().isEmpty()) {

				return cmdData.getCommandInput();

			} else if (cmdData.getCommandArgument().equals("-n")) {

				int zaehler = 1;
				for (String s : cmdData.getCommandInput()) {
					output.add(zaehler + " " + s);
					zaehler++;
				}
				return output;
			} else if (cmdData.getCommandArgument().equals("-b")) {

				int zaehler = 1;
				for (String s : cmdData.getCommandInput()) {
					if (!s.trim().isBlank()) {
						output.add(zaehler + " " + s);
						zaehler++;
					} else {
						output.add(s);
					}

				}
				return output;
			}

		}
		if (!cmdData.getCommandArgument().isEmpty()) {

			File catFile = new File(cmdData.getDirectory(), cmdData.getCommandArgument());
			System.out.println(catFile.getPath());
			try (BufferedReader br = new BufferedReader(new FileReader(catFile))) {
				String line;
				while ((line = br.readLine()) != null) {
					output.add(line);
				}
			} catch (IOException e) {
				System.out.println("IO Error File 1: " + e.getMessage());
			}

			if (!cmdData.getCommandArgument2().isEmpty()) {

				File catFile2 = new File(cmdData.getDirectory(), cmdData.getCommandArgument2());

				try (BufferedReader br = new BufferedReader(new FileReader(catFile2))) {
					String line2;
					while ((line2 = br.readLine()) != null) {
						output.add(line2);
					}
				} catch (IOException e) {
					System.out.println("IO Error File 2: " + e.getMessage());
				}
			}
		} else {
			System.out.println("file not found");
		}
		return output;
	}

	public List<String> mkdircommand(CommandData cmdData) {
		List<String> output = new ArrayList<>();
		if (!cmdData.getCommandArgument().isEmpty()) {
			String[] argumentPath;
			argumentPath = cmdData.getCommandArgument().split("\\\\");

			int x = -1;

			File newDirectory = new File(cmdData.getDirectory().getAbsolutePath());

			while (x < argumentPath.length - 1 && argumentPath[x + 1] != null) {

				File tmp = new File(newDirectory, argumentPath[x + 1]);
				tmp.mkdir();
				newDirectory = tmp;
				x++;

			}
			output.add(newDirectory.getAbsolutePath());
		}

		else {
			System.out.println("wrong directory");
		}
		return output;
	}

	public List<String> wccommand(CommandData cmdData) {
		List<String> input = new ArrayList<>();
		List<String> output = new ArrayList<>();
		if (cmdData.getCommandInput() != null) {
			input = cmdData.getCommandInput();
		}
		int countLines1 = 0;
		String[] words1;
		int countWords1 = 0;
		int countChars1 = 0;
		byte[] bytes1;
		int countBytes1 = 0;

		if (cmdData.hasInput()) {
			countLines1 = input.size();
			for (String oneLine : input) {
				bytes1 = oneLine.getBytes(StandardCharsets.UTF_8);
				countBytes1 = countBytes1 + bytes1.length + 2;
				countChars1 += oneLine.length();
				if (!oneLine.trim().isEmpty()) {
					words1 = oneLine.split("\\s+");
					countWords1 = countWords1 + words1.length;
				}
			}
			output.add("Lines Words Character Bytes Filename: " + countLines1 + " " + countWords1 + " " + countChars1
					+ " " + countBytes1 + " " + cmdData.getCommandArgument());
			return output;
		}

		if (!cmdData.getCommandArgument().isEmpty()) {
			File tmp = new File(cmdData.getDirectory(), cmdData.getCommandArgument());
			try (BufferedReader reader = new BufferedReader(new FileReader(tmp))) {

				String line = "";
				int countLines = 0;
				String[] words;
				int countWords = 0;
				int countChars = 0;
				byte[] bytes;
				int countBytes = 0;

				reader.mark(100000000);
				while ((line = reader.readLine()) != null) {
					countLines++;
					bytes = line.getBytes(StandardCharsets.UTF_8);
					countBytes = countBytes + bytes.length + 2;
					if (!line.trim().isEmpty()) {
						words = line.trim().split("\\s+");
						countWords = countWords + words.length;
					}

				}
				reader.reset();
				while (reader.read() > -1) {
					countChars++;
				}

				output.add("Lines Words Character Bytes Filename: " + countLines + " " + countWords + " " + countChars
						+ " " + countBytes + " " + cmdData.getCommandArgument());

			} catch (IOException e) {
				System.out.println("IO Error: " + e.getMessage());
			}
		} else {
			System.out.println("No such file");
		}
		return output;

	}

	public List<String> touchcommand(CommandData cmdData) {

		List<String> output = new ArrayList<>();
		File tmp = new File(cmdData.getDirectory(), cmdData.getCommandArgument());
		try {
			tmp.createNewFile();
			if (tmp.isFile() && tmp.exists()) {
				output.add(tmp.getPath());
			} else {
				System.out.println("no such file");
			}
		} catch (IOException e) {
			System.out.println("IO Exception:" + e.getMessage());
		}
		return output;
	}

	public List<String> historycommand(CommandData cmdData) {

		List<String> output = new ArrayList<>();

		System.out.println("ccsh>history: ");

		if (cmdData.getCommandArgument().equals("-clear")) {
			File historyFile = new File(cmdData.getDirectory(), ".ccsh_history");
			if (!historyFile.exists()) {
				System.out.println("File doesnt exist");
			}
			historyFile.delete();
			Main.getHistory().clear();
		}

		for (String line : Main.getHistory()) {
			output.add(line);
		}

		return output;

	}

	public void registerCommands() {

		ce.put("ls", this::lscommand);
		ce.put("pwd", this::pwdcommand);
		ce.put("cd", this::cdcommand);
		ce.put("touch", this::touchcommand);
		ce.put("mkdir", this::mkdircommand);
		ce.put("wc", this::wccommand);
		ce.put("cat", this::catcommand);
		ce.put("history", this::historycommand);

	}

	public void pipecommand(CommandData cmdData, String input) {

		String[] commands = input.split("\\|");
		String[] commendParts;
		String commandFirst = "";
		String commandArgument = "";
		String commandArgument2 = "";
		CommandData cmdTransfer = cmdData;
		List<String> cmdExchange = null;

		for (int i = 0; i < commands.length; i++) {

			commendParts = commands[i].trim().split("\\s+");
			commandFirst = commendParts[0];
			if (commendParts.length > 1) {
				commandArgument = commendParts[1];
			}
			if (commendParts.length > 2) {
				commandArgument2 = commendParts[2];
			}
			cmdTransfer.setCommandInput(cmdExchange);
			cmdTransfer.setCommandArgument(commandArgument);
			cmdTransfer.setCommandArgument2(commandArgument2);
			if (cmdExchange != null) {
				if (commandFirst.equals("cd") && cmdExchange != null && !cmdExchange.isEmpty()) {
					File test = new File(cmdExchange.get(0));
					if (test.isDirectory() && test.exists()) {
						cmdTransfer.setDirectory(test);
					}
				}
			}

			commandArgument = "";
			commandArgument2 = "";
			final String safeCommandFirst = commandFirst;
			try {
				cmdExchange = Optional.ofNullable(ce).map(ob -> ob.get(safeCommandFirst))
						.map(sub -> sub.apply(cmdTransfer))
						.orElseThrow(() -> new RuntimeException("command not found."));

			} catch (NullPointerException e) {
				System.out.println("command is wrong" + e.getMessage());
				continue;
			} catch (IllegalArgumentException e) {
				System.out.println("command input failure" + e.getMessage());
				continue;
			} catch (Exception e) {
				System.out.println("command failure-" + e.getMessage());
				continue;
			}

			if (i == commands.length - 1 && cmdExchange != null) {
				for (String line : cmdExchange) {
					System.out.println(line);
				}
			}

		}
	}

	public void curlcommand(CommandData cmdData, String input) {

		String[] commandArguments = input.trim().split("\\s+");
		String jsonData = "";
		List<String> headers = new ArrayList<>();
		String protocol = "";
		String host = "";
		String path = "";
		String[] pathSegments;
		String lastSegment;
		int id = 0;
		int port = 80;
		Request request = Request.GET;
		boolean verbose = false;
		URI uri = null;

		try {
			uri = new URI("http://reqres.in");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		for (int i = 0; i < commandArguments.length; i++) {

			if (commandArguments[i].equals("-v")) {
				verbose = true;
			}

			if (commandArguments[i].equals("-d")) {
				if (i + 1 < commandArguments.length) {
					StringBuilder data = new StringBuilder();
					i++;
					while (i < commandArguments.length && !commandArguments[i].startsWith("-")) {
						data.append(commandArguments[i]).append(" ");
						i++;
					}
					i--;
					jsonData = data.toString().trim();
				} else {
					System.out.println("using -d with wrong data");
				}
			}

			if (commandArguments[i].equals("-h")) {
				if (i + 1 < commandArguments.length) {
					StringBuilder header = new StringBuilder();
					i++;
					while (i < commandArguments.length && !commandArguments[i].startsWith("-")) {
						header.append(commandArguments[i]).append(" ");
						i++;
					}
					i--;
					headers.add(header.toString().trim() + "\r\n");
				} else {
					System.out.println("using -h with wrong header");
				}
			}

			if (commandArguments[i].equals("-X")) {

				final int index = i + 1;
				if (index < commandArguments.length && commandArguments[index] != null
						&& Arrays.stream(Request.values()).anyMatch(e -> e.name().equals(commandArguments[index]))) {

					request = Request.valueOf(commandArguments[index]);
					i++;
				} else {
					System.out.println("using -X with wrong request");
				}

			}

			if (commandArguments[i].startsWith("http://") || commandArguments[i].startsWith("https://")) {

				try {

					uri = new URI(commandArguments[i]);
					protocol = uri.getScheme();
					host = uri.getHost();

					if (uri.getPort() != -1) {
						port = uri.getPort();
					} else {
						port = (uri.getScheme().equals("https")) ? 443 : 80;
					}

					path = (uri.getPath() != null && uri.getPath().isEmpty()) ? "/" : uri.getPath();
					pathSegments = path.split("/");
					if (pathSegments.length != 0) {
						lastSegment = pathSegments[pathSegments.length - 1];
						if (lastSegment.matches("\\d+")) {
							id = Integer.parseInt(lastSegment);
						}

					}

				} catch (Exception e) {
					System.out.println("wrong URL: " + e.getMessage());
				}

			}

		}

		try {

			Socket socket;
			if (uri.getScheme().equals("https")) {
				SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				socket = factory.createSocket(uri.getHost(), port);
			} else {
				socket = new Socket(uri.getHost(), port);
			}

			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			Writer writer = new OutputStreamWriter(os);

			writer.write(request + " " + path + " HTTP/1.1\r\n");
			writer.write("Host: " + uri.getHost() + "\r\n");
			writer.write("User-Agent: curl/7.68.0\r\n");
			writer.write("Accept: */*\r\n");

			if (!headers.isEmpty()) {
				for (int i = 0; i < headers.size(); i++) {
					writer.write(headers.get(i));
				}
			}
			if (request == Request.POST || request == Request.PUT) {
				writer.write("Content-Type: application/json\r\n");
				writer.write("Content-Length: " + jsonData.getBytes(StandardCharsets.UTF_8).length + "\r\n");
			}
			writer.write("Connection: close\r\n");
			writer.write("\r\n");
			if (request == Request.POST || request == Request.PUT) {
				writer.write(jsonData);
			}

			// DEBUGGING{
//			System.out.println("=== REQUEST ===");
//			System.out.println(request + " " + path + " HTTP/1.1");
//			System.out.println("Host: " + uri.getHost());
//			System.out.println("User-Agent: curl/7.68.0");
//			System.out.println("Accept: */*");
//			for (String header : headers) {
//			    System.out.println(header);
//			}
//			if (request == Request.POST || request == Request.PUT) {
//			    System.out.println("Content-Type: application/json");
//			    System.out.println("Content-Length: " + jsonData.getBytes(StandardCharsets.UTF_8).length);
//			}
//			System.out.println("Connection: close");
//			System.out.println("Body: " + jsonData);
//			System.out.println("===============");

			// DEBUGGING }

			writer.flush();

			String line;

			if (!verbose) {

				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}

			} else {

				System.out.println(">connecting to " + uri.getHost());
				System.out.println(">sending request " + request + " " + uri.getScheme());
				System.out.println(">Host: " + uri.getHost());
				System.out.println(">Accept: */*");

				while ((line = reader.readLine()) != null) {
					System.out.println("<" + line);
				}
				verbose = false;
			}
			reader.close();
			writer.close();
			socket.close();
		}

		catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

}

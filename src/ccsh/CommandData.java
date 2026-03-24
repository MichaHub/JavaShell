package ccsh;

import java.io.File;
import java.util.List;

public class CommandData {

	private String commandArgument;
	private String commandArgument2;
	private List<String> commandInput;
	private File directory;

	public CommandData(String commandArgument, String commandArgument2, List<String> commandInput, File directory) {
		this.commandArgument = commandArgument;
		this.commandArgument2 = commandArgument2;
		this.commandInput = commandInput;
		this.directory = directory;
	}

	public String getCommandArgument() {
		return commandArgument;
	}

	public void setCommandArgument(String commandArgument) {
		this.commandArgument = commandArgument;
	}

	public List<String> getCommandInput() {
		return commandInput;
	}

	public void setCommandInput(List<String> commandInput) {
		this.commandInput = commandInput;
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getCommandArgument2() {
		return commandArgument2;
	}

	public void setCommandArgument2(String commandArgument2) {
		this.commandArgument2 = commandArgument2;
	}

	public boolean hasInput() {
		return commandInput != null && !commandInput.isEmpty();
	}

}

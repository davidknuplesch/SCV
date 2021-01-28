package de.uulm.cli;

import java.util.HashMap;
import java.util.Map;

public abstract class CLICommand {
	
	static Map<String, CLICommand> registry = new HashMap<String,CLICommand>(); 
	
	static void executeCommandLine(String line) {
		String[] splitLine = line.trim().split("\\s+");
		if (splitLine.length > 0) {
			CLICommand cliCommand = registry.get(splitLine[0]);
			
			if (cliCommand == null) {
				// no command found
				System.out.println("command "+splitLine[0]+" not found");
			} else {
				cliCommand.run(splitLine);
			}
		
			
		} else {
			//nothing or only spaces => do nothing
		}
	}
	
	String commandName;
	
	public void register(String commandName) {
		this.commandName = commandName; 
		registry.put(commandName,this);
	}
	
	
	public abstract void run(String[] args);
}

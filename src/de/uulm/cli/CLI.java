package de.uulm.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import de.uulm.crossorgcompliance.CrossOrgBPC;
import de.uulm.simpleecrg.CharMarker;
import de.uulm.simpleecrg.Ecrg;
import de.uulm.simpleecrg.Pattern;
import dk.brics.automaton.Automaton;

public class CLI extends CrossOrgBPC {

	private static final String ERROR = "An error occured";

	static Map<String, Automaton> automatons = new TreeMap<String, Automaton>();
	
	static Map<String, String> processes = new TreeMap<String, String>();
	
	static Map<String, Automaton> processAutomatons = new TreeMap<String, Automaton>();

	static Map<String, Ecrg<Character>> ecrgs = new TreeMap<String, Ecrg<Character>>();
	
	static Map<String, Automaton> ecrgAutomatons = new TreeMap<String, Automaton>();

	static String EXIT = "exit";

	static String PROMPT = "> ";

	static String WELCOME = " ********************************************************************************" + "\n"
			+ " ***** Simple Compliance Verication Tool *** UUlm, UVienna, TUM, alphaQuest *****" + "\n"
			+ " ********************************************************************************";

	static String GOODBYE = " ********************************************************************************" + "\n"
			+ " Thank you for using the SCV." + "\n" + "  Sincerely, David Knuplesch" + "\n"
			+ " ********************************************************************************";

	public static void main(String[] args) {
		CLI cli = new CLI();
		cli.registerCommands();
		cli.run();

	}
	
	static Map <String,String> help = new TreeMap<String, String>();

	private void register(String commandName, CLICommand command) {
		command.register(commandName);
		//help.put("("+commandName+")","");
	}
	
	private void register(String commandName, String documentation, CLICommand command) {
		command.register(commandName);
		help.put(commandName,documentation);
	}
	
	

	protected void registerCommands() {
		
		help.put(EXIT,
				PROMPT+EXIT+"\n\n"
				+"Closes the application.");
		
		register("hello", new CLICommand() {
			@Override
			public void run(String[] args) {
				System.out.println();
				for (int i = 1; i < args.length; i++) {
					String arg = args[i];
					System.out.println("Hello " + arg);
				}
			}
		});

		register("help",
				
				PROMPT+"help [<command>]\n\n"
						+"Provides information about the given command.\n"
						+"If no command is provided a list of all commands is printed.\n\n"
						+"<command> the name of a command\n\n"
						+"Example: help anyCommand",
						
			new CLICommand() {
			@Override
			public void run(String[] args) {

				if (args.length > 1) {
					String command = args[1];
					String documentation = help.get(command);
					if (documentation == null || documentation.isEmpty()) {
						System.out.println("Command "+command+" has no documentation or does not exist.");
					} else {
						System.out.println(documentation);
					}
				} else {
					System.out.println();
					System.out.println("Commands:");
					System.out.println();
					for (String command : help.keySet()) {
						System.out.println(command);
					}
					System.out.println();
				} 
			}
		});
		
		
		register("listProcesses",
				
				PROMPT+"listProcesses\n\n"
						+"Lists all processes and process choreorgraphies.\n",
						
			new CLICommand() {
			@Override
			public void run(String[] args) {
				
				System.out.println();
				if (processes.size() > 0) {
					System.out.println("Processes:");
					System.out.println();
					for (String process : processes.keySet()) {
						System.out.println(process);
					}
				} else {
					System.out.println("No processes found.");
				}
				System.out.println();
				 
			}
		});
		
		register("printProcess",
				
				PROMPT+"printProcess <process>\n\n"
						+"prints the definition of a process or process choreography.\n",
						
			new CLICommand() {
			@Override
			public void run(String[] args) {
				
				System.out.println();
				String processDefinition = processes.get(args[1]);
				if (processDefinition != null) {
					System.out.println(args[1] +": "+processDefinition);
				} else {
					System.out.println("Process +"+args[1]+" not found.");
				}
				System.out.println();
				 
			}
		});
		
		register("listEcrgs",
				
				PROMPT+"listEcrgs\n\n"
						+"Lists all eCRGs.\n",
						
			new CLICommand() {
			@Override
			public void run(String[] args) {
				
				System.out.println();
				if (ecrgs.size() > 0) {
					System.out.println("eCRGs:");
					System.out.println();
					for (String ecrg : ecrgs.keySet()) {
						System.out.println(ecrg);
					}				
				} else {
					System.out.println("No eCRGs found.");
				}
				System.out.println();
				 
			}
		});
		
		register("createProcess",
				
				PROMPT+"createProcess <name> <process>\n\n"
						+"Creates a new process automaton.\n\n"
						+"<name> the name of the new process\n"
						+"<process> a description of a process using the following gateways/operators:\n"
						+"  | xor, % parallel, (..) sequence,\n"
						+"  (..)+ loop, (..)* optional loop,\n"
						+"  for tasks use single characters from a-z and 0-9\n"
						+"  for outgoing message use upper case characters from A-Z\n"
						+"  for incoming message use corresponding lower case characters from a-z\n\n"
						+"Example: createProcess newProcess Ma(b|(cd)+)enf",						
			
			new CLICommand() {

			@Override
			public void run(String[] args) {
				Automaton automaton = createAutomaton(args[2]);
				System.out.println("Process " + args[1] + " created.");
				System.out.println(numbers(automaton));
				processes.put(args[1], args[2]);
				processAutomatons.put(args[1], automaton);

			}

		});
		
		register("createProcessChoreography", 
				
				PROMPT+"createProcessChoreography <name> <messages> <process>+\n\n"
						+"Constructs a new process choreography out of a set of processes and messages.\n\n"
						+"<name> the name of the new process choreography\n"
						+"<messages> a string containing all chars that are used as messages\n"
						+"<process> the names of the processes that are combined\n\n"
						+"Example: createProcessChoreography newChoreography mno process1 process2",
				
			new CLICommand() {

			@Override
			public void run(String[] args) {
				String name = args[1];
				String messages = args[2];
				Automaton messageConstraint = messageConstraints(messages);
				
				Automaton[] partnerProcesses = new Automaton[args.length-3];
				
				StringBuffer description = new StringBuffer("(");
				
				for (int i = 3; i < args.length; i++) {
					partnerProcesses[i-3] = processAutomatons.get(args[i]);
					description.append(args[i]).append(": ").append(processes.get(args[i])).append("|");
				}				
				description.append("messages: ").append(messages).append(")");
				
				Automaton automaton = intersect(combineParallel(partnerProcesses),messageConstraint);
				
				System.out.println("Process (Choreography) " + args[1] + " created.");
				System.out.println(numbers(automaton));
				processes.put(name, description.toString());
				processAutomatons.put(name, automaton);

			}

		});

		register("verifyLocalCompliance", 
				
				PROMPT+"verifyLocalCompliance <process> <ecrg> +\n\n"
						+"Verifies if a process complies with a given eCRG.\n"
						+"Provides a counterexample in case compliance is violated.\n\n"
						+"<process> the name of the process to be verified\n"
						+"<ecrg> the name of the compliance rule to be checked\n"
						+"\n"
						+"Example: verifyLocalCompliance myProcess myComplianceRule",
				
			new CLICommand() {

			@Override
			public void run(String[] args) {
				System.out.println("check");
				System.out.println(complies(processAutomatons.get(args[1]), ecrgAutomatons.get(args[2])));
			}

		});
		
		register("verifyGlobalCompliance", 
				
				PROMPT+"verifyGlobalCompliance <choreography> <ecrg> <privateTasks> <assertion>*\n\n"
						+"Verifies if the public view on a process choreography complies with a given eCRG taking a set of assertions into account.\n"
						+"Provides a counterexample in case compliance may be violated.\n\n"
						+"<choreography> the name of the process choreography to be verified\n"
						+"<ecrg> the name of the compliance rule to be checked\n"
						+"<privateTasks> a string containing all chars that refer to private tasks\n"
						+"<assertion> the name of the eCRGs that are used as assertions\n"
						+"\n"
						+"Example: verifyGlobalCompliance myProcessChoreography myComplianceRule abcdstv assertion1 assertion2",
				
			new CLICommand() {

			@Override
			public void run(String[] args) {
				Automaton choreography = processAutomatons.get(args[1]);
				Automaton eCRG = ecrgAutomatons.get(args[2]);				
				String privateTasks = args[3];
				Automaton extendedGlobalModelAutomaton = extendParallel(choreography,privateTasks);
				Automaton[] extendedGlobalModelAutomatonAndAssertions = new Automaton[args.length-3];
				extendedGlobalModelAutomatonAndAssertions[0] = extendedGlobalModelAutomaton;
				for (int i = 4; i < args.length; i++) {
					extendedGlobalModelAutomatonAndAssertions[i-3] = ecrgAutomatons.get(args[i]);
				}				
				Automaton extendedGlobalModelAutomatonWithAssertions = intersect(extendedGlobalModelAutomatonAndAssertions);
				
				
				System.out.println("check");
				System.out.println(complies(extendedGlobalModelAutomatonWithAssertions, eCRG));
			}

		});
		
		register("verifyEcrgDecomposition", 
				
				PROMPT+"verifyEcrgDecomposition <globalEcrg> <decompositionEcrg>+ \n\n"
						+"Verifies if a given global eCRG can be decomposed into a set of local eCRGs.\n"
						+"Provides a counterexample in case the decomposition is invalid.\n\n"
						+"<globalEcrg> the name of the global eCRG to be decomposed\n"
						+"<decompositionEcrg> the names of the eCRGs that are part of the decomposition\n"
						+"\n"
						+"Example: verifyEcrgDecomposition globalEcrg decomposedEcrg1 decomposedEcrg2",
				
			new CLICommand() {

			@Override
			public void run(String[] args) {
				Automaton globalEcrg = ecrgAutomatons.get(args[1]);								
				Automaton[] decompositionEcrgs = new Automaton[args.length-2];				
				for (int i = 2; i < args.length; i++) {
					decompositionEcrgs[i-2] = ecrgAutomatons.get(args[i]);
				}				
				Automaton decompositionAutomaton = intersect(decompositionEcrgs);
				
				
				System.out.println("check");
				System.out.println(complies(decompositionAutomaton, globalEcrg));
			}

		});
		/*
		register("verifyProcessWithAssertions", new CLICommand() {

			@Override
			public void run(String[] args) {
				System.out.println("check");
				System.out.println(complies(processAutomatons.get(args[1]), ecrgAutomatons.get(args[2])));
			}

		});


		register("intersect", new CLICommand() {

			@Override
			public void run(String[] args) {

				Automaton automaton = intersect(automatons.get(args[2]), automatons.get(args[3]));
				System.out.println(numbers(automaton));

				automatons.put(args[1], automaton);
			}

		});*/

		register("createEcrg",
				
				PROMPT+"createEcrg <name> <numberOfNodes> <numberOfEdges>\n\n"
						+"Creates a new eCRG definition.\n\n"
						+"<name> the name of the new eCRG\n"
						+"<numberOfNodes> the number of nodes of the new eCRG\n"
						+"<numberOfEdges> the number of edges of the new eCRG\n\n"
						+"Example: createEcrg newEcrg 2 1\n\n"
						+"Use commands defineEcrgNode, defineEcrgEdge, and finalizeEcrg in order to complete the definition of the eCRG:\n\n"
						+"Example [a]->(b):\n"
						+"createEcrg newEcrg 2 1\n"
						+"defineEcrgNode newEcrg 0 AnteOcc a\n" 
						+"defineEcrgNode newEcrg 1 ConsOcc b\n"
						+"defineEcrgEdge newEcrg 0 ConsOcc 0 1 \n"
						+"finalizeEcrg newEcrg\n",
						
			new  CLICommand() {

			@Override
			public void run(String[] args) {

				String name = args[1];
				Integer nodes = Integer.valueOf(args[2]);
				Integer edges = Integer.valueOf(args[3]);

				Ecrg<Character> ecrg = new Ecrg<Character>(nodes, edges, Character.class);

				ecrgs.put(name, ecrg);

				System.out.println("Ecrg " + name + " created.");
			}

		});

		register("defineEcrgNode",
				
				PROMPT+"defineEcrgNode <ecrg> <id> <pattern> <action>\n\n"
						+"Adds a node to an eCRG definition.\n\n"
						+"<ecrg> the name of the eCRG\n"
						+"<id> the id of the new node\n"
						+"<pattern> := AnteOcc|AnteAbs|ConsOcc|ConsAbs the (sub-)pattern of the new node\n"
						+"<action> a char representing the corresponding task or message \n\n"
						+"Example: defineEcrgNode newEcrg 0 AnteOcc a\n",

			new CLICommand() {

			@Override
			public void run(String[] args) {
				String name = args[1];
				Ecrg<Character> ecrg = ecrgs.get(name);
				Integer id = Integer.valueOf(args[2]);
				Pattern pattern = Pattern.valueOf(args[3]);
				Character activity = args[4].charAt(0);
				Ecrg.defineNode(ecrg, id, pattern, activity);

				System.out.println("Added " + pattern + " node " + name + " with id " + id + " to ecrg " + name + ".");
			}

		});

		register("defineEcrgEdge",
				
				PROMPT+"defineEcrgEdge <ecrg> <id> <pattern> <node1> <node2>\n\n"
						+"Adds an sequence flow edge to an eCRG definition.\n\n"
						+"<ecrg> the name of the eCRG\n"
						+"<id> the id of the new edge\n"
						+"<pattern> := AnteOcc|ConsOcc the pattern of the new node\n"
						+"<node1> the id of the source node of the edge \n"
						+"<node1> the id of the target node of the edge \n\n"
						+"Example: defineEcrgEdge newEcrg 0 ConsOcc 0 1 \n",

			new CLICommand() {

			@Override
			public void run(String[] args) {
				String name = args[1];
				Ecrg<Character> ecrg = ecrgs.get(name);
				Integer id = Integer.valueOf(args[2]);
				Pattern pattern = Pattern.valueOf(args[3]);
				Integer n1 = Integer.valueOf(args[4]);
				Integer n2 = Integer.valueOf(args[5]);
				Ecrg.defineEdge(ecrg, id, pattern, n1, n2);

				System.out.println("Added " + pattern + " edge with id " + id + " from node " + n1 + " to node " + n2
						+ " to ecrg " + name + ".");
			}

		});

		register("finalizeEcrg",
				
				PROMPT+"finalizeEcrg <ecrg> \n\n"
						+"Completes and compiles the definition of an eCRG.\n"
						+"Note, that only compiled eCRGs can be used for compliance verification\n\n"
						+"<ecrg> the name of the eCRG\n\n"
						+"Example: finalizeEcrg newEcrg\n",

			new CLICommand() {

			@Override
			public void run(String[] args) {
				String name = args[1];
				Ecrg<Character> ecrg = ecrgs.get(name);
				Ecrg.finalizeEcrg(ecrg);

				Automaton automaton = new CharMarker(ecrg).toAutomaton();
				System.out.println("Finalized Ecrg " + name + " as automaton.");
				System.out.println(numbers(automaton));
				ecrgAutomatons.put(args[1], automaton);

			}

		});

	}

	public void run() {
		Scanner scanner = new Scanner(System.in);

		System.out.println(WELCOME);

		System.out.println();

		System.out.print(PROMPT);

		// get their input as a String
		String line = scanner.nextLine();

		while (!line.equals(EXIT)) {

			try {
				String[] brokenLines = line.trim().split("\\n+");

				for (String singleLine : brokenLines) {
					singleLine = singleLine.trim();
					if (!singleLine.isEmpty()) {
						CLICommand.executeCommandLine(singleLine.trim());
					}
				}
			} catch (RuntimeException e) {
				System.out.println(ERROR);
				System.out.println(e);
			} finally {

				System.out.println();
				System.out.print(PROMPT);

				line = scanner.nextLine();
			}
		}

		System.out.println();
		System.out.println(GOODBYE);

		scanner.close();

	}

}

package de.uulm.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import de.uulm.crossorgcompliance.CrossOrgBPC;
import de.uulm.simpleecrg.CharMarker;
import de.uulm.simpleecrg.Ecrg;
import de.uulm.simpleecrg.Pattern;
import dk.brics.automaton.Automaton;

public class CLI extends CrossOrgBPC {

	private static final String ERROR = "An error occured";

	static Map<String, Automaton> automatons = new HashMap<String, Automaton>();

	static Map<String, Ecrg<Character>> ecrgs = new HashMap<String, Ecrg<Character>>();

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

	private void register(String commandName, CLICommand command) {
		command.register(commandName);
	}

	protected void registerCommands() {
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

		register("createAutomaton", new CLICommand() {

			@Override
			public void run(String[] args) {
				Automaton automaton = createAutomaton(args[2]);
				System.out.println("Automaton " + args[1] + " created.");
				System.out.println(numbers(automaton));
				automatons.put(args[1], automaton);

			}

		});

		register("complies", new CLICommand() {

			@Override
			public void run(String[] args) {
				System.out.println("check");
				System.out.println(complies(automatons.get(args[1]), automatons.get(args[2])));
			}

		});

		register("intersect", new CLICommand() {

			@Override
			public void run(String[] args) {

				Automaton automaton = intersect(automatons.get(args[2]), automatons.get(args[3]));
				System.out.println(numbers(automaton));

				automatons.put(args[1], automaton);
			}

		});

		register("createEcrg", new CLICommand() {

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

		register("defineEcrgNode", new CLICommand() {

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

		register("defineEcrgEdge", new CLICommand() {

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

		register("finalizeEcrg", new CLICommand() {

			@Override
			public void run(String[] args) {
				String name = args[1];
				Ecrg<Character> ecrg = ecrgs.get(name);
				Ecrg.finalizeEcrg(ecrg);

				Automaton automaton = new CharMarker(ecrg).toAutomaton();
				System.out.println("Finalized Ecrg " + name + " as automaton.");
				System.out.println(numbers(automaton));
				automatons.put(args[1], automaton);

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
					CLICommand.executeCommandLine(singleLine.trim());
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

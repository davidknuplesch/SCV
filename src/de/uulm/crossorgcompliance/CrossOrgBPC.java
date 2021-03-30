package de.uulm.crossorgcompliance;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.ShuffleRegExp;

public class CrossOrgBPC {

	protected static Automaton createAutomaton(String interactionModel) {
		Automaton automaton = new ShuffleRegExp(interactionModel).toAutomaton();
		return automaton;
	}

	private static Automaton shuffleTasks(String tasks) {
		String regExp = "([" + tasks + "]*)";
		Automaton a = createAutomaton(regExp);
		a.minimize();
		return a;
	}

	private static Automaton messageConstraint(char messageChar) {
		char sendMessage = Character.toUpperCase(messageChar);
		char recMessage = Character.toLowerCase(messageChar);
		StringBuilder sb = new StringBuilder();
		sb.append("([^");
		sb.append(sendMessage);
		sb.append(recMessage);
		sb.append("]*)");
		sb.append("(((");
		sb.append(sendMessage);
		sb.append(recMessage);
		sb.append(")([^");
		sb.append(sendMessage);
		sb.append(recMessage);
		sb.append("]*))*)");
		String regExp = sb.toString();
		//System.out.println(regExp);
		Automaton a = createAutomaton(regExp);
		a.minimize();
		return a;
	}
	
	protected static Automaton combineParallel(String...automatonStrings){
		Automaton[] automatons = new Automaton[automatonStrings.length];
		for (int i = 0; i < automatonStrings.length; i++) {
			automatons[i] = createAutomaton(automatonStrings[i]);
		}
		return combineParallel(automatons);
	}
	
	protected static Automaton combineParallel(Automaton...automatons){
		Automaton a = new ShuffleRegExp("").toAutomaton();
		for (Automaton a2 : automatons){
			//System.out.println(":"+a2.getNumberOfStates());
			a = a.shuffle(a2);
			a.minimize();
			//System.out.println("+ "+a2.getNumberOfStates() +" => "+ a.getNumberOfStates());
		}
		return a;
	}
	
	protected static Automaton extendParallel(Automaton automaton, String tasks){
		Automaton a = automaton.shuffle(shuffleTasks(tasks));
		a.minimize();
		return a;
	}
	
	protected static Automaton intersect(Automaton... automatons){
		Automaton a = new ShuffleRegExp("(.*)").toAutomaton();
		for (Automaton a2 : automatons){
			a = a.intersection(a2);
			a.minimize();
		}
		return a;
	}

	protected static Automaton messageConstraints(String messageChars) {
		return messageConstraints(messageChars.toCharArray());
	}

	private static Automaton messageConstraints(char... messageChars) {
		Automaton a = (new ShuffleRegExp(".*")).toAutomaton();
		for (char messageChar : messageChars) {
			a = a.intersection(messageConstraint(messageChar));
			a.minimize();
		}
		return a;
	}
	
	protected static boolean complies(Automaton model, Automaton constraint) {
		Automaton intersection = model.intersection(constraint.complement());
		//System.out.println("sizes: "+numbers(model)+", "+numbers(constraint)+", " + numbers(intersection));
		if (!((intersection.isEmpty()) || intersection.isEmptyString())) {
			String counterexample = intersection.getShortestExample(true);
			System.out.println("\n"+"CounterExample: "+counterexample);// + "-" + constraint.run(counterexample));
		}
		return (intersection.isEmpty() || intersection.isEmptyString());
	}

	protected static boolean compliable(Automaton model, Automaton constraint) {
		Automaton intersection = model.intersection(constraint);
		//System.out.println("sizes: "+numbers(model)+", "+numbers(constraint)+", " + numbers(intersection));
		return !((intersection.isEmpty()) || intersection.isEmptyString());
	}
	
	protected static boolean compliable(Automaton model, Automaton... constraints) {
		Automaton intersectionConstraints = intersect(constraints);
		Automaton intersection = model.intersection(intersectionConstraints);
		//System.out.println("sizes: "+numbers(model)+", "+numbers(intersectionConstraints)+", " + numbers(intersection));
		return !((intersection.isEmpty()) || intersection.isEmptyString());
	}
	
	protected static String numbers(Automaton globalModelAutomaton) {
		return ("States: "+globalModelAutomaton.getNumberOfStates()+" / Transitions: "+globalModelAutomaton.getNumberOfTransitions());
	}

}
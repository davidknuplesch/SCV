package de.uulm.crossorgcompliance;

import de.uulm.simpleecrg.CharMarker;
import de.uulm.simpleecrg.Ecrg;
import de.uulm.simpleecrg.Pattern;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.ShuffleRegExp;

public class healthcare extends CrossOrgBPC {
	public static void main(String[] args) {
		
		/*
		 * MESSAGES: 
		 * s Sample (Gynecologist -> Lab)
		 * r Result (Lab -> Gynecologist)
		 * p Patient Data (Gynecologist -> Hospital)
		 * f Result (forwarded) (Gynecologist -> Hospital)
		 * 
		 * Tasks (public) 
		 * B Blood Test 
		 * S send Sample 
		 * s receive Sample 
		 * R send Result 
		 * r receive Result 
		 * E Examine Patient 
		 * H admit patient into hospital
		 * P send Patient Data 
		 * p receive Patient Data 
		 * I Inform Patient
		 * F send Result (forwarded) 
		 * f receive Result (forwarded) 
		 * A Analyze Sample
		 * M Monitor/Observe Patient
		 * 
		 * Tasks (private) 
		 * D Destroy Sample (Lab)
		 * 
		 */

		String messages = "srpf";
		String allTasks = "BEHPIAMD";
		String privateTasks = "D";
		
		String interactionModel = "((sr)%(p?))(((p?)f)?)";
		
		String publicModelGynecologist = "((BSr)%(E((HP)?)))(I|(((IHP)?)F))";
		String publicModelLab = "(sAR)";
		String publicModelHospital = "(p(f%M))";

		
		Automaton interactionModelAutomaton = createAutomaton(interactionModel);

		Automaton publicModelGynecologistAutomaton = createAutomaton(publicModelGynecologist);
		Automaton publicModelLabAutomaton = createAutomaton(publicModelLab);
		Automaton publicModelHospitalAutomaton = createAutomaton(publicModelHospital);
		
		Automaton messageFlowAutomaton = messageConstraints(messages);

		Automaton globalModelAutomaton =  intersect(combineParallel(publicModelGynecologistAutomaton, publicModelLabAutomaton, publicModelHospitalAutomaton),messageFlowAutomaton);

		Automaton complianceRule_r1 = ecrgR1();
		Automaton complianceRule_r2 = ecrgR2();
		Automaton complianceRule_r3 = ecrgR3();
		Automaton assertion_r4 = ecrgR4();
		
		Automaton extendedInteractionModelAutomaton = extendParallel(interactionModelAutomaton,allTasks); 
				
		Automaton extendedGlobalModelAutomaton = extendParallel(globalModelAutomaton,privateTasks);
		
		Automaton extendedGlobalModelAutomatonWithAssertions = intersect(extendedGlobalModelAutomaton,assertion_r4);
		

		System.out.println("*");
		
		
		
		
		System.out.println("Interaction model is compliable with compliance rule r1? "+compliable(extendedInteractionModelAutomaton, complianceRule_r1));

		System.out.println("Interaction model is compliable with compliance rule r2? "+compliable(extendedInteractionModelAutomaton, complianceRule_r2));
		
		System.out.println("Interaction model is compliable with compliance rule r3? "+compliable(extendedInteractionModelAutomaton, complianceRule_r3));
		System.out.println("Interaction model is compliable with assertion r4? "+compliable(extendedInteractionModelAutomaton, assertion_r4));
		
		System.out.println("Interaction model is compliable with compliance rules r1, r2, r3, and assertion r4? "+compliable(extendedInteractionModelAutomaton, complianceRule_r1, complianceRule_r2, complianceRule_r3, assertion_r4));
		
		
		System.out.println("Interaction model is compliable with compliance rule r5? "+compliable(extendedInteractionModelAutomaton, ecrgR5()));
		
		System.out.println("*");
		
		System.out.println("Global model complies with compliance rule r1? "+complies(extendedGlobalModelAutomaton, complianceRule_r1));
		System.out.println("Global model complies with compliance rule r2? "+complies(extendedGlobalModelAutomaton, complianceRule_r2));
		System.out.println("Global model complies with compliance rule r3? "+complies(extendedGlobalModelAutomaton, complianceRule_r3));
		System.out.println("Global model complies with assertion r4? "+complies(extendedGlobalModelAutomaton, assertion_r4));
		
		System.out.println("Extended global model complies with compliance rule r1? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r1));

	}

	

	/*
	 * After a blood test (B), the blood sample has to be destroyed (D).
	 */
	static private Automaton ecrgR1() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'B');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'D');
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	/*
	 * The hospital shall only receive patient data (p) if the gynecologist has
	 * admitted the patient to hospital (H) before.
	 */

	static private Automaton ecrgR2() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'H');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'p');
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	/*
	 * After the gynecologist has admitted the patient to hospital (H), she has
	 * to be observed in the hospital (M).
	 */
	static private Automaton ecrgR3() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'H');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'M');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	/*
	 * Laboratory ensures to destroy the blood sample (D) after analysis (A).
	 */
	static private Automaton ecrgR4() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'A');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'D');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	
	static private Automaton ecrgR5() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'r');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 's');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	
	

	static private void run(Automaton a, String s) {
		System.out.println("run '" + s + "': " + a.run(s));
	}
}

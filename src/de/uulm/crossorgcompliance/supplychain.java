package de.uulm.crossorgcompliance;

import de.uulm.simpleecrg.CharMarker;
import de.uulm.simpleecrg.Ecrg;
import de.uulm.simpleecrg.Pattern;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.ShuffleRegExp;

public class supplychain extends CrossOrgBPC {
	public static void main(String[] args) {
		
		/*
		 * MESSAGES: 
		 * a Order
		 * b Order Intermediate a
		 * c Order Intermediate b
		 * d Forward Order Intermediate a
		 * e Order Special Transport
		 * f Request Details
		 * g Transport Details
		 * h Waybill Intermediate a
		 * i Intermediate a on the road
		 * j Quality Report intermediate a
		 * k Arrival of Intermediate a
		 * l Arrival of Intermediate b
		 * m Status Report Start of Production
		 * n Status Report Production Finished
		 * o Delivery of Product
		 * 
		 * 
		 * Tasks (public) 
		 * P Process Order
		 * Q Calculated Demand for Intermediate a 
		 * R Calculated Demand for Intermediate b
		 * S* Get Permission of Authority
		 * T Produce Intermediate a
		 * U* Full Quality Test Intermediate a
		 * V* Full Quality Test Intermediate b
		 * W Prepare Transport 
		 * X* Safety Check
		 * Y Transport Intermediate a
		 * Z* Quick Test Intermediate a
		 * 0* Quick Test Intermediate b
		 * 1 Prepare Processing Intermediate a
		 * 2 Process Intermediate a 
		 * 3 Compare Test Results for Intermediate b
		 * 4 Production
		 * 5 Final Test
		 * 6 Prepare Delivery
		 * 7 Compare Test Results for Intermediate a
		 * 8* Transport Intermediate b
		 * 
		 */

		String messages = "abcdefghijklm";
		String messages2 = "defgh";
		String messages3 = "aclmno";
		String allTasks = "NOPQRSTUVWXYZ123";
		String privateTasks = "SUVXZ08";
		
		String publicModel_BulkBuyer = "(Amno)";
		String publicModel_Manufacturer = "(aP((QBi((kU)%(1))2)%(RCl3))M4N56O)";
		String publicModel_Middleman = "bDE"; //"bDQE";
		String publicModel_SpecialCarrier = "(eFghIYK)";
		String publicModel_Supplier_A = "(d((fG)%(TW))H)";	
		String publicModel_Supplier_B = "(cL)";

		Automaton publicModel_BulkBuyer_Automaton = createAutomaton(publicModel_BulkBuyer);
		Automaton publicModel_Manufacturer_Automaton = createAutomaton(publicModel_Manufacturer);
		Automaton publicModel_Middleman_Automaton = createAutomaton(publicModel_Middleman);
		Automaton publicModel_SpecialCarrier_Automaton = createAutomaton(publicModel_SpecialCarrier);
		Automaton publicModel_Supplier_A_Automaton = createAutomaton(publicModel_Supplier_A);
		Automaton publicModel_Supplier_B_Automaton = createAutomaton(publicModel_Supplier_B);

		Automaton messageFlowAutomaton = messageConstraints(messages);
		
		Automaton messageFlowAutomaton2 = messageConstraints(messages2);
		Automaton messageFlowAutomaton3 = messageConstraints(messages3);

		Automaton complianceRule_r1 = ecrgR1();
		Automaton complianceRule_r2a = ecrgR2a();
		Automaton complianceRule_r2b = ecrgR2b();
		Automaton complianceRule_r3a = ecrgR3a();
		//Automaton complianceRule_r3b = ecrgR3b();
		Automaton complianceRule_r4a = ecrgR4a();
		Automaton complianceRule_r4b = ecrgR4b();
		Automaton complianceRule_r5a = ecrgR5a();
		Automaton complianceRule_r5b = ecrgR5b();
		
		Automaton assertion_A11a = ecrgA11a();
		Automaton assertion_A11b = ecrgA11b();
		Automaton assertion_A12a = ecrgA12a();
		Automaton assertion_A12b = ecrgA12b();
		Automaton assertion_A2 = ecrgA2();
		Automaton assertion_A3 = ecrgA3();
		Automaton assertion_A4 = ecrgA4();
		Automaton assertion_Aia = ecrgAia();
		Automaton assertion_Aib = ecrgAib();
		
		Automaton supplyChainIntermediate_A =  intersect(combineParallel(
												publicModel_Middleman_Automaton,
												publicModel_SpecialCarrier_Automaton,
												publicModel_Supplier_A_Automaton
											), 
											messageFlowAutomaton2);
		
		Automaton mainProcess =  intersect(combineParallel(
												publicModel_BulkBuyer_Automaton, 
												publicModel_Manufacturer_Automaton,
												publicModel_Supplier_B_Automaton
											), 
											messageFlowAutomaton3);
		
		System.out.println("Automaton for supply chain of Intermediate A: "+numbers(supplyChainIntermediate_A));
		System.out.println();
		
		System.out.println("Automaton for the remaining process: "+numbers(mainProcess));
		System.out.println();
		
		Automaton globalModelAutomaton = intersect(combineParallel(supplyChainIntermediate_A,
				mainProcess
			), 
			messageFlowAutomaton);
		
		System.out.println("GlobalModel Automaton: "+numbers(globalModelAutomaton));
		System.out.println();
		
		
		
		//Automaton extendedInteractionModelAutomaton = extendParallel(interactionModelAutomaton,allTasks); 
				
		Automaton extendedGlobalModelAutomaton = intersect(extendParallel(globalModelAutomaton,
																		privateTasks
																		), 
																		messageFlowAutomaton);
		
		System.out.println("ExtendedGlobalModel Automaton: "+numbers(extendedGlobalModelAutomaton));
		
		Automaton extendedGlobalModelAutomatonWithAssertions = intersect(extendedGlobalModelAutomaton,
																			assertion_A11a,
																			assertion_A11b,
																			assertion_A12a,
																			assertion_A12b,
																			assertion_A2,
																			assertion_A3,
																			assertion_A4,
																			assertion_Aia,
																			assertion_Aib);
		

		System.out.println();
		System.out.println();
		System.out.println("1. Compliance Checking without Assertions (againgst GlobalModel)");
		System.out.println();
		
		
		System.out.println("GlobalModel complies with compliance rule r1?  "+complies(extendedGlobalModelAutomaton, complianceRule_r1));
		System.out.println("GlobalModel complies with compliance rule r2a? "+complies(extendedGlobalModelAutomaton, complianceRule_r2a));
		System.out.println("GlobalModel complies with compliance rule r2b? "+complies(extendedGlobalModelAutomaton, complianceRule_r2b));
		System.out.println("GlobalModel complies with compliance rule r3a? "+complies(extendedGlobalModelAutomaton, complianceRule_r3a));
		//System.out.println("GlobalModel complies with compliance rule r3b? "+complies(extendedGlobalModelAutomaton, complianceRule_r3b));
		System.out.println("GlobalModel complies with compliance rule r4a? "+complies(extendedGlobalModelAutomaton, complianceRule_r4a));
		System.out.println("GlobalModel complies with compliance rule r4b? "+complies(extendedGlobalModelAutomaton, complianceRule_r4b));
		System.out.println("GlobalModel complies with compliance rule r5a? "+complies(extendedGlobalModelAutomaton, complianceRule_r5a));
		System.out.println("GlobalModel complies with compliance rule r5b? "+complies(extendedGlobalModelAutomaton, complianceRule_r5b));

		System.out.println();
		System.out.println();
		System.out.println("2. Compliance Checking with Assertions (against ExtendedGlobalModel)");
		System.out.println();
		
		System.out.println("ExtendedGlobalModel complies with compliance rule r1?  "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r1));
		System.out.println("ExtendedGlobalModel complies with compliance rule r2a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r2a));
		System.out.println("ExtendedGlobalModel complies with compliance rule r2b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r2b));
		System.out.println("ExtendedGlobalModel complies with compliance rule r3a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r3a));
		//System.out.println("ExtendedGlobalModel complies with compliance rule r3b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r3b));
		System.out.println("ExtendedGlobalModel complies with compliance rule r4a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r4a));
		System.out.println("ExtendedGlobalModel complies with compliance rule r4b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r4b));
		System.out.println("ExtendedGlobalModel complies with compliance rule r5a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r5a));
		System.out.println("ExtendedGlobalModel complies with compliance rule r5b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r5b));

		// after change
		System.out.println();
		System.out.println();
		System.out.println("Change occurs");
		System.out.println();
		String publicModel_Manufacturer_2 = "(aP((QBji((k)%(1))2)%(RCl3))M4N56O)";
		String publicModel_Supplier_A_2 = "(d((fG)%(TJW))H)";
		Automaton assertion_A5 = ecrgA5();
		
		Automaton publicModel_Manufacturer_Automaton_2 = createAutomaton(publicModel_Manufacturer_2);
		Automaton publicModel_Supplier_A_Automaton_2 = createAutomaton(publicModel_Supplier_A_2);

		
		supplyChainIntermediate_A =  intersect(combineParallel(
															publicModel_Middleman_Automaton,
															publicModel_SpecialCarrier_Automaton,
															publicModel_Supplier_A_Automaton_2
														), 
														messageFlowAutomaton2);

		mainProcess =  intersect(combineParallel(
															publicModel_BulkBuyer_Automaton, 
															publicModel_Manufacturer_Automaton_2,
															publicModel_Supplier_B_Automaton
														), 
														messageFlowAutomaton3);
		
		System.out.println("Automaton for supply chain of Intermediate A: "+numbers(supplyChainIntermediate_A));
		System.out.println();
		
		System.out.println("Automaton for the remaining process: "+numbers(mainProcess));
		System.out.println();
		
		globalModelAutomaton = intersect(combineParallel(supplyChainIntermediate_A,
															mainProcess
														), 
														messageFlowAutomaton);
		
		System.out.println("GlobalModel Automaton: "+numbers(globalModelAutomaton));
		System.out.println();
		
				
		extendedGlobalModelAutomaton = intersect(extendParallel(globalModelAutomaton,
																privateTasks
																), 
																messageFlowAutomaton);
		
		System.out.println("ExtendedGlobalModel Automaton: "+numbers(extendedGlobalModelAutomaton));
		
		extendedGlobalModelAutomatonWithAssertions = intersect(extendedGlobalModelAutomaton,
											assertion_A11a,
											assertion_A11b,
											assertion_A12a,
											assertion_A12b,
											assertion_A2,
											assertion_A3,
											assertion_A4,
											assertion_A5,
											assertion_Aia,
											assertion_Aib);
		
		
		System.out.println();
		System.out.println();
		System.out.println("3. Compliance Checking without Assertions (againgst GlobalModel)");
		System.out.println();
		
		
		System.out.println("GlobalModel complies with compliance rule r1?  "+complies(extendedGlobalModelAutomaton, complianceRule_r1));
		System.out.println("GlobalModel complies with compliance rule r2a? "+complies(extendedGlobalModelAutomaton, complianceRule_r2a));
		System.out.println("GlobalModel complies with compliance rule r2b? "+complies(extendedGlobalModelAutomaton, complianceRule_r2b));
		System.out.println("GlobalModel complies with compliance rule r3a? "+complies(extendedGlobalModelAutomaton, complianceRule_r3a));
		//System.out.println("GlobalModel complies with compliance rule r3b? "+complies(extendedGlobalModelAutomaton, complianceRule_r3b));
		System.out.println("GlobalModel complies with compliance rule r4a? "+complies(extendedGlobalModelAutomaton, complianceRule_r4a));
		System.out.println("GlobalModel complies with compliance rule r4b? "+complies(extendedGlobalModelAutomaton, complianceRule_r4b));
		System.out.println("GlobalModel complies with compliance rule r5a? "+complies(extendedGlobalModelAutomaton, complianceRule_r5a));
		System.out.println("GlobalModel complies with compliance rule r5b? "+complies(extendedGlobalModelAutomaton, complianceRule_r5b));
		
		System.out.println();
		System.out.println();
		System.out.println("4. Compliance Checking witht Assertions (against ExtendedGlobalModel)");
		System.out.println();
		
		System.out.println("ExtendedGlobalModel complies with compliance rule r1?  "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r1));
		System.out.println("ExtendedGlobalModel complies with compliance rule r2a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r2a));
		System.out.println("ExtendedGlobalModel complies with compliance rule r2b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r2b));
		System.out.println("ExtendedGlobalModel complies with compliance rule r3a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r3a));
		//System.out.println("ExtendedGlobalModel complies with compliance rule r3b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r3b));
		System.out.println("ExtendedGlobalModel complies with compliance rule r4a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r4a));
		System.out.println("ExtendedGlobalModel complies with compliance rule r4b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r4b));
		System.out.println("ExtendedGlobalModel complies with compliance rule r5a? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r5a));
		System.out.println("ExtendedGlobalModel complies with compliance rule r5b? "+complies(extendedGlobalModelAutomatonWithAssertions, complianceRule_r5b));
		

	}



	

	

	
	static Automaton ecrgR1() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, '5');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	

	static Automaton ecrgR2a() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'U');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, '4');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgR2b() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'V');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, '4');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	static Automaton ecrgR3a() {
		Ecrg<Character> ecrg = new Ecrg<Character>(3, 2, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'X');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'S');	
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, 'Y');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.ConsOcc, 1, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgR3b() {
		Ecrg<Character> ecrg = new Ecrg<Character>(3, 2, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'X');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'S');	
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, '8');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.ConsOcc, 1, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgR4a_original() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'Z');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, '7');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	
	static Automaton ecrgR4b_orgiginal() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, '0');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, '3');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgR4a() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 0, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'Z');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, '7');	
		//Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	
	static Automaton ecrgR4b() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 0, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, '0');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, '3');	
		//Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgR5a() {
		Ecrg<Character> ecrg = new Ecrg<Character>(5, 6, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'U');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'Y');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteAbs, 'U');
		Ecrg.defineNode(ecrg, 3, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 4, Pattern.ConsOcc, 'Z');	
		
		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteOcc, 1, 3);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteAbs, 1, 2);
		Ecrg.defineEdge(ecrg, 3, Pattern.AnteAbs, 2, 3);
		Ecrg.defineEdge(ecrg, 4, Pattern.ConsOcc, 1, 4);
		Ecrg.defineEdge(ecrg, 5, Pattern.ConsOcc, 4, 3);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgR5b() {
		Ecrg<Character> ecrg = new Ecrg<Character>(5, 6, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'V');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, '8');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteAbs, 'V');
		Ecrg.defineNode(ecrg, 3, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 4, Pattern.ConsOcc, '0');	
		
		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteOcc, 1, 3);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteAbs, 1, 2);
		Ecrg.defineEdge(ecrg, 3, Pattern.AnteAbs, 2, 3);
		Ecrg.defineEdge(ecrg, 4, Pattern.ConsOcc, 1, 4);
		Ecrg.defineEdge(ecrg, 5, Pattern.ConsOcc, 4, 3);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	
	
	static Automaton ecrgA11a() {
		Ecrg<Character> ecrg = new Ecrg<Character>(4, 5, Character.class);

		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'k');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteAbs, 'U');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 3, Pattern.ConsOcc, 'Z');	
		

		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteAbs, 0, 1);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteAbs, 1, 2);
		Ecrg.defineEdge(ecrg, 3, Pattern.ConsOcc, 0, 3);
		Ecrg.defineEdge(ecrg, 4, Pattern.ConsOcc, 3, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgA11b() {
		Ecrg<Character> ecrg = new Ecrg<Character>(4, 5, Character.class);

		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'l');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteAbs, 'V');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 3, Pattern.ConsOcc, '0');	
		

		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteAbs, 0, 1);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteAbs, 1, 2);
		Ecrg.defineEdge(ecrg, 3, Pattern.ConsOcc, 0, 3);
		Ecrg.defineEdge(ecrg, 4, Pattern.ConsOcc, 3, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgA12a() {
		Ecrg<Character> ecrg = new Ecrg<Character>(4, 3, Character.class);

		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'k');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'U');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 3, Pattern.ConsAbs, 'Z');	
		

		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteOcc, 1, 2);
		//Ecrg.defineEdge(ecrg, 3, Pattern.ConsAbs, 0, 3);
		//Ecrg.defineEdge(ecrg, 4, Pattern.ConsAbs, 3, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgA12b() {
		Ecrg<Character> ecrg = new Ecrg<Character>(4, 3, Character.class);

		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'l');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'V');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 3, Pattern.ConsAbs, '0');	
		

		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteOcc, 1, 2);
		//Ecrg.defineEdge(ecrg, 3, Pattern.ConsAbs, 0, 3);
		//Ecrg.defineEdge(ecrg, 4, Pattern.ConsAbs, 3, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgA12a_original() {
		Ecrg<Character> ecrg = new Ecrg<Character>(4, 5, Character.class);

		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'k');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'U');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 3, Pattern.ConsAbs, 'Z');	
		

		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteOcc, 1, 2);
		Ecrg.defineEdge(ecrg, 3, Pattern.ConsAbs, 0, 3);
		Ecrg.defineEdge(ecrg, 4, Pattern.ConsAbs, 3, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgA12b_original() {
		Ecrg<Character> ecrg = new Ecrg<Character>(4, 5, Character.class);

		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'l');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'V');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, '4');
		Ecrg.defineNode(ecrg, 3, Pattern.ConsAbs, '0');	
		

		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.AnteOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 2, Pattern.AnteOcc, 1, 2);
		Ecrg.defineEdge(ecrg, 3, Pattern.ConsAbs, 0, 3);
		Ecrg.defineEdge(ecrg, 4, Pattern.ConsAbs, 3, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	

	static Automaton ecrgA2() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'S');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'E');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgA3() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'X');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'Y');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgA4() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'V');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'L');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	static Automaton ecrgA5() {
		Ecrg<Character> ecrg = new Ecrg<Character>(2, 1, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'U');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'J');	
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgAia() {
		Ecrg<Character> ecrg = new Ecrg<Character>(3, 2, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, 'Y');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'k');	
		Ecrg.defineNode(ecrg, 2, Pattern.ConsAbs, 'Y');
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 1, Pattern.ConsOcc, 1, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}
	
	static Automaton ecrgAib() {
		Ecrg<Character> ecrg = new Ecrg<Character>(3, 2, Character.class);

		Ecrg.defineNode(ecrg, 0, Pattern.ConsOcc, '8');
		Ecrg.defineNode(ecrg, 1, Pattern.AnteOcc, 'l');	
		Ecrg.defineNode(ecrg, 2, Pattern.ConsAbs, '8');
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 1, Pattern.ConsOcc, 1, 2);

		Ecrg.finalizeEcrg(ecrg);

		return new CharMarker(ecrg).toAutomaton();
	}

	static void run(Automaton a, String s) {
		System.out.println("run '" + s + "': " + a.run(s));
	}
}

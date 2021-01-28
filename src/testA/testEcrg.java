package testA;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uulm.simpleecrg.CharMarker;
import de.uulm.simpleecrg.Ecrg;
import de.uulm.simpleecrg.EcrgState;
import de.uulm.simpleecrg.EcrgStateComparator;
import de.uulm.simpleecrg.Marker;
import de.uulm.simpleecrg.MarkingStructure;
import de.uulm.simpleecrg.NodeState;
import de.uulm.simpleecrg.Pattern;
import dk.brics.automaton.Automaton;

public class testEcrg {
	public static void main(String[] args){
		Ecrg<Character> ecrg = new Ecrg<Character>(2,1, Character.class);
		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'c');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'f');
		//Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, 'e');
		
		//Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 0, Pattern.ConsOcc, 0, 1);
		//Ecrg.defineEdge(ecrg, 2, Pattern.ConsOcc, 1, 2);
		
		
		Ecrg.finalizeEcrg(ecrg);
		
		CharMarker mk = new CharMarker(ecrg);
		mk.printMarking(mk.initialMarking());
		System.out.println("********");
		System.out.println();
				
		
		
	/*	EcrgState<Character> initialEcrgState = mk.initialEcrgState();
		mk.printEcrgState(initialEcrgState);

		Queue<EcrgState<Character>> q = new LinkedList<EcrgState<Character>>();
		q.add(initialEcrgState);
	
		Map<EcrgState<Character>,EcrgState<Character>> map = new TreeMap<EcrgState<Character>,EcrgState<Character>>(new EcrgStateComparator<Character>());
		map.put(initialEcrgState,initialEcrgState);
		
		while(!q.isEmpty()){
			//Set<EcrgState<Character>> tset = new HashSet<EcrgState<Character>>();
			//Set<EcrgState<Character>> tset = mk.calcSuccessors(q.poll(), map);
			q.addAll(mk.calcSuccessors(q.poll(), map));
			for (EcrgState<Character> es : tset){
				if (map.add(es)) {
					q.add(es);
				} else {
					//System.out.print("?-");
					//mk.printEcrgState(es);
				}
			}
		}
		
		Automaton a = new Automaton();
		a.setInitialState(initialEcrgState.getState());
		a.minimize();
		
		
		
		System.out.println("********");
		
		for (EcrgState<Character> es : map.keySet()){
			mk.printEcrgState(es);
		}
			*/
		Automaton a = mk.toAutomaton();
		
		run(a,"x");
		run(a,"c");
		run(a,"cf");
		run(a,"cfc");
		run(a,"cc");
		run(a,"ccf");
		run(a,"cfxx");
		run(a,"cfcdfxx");
		run(a,"cfcfccx");
		
		System.out.println("********");
		
		ecrg = new Ecrg<Character>(3,3, Character.class);
		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteOcc, 'c');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'd');
		Ecrg.defineNode(ecrg, 2, Pattern.AnteOcc, 'e');
		
		Ecrg.defineEdge(ecrg, 0, Pattern.AnteOcc, 0, 2);
		Ecrg.defineEdge(ecrg, 1, Pattern.ConsOcc, 0, 1);
		Ecrg.defineEdge(ecrg, 2, Pattern.ConsOcc, 1, 2);
		
		
		Ecrg.finalizeEcrg(ecrg);
		
		mk = new CharMarker(ecrg);
		
		a = mk.toAutomaton();
		
		run(a,"cde");
		run(a,"cd");
		run(a,"cxe");
		run(a,"ce");
		run(a,"cc");
		run(a,"ccf");
		run(a,"cfxx");
		run(a,"cfcdfxx");
		run(a,"cfcfccx");
		
		System.out.println("********");
		
		ecrg = new Ecrg<Character>(2,0, Character.class);
		
		Ecrg.defineNode(ecrg, 0, Pattern.AnteAbs, 'c');
		Ecrg.defineNode(ecrg, 1, Pattern.ConsOcc, 'e');
		
		//Ecrg.defineEdge(ecrg, 0, Pattern.ConsAbs, 0, 1);
		
		Ecrg.finalizeEcrg(ecrg);
		
		mk = new CharMarker(ecrg);
		
		a = mk.toAutomaton();
		
		run(a,"ce");
		run(a,"cd");
		run(a,"ex");
		
		//TODO klappt noch nicht!!
		run(a,"ec");
		
		
		
		
		
		//System.out.println(a.toString());
		
	}
	
	static private void run(Automaton a, String s){
		System.out.println("run '"+s+"': "+a.run(s));
	}
}

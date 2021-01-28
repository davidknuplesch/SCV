package de.uulm.crossorgcompliance;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class supplychain2 extends supplychain {
	
	public static void main (String[] args){
		
		run(ecrgR5b(),"AaPQBbDdRCcSEeFfGgTUJjV8Ll3VWHhIi1XYKk2MmU4Nn56Oo");
		run(ecrgR5b(),"AaPQBbDdRCcSEeFfGgTVLl038WHhIi1XYKkU2Mm4Nn56Oo");
		run(ecrgR5b(),"AaPQBbDdEeFfGgRCcLl3TJjV8WHhIi1YKk2Mm4Nn56Oo");
		run(ecrgR5b(),"8AaPQBbDdRCcSEeFfGgTUJjVLl038WHhIi1XYKk2MmU4Nn56Oo");
		run(ecrgR5b(),"V8Ll3VWHhIi1XYKk2MmU4");
		
		Automaton a = ecrgR5b();
		
		for (State s : a.getStates()){
			Character old = null;
			for (Transition t : s.getTransitions()){
				
				if (old != null && t.getMin() <= old && t.getMax() >= old){
					System.out.println("found: " + t.getMin() + " <= " + old + " <= " + t.getMax());
				}
				old = t.getMin();
			}
		}
	}
}

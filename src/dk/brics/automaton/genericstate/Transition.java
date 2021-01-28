package dk.brics.automaton.genericstate;

import testA.GenericState;
import testA.GenericTransition;

public class Transition extends GenericTransition<Character> {

	public Transition(Character min, Character max, GenericState<Character> to) {
		super(min, max, to);
	}
	
	public Transition(Character c, GenericState<Character> to) {
		super(c, to);
	}

	/**
	 * 
	 */
	static final long serialVersionUID = 40001;

}

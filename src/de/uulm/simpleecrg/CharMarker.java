package de.uulm.simpleecrg;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.Transition;

public class CharMarker extends Marker<Character>{

	public CharMarker(Ecrg<Character> e) {
		super(e);
		
	}
	
	@Override
	protected void addTransition(Character ca, EcrgState<Character> es, EcrgState<Character> es2) {
		es.getState().addTransition(new Transition(ca,es2.getState()));
		
	}

	@Override
	protected Character getMin() {
		return Character.MIN_VALUE;
	}

	@Override
	protected Character getMax() {
		return Character.MAX_VALUE;
	}

	@Override
	protected Character getPredecessor(Character t) {
		return (char)(t-1);
	}

	@Override
	protected Character getSuccessor(Character t) {
		return (char)(t+1);
	}

	@Override
	protected void addTransition(Character cstart, Character cend, EcrgState<Character> es, EcrgState<Character> es2) {
		es.getState().addTransition(new Transition(cstart,cend,es2.getState()));
		
	}

	
	public Automaton toAutomaton(){
		EcrgState<Character> initialEcrgState = initialEcrgState();
		
		Queue<EcrgState<Character>> q = new LinkedList<EcrgState<Character>>();
		q.add(initialEcrgState);
	
		Map<EcrgState<Character>,EcrgState<Character>> map = new TreeMap<EcrgState<Character>,EcrgState<Character>>(new EcrgStateComparator<Character>());
		map.put(initialEcrgState,initialEcrgState);
		
		while(!q.isEmpty()){
			
			q.addAll(calcSuccessors(q.poll(), map));
			
		}
		
		Automaton a = new Automaton();
		a.setInitialState(initialEcrgState.getState());
		//a.setDeterministic(false); 
		a.minimize();
		return a;
	}
}

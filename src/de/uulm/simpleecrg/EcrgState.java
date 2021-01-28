package de.uulm.simpleecrg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import dk.brics.automaton.State;

public class EcrgState<T extends Comparable<T>> {

	
	public Set<MarkingStructure> structures = new TreeSet<MarkingStructure>(StructureComparator.singleton);
	protected Map<T,Set<ListenerEntry>> listeners = new TreeMap<T,Set<ListenerEntry>>(EntryComparator.singleton);
	protected Map<T,Set<ListenerEntry>> listenersAnte = new TreeMap<T,Set<ListenerEntry>>(EntryComparator.singleton);
	Map<MarkingStructure,MarkingStructure> clones = null;
	Map<NodeState[],NodeState[]> clonedMarkings = null;
	
	private State state = new State();
	
	public EcrgState(MarkingStructure ms) {
		structures.add(ms);
		
		
	}
	
	public EcrgState(EcrgState<T> es) {
		
		clones = new HashMap<MarkingStructure,MarkingStructure>();
		clonedMarkings = new HashMap<NodeState[],NodeState[]>();
		
		for (MarkingStructure ms : es.structures){
			MarkingStructure ms2 = new MarkingStructure(ms,clonedMarkings);
			structures.add(ms2);
			clones.put(ms,ms2);
		}
		/*
		for (T key : listenersAnte.keySet()){
			Set<ListenerEntry> s1 = es.listenersAnte.get(key);
			Set<ListenerEntry> s2 = new TreeSet<ListenerEntry>(ListenerEntryComparator.singleton);
			for (ListenerEntry le : s1){
				ListenerEntry le2 = new ListenerEntry(clones.get(le.ms), null, le.pos);
				s2.add(le2);
			}
			listenersAnte.put(key, s2);
		}
		
		
		for (T key : listeners.keySet()){
			Set<ListenerEntry> s1 = es.listenersAnte.get(key);
			Set<ListenerEntry> s2 = new TreeSet<ListenerEntry>(ListenerEntryComparator.singleton);
			for (ListenerEntry le : s1){
				MarkingStructure ms2 = clones.get(le.ms);
				NodeState[] cms = clonedMarkings.get(le.m);
				ListenerEntry le2 = new ListenerEntry(ms2, cms, le.pos);
				s2.add(le2);
			}
			listeners.put(key, s2);
		} */
	}

	public State getState() {
		return state;
	}

	
	

}

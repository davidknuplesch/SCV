package de.uulm.simpleecrg;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MarkingStructure {
	protected NodeState[] antecedence;
	protected Set<NodeState[]> consequences= new TreeSet<NodeState[]>(StateComparator.singleton);
	
	protected boolean accept;
	protected boolean activated;
	
	protected MarkingStructure(NodeState[] m){
		antecedence = m;
		consequences.add(m.clone());
		//consequences = new TreeSet<NodeState[]>(StateComparator.singleton);
	}
	
	protected MarkingStructure(MarkingStructure ms){
		accept = ms.accept;
		activated = ms.activated;
		antecedence = ms.antecedence.clone();
		for (NodeState[] s : ms.consequences){
			consequences.add(s.clone());
		}
	}
	
	protected MarkingStructure(MarkingStructure ms, Map<NodeState[], NodeState[]> cloneMap){
		accept = ms.accept;
		activated = ms.activated;
		antecedence = ms.antecedence.clone();
		for (NodeState[] s : ms.consequences){
			NodeState[] clone = s.clone();
			consequences.add(clone);
			cloneMap.put(s,clone);
		}
	}
}

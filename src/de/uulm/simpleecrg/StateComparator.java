package de.uulm.simpleecrg;

import java.util.Comparator;

public class StateComparator implements Comparator<NodeState[]> {

	public static StateComparator singleton = new StateComparator();
	
	@Override
	public int compare(NodeState[] arg0, NodeState[] arg1) {
		for (int i = 0; i < arg0.length; i++){
			int c = arg0[i].ordinal() - arg1[i].ordinal();
			if (c != 0) return c;
		}
		return 0;
	}

}

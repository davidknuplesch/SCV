package de.uulm.simpleecrg;

import java.util.Comparator;
import java.util.Iterator;

public class StructureComparator implements Comparator<MarkingStructure> {

	
	static StateComparator sc = StateComparator.singleton;
	
	public static StructureComparator singleton = new StructureComparator();
	
	@Override
	public int compare(MarkingStructure arg0, MarkingStructure arg1) {
		int c = StateComparator.singleton.compare(arg0.antecedence, arg1.antecedence);
		if (c != 0) return c;
		Iterator i0 = arg0.consequences.iterator();
		Iterator i1 = arg1.consequences.iterator();
		
		while(i0.hasNext()){
			if (!i1.hasNext()) return 1;
			
			c = StateComparator.singleton.compare((NodeState[])i0.next(),(NodeState[])i1.next());
			if (c != 0) return c;
		}
		if (i1.hasNext()) return -1;
		
		return 0;
	}

}

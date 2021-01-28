package de.uulm.simpleecrg;

import java.util.Comparator;
import java.util.Iterator;

public class EcrgStateComparator<T extends Comparable<T>> implements Comparator<EcrgState<T>> {

	
	
	public static StructureComparator singleton = new StructureComparator();
	
	
	@Override
	public int compare(EcrgState<T> arg0, EcrgState<T> arg1) {
		Iterator i0 = arg0.structures.iterator();
		Iterator i1 = arg1.structures.iterator();
		
		while(i0.hasNext()){
			if (!i1.hasNext()) return 1;
			
			int c = StructureComparator.singleton.compare((MarkingStructure)i0.next(),(MarkingStructure)i1.next());
			if (c != 0) return c;
		}
		if (i1.hasNext()) return -1;
		
		return 0;

	}

}
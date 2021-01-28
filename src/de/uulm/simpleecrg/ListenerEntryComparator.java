package de.uulm.simpleecrg;

import java.util.Comparator;

public class ListenerEntryComparator implements Comparator<ListenerEntry> {

	public static ListenerEntryComparator singleton = new ListenerEntryComparator();
	
	@Override
	public int compare(ListenerEntry o1, ListenerEntry o2) {
		int c = StateComparator.singleton.compare(o1.m,o2.m);
		if (c != 0) return c;
		
		c = StructureComparator.singleton.compare(o1.ms,o2.ms);
		return c;
	}
	

}

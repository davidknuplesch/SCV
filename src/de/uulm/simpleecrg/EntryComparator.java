package de.uulm.simpleecrg;

import java.util.Comparator;

public class EntryComparator<T extends Comparable<T>> implements Comparator<T> {

	
	
	@SuppressWarnings("rawtypes")
	public static final Comparator singleton = new EntryComparator();

	@Override
	public int compare(T arg0, T arg1) {
		return arg0.compareTo(arg1);
	}

}

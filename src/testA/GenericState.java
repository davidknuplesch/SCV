/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2011 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package testA;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.automaton.Automaton;

/** 
 * <tt>Automaton</tt> state. 
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 */
public class GenericState <T extends Comparable<T>> implements Serializable, Comparable<GenericState<T>> {
	
	static final long serialVersionUID = 30001123;
	
	boolean accept;
	Set<GenericTransition<T>> transitions;
	
	int number;
	
	int id;
	static int next_id;
	
	/** 
	 * Constructs a new state. Initially, the new state is a reject state. 
	 */
	public GenericState() {
		resetTransitions();
		id = next_id++;
	}
	
	/** 
	 * Resets transition set. 
	 */
	final void resetTransitions() {
		transitions = new HashSet<GenericTransition<T>>();
	}
	
	/** 
	 * Returns the set of outgoing transitions. 
	 * Subsequent changes are reflected in the automaton.
	 * @return transition set
	 */
	public Set<GenericTransition<T>> getTransitions()	{
		return transitions;
	}
	
	/**
	 * Adds an outgoing transition.
	 * @param t transition
	 */
	public void addTransition(GenericTransition<T> t)	{
		transitions.add(t);
	}
	
	/** 
	 * Sets acceptance for this state.
	 * @param accept if true, this state is an accept state
	 */
	public void setAccept(boolean accept) {
		this.accept = accept;
	}
	
	/**
	 * Returns acceptance status.
	 * @return true is this is an accept state
	 */
	public boolean isAccept() {
		return accept;
	}
	
	/** 
	 * Performs lookup in transitions, assuming determinism. 
	 * @param c character to look up
	 * @return destination state, null if no matching outgoing transition
	 * @see #step(char, Collection)
	 */
	public GenericState<T> step(T c) {
		for (GenericTransition<T> t : transitions)
			if (c.compareTo(t.min) >= 0 && c.compareTo(t.max) <= 0)
				return t.to;
		return null;
	}

	/** 
	 * Performs lookup in transitions, allowing nondeterminism.
	 * @param c character to look up
	 * @param dest collection where destination states are stored
	 * @see #step(char)
	 */
	public void step(T c, Collection<GenericState<T>> dest) {
		for (GenericTransition<T> t : transitions)
			if (c.compareTo(t.min) >= 0 && c.compareTo(t.max) <= 0)
				dest.add(t.to);
	}

	void addEpsilon(GenericState<T> to) {
		if (to.accept)
			accept = true;
		for (GenericTransition<T> t : to.transitions)
			transitions.add(t);
	}
	
	/** Returns transitions sorted by (min, reverse max, to) or (to, min, reverse max) */
	GenericTransition<T>[] getSortedTransitionArray(boolean to_first) {
		@SuppressWarnings("unchecked")
		GenericTransition<T>[] e = transitions.toArray(new GenericTransition[transitions.size()]);
		Arrays.sort(e, new GenericTransitionComparator<T>(to_first));
		return e;
	}
	
	/**
	 * Returns sorted list of outgoing transitions.
	 * @param to_first if true, order by (to, min, reverse max); otherwise (min, reverse max, to)
	 * @return transition list
	 */
	public List<GenericTransition<T>> getSortedTransitions(boolean to_first)	{
		return Arrays.asList(getSortedTransitionArray(to_first));
	}
	
	/** 
	 * Returns string describing this state. Normally invoked via 
	 * {@link Automaton#toString()}. 
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("state ").append(number);
		if (accept)
			b.append(" [accept]");
		else
			b.append(" [reject]");
		b.append(":\n");
		for (GenericTransition<T> t : transitions)
			b.append("  ").append(t.toString()).append("\n");
		return b.toString();
	}
	
	/**
	 * Compares this object with the specified object for order.
	 * States are ordered by the time of construction.
	 */
	public int compareTo(GenericState<T> s) {
		return s.id - id;
	}

	/**
	 * See {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * See {@link java.lang.Object#hashCode()}.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}

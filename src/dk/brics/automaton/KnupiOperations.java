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

package dk.brics.automaton;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Special automata operations.
 */
final public class KnupiOperations {

	private KnupiOperations() {
	}

	/**
	 * Returns an automaton where all transitions of the given char are replaced
	 * by a string.
	 * 
	 * @param c
	 *            char
	 * @param s
	 *            string
	 * @return new automaton
	 */
	public static Automaton projection(Automaton a, Set<Character> set) {
		a = a.cloneExpandedIfRequired();
		Set<StatePair> epsilons = new HashSet<StatePair>();
		for (State p : a.getStates()) {
			Set<Transition> st = p.transitions;
			p.resetTransitions();
			for (Transition t : st) {
				char min = t.min;
				boolean in = false;
				for (char i = t.min; i <= t.max; i++) {
					if (in) {
						if (!set.contains(i)) {
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
						}
					} else {
						if (set.contains(i)) {
							in = true;
							min = i;
						}
					}
				}
				if (in) {
					p.transitions.add(new Transition(min, t.max, t.to));
					if (min > t.min)
						epsilons.add(new StatePair(p, t.to));
				} else {
					epsilons.add(new StatePair(p, t.to));
				}

			}
		}
		a.addEpsilons(epsilons);
		a.deterministic = false;
		a.removeDeadTransitions();
		a.checkMinimizeAlways();
		return a;
	}

	public static Automaton projection(Automaton a, Set<Character> set, Map<Character, Character> map) {

		// a = projection(a, map.keySet());
		Set<Character> keySet = map.keySet();
		a = a.cloneExpandedIfRequired();
		Set<StatePair> epsilons = new HashSet<StatePair>();
		for (State p : a.getStates()) {
			Set<Transition> st = p.transitions;
			p.resetTransitions();
			for (Transition t : st) {
				boolean eps = false;
				char min = t.min;
				boolean in = false;
				for (char i = t.min; i <= t.max; i++) {
					if (in) {
						if (!set.contains(i)) {
							eps = true;
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
						} else if (keySet.contains(i)) {
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
							char c = map.get(i);
							p.transitions.add(new Transition(c, c, t.to));
						}
					} else {
						if (set.contains(i)) {
							if (keySet.contains(i)) {
								char c = map.get(i);
								p.transitions.add(new Transition(c, c, t.to));
							} else {
								in = true;
								min = i;
							}
						} else {
							eps = true;
						}
					}
				}
				if (in) {
					p.transitions.add(new Transition(min, t.max, t.to));
				}
				if (eps) {
					epsilons.add(new StatePair(p, t.to));
				}

			}
		}

		a.addEpsilons(epsilons);
		a.deterministic = false;
		a.removeDeadTransitions();
		a.checkMinimizeAlways();

		return a;
	}

	public static Automaton projection(Automaton a, Map<Character, Character> map) {
		return projection(a, map.keySet(), map);
	}

	public static Automaton projection(Automaton a, Set<Character> set, Map<Character, Character> map,
			Map<Character, Automaton> amap) {

		// a = projection(a, map.keySet());
		Set<Character> keySet = map.keySet();
		Set<Character> akeySet = amap.keySet();
		a = a.cloneExpandedIfRequired();
		Set<StatePair> epsilons = new HashSet<StatePair>();
		for (State p : a.getStates()) {
			Set<Transition> st = p.transitions;
			p.resetTransitions();
			for (Transition t : st) {
				boolean eps = false;
				char min = t.min;
				boolean in = false;
				for (char i = t.min; i <= t.max; i++) {
					if (in) {
						if (!set.contains(i)) {
							eps = true;
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
						} else if (keySet.contains(i)) {
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
							char c = map.get(i);
							p.transitions.add(new Transition(c, c, t.to));
						} else if (akeySet.contains(i)) {
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
							Automaton r = amap.get(i).cloneExpandedIfRequired();
							State s = r.getInitialState();
							epsilons.add(new StatePair(p, s));
							for (State f : r.getAcceptStates()) {
								f.accept = false;
								epsilons.add(new StatePair(f, t.to));
							}
						}
					} else {
						if (set.contains(i)) {
							if (keySet.contains(i)) {
								char c = map.get(i);
								p.transitions.add(new Transition(c, c, t.to));
							} else if (akeySet.contains(i)) {
								Automaton r = amap.get(i).cloneExpandedIfRequired();
								State s = r.getInitialState();
								epsilons.add(new StatePair(p, s));
								for (State f : r.getAcceptStates()) {
									f.accept = false;
									epsilons.add(new StatePair(f, t.to));
								}
							} else {
								in = true;
								min = i;
							}
						} else {
							eps = true;
						}
					}
				}
				if (in) {
					p.transitions.add(new Transition(min, t.max, t.to));
				}
				if (eps) {
					epsilons.add(new StatePair(p, t.to));
				}

			}
		}

		a.addEpsilons(epsilons);
		a.deterministic = false;
		a.removeDeadTransitions();
		a.checkMinimizeAlways();

		return a;
	}

	public static Automaton projectionA(Automaton a, Set<Character> set, Map<Character, Automaton> amap) {

		// a = projection(a, map.keySet());
		// Set<Character> keySet = map.keySet();
		Set<Character> akeySet = amap.keySet();
		a = a.cloneExpandedIfRequired();
		Set<StatePair> epsilons = new HashSet<StatePair>();
		for (State p : a.getStates()) {
			Set<Transition> st = p.transitions;
			p.resetTransitions();
			for (Transition t : st) {
				boolean eps = false;
				char min = t.min;
				boolean in = false;
				for (char i = t.min; i <= t.max; i++) {
					if (in) {
						if (!set.contains(i)) {
							eps = true;
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
						} else if (akeySet.contains(i)) {
							in = false;
							p.transitions.add(new Transition(min, (char) (i - 1), t.to));
							Automaton r = amap.get(i).cloneExpandedIfRequired();
							State s = r.getInitialState();
							epsilons.add(new StatePair(p, s));
							for (State f : r.getAcceptStates()) {
								f.accept = false;
								epsilons.add(new StatePair(f, t.to));
							}
						}
					} else {
						if (set.contains(i)) {
							if (akeySet.contains(i)) {
								Automaton r = amap.get(i).cloneExpandedIfRequired();
								State s = r.getInitialState();
								epsilons.add(new StatePair(p, s));
								for (State f : r.getAcceptStates()) {
									f.accept = false;
									epsilons.add(new StatePair(f, t.to));
								}
							} else {
								in = true;
								min = i;
							}
						} else {
							eps = true;
						}
					}
				}
				if (in) {
					p.transitions.add(new Transition(min, t.max, t.to));
				}
				if (eps) {
					epsilons.add(new StatePair(p, t.to));
				}

			}
		}

		a.addEpsilons(epsilons);
		a.deterministic = false;
		a.removeDeadTransitions();
		a.checkMinimizeAlways();

		return a;
	}

	/**
	 * Returns an automaton that accepts the shuffle (interleaving) of the
	 * languages of the given automata. As a side-effect, both automata are
	 * determinized, if not already deterministic. Never modifies the input
	 * automata languages.
	 * <p>
	 * Complexity: quadratic in number of states (if already deterministic).
	 * <p>
	 * <dl>
	 * <dt><b>Author:</b></dt>
	 * <dd>Torben Ruby &lt;
	 * <a href="mailto:ruby@daimi.au.dk">ruby@daimi.au.dk</a>&gt;</dd>
	 * </dl>
	 */
	public static Automaton shuffleWithSuspend(Automaton a1, Automaton a2, char suspend, char resume) {
		a1.determinize();
		a2.determinize();
		Transition[][] transitions1 = Automaton.getSortedTransitions(a1.getStates());
		Transition[][] transitions2 = Automaton.getSortedTransitions(a2.getStates());
		Automaton c = new Automaton();
		LinkedList<ColoredStatePair> worklist = new LinkedList<ColoredStatePair>();
		HashMap<ColoredStatePair, ColoredStatePair> newstates = new HashMap<ColoredStatePair, ColoredStatePair>();
		State s = new State();
		c.initial = s;
		ColoredStatePair p = new ColoredStatePair(StatePairColor.NONE, s, a1.initial, a2.initial);
		worklist.add(p);
		newstates.put(p, p);
		while (worklist.size() > 0) {
			p = worklist.removeFirst();
			p.s.accept = p.s1.accept && p.s2.accept;

			if (p.c == StatePairColor.NONE) {
				Transition[] t = transitions1[p.s1.number];
				for (int n = 0; n < t.length; n++) {
					if (suspend < t[n].min || suspend > t[n].max) {
						ColoredStatePair q = new ColoredStatePair(p.c, t[n].to, p.s2);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(t[n].min, t[n].max, r.s));
					} else {
						ColoredStatePair q = new ColoredStatePair(StatePairColor.FIRST, t[n].to, p.s2);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(suspend, suspend, r.s));

						if (suspend - t[n].min > 0) {
							q = new ColoredStatePair(p.c, t[n].to, p.s2);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(t[n].min, suspend, r.s));
						}

						if (t[n].max - suspend > 0) {
							q = new ColoredStatePair(p.c, t[n].to, p.s2);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(suspend, t[n].max, r.s));
						}
					}
				}

				t = transitions2[p.s2.number];
				for (int n = 0; n < t.length; n++) {
					if (suspend < t[n].min || suspend > t[n].max) { // listen
						ColoredStatePair q = new ColoredStatePair(p.c, p.s1, t[n].to);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(t[n].min, t[n].max, r.s));
					} else {
						ColoredStatePair q = new ColoredStatePair(StatePairColor.SECOND, p.s1, t[n].to);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(suspend, suspend, r.s));

						if (suspend - t[n].min > 0) {
							q = new ColoredStatePair(p.c, p.s1, t[n].to);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(t[n].min, suspend, r.s));
						}

						if (t[n].max - suspend > 0) {
							q = new ColoredStatePair(p.c, p.s1, t[n].to);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(suspend, t[n].max, r.s));
						}
					}
				}

			} else if (p.c == StatePairColor.FIRST) {
				Transition[] t = transitions1[p.s1.number];
				for (int n = 0; n < t.length; n++) {
					if (resume < t[n].min || resume > t[n].max) { // listen
						ColoredStatePair q = new ColoredStatePair(p.c, t[n].to, p.s2);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(t[n].min, t[n].max, r.s));
					} else {
						ColoredStatePair q = new ColoredStatePair(StatePairColor.NONE, t[n].to, p.s2);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(resume, resume, r.s));

						if (resume - t[n].min > 0) {
							q = new ColoredStatePair(p.c, t[n].to, p.s2);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(t[n].min, resume, r.s));
						}
						if (t[n].min - resume > 0) {
							q = new ColoredStatePair(p.c, t[n].to, p.s2);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(resume, t[n].max, r.s));
						}
					}
				}
			} else if (p.c == StatePairColor.SECOND) {

				Transition[] t = transitions2[p.s2.number];
				for (int n = 0; n < t.length; n++) {
					if (resume < t[n].min || resume > t[n].max) { // listen
						ColoredStatePair q = new ColoredStatePair(p.c, p.s1, t[n].to);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(t[n].min, t[n].max, r.s));
					} else {
						ColoredStatePair q = new ColoredStatePair(StatePairColor.NONE, p.s1, t[n].to);
						ColoredStatePair r = newstates.get(q);
						if (r == null) {
							q.s = new State();
							worklist.add(q);
							newstates.put(q, q);
							r = q;
						}
						p.s.transitions.add(new Transition(resume, resume, r.s));
						if (resume - t[n].min > 0) {
							q = new ColoredStatePair(p.c, p.s1, t[n].to);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(t[n].min, resume, r.s));
						}
						if (t[n].min - resume > 0) {
							q = new ColoredStatePair(p.c, p.s1, t[n].to);
							r = newstates.get(q);
							if (r == null) {
								q.s = new State();
								worklist.add(q);
								newstates.put(q, q);
								r = q;
							}
							p.s.transitions.add(new Transition(resume, t[n].max, r.s));
						}
					}
				}

			}
		}
		c.deterministic = false;
		c.removeDeadTransitions();
		c.checkMinimizeAlways();
		return c;
	}

}

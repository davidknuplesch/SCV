package de.uulm.simpleecrg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

class ListenerEntry {
	MarkingStructure ms;
	NodeState[] m;
	int pos;

	protected ListenerEntry(MarkingStructure ms, NodeState[] m, int pos) {
		this.ms = ms;
		this.m = m;
		this.pos = pos;
	}
}

class ListenerSubEntry{
	MarkingStructure ms;
	NodeState[] m;
	int pos;

	protected ListenerSubEntry(MarkingStructure ms, NodeState[] m, int pos) {
		this.ms = ms;
		this.m = m;
		this.pos = pos;
	}
}

public abstract class Marker<T extends Comparable<T>> {
	protected Ecrg<T> ecrg;

	int nc, ec;

	public Marker(Ecrg<T> e) {
		ecrg = e;
		nc = ecrg.nodes.length;
		ec = ecrg.edges.length;
	}

	private NodeState[] emptyMarking() {
		NodeState[] m = new NodeState[nc + ec];
		// Arrays.fill(m, State.NOT_ACTIVATED);
		return m;
	}

	public NodeState[] initialMarking() {
		NodeState[] m = emptyMarking();
		for (int n = 0; n < nc; n++) {
			if (ecrg.depends[n].length == 0) {
				m[n] = NodeState.ACTIVATED;
			} else {
				m[n] = NodeState.NOT_ACTIVATED;
			}
		}
		for (int n = nc; n < nc + ec; n++) {
			m[n] = NodeState.NOT_ACTIVATED;
		}
		return m;
	}

	public EcrgState<T> initialEcrgState() {
		NodeState[] m = initialMarking();
		MarkingStructure ms = new MarkingStructure(m);
		EcrgState<T> es = new EcrgState<T>(ms);
		calcListeners(ms, es);
		calcAcceptance(es);
		return es;
	}

	private void calcListeners(MarkingStructure ms, EcrgState<T> es) {

		for (int i = 0; i < nc; i++) {
			if (ms.antecedence[i] == NodeState.ACTIVATED
					&& (ecrg.nodes[i] == Pattern.AnteOcc || ecrg.nodes[i] == Pattern.AnteAbs)) {
				addListenerAnte(ecrg.container[i], es, ms, i);
			}
		}
		for (NodeState[] m : ms.consequences) {
			for (int i = 0; i < nc; i++) {
				if (m[i] == NodeState.ACTIVATED
						&& (ecrg.nodes[i] == Pattern.ConsOcc || ecrg.nodes[i] == Pattern.ConsAbs)) {
					addListener(ecrg.container[i], es, ms, m, i);
				}
			}
		}

	}

	public boolean isActivation(NodeState[] s) {
		for (int n = 0; n < nc; n++) {

			switch (ecrg.nodes[n]) {

			case AnteOcc:
				if (s[n] != NodeState.COMPLETED)
					return false;
				break;
			case AnteAbs:
				if (s[n] == NodeState.COMPLETED)
					return false;
				break;
			default:
				break;
			}
		}

		for (int e = nc; e < (nc + ec); e++) {

			switch (ecrg.edges[e - nc]) {

			case AnteOcc:
				if (s[e] != NodeState.COMPLETED)
					return false;
				break;
			default:
				break;
			}
		}

		return true;
	}

	public boolean isFullfilment(NodeState[] s) {
		for (int n = 0; n < nc; n++) {

			switch (ecrg.nodes[n]) {

			case ConsOcc:
				if (s[n] != NodeState.COMPLETED)
					return false;
				break;
			case ConsAbs:
				if (s[n] == NodeState.COMPLETED)
					return false;
				break;
			default:
				break;
			}
		}

		for (int e = nc; e < nc + ec; e++) {

			switch (ecrg.edges[e - nc]) {

			case ConsOcc:
				if (s[e] != NodeState.COMPLETED)
					return false;
				break;
			default:
				break;

			}
		}

		return true;
	}

	public void calcAcceptance(MarkingStructure ms) {
		ms.activated = isActivation(ms.antecedence);

		if (ms.activated) {
			ms.accept = false;
			for (NodeState[] s : ms.consequences) {
				if (isFullfilment(s)) {
					ms.accept = true;
					break;
				}
			}

		} else {
			ms.accept = true;
		}

	}

	public void calcAcceptance(EcrgState<T> es) {
		es.getState().setAccept(true);
		for (MarkingStructure ms : es.structures) {
			calcAcceptance(ms);
			if (!ms.accept) {
				es.getState().setAccept(false);
				return;
			}
		}
		es.getState().setAccept(true);
	}

	private void addListener(T t, EcrgState<T> es, MarkingStructure ms, NodeState[] m, int pos) {
		Set<ListenerEntry> s = es.listeners.get(t);
		if (s == null) {
			s = new TreeSet<ListenerEntry>(ListenerEntryComparator.singleton);
			es.listeners.put(t, s);
		}
		s.add(new ListenerEntry(ms, m, pos));
	}

	private void addListenerAnte(T t, EcrgState<T> es, MarkingStructure ms, int pos) {
		Set<ListenerEntry> s = es.listenersAnte.get(t);
		if (s == null) {
			s = new TreeSet<ListenerEntry>(ListenerEntryComparator.singleton);
			es.listenersAnte.put(t, s);
		}
		s.add(new ListenerEntry(ms, ms.antecedence, pos));
		

	}

	public Set<EcrgState<T>> calcSuccessors(EcrgState<T> es, Map<EcrgState<T>, EcrgState<T>> map) {
		Iterator<T> ia = es.listenersAnte.keySet().iterator();
		Iterator<T> ic = es.listeners.keySet().iterator();

		T ca = null;
		T cc = null;
		T last = this.getMin();
		
		Set<EcrgState<T>> retVal = new TreeSet<EcrgState<T>>(new EcrgStateComparator<T>());
		
		if (ia.hasNext() && ic.hasNext()) {

			ca = ia.next();
			cc = ic.next(); // deal with both lists
			boolean both = true;

			while (both) {
				int c = ca.compareTo(cc);
				if (c < 0) {

					EcrgState<T> es2 = handleAnte(es, ca);
					es2 = putOrGet(map, es2, retVal);
					last = addTransitions(last, ca, es, es2);

					if (ia.hasNext()) {
						ca = ia.next();
					} else {
						// handle cc first!
						es2 =handleCons(es, cc);
						es2 = putOrGet(map, es2, retVal);
						last = addTransitions(last, cc, es, es2);
						break;
					}

				} else if (c > 0) {

					EcrgState<T> es2 =handleCons(es, cc);
					es2 = putOrGet(map, es2, retVal);
					last = addTransitions(last, cc, es, es2);

					if (ic.hasNext()) {
						cc = ic.next();
					} else {
						// handle ca first!
						es2 = handleAnte(es, ca);
						es2 = putOrGet(map, es2, retVal);
						last = addTransitions(last, ca, es, es2);
						break;
					}

				} else { // c == 0

					EcrgState<T> es2 = handleAll(es, cc);
					es2 = putOrGet(map, es2, retVal);
					last = addTransitions(last, cc, es, es2);

					if (ia.hasNext() && ic.hasNext()) {
						ca = ia.next();
						cc = ic.next();
					} else {
						break;
					}
				}
			}

		}

		while (ia.hasNext()) {
			ca = ia.next();
			EcrgState<T> es2 = handleAnte(es, ca);
			es2 = putOrGet(map, es2, retVal);
			last = addTransitions(last, ca, es, es2);
		}

		while (ic.hasNext()) {
			cc = ic.next();
			EcrgState<T> es2 =handleCons(es, cc);
			es2 = putOrGet(map, es2, retVal);
			last = addTransitions(last, cc, es, es2);
		}
		completeTransitions(last,es);
		for (EcrgState<T> e2 : map.keySet()){
			for (MarkingStructure ms : e2.structures){
				calcListeners(ms, e2);
			}
		}
		return retVal;
	}

	private EcrgState<T> putOrGet(Map<EcrgState<T>, EcrgState<T>> map, EcrgState<T> es, Set<EcrgState<T>> newOnes) {
		if (map.containsKey(es)) {
			es = map.get(es);
		} else {
			map.put(es, es);
			newOnes.add(es);
		}
		return es;
	}
	

	private void completeTransitions(T last, EcrgState<T> es) {
		if (last.compareTo(getMax()) <= 0) addTransition(last,getMax(),es,es);
		
	}

	private T addTransitions(T last, T ca, EcrgState<T> es, EcrgState<T> es2) {
		T predecessor = getPredecessor(ca);
		if (last.compareTo(predecessor) <= 0) addTransition(last,predecessor,es,es);
		addTransition(ca,es,es2);
		return getSuccessor(ca);
	}

	protected abstract T getMin();
	protected abstract T getMax();
	protected abstract T getPredecessor(T t);
	protected abstract T getSuccessor(T t);

	// does not affect ordering

	protected abstract void addTransition(T ca, EcrgState<T> es, EcrgState<T> es2);
	protected abstract void addTransition(T cstart, T cend, EcrgState<T> es, EcrgState<T> es2);

	private EcrgState<T> handleAll(EcrgState<T> es, T cc) {
		// TODO Auto-generated method stub
		/// not yet supported
		// TODO !!!

		System.out.println("not yet supported");
		
		return null;
	}

	private EcrgState<T> handleCons(EcrgState<T> es, T cc) {
		EcrgState<T> e2 = new EcrgState<T>(es);
		for (ListenerEntry le : es.listeners.get(cc)) {
			MarkingStructure ms2 = e2.clones.get(le.ms);
			if (ecrg.nodes[le.pos] == Pattern.ConsAbs) {
				ms2.consequences.remove(e2.clonedMarkings.get(le.m)); //
			} else { // Pattern == AnteOcc
				e2.structures.remove(ms2);
				NodeState[] m = le.m.clone();
				m[le.pos] = NodeState.COMPLETED;
				updateConsMarking(es, le.ms, m, le.pos);
				ms2.consequences.add(m);
				e2.structures.add(ms2);
				
			}
		}
		calcAcceptance(e2);
		return e2;
	}

	private void updateConsMarking(EcrgState<T> es, MarkingStructure ms, NodeState[] m, int pos) {

		outer: for (int e : ecrg.out[pos]) {
			m[nc + e] = NodeState.COMPLETED;
			int n = ecrg.to[e];
			if (m[n] == NodeState.NOT_ACTIVATED
					&& (ecrg.nodes[n] == Pattern.ConsOcc || ecrg.nodes[n] == Pattern.ConsAbs)) {
				inner: for (int nd : ecrg.depends[n]) {
					if (ms.antecedence[nd] != NodeState.COMPLETED && m[nd] != NodeState.COMPLETED) {
						continue outer;
					}
				}
				m[n] = NodeState.ACTIVATED;
			}

		}
		final Queue<Integer> q = new LinkedList<Integer>();
		for (int e : ecrg.in[pos]) {
			if (m[nc + e] != NodeState.COMPLETED) {
				m[nc + e] = NodeState.SKIPPED;
				q.add(ecrg.from[e]);
			}
		}
		Integer n = q.poll();
		while (n != null) {
			if (m[n] == NodeState.NOT_ACTIVATED || m[n] == NodeState.ACTIVATED) {
				if (ecrg.nodes[n] == Pattern.ConsOcc) {
					m[n] = NodeState.SKIPPED;
					for (int e : ecrg.in[n]) {
						if (m[nc + e] != NodeState.COMPLETED) {
							m[nc + e] = NodeState.SKIPPED;
							q.add(ecrg.from[e]);
						}
					}
				} else if (ecrg.nodes[n] == Pattern.ConsAbs) {
					m[n] = NodeState.SKIPPED;
					for (int e : ecrg.in[n]) {
						if (m[nc + e] != NodeState.COMPLETED) {
							m[nc + e] = NodeState.SKIPPED;
						}
					}
				}
			}
			n = q.poll();
		}

	}

	private EcrgState<T> handleAnte(EcrgState<T> es, T ca) {

		EcrgState<T> e2 = new EcrgState<T>(es);
		for (ListenerEntry le : es.listenersAnte.get(ca)) {
			if (ecrg.nodes[le.pos] == Pattern.AnteAbs) {
				e2.structures.remove(e2.clones.get(le.ms)); //
			} else { // Pattern == AnteOcc
				MarkingStructure ms = cloneMarkingStructure(le.ms);
				ms.antecedence[le.pos] = NodeState.COMPLETED;
				updateAnteMarking(ms, le.pos);
				updateConsMarkings(ms, le.pos);
				e2.structures.add(ms);
			}
		}
		
		calcAcceptance(e2);
		return e2;
	}

	private void updateConsMarkings(MarkingStructure ms, int pos) {
		for (NodeState[] m : ms.consequences) {
			outer: for (int e : ecrg.out[pos]) {
				if (ecrg.edges[e] == Pattern.ConsOcc || ecrg.edges[e] == Pattern.ConsAbs){
							
					if (m[nc + e] == NodeState.NOT_ACTIVATED) m[nc + e] = NodeState.COMPLETED;
					
					int n = ecrg.to[e];
					if (m[n] == NodeState.NOT_ACTIVATED
							&& (ecrg.nodes[n] == Pattern.ConsOcc || ecrg.nodes[n] == Pattern.ConsAbs)) {
						inner: for (int nd : ecrg.depends[n]) {
							if (ms.antecedence[nd] != NodeState.COMPLETED && m[nd] != NodeState.COMPLETED) {
								continue outer;
							}
						}
						m[n] = NodeState.ACTIVATED;
					}
				}

			}
			final Queue<Integer> q = new LinkedList<Integer>();
			for (int e : ecrg.in[pos]) {
				if (m[nc + e] != NodeState.COMPLETED) {
					if (ecrg.edges[e] == Pattern.ConsOcc || ecrg.edges[e] == Pattern.ConsAbs) m[nc + e] = NodeState.SKIPPED;
					q.add(ecrg.from[e]);
				}
			}
			Integer n = q.poll();
			while (n != null) {
				if (m[n] == NodeState.NOT_ACTIVATED || m[n] == NodeState.ACTIVATED) {
					if (ecrg.nodes[n] == Pattern.ConsOcc) {
						m[n] = NodeState.SKIPPED;
						for (int e : ecrg.in[n]) {
							if (m[nc + e] != NodeState.COMPLETED) {
								m[nc + e] = NodeState.SKIPPED;
								q.add(ecrg.from[e]);
							}
						}
					} else if (ecrg.nodes[n] == Pattern.ConsAbs) {
						m[n] = NodeState.SKIPPED;
						for (int e : ecrg.in[n]) {
							if (m[nc + e] != NodeState.COMPLETED) {
								m[nc + e] = NodeState.SKIPPED;
							}
						}
					}
				}
				n = q.poll();
			}

		}
	}

	private void updateAnteMarking(MarkingStructure ms, int pos) {
		NodeState[] m = ms.antecedence;

		outer: for (int e : ecrg.out[pos]) {

			if (ecrg.edges[e] == Pattern.AnteOcc || ecrg.edges[e] == Pattern.AnteAbs) {
				m[nc + e] = NodeState.COMPLETED;
				int n = ecrg.to[e];
				if (ecrg.nodes[n] == Pattern.AnteOcc || ecrg.nodes[n] == Pattern.AnteAbs) {
					inner: for (int nd : ecrg.depends[n]) {
						if (m[nd] != NodeState.COMPLETED) {
							continue outer;
						}
					}
					m[n] = NodeState.ACTIVATED;
				}
			}

		}
		final Queue<Integer> q = new LinkedList<Integer>();
		for (int e : ecrg.in[pos]) {
			if ((ecrg.edges[e] == Pattern.AnteOcc || ecrg.edges[e] == Pattern.AnteAbs)
					&& m[nc + e] != NodeState.COMPLETED) {
				m[nc + e] = NodeState.SKIPPED;
				q.add(ecrg.from[e]);
			}
		}
		Integer n = q.poll();
		while (n != null) {
			if (m[n] == NodeState.NOT_ACTIVATED || m[n] == NodeState.ACTIVATED) {
				if (ecrg.nodes[n] == Pattern.AnteOcc) {
					m[n] = NodeState.SKIPPED;
					for (int e : ecrg.in[n]) {
						if ((ecrg.edges[e] == Pattern.AnteOcc || ecrg.edges[e] == Pattern.AnteAbs)
								&& m[nc + e] != NodeState.COMPLETED) {
							m[nc + e] = NodeState.SKIPPED;
							q.add(ecrg.from[e]);
						}
					}
				} else if (ecrg.nodes[n] == Pattern.AnteAbs) {
					m[n] = NodeState.SKIPPED;
					for (int e : ecrg.in[n]) {
						if (m[nc + e] != NodeState.COMPLETED) {
							m[nc + e] = NodeState.SKIPPED;
						}
					}
				}
			}
			n = q.poll();
		}

	}

	public void setAntecedence(MarkingStructure ms, int antepos, NodeState ns) {
		ms.antecedence[antepos] = ns;

	}

	protected MarkingStructure cloneMarkingStructure(MarkingStructure ms) {
		return new MarkingStructure(ms);
	}

	protected EcrgState<T> cloneEcrgState(EcrgState<T> es) {
		return new EcrgState<T>(es);
	}

	protected NodeState[] updateConsMarking(MarkingStructure ms, NodeState[] m, int pos, NodeState ns) {
		ms.consequences.remove(m);
		m[pos] = ns;
		ms.consequences.add(m);
		return m;
	}

	protected NodeState[] branchConsMarking(MarkingStructure ms, NodeState[] m, int pos, NodeState ns) {
		NodeState[] m2 = m.clone();
		m2[pos] = ns;
		ms.consequences.add(m2);
		return m2;
	}

	public void printMarking(NodeState[] m) {
		System.out.print("[ " + m[0] + " ");
		;
		for (int i = 1; i < m.length; i++) {
			if (i == nc)
				System.out.print("|");
			System.out.print("| " + m[i] + " ");
		}
		System.out.println("]");
	}

	public void printStructure(MarkingStructure ms) {
		System.out.print("" + (ms.activated ? "!" + (ms.accept ? "+" : "-") : ".."));

		printMarking(ms.antecedence);

		for (NodeState[] m : ms.consequences) {
			System.out.print("  |--");
			printMarking(m);
		}
	}

	public void printEcrgState(EcrgState<T> es) {
		System.out.println(es.getState().isAccept());
		for (MarkingStructure ms : es.structures) {
			printStructure(ms);
			// System.out.println();
		}
		System.out.println(
				"  |-------------------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println();
	}

}

package de.uulm.simpleecrg;

import java.lang.reflect.Array;
import java.util.Arrays;

public class Ecrg<T extends Comparable<?>> {
	
	/* pattern encoding
	 * 0 - AnteOcc
	 * 1 - AnteAbs
	 * 2 - ConsOcc
	 * 3 - ConsAbs
	 */
	
	Pattern[] nodes; //stores pattern;
	
	Pattern[] edges; //stores pattern;
	int[] from;
	int[] to;
	
	int[][] out;	
	int[][] in;
	int[][] depends;
	
	T[] container;	

	protected boolean ready = false;
	
	@SuppressWarnings("unchecked")
	public Ecrg(int nodes, int edges, Class<T> c){
		this.nodes = new Pattern[nodes];
		this.container = (T[])Array.newInstance(c, nodes);
		this.out = new int[nodes][1];
		this.in = new int[nodes][1];
		this.depends = new int[nodes][1];
		this.edges = new Pattern[edges];
		this.from = new int[edges];
		this.to = new int[edges];
		Arrays.fill(from, -1);
		Arrays.fill(to, -1);
	}
	
	public static <T extends Comparable<?>> void defineNode(Ecrg <T>  ecrg, int id, Pattern p, T t){
		if (ecrg.ready) throw new RuntimeException("Ecrg in State ready must not be modified");
		ecrg.nodes[id] = p;
		ecrg.container[id] = t;
	}
	
	public static <T extends Comparable<?>>  void defineEdge(Ecrg <T>  ecrg, int id, Pattern p, int from, int to){
		if (ecrg.ready) throw new RuntimeException("Ecrg in State ready must not be modified");
		
		if (ecrg.from[id] != -1){ // => ecrg.to[id] != -1 as well
			ecrg.out[ecrg.from[id]][0]--;
			ecrg.in[ecrg.to[id]][0]--;
			if (ecrg.edges[id] == ecrg.nodes[ecrg.to[id]]){
				ecrg.depends[ecrg.to[id]][0]--;
			}
		}	
		
		ecrg.edges[id] = p;
		ecrg.from[id] = from;
		ecrg.to[id] = to;
		ecrg.out[from][0]++;
		ecrg.in[to][0]++;
		
		if (p == ecrg.nodes[to]){
			ecrg.depends[to][0]++;
		}
	}
	
	
	public static void finalizeEcrg(Ecrg ecrg){
		int nc = ecrg.nodes.length;
		for (int n = 0; n < nc; n++){
			ecrg.in[n]= new int[ecrg.in[n][0]];
			ecrg.out[n]= new int[ecrg.out[n][0]];
			ecrg.depends[n]= new int[ecrg.depends[n][0]];
		}
		
		int[] io = new int[nc];
		int[] ii = new int[nc];
		int[] id = new int[nc];
		
		for (int e = 0; e < ecrg.edges.length; e++){
			int nto = ecrg.to[e];
			int nfrom = ecrg.from[e];
			
			ecrg.out[nfrom][io[nfrom]++] = e;
			ecrg.in[nto][ii[nto]++] = e;
			
			if (ecrg.edges[e] == ecrg.nodes[nto]){
				ecrg.depends[nto][id[nto]++] = nfrom;
			}
			
			edgeMayConnect(ecrg.edges[e], ecrg.nodes[nfrom], ecrg.nodes[nto]);
		}
		
		
		
	}
	
	
	protected static boolean edgeMayConnect(Pattern ep, Pattern np1, Pattern np2) {

		if (ep.ordinal() < np1.ordinal())
			return false;
		if (ep.ordinal() < np2.ordinal())
			return false;

		final Pattern pt1, pt2;
		if (np1.ordinal() > np2.ordinal()) {
			pt1 = np2;
			pt2 = np1;
		} else {
			pt1 = np1;
			pt2 = np2;
		}

		switch (ep) {
			case AnteOcc: return (pt1 == Pattern.AnteOcc && pt2 == Pattern.AnteOcc);
			case AnteAbs: return (pt1 == Pattern.AnteOcc && pt2 == Pattern.AnteAbs);
			case ConsOcc: return (pt1 == pt2  && (pt1 == Pattern.AnteOcc || pt1 == Pattern.ConsOcc));
			case ConsAbs: return (pt2 == Pattern.ConsAbs  && (pt1 == Pattern.AnteOcc || pt1 == Pattern.ConsOcc));
			default : return false; // not reachable
		}
		
		
	}
	
}

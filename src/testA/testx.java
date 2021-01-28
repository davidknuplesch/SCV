package testA;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeSet;

import de.uulm.simpleecrg.NodeState;

class CompI implements Comparator<Integer[]> {


	public CompI() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Integer[] arg0, Integer[] arg1) {
		return arg0[0] - arg1[0];
	}
	
}

public class testx {
	
	public static void main(String[] args ){
		TreeSet<Integer[]> ts = new TreeSet<Integer[]>(new CompI() );
		
		Integer[] a = {111,2,3};
		Integer[] b = {434,5,777};
		
		ts.add(a);
		ts.add(b);
		
		@SuppressWarnings("unchecked")
		TreeSet<Integer[]> t2 = (TreeSet<Integer[]>) ts.clone();
		TreeSet<Integer[]> t3 = new TreeSet<Integer[]>(new CompI() );
		
		t3.add(a.clone());
		t3.add(b.clone());
		
		a[0] = 999;
		
		for (Integer[] ii : t2){
			for (Integer i : ii){
				System.out.println(i);
			}
		}
		for (Integer[] ii : t3){
			for (Integer i : ii){
				System.out.println(i);
			}
		}
		
		
	}
}

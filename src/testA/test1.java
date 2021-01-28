package testA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.KnupiOperations;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.ShuffleOperations;
import dk.brics.automaton.ShuffleRegExp;
import dk.brics.automaton.SpecialOperations;

public class test1 {

	
	protected static Automaton interactions(int x){
		StringBuffer sb = new StringBuffer("([^sr]*(s0r0");		
		for(int i = 1; i < x; i++){
			sb.append("|s");
			sb.append(i);
			sb.append("r");
			sb.append(i);
		}				
		sb.append("))*[^sr]*");
		System.out.println(sb.toString());
		RegExp rc = new RegExp(sb.toString());
		Automaton ac = rc.toAutomaton();
		return ac;
	}
	
	protected static Automaton actions(char[] c){
		StringBuffer sb = new StringBuffer("(("+c[0]);		
		for(int i = 1; i < c.length; i++){
			sb.append("|"+c[i]);
		}				
		sb.append(")(0|(<1-9>(<0-9>)*)))*");
		System.out.println(sb.toString());
		RegExp rc = new RegExp(sb.toString());
		Automaton ac = rc.toAutomaton();
		return ac;
	}
	
	protected static String implodeActivities(String[] act){
		String s = "((('"+act[0];
		for (int i = 1; i < act.length; i++){
			s = s+"')|('"+act[i];
		}
		return s+"'))*)";
	}
	
	
//	protected static Automaton actions2(char[] c){
//		StringBuffer sb = new StringBuffer("(("+c[0]);		
//		for(int i = 1; i < c.length; i++){
//			sb.append("|"+c[i]);
//		}				
//		sb.append(")(0|(<1-9>(<0-9>)*)))*");
//		System.out.println(sb.toString());
//		RegExp rc = new RegExp(sb.toString());
//		Automaton ac = rc.toAutomaton();
//		return ac;
//	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RegExp r = new RegExp("abs*(c|d)*");
		Automaton a = r.toAutomaton();
		RegExp r2 = new RegExp("xym*z*");		
		Automaton a2 = r2.toAutomaton();
		Automaton a3 = a2.shuffle(a);
		RegExp rc = new RegExp("([^sm]*sm)*[^sm]*");
		Automaton ac = rc.toAutomaton();
		Automaton a4 = a3.intersection(ac);
		 String s = "abxysmsmsmzcdcccdc";
		System.out.println("Match 1: " + a4.run(s)); // prints: true
		Automaton ia = interactions(3);
		 s = "abxys1r1s2r2adfs0r0zcdcccdc";
		System.out.println("Match 2: " + ia.run(s)); // prints: true
		 s = "abxys1r1s2r2adrfs0r0zcdcccdc";
		System.out.println("Match 2': " + ia.run(s)); // prints: false
		Automaton act = actions(new char[]{'a','b','c'});
		 s= "a1b3c0";
		 System.out.println("Match 2+: " + act.run(s)); // prints: false
		ShuffleRegExp sr = new ShuffleRegExp("s((a*b)%(x(e|f)))");
		Automaton as = sr.toAutomaton();
		 s = "saxaeab";
		System.out.println("Match 3: " + as.run(s)); // prints: true
		Automaton at = as.subst('a', "");
		 s = "sxeb";
		System.out.println("Match 4: " + at.run(s)); // prints: true
		
		Set<Character> set = new HashSet<Character>();
		set.add('a');
		set.add('b');
		set.add('d');
		set.add('x');
		/*set.add('c');*/
		
		System.out.println("***********");
		
		ShuffleRegExp sr2 = new ShuffleRegExp("'s1'§a");
		Automaton ar2 = sr2.toAutomaton();
		
		s = "'s1'a";
		
		
		String interactionModelString = "('sample''result')§(('data')?)(('data')?'results')?";
				
		String[] activities = {"test","analyze"};
		
		String enrichedInteractionModelString = "(("+interactionModelString+")§("+implodeActivities(activities)+"))";
		
		//String enrichedInteractionModelString = "("+interactionModelString+")§(('test')|('analyze'))*";
		
		System.out.println(enrichedInteractionModelString);
		
		ShuffleRegExp interactionModel = new ShuffleRegExp(enrichedInteractionModelString);
		Automaton Ai = interactionModel.toAutomaton();
		
		s = "'sample''data''result'";
		System.out.println("Match -1: " + Ai.run(s)); // prints: true
		s = "'sample''data''result''result'";
		System.out.println("Match -1: " + Ai.run(s)); // prints: true
		
		s = "'sampl'de'ata''result'";
		System.out.println("Match -2: " + Ai.run(s)); // prints: false
		
		s = "'sample''data''test''result'";
		System.out.println("Match -3: " + Ai.run(s)); // prints: true
		s = "'sample''data''result''test'";
		
		System.out.println("Match -4: " + Ai.run(s)); // prints: true
		
		String c1 = "~(@*'test'(~(@*'analyze'@*)))";
		RegExp c1reg = new RegExp(c1);
		System.out.println("Match c1: " + !Ai.intersection(c1reg.toAutomaton()).isEmpty()); // prints: true
		
		String c2 = "@'test'(~(@*'analyze'@*))";
		RegExp c2reg = new RegExp(c2);
		System.out.println("Match c2: " + !Ai.intersection(c2reg.toAutomaton()).isEmpty()); // prints: true
		
		String c0 = "@";
		RegExp c0reg = new RegExp(c0);
		
		System.out.println("Match c0: " + !Ai.intersection(c0reg.toAutomaton()).isEmpty()); // prints: true
		
		s = "'sa1'";
		System.out.println("Match 0: " + ar2.run(s)); // prints: true

		s = "a's1'";
		System.out.println("Match 0: " + ar2.run(s)); // prints: true
		
		
		RegExp r31 = new RegExp("{ca(b+)}");	
		Automaton a51 = r31.toAutomaton();
		
				
		RegExp r3 = new RegExp("ca(b+)");	
		Automaton a5 = r3.toAutomaton();
		
		//Automaton ap = a5.projectChars(set);
		
		//System.out.println("Match: " + a5.run(s)); // prints: true
		//System.out.println("Match: " + ap.run(s)); // prints: true
		
		s = "abb";
		Automaton ap = KnupiOperations.projection(a5, set);
		System.out.println("Match 1: " + ap.run(s)); // prints: true
		
		
		
		Map<Character,Character> map = new HashMap<Character,Character>();
		map.put('a','a');
		map.put('b','d');
		Automaton ap2 = KnupiOperations.projection(a5, map);
		System.out.println("Match 2: " + ap2.run(s)); // prints: true

		 s = "add";
		//System.out.println("Match: " + ap3.run(s)); // prints: true
		//System.out.println(ap3.getShortestExample(true));
		// s = "bc";
		System.out.println("Match 3: " + ap2.run(s)); // prints: true
	
		RegExp r4 = new RegExp("(xy)+");	
		Automaton ar = r4.toAutomaton();
		
		Map<Character,Automaton> amap = new HashMap<Character,Automaton>();
		amap.put('a',ar);		
		
		Automaton ap3 = KnupiOperations.projection(ap2, set, new HashMap<Character,Character>(), amap);
		
		s = "xydd";
		System.out.println("Match 4: " + ap3.run(s)); // prints: true
		
		for (String str : ap3.getStrings(4))
		{System.out.println(str);}
		

		
	}

}

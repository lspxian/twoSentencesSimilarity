package fr.ish.shuopeng;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class main {
	
	public static void main(String[] args){
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");;
		StanfordLemmatizer slem = new StanfordLemmatizer();
		
		SentenceSim ss = new SentenceSim(lp,slem);
		
		
		
		
		System.out.println(ss.twoSS("Four boys are standing in front of the burning car.", "Four young men stand still as a car explodes behind them."));
	}
}

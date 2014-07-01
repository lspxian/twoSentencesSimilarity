package fr.ish.shuopeng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class MainClass {
	public static void main(String[] args){
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");;
		StanfordLemmatizer slem = new StanfordLemmatizer();
		
		SentenceSim ss = new SentenceSim(lp,slem);
		
		List<String> sentences1 = new ArrayList<String>();
		List<String> sentences2 = new ArrayList<String>();
		List<Double> values = new ArrayList<Double>();
		
		//read file
		try {
			//BufferedReader br = new BufferedReader(new FileReader("data/STS.input.MSRvid.txt"));
			BufferedReader br = new BufferedReader(new FileReader("data/testFile.txt"));
		
			String line = br.readLine();
			while(line != null){
				sentences1.add(line.substring(0, line.indexOf(".")+1));
				sentences2.add(line.substring(line.indexOf(".")+2));
				line = br.readLine();
			}
			
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//compute
		for(int i=0;i<sentences1.size();i++){
			values.add(ss.twoSS(sentences1.get(i), sentences2.get(i)));
		}
		
		//write file
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/myOutput.txt")));
			
			for(Double dl:values){
				bw.write(dl.toString()+"\n");
			}
			
			bw.close();
			System.out.println("finished!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(ss.twoSS("John hits the ball.", "John hits the ball."));
		//System.out.println(ss.twoSS("Four boys are standing in front of the burning car.", "Four young men stand still as a car explodes behind them."));
	}
}

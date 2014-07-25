package fr.ish.shuopeng;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import Jama.Matrix;
import Jama.SingularValueDecomposition;


public class SentenceSim {
	
	protected LexicalizedParser lp;
	protected StanfordLemmatizer slem;
	
	public SentenceSim(LexicalizedParser lp, StanfordLemmatizer slem){
		this.lp = lp;
		this.slem = slem;
	}
	
	public Matrix setSentenceMatrix(String str,int n, LexicalizedParser lp){
		
		
	    TokenizerFactory<CoreLabel> tokenizerFactory =
	        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	    Tokenizer<CoreLabel> tok =
	        tokenizerFactory.getTokenizer(new StringReader(str));
	    List<CoreLabel> rawWords2 = tok.tokenize();
	    Tree parse = lp.apply(rawWords2);

	    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    List<TypedDependency> tdl = gs.typedDependencies(false);
	    	    
	    //int n = tdl.size();
		double[][] ma = new double[n][n];
		
		for(int i=0;i<tdl.size();i++){
			//gov->dep
			 TypedDependency td = tdl.get(i);
			 TreeGraphNode gov = td.gov();
			 TreeGraphNode dep = td.dep();
			 String gsn = td.reln().getShortName();
			 if(gov.index()!=0){
				 // -1 eliminer le root
				 
				 if(gsn.equals("det")||gsn.equals("cc")){
					 ma[gov.label().index()-1][dep.label().index()-1] = 0.1;
				 }else if(gsn.equals("aux")||gsn.equals("auxpass")||gsn.equals("prep")||gsn.equals("pcomp")||gsn.equals("pobj")){
					 ma[gov.label().index()-1][dep.label().index()-1] = 0.3;
				 }else if(gsn.equals("nsubj")||gsn.equals("nsubjpass")){
					 ma[gov.label().index()-1][dep.label().index()-1] = 2;			 
				 }else if(gsn.equals("dobj")||gsn.equals("conj")){
					 ma[gov.label().index()-1][dep.label().index()-1] = 2;				 
				 }else {
					 ma[gov.label().index()-1][dep.label().index()-1] = 1;				 
				 }
				 //ma[gov.label().index()-1][dep.label().index()-1] = 1;
				 
			 }
		}
		return new Matrix(ma);
	}
	
		public double twoSS(String sen1, String sen2){
			
			System.out.println("similarity : "+ sen1+" & " + sen2);
		
		Matrix simMatRes = this.slem.sentenceSimMatrix(sen1, sen2);
		int m = simMatRes.getRowDimension();
		int n = simMatRes.getColumnDimension();
		
		//System.out.println("Matrice de similarité des mots( normé X)");
		//simMatRes.print(n, 3);
		
		Matrix matrix1 = setSentenceMatrix(sen1,m,this.lp);
		
		//System.out.println("Matrice d'adjacence pour '"+sen1+"' (normé A)");
		//matrix1.print(m, 3);
		
		Matrix matrix2 = setSentenceMatrix(sen2,n,this.lp);
		
		//System.out.println("Matrice d'adjacence pour '"+sen2+"' (normé B)");
		//matrix2.print(n, 3);
		
		for(int j=0;j<1;j++){
		//A*X*t(B) + t(A)*X*B
		simMatRes = matrix1.times(simMatRes).times(matrix2.transpose()).plus(matrix1.transpose().times(simMatRes).times(matrix2));
		
		//System.out.println("calcul S=A*X*t(B) + t(A)*X*B, resultat normé S");
		//simMatRes.print(m, 3);
		
		//normaliser
		//double normf = simMatRes.normF(); 
		//System.out.println(normf);
		//simMatRes.arrayRightDivideEquals(new Matrix(m,n,normf)) ;
		}
		
		SingularValueDecomposition svd = new SingularValueDecomposition(simMatRes);
		Matrix resultMat = svd.getS();
		
		//System.out.println("Matrice diagonale SVD");
		//resultMat.print(resultMat.getColumnDimension(), 4);
		
		double sum = 0;
		
		double produit = resultMat.get(0, 0);
		int i=1;
		
		while(i<n&&resultMat.get(i-1, i-1)/resultMat.get(i, i)<4){
			produit *= resultMat.get(i,i);
			i++;
		}
		
		for(i=0;i<resultMat.getRowDimension();i++){
		//TODO	
			sum += resultMat.get(i,i);
			System.out.print(resultMat.get(i,i));
			System.out.print(" ");
			/*
			if(resultMat.get(i,i)>0.1)
				produit *= resultMat.get(i,i);*/
		}
		System.out.println();
		
		System.out.println("norm2 : "+svd.norm2());
		System.out.println("sum : "+sum);
		System.out.println("produit : "+produit);
		//System.out.println("sim value : "+sum*25/n);		
		//System.out.println("produit : "+produit);
		//System.out.println("sum/(m+n-2) valeur : "+sum/(m+n-2));
		System.out.println();
		return sum;
		//return produit;
		//return 0;
	}
	
	public static void main(String[] args){
		//par default m>n
		
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		StanfordLemmatizer slem = new StanfordLemmatizer();
		
		SentenceSim ss = new SentenceSim(lp,slem);

		//System.out.println("sim value : "+ss.twoSS("Two Sumo ringers are fighting each other.","A man rides a water toy in the water."));
		/*
		ss.twoSS("Three men are playing chess.","Two men are playing chess.");
		ss.twoSS("Three men are playing chess.","Three men are playing chess.");
		ss.twoSS("Two men are playing chess.","Two men are playing chess.");
		*/
		String s1 = "The dog eats the meal.";
		String s2 = "The cat eats the meal.";
		double p1 = ss.twoSS(s1, s2);
		double p2 = ss.twoSS(s1, s1);
		double p3 = ss.twoSS(s2, s2);
		//System.out.println("p1*p1/p2/p3 : "+p1*p1/p2/p3);
		
		
	}
}

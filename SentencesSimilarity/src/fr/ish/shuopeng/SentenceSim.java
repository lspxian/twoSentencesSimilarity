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
	
	public Matrix setSentenceMatrix(String str,LexicalizedParser lp){
		
		
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
	    	    
	    int n = tdl.size();
		double[][] ma = new double[n][n];
		
		for(int i=0;i<n;i++){
			//gov->dep
			 TypedDependency td = tdl.get(i);
			 TreeGraphNode gov = td.gov();
			 TreeGraphNode dep = td.dep();
			 String gsn = td.reln().getShortName();
			 if(gov.index()!=0){
				 // -1 eliminer le root
				 /*
				 if(gsn=="det"){
					 ma[gov.label().index()-1][dep.label().index()-1] = 0.1;
				 }else if(gsn=="aux"){
					 ma[gov.label().index()-1][dep.label().index()-1] = 0.3;
				 }else{
					 ma[gov.label().index()-1][dep.label().index()-1] = 1;				 
				 }*/
				 ma[gov.label().index()-1][dep.label().index()-1] = 1;
				 
			 }
		}
		return new Matrix(ma);
	}
	
		public double twoSS(String sen1, String sen2){
		
		Matrix matrix1 = setSentenceMatrix(sen1,this.lp);
		Matrix matrix2 = setSentenceMatrix(sen2,this.lp);
		int m = matrix1.getRowDimension();
		int n = matrix2.getRowDimension();
		
		Matrix senSim = this.slem.sentenceSimMatrix(sen1, sen2);
		
		//A*X*t(B) + t(A)*X*B
		Matrix simMatRes = matrix1.times(senSim).times(matrix2.transpose()).plus(matrix1.transpose().times(senSim).times(matrix2));
		
		
		SingularValueDecomposition svd = new SingularValueDecomposition(simMatRes);
		Matrix resultMat = svd.getS();
		
		//resultMat.print(arg0, arg1);
		
		double sum = 0;
		
		//double produit = 1;
		for(int i=0;i<resultMat.getRowDimension();i++){
		//TODO	
			sum += resultMat.get(i,i);
			System.out.print(resultMat.get(i,i));
			System.out.print(" ");
			
			/*if(resultMat.get(i,i)!=0)
				produit *= resultMat.get(i,i);*/
		}
		System.out.print("\n");
		System.out.println("similarity : "+ sen1+" & " + sen2);
		System.out.println("sum : "+sum);
		System.out.println("sim value : "+sum/(m+n-2));		
		//System.out.println("produit : "+produit);
		return sum/(m+n-2);
		
	}
	
	public static void main(String[] args){
		//par default m>n
		
		LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");;
		StanfordLemmatizer slem = new StanfordLemmatizer();
		
		SentenceSim ss = new SentenceSim(lp,slem);
		
		System.out.println("sim value : "+ss.twoSS("A woman picks up and holds a baby kangaroo in her arms.", "A woman picks up and holds a baby kangaroo."));
		//System.out.println("sim value : "+ss.twoSS("A plane is taking off.", "An air plane is taking off."));
		//System.out.println("sim value : "+ss.twoSS("A man seated is playing the cello.",	"A man seated is playing the cello."));
		//System.out.println(ss.twoSS("A woman is talking to the man seated beside her.",	"A woman driving a car is talking to the man seated beside her."));
		
	}
}

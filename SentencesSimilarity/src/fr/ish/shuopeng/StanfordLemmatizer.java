package fr.ish.shuopeng;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import Jama.Matrix;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordLemmatizer {

    protected StanfordCoreNLP pipeline;

    public StanfordLemmatizer() {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        /*
         * This is a pipeline that takes in a string and returns various analyzed linguistic forms. 
         * The String is tokenized via a tokenizer (such as PTBTokenizerAnnotator), 
         * and then other sequence model style annotation can be used to add things like lemmas, 
         * POS tags, and named entities. These are returned as a list of CoreLabels. 
         * Other analysis components build and store parse trees, dependency graphs, etc. 
         * 
         * This class is designed to apply multiple Annotators to an Annotation. 
         * The idea is that you first build up the pipeline by adding Annotators, 
         * and then you take the objects you wish to annotate and pass them in and 
         * get in return a fully annotated object.
         * 
         *  StanfordCoreNLP loads a lot of models, so you probably
         *  only want to do this once per execution
         */
        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> lemmatize(String documentText)
    {
        List<String> lemmas = new LinkedList<String>();
        
        Annotation document = new Annotation(documentText);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
            	if(!(token.get(LemmaAnnotation.class).equals("."))){
            		lemmas.add(token.get(LemmaAnnotation.class));
            	}
            }
        }
        return lemmas;
    }
    
    public List<String> partOfSpeech(String documentText)
    {
        List<String> pos = new LinkedList<String>();
        
        Annotation document = new Annotation(documentText);
        this.pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
               if(!(token.get(PartOfSpeechAnnotation.class).equals("."))){
            	   pos.add(token.get(PartOfSpeechAnnotation.class));            	   
               }
            }
        }
        return pos;
    }
    
    
    public Matrix sentenceSimMatrix(String s1, String s2){
    	List<String> lemma1 = this.lemmatize(s1);
    	List<String> lemma2 = this.lemmatize(s2);
    	
    	List<String> pos1 = this.partOfSpeech(s1);
    	List<String> pos2 = this.partOfSpeech(s2);
    	
    	double[][] simmat = new double[lemma1.size()][lemma2.size()];
    	
    	for(int i=0;i<lemma1.size();i++){
    		for(int j=0;j<lemma2.size();j++){
   			
    			if(pos1.get(i).startsWith(".")&&pos2.get(j).startsWith(".")){}
    			else if(lemma1.get(i).equals(lemma2.get(j))){
    				simmat[i][j] = 1;
    			}else if(pos1.get(i).startsWith("NN")&&pos2.get(j).startsWith("NN")){
    				WordSimilarity ws = new WordSimilarity();
    				simmat[i][j] = ws.getSimilarity(lemma1.get(i), lemma2.get(j), "n", "n");
    			}else if(pos1.get(i).startsWith("VB")&&pos2.get(j).startsWith("VB")){
    				WordSimilarity ws = new WordSimilarity();
    				simmat[i][j] = ws.getSimilarity(lemma1.get(i), lemma2.get(j), "v", "v");
    			} 
    			else{
    				simmat[i][j] = 0;
    			}
    		}
    	}
    	
    	return new Matrix(simmat);
    }
    


    public static void main(String[] args) {
    	//par default m>n
    	
        //System.out.println("Starting Stanford Lemmatizer");
       // String text = "A plane is taking off.	An air plane is taking off.";
        //String text = "Once you write the code, Eclipse will tell you to import the MaxentTagger and inform you that it throws some exceptions.	Foxes are eating from a plate.";
        StanfordLemmatizer slem = new StanfordLemmatizer();
        //System.out.println(slem.lemmatize(text));
        
       // List<Matrix> test = slem.similarityMatrix(text);
        Matrix test = slem.sentenceSimMatrix("A boy sits on a bed, sings and plays a guitar", "A boy sits on a bed, sings and plays a guitar");
        System.out.println(test);
    }

}
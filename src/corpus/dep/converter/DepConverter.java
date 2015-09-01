package corpus.dep.converter;

import cdt.Helper;
import corpus.Corpus;
import experiment.TargetElements;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import io.VocabularyMatrixExporter;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.count.CountMatrix;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
public class DepConverter {

    protected HashSet<Runnable> threads;
    protected HashSet<String> targetWords;
	//protected HashMap<String, CountMatrix> targetWordJointCountsMatrixMap;
    
    public DepConverter(HashSet<String> targetWords){
        threads = new HashSet<>();
        this.targetWords = new HashSet<>();
        
        //if no target words are specified use the entire vocabulary
        if(targetWords == null){
            for(int i=0; i<TargetElements.getSize(); i++){
                TargetWord targetWord = Vocabulary.getTargetWord(i);
                this.targetWords.add(targetWord.getWord());
            }
        }
        
        //targetWordJointCountsMatrixMap = new HashMap<>();
        for(String targetWord : targetWords){
            this.targetWords.add(targetWord);
            //targetWordJointCountsMatrixMap.put(targetWord, new CountMatrix());
        }
    }
    
    public synchronized void extractJointCountsFromCorpus(){
		//waitForThreadsToFinish();
		Helper.report("[DepConverter] Extracting joint counts from corpus for " + targetWords.size() + " words...");
		
		//identify corpus files
		File corpusFolder = new File(DepNeighbourhoodSpace.getProjectFolder(), Corpus.getFolderName());
		String[] corpusFilenames = corpusFolder.list();
        //for(String cf : corpusFilenames) System.out.println("corpus file: " + cf); //DEBUG
		Arrays.sort(corpusFilenames);

		
		int maxAmountOfThreads = Helper.getAmountOfCores();
		int fileCounter=0;
		try{
			do{
				
				while(threads.size() < maxAmountOfThreads && fileCounter < corpusFilenames.length){
					Runnable converterThread;
                    converterThread = new DepConverterThread(this, new File(corpusFolder, corpusFilenames[fileCounter]), targetWords);

					threads.add(converterThread);
					(new Thread(converterThread)).start();
					
					fileCounter++;
				}
				
				wait();
			}while(!threads.isEmpty() || fileCounter < corpusFilenames.length);
		}catch(InterruptedException e){}
		
		Helper.report("[DepConverter] ...Finished extracting joint counts from " + fileCounter + " corpus files.");
	}
    
	public synchronized void reportConverterThreadDone(Runnable corpusConverterThread, HashMap<String, CountMatrix> localTargetWordJointCountsMatrixMap){
        //add local LDOPs into global LDOPs
		for(String word : localTargetWordJointCountsMatrixMap.keySet()){
			//CountMatrix jdop = targetWordJointCountsMatrixMap.get(word);
            TargetWord tw = Vocabulary.getTargetWord(word);
            CountMatrix jdop = (CountMatrix) tw.getLexicalRepresentation();
            if(jdop == null){
                jdop = new CountMatrix();
                jdop.setName(word);
            }
            //int existingCard = jdop.getCardinality();
            CountMatrix newJdop = localTargetWordJointCountsMatrixMap.get(word);
            jdop.add(newJdop);
            tw.setLexicalRepresentation(jdop);
            //System.out.println("word: " + word + ", existing counts card: " + existingCard + ", new counts card: " + newJdop.getCardinality() + " sum counts card: " + jdop.getCardinality()); //DEBUG
			//jdop.reduceCardinality(space.getIntegerParameter("jdopscard"));
		}
        
		threads.remove(corpusConverterThread);
		notify();
	}
    
    
    
    //create lexical count matrices from corpus
    public static void main(String[] args){
        
        //File projectFolder1 = new File("/disk/scratch/william/postdoc");
        File projectFolder1 = new File(args[0]);
        DepNeighbourhoodSpace.setProjectFolder(projectFolder1);
        
        File inFile = new File(projectFolder1, "preprocessed/ukwac.depParsed/5up5down/wordsim353/space.test");
        DepNeighbourhoodSpace.importFromFile(inFile);
        
        Corpus.setAmountOfDocuments(-1);
        DepConverter dc = new DepConverter(null);
        dc.extractJointCountsFromCorpus();
        
        VocabularyMatrixExporter me = new VocabularyMatrixExporter();
        me.exportMatricesToFiles(new File(projectFolder1, "experiments/wordsim353/ldops"), 8);
        
    }


}

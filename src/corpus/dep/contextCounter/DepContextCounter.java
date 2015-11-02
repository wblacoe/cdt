package corpus.dep.contextCounter;

import cdt.Helper;
import corpus.Corpus;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import space.dep.DepNeighbourhoodSpace;

//collect counts: for each target word and each context word from each relation, how often does this word appear under this relation ?
//creates one file per relation with these counts
//these counts are necessary to build the dimensions/context words/vocabulary of size [dimensionality] for each relation cluster
public class DepContextCounter /*implements Runnable*/{
	
	protected File contextCountsFolder;
	protected int amountOfSentences;
    
	protected HashSet<Runnable> threads;
    protected DepContextCounts counts;
	
	public DepContextCounter(String corpusFolderName, File contextCountsFile, int amountOfSentences){
        Corpus.setFolderName(corpusFolderName);
		Helper.ensureContainingFolderExists(contextCountsFile);
		threads = new HashSet<>();
        counts = new DepContextCounts();
        this.amountOfSentences = amountOfSentences;
	}
	
    public DepContextCounts getContextCounts(){
        return counts;
    }
	
	//marginalise over all corpus files using threads
	public synchronized void count(){
		Helper.report("[ContextCounter] Counting over all corpus files...");
		
        File corpusFolder = new File(DepNeighbourhoodSpace.getProjectFolder(), Corpus.getFolderName());
		String[] corpusFilenames = corpusFolder.list();
		Arrays.sort(corpusFilenames);
		
		//run each dep marginaliser thread
		for(String corpusFilename : corpusFilenames){
			DepContextCounterThread ccThread = new DepContextCounterThread(this, corpusFilename, new File(corpusFolder, corpusFilename), amountOfSentences);
			threads.add(ccThread);
			(new Thread(ccThread)).start();
		}
		
		//wait for all threads to finish
		try{
			while(!threads.isEmpty()){
				wait();
			}
		}catch(InterruptedException e){}
		
		Helper.report("[ContextCounter] ...Finished counting over all corpus files...");
	}
	public synchronized void reportDepContextCounterThreadDone(DepContextCounterThread ccThread, DepContextCounts localCounts){
		threads.remove(ccThread);
        
        counts.add(localCounts);
		
		notify();
	}
	
	
	/*@Override
	public void run(){
		count();
		saveToFile();
	}
	*/
	
	
	public static void main(String[] args){
		
		if(args.length != 3){
			System.out.println("Wrong amount of arguments! Usage: ContextCounter corpusFolder outputFolder amountOfSentences");
		}else{
			DepContextCounter cc = new DepContextCounter(
                args[0], //absolute path to the folder containing the corpus files
				new File(args[1]), //absolute path of the context counts file
				Integer.parseInt(args[2])  //amount of sentences
			);
			//(new Thread(cc)).start();
            cc.count();
            File file = new File("/local/william/preprocessed/ukwac.depParsed/cc.gz");
            cc.getContextCounts().saveToFile(file);
		}
	}
	
}

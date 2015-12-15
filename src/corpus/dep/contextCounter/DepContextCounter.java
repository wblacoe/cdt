package corpus.dep.contextCounter;

import cdt.Helper;
import corpus.Corpus;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import space.dep.DepNeighbourhoodSpace;

//collect counts: for each target word and each context word from each relation, how often does this word appear under this relation ?
//these counts are necessary to build the dimensions/context words/vocabulary of size [dimensionality] for each relation cluster
public class DepContextCounter {
	
	protected int amountOfSentences;
    
	protected HashSet<Runnable> threads;
    protected DepContextCounts counts;
	
	public DepContextCounter(String corpusFolderName, int amountOfSentences){
        Corpus.setFolderName(corpusFolderName);
		threads = new HashSet<>();
        counts = new DepContextCounts();
        this.amountOfSentences = amountOfSentences; //how many sentences per corpus file should be included in the count?
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
	
    //load if already exists. compute otherwise.
    public static DepContextCounts getContextCounts(String corpusFolder, File contextCountsFile, int amountOfSentences){
        
        DepContextCounts dcc;
        if(contextCountsFile.exists()){
            dcc = DepContextCounts.importFromFile(contextCountsFile);
            if(!dcc.isEmpty()) return dcc;
        }
        
        DepContextCounter cc = new DepContextCounter(corpusFolder, amountOfSentences);
        cc.count();
        dcc = cc.getContextCounts();
        dcc.saveToFile(contextCountsFile);
        
        DepContextCounts dcc1 = DepContextCounts.importFromFile(contextCountsFile); //workaround for now
        return dcc1;
    }
	
	public static void main(String[] args){
		
		if(args.length != 3){
			System.out.println("Wrong amount of arguments! Usage: DepContextCounter corpusFolder outputFile amountOfSentences");
		}else{
            getContextCounts(
                args[0], //absolute path to the folder containing the corpus files
                new File(args[1]), //absolute path of the context counts file
                Integer.parseInt(args[2])  //amount of sentences
            );
		}
	}
	
}

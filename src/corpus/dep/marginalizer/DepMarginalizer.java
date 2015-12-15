package corpus.dep.marginalizer;

import cdt.Helper;
import corpus.Corpus;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import space.dep.DepNeighbourhoodSpace;

//collects counts: for each target word and each context word from each relation cluster, how often does this word appear under this relation cluster (only save counts >= [minmarginalcount]) ?
//collects counts: for each relation cluster what is the sum of the marginal counts for all words under that relation cluster (the cluster "null" is for target words) ?
//these counts are necessary to turn joint count matrices into ppmi ldops
public class DepMarginalizer {
	
    protected HashSet<Runnable> threads;
    protected DepMarginalCounts dmc;
	
	public DepMarginalizer(){
		threads = new HashSet<>();
		dmc = new DepMarginalCounts();
	}
	
	
	//marginalise over all corpus files using threads
	public synchronized void marginalize(){
		Helper.report("[DepMarginalizer] Marginalising all corpus files...");
		
		File corpusFolder = new File(DepNeighbourhoodSpace.getProjectFolder(), Corpus.getFolderName());
		String[] corpusFilenames = corpusFolder.list();
		Arrays.sort(corpusFilenames);
		
        //TODO all sentences
		int amountOfSentences = -1; //space.getIntegerParameter("amountofsentences");
		
		//run each dep marginaliser thread
		for(String corpusFilename : corpusFilenames){
			DepMarginalizerThread dmThread = new DepMarginalizerThread(this, corpusFilename, new File(corpusFolder, corpusFilename), amountOfSentences);
			threads.add(dmThread);
			(new Thread(dmThread)).start();
		}
		
		//wait for all threads to finish
		try{
			while(!threads.isEmpty()){
				wait();
			}
		}catch(InterruptedException e){}
		
        dmc.setTotalTargetAndContextWordCounts();
		Helper.report("[DepMarginalizer] ...Finished marginalising all corpus files.");
	}
	public synchronized void reportDepMarginalizerThreadDone(DepMarginalizerThread dmThread, DepMarginalCounts localDmc, long lineCount){
		threads.remove(dmThread);

        dmc.add(localDmc);
        dmc.totalCorpusCount += lineCount;
		
		notify();
	}
    
    public DepMarginalCounts getMarginalCounts(){
        return dmc;
    }
    
}

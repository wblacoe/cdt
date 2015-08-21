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
public class DepMarginalizer /*implements Runnable*/{
	
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
		
		Helper.report("[DepMarginalizer] ...Finished marginalising all corpus files.");
	}
	public synchronized void reportDepMarginalizerThreadDone(DepMarginalizerThread dmThread, DepMarginalCounts localDmc /*HashMap<DepRelationCluster, HashMap<String, Long>> givenSubSpaceWordCountMap*/){
		threads.remove(dmThread);

        dmc.add(localDmc);
        
        
		/*for(DepRelationCluster drc : givenSubSpaceWordCountMap.keySet()){
			
			HashMap<String, Long> givenWordCountMap = givenSubSpaceWordCountMap.get(drc);
			HashMap<String, Long> globalWordCountMap = modeWordCountMap.get(drc);
			
			//get the total count for this subspace
			Long subSpaceTotalCount = globalWordCountMap.get(null);
			if(subSpaceTotalCount == null) subSpaceTotalCount = 0L;
				
			for(String word : givenWordCountMap.keySet()){
				if(word == null) continue; //skip erroneous words
				
				Long existingCount = globalWordCountMap.get(word);
				long givenCount = givenWordCountMap.get(word);
				
				//update the total count for this subspace
				subSpaceTotalCount += givenCount;
				
				//update the count for this context word in this subspace
				if(existingCount == null){
					globalWordCountMap.put(word, givenCount);
				}else{
					globalWordCountMap.put(word, existingCount + givenCount);
				}
			}
			
			//save the total count for this subspace
			globalWordCountMap.put(null, subSpaceTotalCount);
		}
        */
		
		notify();
	}
	
	/*@Override
	public void run(){
		marginalize();
		saveToFile();
	}
    */
    
}

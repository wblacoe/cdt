package corpus.dep.marginalizer;

import cdt.Helper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

public class DepMarginalizerThread implements Runnable{

	protected DepMarginalizer marginalizer;
	protected String name;
	protected File corpusFile;
	protected int amountOfSentences;
	
    protected DepMarginalCounts localDmc;
	
	public DepMarginalizerThread(DepMarginalizer marginalizer, String name, File corpusFile, int amountOfSentences){
		this.marginalizer = marginalizer;
		this.name = name;
		this.corpusFile = corpusFile;
		this.amountOfSentences = amountOfSentences;
		
        localDmc = new DepMarginalCounts();
	}
	
	
	//collects counts of target words and context words
	protected void marginalize(){
		Helper.report("[DepMarginalizer] Marginalising corpus file \"" + name + "\"...");
		
        long lineCount = 0;
		int sentenceCounter = 0; //count sentences
		
		//save current sentence in here
		HashMap<String, String> wordNumberInSentenceWordMap = new HashMap<>();
		HashMap<String, String> relationHeadNumberInSentenceMap = new HashMap<>();
		try{
			BufferedReader in =
				corpusFile.getName().endsWith(".gz") ?
				new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(corpusFile)))) :
				new BufferedReader(new FileReader(corpusFile));
			
			String line;
			while((line = in.readLine()) != null){
				
				//skip empty lines
				if(line.isEmpty()) continue;
				
				//at end of sentence process the buffered counts from dependent to head
				if(line.equals("</s>")){
					//go through all dependency arcs (from dependent to head. the reverse arcs have already been processed on the fly)
					for(Entry<String, String> entry : relationHeadNumberInSentenceMap.entrySet()){
                        DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationString(entry.getKey() + "-1");
                        if(drc != null){
                            localDmc.addContextWordCount(drc.getName(), wordNumberInSentenceWordMap.get(entry.getValue()), 1L);
                        }
					}
					//clear data structures for this sentence
					wordNumberInSentenceWordMap.clear();
					relationHeadNumberInSentenceMap.clear();

					sentenceCounter++; //DEBUG
					if(amountOfSentences > 0 && sentenceCounter >= amountOfSentences) break;
					if(sentenceCounter % 100000 == 0) System.out.println("[DepMarginalizerThread] (" + name + ") " + sentenceCounter + " sentences have been processed.");
				}
				
				//identify entries in line
				String[] entries = line.split("\t");
				//only consider well-formed lines with content
				if(entries.length != 10) continue;
				
                lineCount++;
				String targetWordNumberInSentence = entries[0];
				String targetWord = entries[2];
				String headNumberInSentence = entries[6];
				String relationWithHead = entries[7];
				
				//increase target word count
				//increaseCountTargetWord(targetWord);
                localDmc.addTargetWordCount(targetWord, 1);
				
				//save target word
				wordNumberInSentenceWordMap.put(targetWordNumberInSentence, targetWord);
				//save dep arc to head
				relationHeadNumberInSentenceMap.put(relationWithHead, headNumberInSentence);
				
				//increase count from head to dependent (right now. increase reverse count later)
				//increaseCountHeadToDependent(relationWithHead, targetWord);
                //localDmc.addContextWordCount(DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationString(relationWithHead), targetWord, 1L);
                //localDmc.addContextWordCount(relationWithHead, targetWord, 1L);
                DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationString(relationWithHead);
                if(drc != null){
                    localDmc.addContextWordCount(drc.getName(), targetWord, 1L);
                }//else{
                    //System.out.println("there is no drc for relation \"" + relationWithHead + "\"!"); //DEBUG
                //}
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//marginalizer.reportDepMarginalizerThreadDone(this, subSpaceWordCountMap);
        marginalizer.reportDepMarginalizerThreadDone(this, localDmc, lineCount);
		Helper.report("[DepMarginalizer] (" + name + ") ...Finished marginalising " + sentenceCounter + " sentences from corpus file \"" + name + "\".");
	}
	
	
	@Override
	public void run(){
		marginalize();
	}
}

package corpus.dep.contextCounter;

import cdt.Helper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class DepContextCounterThread implements Runnable{

	protected DepContextCounter contextCounter;
	protected String name;
	protected File corpusFile;
	protected int amountOfSentences;
	protected DepContextCounts localCounts;
	
	public DepContextCounterThread(DepContextCounter contextCounter, String name, File corpusFile, int amountOfSentences){
		this.contextCounter = contextCounter;
		this.name = name;
		this.corpusFile = corpusFile;
		this.amountOfSentences = amountOfSentences;
		localCounts = new DepContextCounts();
	}

	
	//collects counts of target words and context words
	protected void count(){
		Helper.report("[ContextCounter] Counting over corpus file \"" + name + "\"...");
		

		//save current sentence in here
		HashMap<String, String> wordNumberInSentenceWordMap = new HashMap<>();
		HashMap<String, String> relationHeadNumberInSentenceMap = new HashMap<>();
		try{
			BufferedReader in = Helper.getFileReader(corpusFile);
			
			String line;
			int counter = 0; //count sentences
			while((line = in.readLine()) != null){
				
				//skip empty lines
				if(line.isEmpty()) continue;
				
				//at end of sentence process the buffered counts from dependent to head
				if(line.equals("</s>")){
					//go through all dependency arcs (from dependent to head. the reverse arcs have already been processed on the fly)
					for(Entry<String, String> entry : relationHeadNumberInSentenceMap.entrySet()){
						localCounts.increaseCountDependentToHead(entry.getKey(), wordNumberInSentenceWordMap.get(entry.getValue()));
					}
					//clear data structures for this sentence
					wordNumberInSentenceWordMap.clear();
					relationHeadNumberInSentenceMap.clear();

					counter++; //DEBUG
					if(amountOfSentences > 0 && counter > amountOfSentences) break;
					if(counter % 100000 == 0) System.out.println("[ContextCounterThread] (" + name + ") " + counter + " sentences have been processed.");
				}
				
				//identify entries in line
				String[] entries = line.split("\t");
				//only consider well-formed lines with content
				if(entries.length != 10) continue;
				
				String targetWordNumberInSentence = entries[0];
				String targetWord = entries[2];
				String headNumberInSentence = entries[6];
				String relationWithHead = entries[7];
				
				//save target word
				wordNumberInSentenceWordMap.put(targetWordNumberInSentence, targetWord);
				//save dep arc to head
				relationHeadNumberInSentenceMap.put(relationWithHead, headNumberInSentence);
				
				//increase count from head to dependent (right now. increase reverse count later)
				localCounts.increaseCountHeadToDependent(relationWithHead, targetWord);
				
				
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		contextCounter.reportDepContextCounterThreadDone(this, localCounts);
		Helper.report("[ContextCounter] ...Finished counting over corpus file \"" + name + "\".");
	}
	
	
	@Override
	public void run(){
		count();
	}
}

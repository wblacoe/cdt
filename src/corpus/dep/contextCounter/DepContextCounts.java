package corpus.dep.contextCounter;

import cdt.Helper;
import corpus.Corpus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
public class DepContextCounts {

    protected static Pattern contextCountsPattern = Pattern.compile("<contextcounts corpus=\\\"(.*?)\\\">");
    protected static Pattern depRelationPattern = Pattern.compile("<deprelation name=\\\"(.*?)\\\">");
    protected HashMap<String, HashMap<String, Long>> depRelationWordCountMap; //count map for target words (under null subspace), and context words under all subspaces
    protected HashMap<String, TreeSet<Entry<String, Long>>> sortedDepRelationWordCountMap;

    public DepContextCounts(){
        //prepare count map for all dep relations
		depRelationWordCountMap = new HashMap<>();
        sortedDepRelationWordCountMap = new HashMap<>();
    }
    
    public boolean isEmpty(){
        return depRelationWordCountMap.isEmpty();
    }
    
    public HashMap<String, HashMap<String, Long>> getDepRelationWordCountMap(){
    //public HashMap<String, TreeMap<WordCountPair, WordCountPair>> getDepRelationWordCountMap(){
        return depRelationWordCountMap;
    }
    
    public void setCount(String depRelation, String word, long count){
        HashMap<String, Long> wordCountMap = depRelationWordCountMap.get(depRelation);
        //TreeMap<WordCountPair, WordCountPair> wordCountMap = depRelationWordCountMap.get(depRelation);
		if(wordCountMap == null){
            wordCountMap = new HashMap<>();
			//wordCountMap = new TreeMap<>();
			depRelationWordCountMap.put(depRelation, wordCountMap);
		}
		
		wordCountMap.put(word, count);
        //WordCountPair wcp = new WordCountPair(word, count);
        //wordCountMap.put(wcp, wcp);
    }
    public Long getCount(String depRelation, String word){
        HashMap<String, Long> wordCountMap = depRelationWordCountMap.get(depRelation);
        //TreeMap<WordCountPair, WordCountPair> wordCountMap = depRelationWordCountMap.get(depRelation);
		if(wordCountMap == null){
			return null;
		}else{
            return wordCountMap.get(word);
            //return wordCountMap.get(new WordCountPair(word)).getCount();
        }
    }
	
    
    private void increaseCount(String depRelation, String word){
		HashMap<String, Long> wordCountMap = depRelationWordCountMap.get(depRelation);
        //TreeMap<WordCountPair, WordCountPair> wordCountMap = depRelationWordCountMap.get(depRelation);
		if(wordCountMap == null){
			wordCountMap = new HashMap<>();
            //wordCountMap = new TreeMap<>();
			depRelationWordCountMap.put(depRelation, wordCountMap);
		}
		
        Long existingCount = wordCountMap.get(word);
		//Long existingCount = wordCountMap.get(new WordCountPair(word)).getCount();
		if(existingCount == null){
			wordCountMap.put(word, 1L);
            //WordCountPair wcp = new WordCountPair(word, 1L);
            //wordCountMap.put(wcp, wcp);
		}else{
			wordCountMap.put(word, existingCount + 1L);
            //WordCountPair wcp = new WordCountPair(word, existingCount + 1L);
            //wordCountMap.put(wcp, wcp);
		}
	}
	protected void increaseCountHeadToDependent(String depRelation, String dependentWord){
		increaseCount(depRelation, dependentWord);
	}
	protected void increaseCountDependentToHead(String depRelation, String headWord){
		increaseCount(depRelation + "-1", headWord);
	}

    
    public TreeSet<Entry<String, Long>> getSortedMap(String depRelation){
        TreeSet<Entry<String, Long>> sortedWordCountMap = sortedDepRelationWordCountMap.get(depRelation);
        if(sortedWordCountMap == null && depRelationWordCountMap.containsKey(depRelation)){
            sortedWordCountMap = new TreeSet<>(new Comparator<Entry<String, Long>>(){
                @Override
                public int compare(Entry<String, Long> t1, Entry<String, Long> t2) {
                    int valueCompare = Long.compare(t1.getValue(), t2.getValue());
                    if(valueCompare != 0){
                        return valueCompare;
                    }else{
                        return t1.getKey().compareTo(t2.getKey());
                    }
                }
            });
            for(Entry<String, Long> t : depRelationWordCountMap.get(depRelation).entrySet()){
                sortedWordCountMap.add(t);
            }
            sortedDepRelationWordCountMap.put(depRelation, sortedWordCountMap);
        }
        
        return sortedWordCountMap;
	}

    
    public void add(DepContextCounts givenCounts){
        HashMap<String, HashMap<String, Long>> givenDepRelationWordCountMap = givenCounts.getDepRelationWordCountMap();
        //HashMap<String, TreeMap<WordCountPair, WordCountPair>> givenDepRelationWordCountMap = givenCounts.getDepRelationWordCountMap();
        for(String depRelation : givenDepRelationWordCountMap.keySet()){
			
			HashMap<String, Long> givenWordCountMap = givenDepRelationWordCountMap.get(depRelation);
            //TreeMap<WordCountPair, WordCountPair> givenWordCountMap = givenDepRelationWordCountMap.get(depRelation);
			
			HashMap<String, Long> globalWordCountMap = depRelationWordCountMap.get(depRelation);
            //TreeMap<WordCountPair, WordCountPair> globalWordCountMap = depRelationWordCountMap.get(depRelation);
			if(globalWordCountMap == null){
				globalWordCountMap = new HashMap<>();
				depRelationWordCountMap.put(depRelation, globalWordCountMap);
			}
			
			for(String word : givenWordCountMap.keySet()){
				if(word == null) continue; //skip erroneous words
				
				Long existingCount = globalWordCountMap.get(word);
				long givenCount = givenWordCountMap.get(word);
				
				//update the count for this context word in this subspace
				if(existingCount == null){
					globalWordCountMap.put(word, givenCount);
				}else{
					globalWordCountMap.put(word, existingCount + givenCount);
				}
			}
			
		}
    }

    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<contextcounts corpus=\"" + DepNeighbourhoodSpace.getName() + "\">\n");
        for(String depRelation : depRelationWordCountMap.keySet()){
            Helper.report("[ContextCounts] Writing counts for dep relation \"" + depRelation + "\" to file");
            out.write("<deprelation name=\"" + depRelation + "\">\n");
            TreeSet<Entry<String, Long>> sortedWordCountMap = getSortedMap(depRelation);
            while(!sortedWordCountMap.isEmpty()){
                Entry<String, Long> topWordCountPair = sortedWordCountMap.pollLast();
                out.write(topWordCountPair.getValue() + "\t" + topWordCountPair.getKey() + "\n");
            }
            out.write("</deprelation>\n");
        }
        out.write("</contextcounts>\n");
	}
    public void saveToFile(File contextCountsFile) {
        try{
            Helper.report("[ContextCounts] Saving counts to \"" + contextCountsFile + "\"...");
            BufferedWriter out = Helper.getFileWriter(contextCountsFile);
            saveToWriter(out);
            out.close();
            Helper.report("[ContextCounts] ...Finished saving counts to \"" + contextCountsFile + "\"...");
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public static void importDepRelationCounts(BufferedReader in, DepContextCounts dmc, String depRelationString) throws IOException{
        Helper.report("[ContextCounts] Importing context word counts for dep relation \"" + depRelationString + "\"...");
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</deprelation>")){
                break;
            }else{
                String[] entries = line.split("\t");
                long count = Long.parseLong(entries[0]);
                String contextWord = entries[1];
                dmc.setCount(depRelationString, contextWord, count);
            }
        }
    }
 
    public static DepContextCounts importFromReader(BufferedReader in) throws IOException{
        Helper.report("[ContextCounts] Importing context word counts...");
        DepContextCounts dmc = new DepContextCounts();
        
        String line;
        while((line = in.readLine()) != null){
            
            if(line.startsWith("<contextcounts")){
                Matcher matcher = contextCountsPattern.matcher(line);
                if(matcher.find()){ //ignore first entry: corpus name
                    Corpus.setName(matcher.group(1));
                }
                
            }else if(line.startsWith("<deprelation")){
                Matcher matcher = depRelationPattern.matcher(line);
                if(matcher.find()){ //ignore first entry: corpus name
                    String depRelationString = matcher.group(1);
                    importDepRelationCounts(in, dmc, depRelationString);
                }
                
            }else if(line.equals("</contextcounts>")){
                break;
            }
            
        }
        
        Helper.report("[ContextCounts] ...Finished importing context word counts.");
        return dmc;
    }
    public static DepContextCounts importFromFile(File file) {
        DepContextCounts dcc = null;
        try{
            BufferedReader in = Helper.getFileReader(file);
            dcc = importFromReader(in);
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return dcc;
    }
    
    @Override
    public String toString(){
        String s = "CORPUS \"" + Corpus.getName() + "\"\n";
        for(String depRelationString : depRelationWordCountMap.keySet()){
            HashMap<String, Long> wordCountMap = depRelationWordCountMap.get(depRelationString);
            s += "DEPRELATION \"" + depRelationString + "\"\n";
            int i=0;
            for(String contextWord : wordCountMap.keySet()){
                long count = wordCountMap.get(contextWord);
                s += count + "\t" + contextWord + "\n";
                if(++i>=5) break;
            }
        }
        
        return s;
    }

}

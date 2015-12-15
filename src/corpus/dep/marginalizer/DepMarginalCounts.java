package corpus.dep.marginalizer;

import cdt.Helper;
import corpus.Corpus;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

/**
 *
 * @author wblacoe
 */
public class DepMarginalCounts {

    protected static Pattern marginalCountsPattern = Pattern.compile("<marginalcounts corpus=\\\"(.*?)\\\" totalcount=\\\"(.*?)\\\">");
    protected static Pattern targetElementsPattern = Pattern.compile("<targetelements totalcount=\\\"(.*?)\\\">");
    protected static Pattern modePattern = Pattern.compile("<mode index=\\\"(.*?)\\\" description=\\\"(.*?)\\\" totalcount=\\\"(.*?)\\\">");
    
    protected HashMap<String, HashMap<String, Long>> modeWordCountMap; //count map for target words (under null subspace), and context words under all subspaces
    protected long totalCorpusCount;
    
    public DepMarginalCounts(){
		//prepare count map for target words (under null subspace), and context words under all subspaces
		modeWordCountMap = new HashMap<>();
		modeWordCountMap.put(null, new HashMap<String, Long>());
        for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
            String drcName = DepNeighbourhoodSpace.getDepRelationCluster(m).getName();
            modeWordCountMap.put(drcName, new HashMap<String, Long>());
        }
        totalCorpusCount = 0L;
    }

    
    public HashMap<String, HashMap<String, Long>> getModeWordCountMap(){
        return modeWordCountMap;
    }

    public void setContextWordCount(String drcName, String word, Long count){
        HashMap<String, Long> wordCountMap = modeWordCountMap.get(drcName);
        if(wordCountMap == null){
            HashMap<String, Long> newWordCountMap = new HashMap<>();
            newWordCountMap.put(word, count);
            modeWordCountMap.put(drcName, wordCountMap);
        }else{
            wordCountMap.put(word, count);
        }
    }
    public Long getContextWordCount(String drcName, String word){
        HashMap<String, Long> wordCountMap = modeWordCountMap.get(drcName);
        if(wordCountMap == null){
            modeWordCountMap.put(drcName, new HashMap<String, Long>());
            return 0L;
        }else{
            return wordCountMap.get(word);
        }
    }
    public void addContextWordCount(String drcName, String word, Long n){
        Long existingCount = getContextWordCount(drcName, word);
        if(existingCount == null){
            setContextWordCount(drcName, word, 1L);
        }else{
            setContextWordCount(drcName, word, existingCount + n);
        }
    }
    
    public void setTotalContextWordCount(String drcName, Long count){
        modeWordCountMap.get(drcName).put(null, count);
    }
    public Long getTotalContextWordCount(String drcName){
        return getContextWordCount(drcName, null);
    }
    
    public void setTargetWordCount(String word, Long count){
        modeWordCountMap.get(null).put(word, count);
    }
    public Long getTargetWordCount(String word){
        return modeWordCountMap.get(null).get(word);
    }
    public void addTargetWordCount(String word, int n){
        Long existingCount = getTargetWordCount(word);
        if(existingCount == null){
            setTargetWordCount(word, 1L);
        }else{
            setTargetWordCount(word, existingCount + n);
        }
    }
    
    public void setTotalTargetWordCount(Long count){
        modeWordCountMap.get(null).put(null, count);
    }
    public Long getTotalTargetWordCount(){
        return getContextWordCount(null, null);
    }
    
    public void setCorpusTotalCount(long count){
        //corpusTotalCount = count;
        //Corpus.setTotalWordCount(count);
        totalCorpusCount = count;
    }
    public long getCorpusTotalCount(){
        //return corpusTotalCount;
        //return Corpus.getTotalWordCount();
        return totalCorpusCount;
    }
    
    public void setTotalTargetAndContextWordCounts(){
        for(String key : modeWordCountMap.keySet()){
            Long totalCount = 0L;
            HashMap<String, Long> wordCountMap = modeWordCountMap.get(key);
            for(Entry<String, Long> wordCount : wordCountMap.entrySet()){
                totalCount += wordCount.getValue();
            }
            wordCountMap.put(null, totalCount);
        }
    }
    
    
    public void add(DepMarginalCounts givenDmc){
        HashMap<String, HashMap<String, Long>> givenModeWordCountMap = givenDmc.getModeWordCountMap();
        for(String drcName : givenModeWordCountMap.keySet()){
            HashMap<String, Long> givenWordCountMap = givenModeWordCountMap.get(drcName);
            for(String word : givenWordCountMap.keySet()){
                Long givenCount = givenWordCountMap.get(word);
                addContextWordCount(drcName, word, givenCount);
            }
        }
    }
    
    public void saveToWriter(BufferedWriter out) throws IOException{
		int minCount = Helper.getMinMarginalCount(); //only save context counts over or equal to this threshold
		
        //write meta information
        out.write("<marginalcounts " +
            "corpus=\"" + DepNeighbourhoodSpace.getName() + "\" " +
            //"totalcount=\"" + Corpus.getTotalWordCount() + "\">\n");
            "totalcount=\"" + totalCorpusCount + "\">\n");

        //go through all target words
        HashMap<String, Long> wordCountMap = modeWordCountMap.get(null);
        long totalSubSpaceCount = wordCountMap.get(null);
        out.write("<targetelements totalcount=\"" + totalSubSpaceCount + "\">\n");
        for(Map.Entry<String, Long> entry : wordCountMap.entrySet()){
            String word = entry.getKey();
            if(word == null) continue; //skip the null entry (i.e. total count for this subspace), this goes into the meta information
            Long count = entry.getValue();
            if(count >= minCount) out.write(word + "\t" + count + "\n");
        }
        
        //go through all modes
        for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
            DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);

            wordCountMap = modeWordCountMap.get(drc.getName());
            totalSubSpaceCount = wordCountMap.get(null);
            out.write("<mode name=\"" + drc.getName() + "\" totalcount=\"" + totalSubSpaceCount + "\">\n");

            //go through all words under this subspace
            for(Map.Entry<String, Long> entry : wordCountMap.entrySet()){
                String word = entry.getKey();
                if(word == null) continue; //skip the null entry (i.e. total count for this subspace), this goes into the meta information
                Long count = entry.getValue();
                if(count >= minCount) out.write(word + "\t" + count + "\n");
            }

            out.write("</mode>\n");
        }

        out.write("</marginalcounts>");
	}
    
    public void saveToWriterDEBUG(BufferedWriter out) throws IOException{
		int minCount = Helper.getMinMarginalCount(); //only save context counts over or equal to this threshold
		
        //write meta information
        out.write("<marginalcounts " +
            "corpus=\"" + DepNeighbourhoodSpace.getName() + "\" " +
            //"totalcount=\"" + Corpus.getTotalWordCount() + "\">\n");
            "totalcount=\"" + totalCorpusCount + "\">\n");

        //go through all target words
        HashMap<String, Long> wordCountMap = modeWordCountMap.get(null);
        long totalSubSpaceCount = wordCountMap.get(null);
        out.write("<targetelements totalcount=\"" + totalSubSpaceCount + "\">\n");
        for(Map.Entry<String, Long> entry : wordCountMap.entrySet()){
            String word = entry.getKey();
            if(word == null) continue; //skip the null entry (i.e. total count for this subspace), this goes into the meta information
            Long count = entry.getValue();
            if(count >= minCount) out.write(word + "\t" + count + "\n");
        }
        
        //go through all modes
        //for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
        for(String key : modeWordCountMap.keySet()){
            //DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);

            wordCountMap = modeWordCountMap.get(key);
            totalSubSpaceCount = wordCountMap.get(null);
            out.write("<mode name=\"" + key + "\" totalcount=\"" + totalSubSpaceCount + "\">\n");

            //go through all words under this subspace
            for(Map.Entry<String, Long> entry : wordCountMap.entrySet()){
                String word = entry.getKey();
                if(word == null) continue; //skip the null entry (i.e. total count for this subspace), this goes into the meta information
                Long count = entry.getValue();
                if(count >= minCount) out.write(word + "\t" + count + "\n");
            }

            out.write("</mode>\n");
        }

        out.write("</marginalcounts>");
	}
    
    public void saveToFile(File marginalCountsFile){
        Helper.report("[DepMarginalizer] Saving counts to file \"" + marginalCountsFile + "\"...");
        try{
            BufferedWriter out = Helper.getFileWriter(marginalCountsFile);
            saveToWriter(out);
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Helper.report("[DepMarginalizer] ...Finished saving counts to file \"" + marginalCountsFile + "\"");
    }
	
    
    public static void importTargetWordMarginalCounts(BufferedReader in, DepMarginalCounts dmc) throws IOException{
        String line;
        int minCount = Helper.getMinMarginalCount();
        while((line = in.readLine()) != null){
            if(line.equals("</targetelements>")){
                break;
            }else{
                String[] entries = line.split("\t");
                if(entries.length == 2){
                    String word = entries[0];
                    long count = Long.parseLong(entries[1]);
                    if(Vocabulary.contains(word) && count >= minCount){
                        dmc.setTargetWordCount(word, count);
                    }
                }
            }
        }
    }
    
    public static void importContextWordMarginalCounts(BufferedReader in, DepMarginalCounts dmc, DepRelationCluster drc) throws IOException{
        String line;
        int minCount = Helper.getMinMarginalCount();
        while((line = in.readLine()) != null){
            if(line.equals("</mode>")){
                break;
            }else{
                String[] entries = line.split("\t");
                String word = entries[0];
                long count = Long.parseLong(entries[1]);
                if(drc.hasContextWord(word) && count >= minCount){
                    dmc.setContextWordCount(drc.getName(), word, count);
                }
            }
        }
    }
    
    //import counts from count file, generated earlier by dep marginalizer
	public static DepMarginalCounts importFromReader(BufferedReader in/*, Vocabulary vocabulary*/) throws IOException{
		Helper.report("[DepMarginalCounts] Importing target and context word counts...");
		
        DepMarginalCounts dmc = new DepMarginalCounts();
        
        String line;
        while((line = in.readLine()) != null){

            //skip empty lines
            if(line.isEmpty()) continue;

            //start the counts file
            if(line.startsWith("<marginalcounts")){
                Matcher matcher = marginalCountsPattern.matcher(line);
                if(matcher.find()){ //ignore first entry: corpus name
                    dmc.setCorpusTotalCount(Long.parseLong(matcher.group(2)));
                }
            }else if(line.startsWith("<targetelements")){
                Matcher matcher = targetElementsPattern.matcher(line);
                if(matcher.find()){
                    dmc.setTotalTargetWordCount(Long.parseLong(matcher.group(1)));
                    importTargetWordMarginalCounts(in, dmc);
                }
            }else if(line.startsWith("<mode")){
                Matcher matcher = modePattern.matcher(line);
                if(matcher.find()){ //ignore second entry: mode name
                    int modeIndex = Integer.parseInt(matcher.group(1));
                    DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(modeIndex);
                    dmc.setTotalContextWordCount(drc.getName(), Long.parseLong(matcher.group(3)));
                    importContextWordMarginalCounts(in, dmc, drc);
                }
            }else if(line.equals("</marginalcounts>")){
                break;
            }
        }

        in.close();
		
		
		Helper.report("[DepMarginalCounts] ...Finished importing marginal word counts.");
        return dmc;
	}
    
    public static DepMarginalCounts importFromFile(File file){
        DepMarginalCounts dmc = null;
        try{
            BufferedReader in = Helper.getFileReader(file);
            dmc = importFromReader(in);
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return dmc;
    }

    @Override
    public String toString(){
        //String s = "CORPUS\ntotal count=" + Corpus.getTotalWordCount() + "\n";
        String s = "CORPUS\ntotal count=" + totalCorpusCount + "\n";
        s += "TARGET WORDS\ntotal count=" + getTotalTargetWordCount() + "\n";
        
        int i=0;
        for(String targetWord : modeWordCountMap.get(null).keySet()){
            if(targetWord == null) continue;
            Long count = modeWordCountMap.get(null).get(targetWord);
            s += targetWord + "\t" + count + "\n";
            if(++i==5) break;
        }
        
        for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
            DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);
            s += "CONTEXT WORDS for mode #" + m + "\n" + getTotalContextWordCount(drc.getName()) + "\n";
            i=0;
            for(String contextWord : modeWordCountMap.get(drc.getName()).keySet()){
                if(contextWord == null) continue;
                Long count = modeWordCountMap.get(null).get(contextWord);
                s += contextWord + "\t" + count + "\n";
                if(++i==5) break;
            }
        }
        
        return s;
    }

}

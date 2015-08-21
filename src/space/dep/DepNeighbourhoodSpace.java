package space.dep;

import cdt.Helper;
import corpus.Corpus;
import corpus.dep.contextCounter.DepContextCounts;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import space.TensorSpace;

/*
 * Contains dep relation clusters as modes, their index starts with 1
 *
 * @author wblacoe
 */
public class DepNeighbourhoodSpace extends TensorSpace {
    
    protected static final Pattern depNeighbourhoodSpacePattern = Pattern.compile("<space name=\\\"(.*?)\\\" order=\\\"(.*?)\\\" dimensionality=\\\"(.*?)\\\" corpusformat=\\\"(.*?)\\\" corpusfolder=\\\"(.*?)\\\" contextcountsfile=\\\"(.*?)\\\" datasetwordsfile=\\\"(.*?)\\\">");
    
    private static final HashSet<String> stoplist = new HashSet<>();
    private static final HashMap<String, DepRelationCluster> depRelationStringDepRelationClusterMap = new HashMap<>();
    private static String contextCountsFilename, datasetWordsFilename;
    
    public static void saveDepRelationClusterUnderDepRelationString(String depRelationString, DepRelationCluster drc){
        depRelationStringDepRelationClusterMap.put(depRelationString, drc);
    }
    public static DepRelationCluster getDepRelationClusterFromDepRelationString(String depRelationString){
        return depRelationStringDepRelationClusterMap.get(depRelationString);
    }
    
    public static void setDepRelation(int modeIndex, DepRelationCluster depRel){
        setModeObject(modeIndex, depRel);
    }
	
    public static DepRelationCluster getDepRelationCluster(int modeIndex){
        return (DepRelationCluster) getModeObject(modeIndex);
    }
	
    //import the vocabulary for given subspace by jointly mixing it from the [amount] most frequent contexts in all dep relations covered by given subspace
	public static void assignDimensionObjects(DepRelationCluster drc, DepContextCounts dcc){
        Helper.report("[DepNeighbourhoodSpace] Assigning context words to dep relation cluster \"" + drc.getName() + "\"...");

        HashMap<String, Iterator<Entry<String, Long>>> iterators = new HashMap<>();
        for(String drString : drc.getDepRelationStrings()){
            TreeSet<Entry<String, Long>> wordCountMap = dcc.getSortedMap(drString);
            if(wordCountMap != null) iterators.put(drString, wordCountMap.descendingIterator());
        }
        
        int d=0;
        while(true){
            for(String drString : iterators.keySet()){
                Iterator<Entry<String, Long>> iterator = iterators.get(drString);
                Entry<String, Long> entry = null;
                if(iterator.hasNext()){
                    String contextWord = iterator.next().getKey();
                    //System.out.println("drc=" + drString + ", context word=" + contextWord + ", drc has context word=" + drc.hasContextWord(contextWord) + ", context word is in stoplist=" + stoplist.contains(contextWord)); //DEBUG
                    if(contextWord != null && !drc.hasContextWord(contextWord) && !stoplist.contains(contextWord)){
                        d++;
                        drc.setContextWord(d, new ContextWord(d, contextWord));
                        if(d >= getDimensionality()) return;
                    }
                }
            }
        }
        
	}
	public static void ensureDimensionObjects(File contextCountsFile){
        if(!hasDimensions()){
            Helper.report("[DepNeighbourhoodSpace] This space is missing dimension objects! Assigning context words to all dep relation clusters from \"" + contextCountsFile.getName() + "\"");
            DepContextCounts dcc = DepContextCounts.importFromFile(contextCountsFile);

            //create vocabulary file for each subspace that doesn't already have one
            for(int m=1; m<=getOrder(); m++){
                DepRelationCluster drc = getDepRelationCluster(m);
                assignDimensionObjects(drc, dcc);
            }
            //setHasDimensions(true);
        }
	}
    
    public static void ensureVocabulary(File datasetWordsFile){
        if(Vocabulary.isEmpty()){
            Helper.report("[DepNeighbourhoodSpace] Creating vocabulary from context words and dataset words...");

            HashSet<String> targetWordsSet = new HashSet<>();

            //add dataset words
            try{
                BufferedReader in = Helper.getFileReader(datasetWordsFile);
                String line;
                while((line = in.readLine()) != null){
                    targetWordsSet.add(line);
                }
                in.close();
            }catch(IOException e){
                e.printStackTrace();
            }

            //go through all modes
            for(int m=1; m<=getOrder(); m++){
                DepRelationCluster drc = getDepRelationCluster(m);
                //save all context words under this mode in vocabulary
                for(int d=1; d<=getDimensionality(); d++){
                    ContextWord cw = drc.getContextWord(d);
                    if(cw != null){
                        targetWordsSet.add(cw.getWord());
                    }
                }
            }

            //go through all collected target words and save them in vocabulary
            Vocabulary.setSize(targetWordsSet.size());
            int d=0;
            HashMap<String, Integer> wordVocabularyIndexMap = new HashMap<>();
            for(String targetWord : targetWordsSet){
                TargetWord tw = new TargetWord(d, targetWord);
                wordVocabularyIndexMap.put(targetWord, d);
                Vocabulary.setTargetWord(d, tw);
                d++;
            }

            //go through all modes again and update vocabulary index of context words
            for(int m=1; m<=getOrder(); m++){
                DepRelationCluster drc = getDepRelationCluster(m);
                //save all context words under this mode in vocabulary
                for(d=1; d<=getDimensionality(); d++){
                    ContextWord cw = drc.getContextWord(d);
                    if(cw != null){
                        int vocabularyIndex = wordVocabularyIndexMap.get(cw.getWord());
                        cw.setVocabularyIndex(vocabularyIndex);
                    }
                }
            }
        }
    }
	
    public static void importStoplist(File file) throws IOException{
        BufferedReader in = Helper.getFileReader(file);
        String line;
        while((line = in.readLine()) != null){
            stoplist.add(line);
        }
        in.close();
    }
	public static HashSet<String> getStoplist(){
		return stoplist;
	}
    
    public static void importFromReader(BufferedReader in) throws IOException{
        Helper.report("[DepNeighbourhoodSpace] Importing space...");

        String line = in.readLine();
        Matcher matcher = depNeighbourhoodSpacePattern.matcher(line);
        if(matcher.find()){
            
            //DEBUG
            //for(int i=0; i<=matcher.groupCount(); i++) System.out.println("" + i + ": " + matcher.group(i));
            
            setName(matcher.group(1));
            setOrder(Integer.parseInt(matcher.group(2)));
            setDimensionality(Integer.parseInt(matcher.group(3)));
            Corpus.setFormat(matcher.group(4));
            Corpus.setFolderName(matcher.group(5));
            contextCountsFilename = matcher.group(6);
            datasetWordsFilename = matcher.group(7);
            while((line = in.readLine()) != null){
                if(line.startsWith("<vocabulary")){
                    Vocabulary.importFromReader(in, line); //static
                }else if(line.startsWith("<mode")){
                    DepRelationCluster drc = DepRelationCluster.importFromReader(in, line);
                    //save drc
                    setDepRelation(drc.getModeIndex(), drc);
                    //make drc findable under all dep relation strings it consists of
                    for(String depRelationString : drc.getDepRelationStrings()){
                        depRelationStringDepRelationClusterMap.put(depRelationString, drc);
                    }
                }else if(line.equals("</space>")){
                    break;
                }
            }
            
            
            //make sure all modes have dimension objects
            ensureDimensionObjects(new File(getProjectFolder(), contextCountsFilename));

            //make sure vocabulary is deinfed
            ensureVocabulary(new File(getProjectFolder(), datasetWordsFilename));
            
        }else{
            Helper.report("[DepNeighbourhoodSpace] Space file has wrong format.");
        }
        
        Helper.report("[DepNeighbourhoodSpace] Finished importing space with " + Vocabulary.getSize() + " target words.");
    }
    public static void importFromFile(File file){
        try{
            BufferedReader in = Helper.getFileReader(file);
            importFromReader(in);
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static String getString(){
        String s = "space \"" + getName() + "\": order=" + getOrder() + ",  dimensionality=" + getDimensionality() + ", corpus format=" + Corpus.getFormat() + ", corpus folder=" + Corpus.getFolderName() + "\n";
        for(int m=1; m<=getOrder(); m++){
            s += getDepRelationCluster(m);
        }
        return s;
    }
    
    public static void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<space name=\"" + getName() + "\" order=\"" + getOrder() + "\" dimensionality=\"" + getDimensionality() + "\" corpusformat=\"" + Corpus.getFormat() + "\" corpusfolder=\"" + Corpus.getFolderName() + "\" contextcountsfile=\"" + contextCountsFilename + "\" datasetwordsfile=\"" + datasetWordsFilename + "\">\n");
        Vocabulary.saveToWriter(out);
        for(int m=1; m<=getOrder(); m++){
            getDepRelationCluster(m).saveToWriter(out);
        }
        out.write("</space>");
    }
    public static void saveToFile(File file){
        try{
            BufferedWriter out = Helper.getFileWriter(file);
            saveToWriter(out);
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args){

        File projectFolder1 = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder1);
        
        File inFile = new File(projectFolder1, "preprocessed/ukwac.depParsed/5up5down/wordsim353/space.empty");
        File outFile = new File(projectFolder1, "preprocessed/ukwac.depParsed/5up5down/wordsim353/space.test");
        DepNeighbourhoodSpace.importFromFile(inFile);
        DepNeighbourhoodSpace.saveToFile(outFile);
        System.out.println(DepNeighbourhoodSpace.getString());
        
    }
    
}

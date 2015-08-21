package experiment.dep;

import cdt.Helper;
import experiment.TargetElements;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains target words, index starts with 0
 * @author wblacoe
 */
public class Vocabulary extends TargetElements {

    protected static final Pattern vocabularyPattern = Pattern.compile("<vocabulary size=\\\"(.*?)\\\">");
    protected static final Pattern targetElementPattern = Pattern.compile("<targetelement index=\\\"(.*?)\\\">(.*?)</targetelement>");
    protected static HashMap<String, TargetWord> targetWordMap = new HashMap<>();
    
    /*public Vocabulary(int amountOfTargetElements) {
        super(amountOfTargetElements);
        targetWordMap = new HashMap<>();
    }
    */

    public static void setTargetWord(int index, TargetWord targetWord){
        setTargetElement(index, targetWord);
        targetWordMap.put(targetWord.getWord(), targetWord);
    }
    
    public static TargetWord getTargetWord(int index){
        return (TargetWord) getTargetElement(index);
    }
    
    public static TargetWord getTargetWord(String word){
        return targetWordMap.get(word);
    }
    
    public static int getTargetWordIndex(String word){
        return getTargetWord(word).getIndex();
    }
    
    public static int getSizeOfTargetWordMap(){
        return targetWordMap.size();
    }
    
    public static boolean contains(TargetWord te){
        return targetWordMap.containsValue(te);
    }
    
    public static boolean contains(String word){
        return targetWordMap.containsKey(word);
    }
    
    public static boolean isEmpty(){
        return getSize() == 0;
    }
    
    public static void removeAllRepresentations(){
        for(int i=0; i<getSize(); i++){
            getTargetWord(i).removeRepresentation();
        }
    }

    public static void importTargetWordsFromReader(BufferedReader in) throws IOException{
        String line;
        while((line = in.readLine()) != null){
            if(line.startsWith("<targetelement")){
                Matcher matcher = targetElementPattern.matcher(line);
                if(matcher.find()){
                    int index = Integer.parseInt(matcher.group(1));
                    String word = matcher.group(2);
                    TargetWord tw = new TargetWord(index, word);
                    //System.out.println("targetelement \"" + word + "\" has index " + index); //DEBUG
                    setTargetWord(index, tw);
                }
            }else if(line.equals("</vocabulary>")){
                break;
            }
        }   
    }
    
    public static void importFromReader(BufferedReader in, String line) throws IOException{
        Helper.report("[Vocabulary] Importing vocabulary...");
        Matcher matcher = vocabularyPattern.matcher(line);
        if(matcher.find()){
            int size = Integer.parseInt(matcher.group(1));
            setSize(size);
            importTargetWordsFromReader(in);
        }
        Helper.report("[Vocabulary] ...Finished importing vocabulary.");
    }
    
    public static void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<vocabulary size=\"" + getSize() + "\">\n");
        for(int i=0; i<getSize(); i++){
            getTargetWord(i).saveToWriter(out);
        }
        out.write("</vocabulary>\n");
    }
    
}

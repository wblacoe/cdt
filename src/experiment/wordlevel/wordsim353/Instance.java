package experiment.wordlevel.wordsim353;

import experiment.AbstractInstance;
import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author wblacoe
 */
public class Instance extends AbstractInstance {

    protected String wordPair;
    protected float label;
    
    public Instance(){
        index = -1;
        wordPair = null;
        label = -1;
    }
    public Instance(String word1, String word2, float label){
        setInstance(word1, word2, label);
    }
    
    //ignore word order
    private void setInstance(String word1, String word2, float label){
        wordPair = word1.compareTo(word2) <= 0 ? word1 + "#" + word2 : word2 + "#" + word1;
        this.label = label;
    }
    
    public static Instance importFromReader(BufferedReader in) throws IOException{
        String line = in.readLine();
        if(line == null){
            return null;
        }else{
            String[] entries = line.split("\t");
            String word1 = entries[0];
            String word2 = entries[1];
            Float label = Float.parseFloat(entries[2]);
            return new Instance(word1, word2, label);
        }
    }

    
}

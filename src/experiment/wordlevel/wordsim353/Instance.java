package experiment.wordlevel.wordsim353;

import experiment.AbstractInstance;
import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author wblacoe
 */
public class Instance extends AbstractInstance {

    protected String word1, word2;
    protected float expected, predicted;
    
    public Instance(){
        index = -1;
        word1 = null;
        word2 = null;
        expected = -1;
        predicted = -1;
    }
    public Instance(String word1, String word2, float label){
        setInstance(word1, word2, label);
    }
    
    //ignore word order
    private void setInstance(String word1, String word2, float label){
        this.word1 = word1;
        this.word2 = word2;
        this.expected = label;
    }
    
    public static Instance importFromReader(BufferedReader in) throws IOException{
        String line = in.readLine();
        if(line == null){
            return null;
        }else{
            String[] entries = line.split("\t");
            String word1 = entries[0].toLowerCase();
            String word2 = entries[1].toLowerCase();
            Float label = Float.parseFloat(entries[2]);
            return new Instance(word1, word2, label);
        }
    }

    
}

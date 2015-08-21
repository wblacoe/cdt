package space.dep;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import space.Dimension;

public class ContextWord extends Dimension {

    protected static final Pattern contextWordPattern = Pattern.compile("<contextelement dimensionindex=\\\"(.*?)\\\" vocabularyindex=\\\"(.*?)\\\">(.*?)</contextelement>");
    private String word;
    private int vocabularyIndex;

    public ContextWord(){
        super();
        word = null;
        vocabularyIndex = -1;
    }
    public ContextWord(int dimensionIndex){
        super(dimensionIndex);
        word = null;
        vocabularyIndex = -1;
    }
    public ContextWord(int dimensionIndex, String word){
        super(dimensionIndex);
        this.word = word;
        vocabularyIndex = -1;
    }
    public ContextWord(int dimensionIndex, String word, int vocabularyIndex){
        super(dimensionIndex);
        this.word = word;
        this.vocabularyIndex = vocabularyIndex;
    }

    
    public void setWord(String word){
        this.word = word;
    }
    public String getWord(){
        return word;
    }
    
    public void setVocabularyIndex(int vocabularyIndex){
        this.vocabularyIndex = vocabularyIndex;
    }
    public int getVocabularyIndex(){
        return vocabularyIndex;
    }
    
    public static ContextWord importFromString(String line) throws IOException{
        
        ContextWord cw = null;
        
        Matcher matcher = contextWordPattern.matcher(line);
        if(matcher.find()){
            int dimensionIndex = Integer.parseInt(matcher.group(1));
            int vocabularyIndex = Integer.parseInt(matcher.group(2));
            String word = matcher.group(3);
            cw = new ContextWord(dimensionIndex, word, vocabularyIndex);
        }

        return cw;
    }
    
    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<contextelement dimensionindex=\"" + dimensionIndex + "\" vocabularyindex=\"" + vocabularyIndex + "\">" + word + "</contextelement>\n");
    }

    @Override
    public String toString(){
        return "(" + dimensionIndex + ") " + word;
    }
    
}

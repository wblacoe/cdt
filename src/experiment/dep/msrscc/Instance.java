package experiment.dep.msrscc;

import corpus.dep.converter.DepTree;
import experiment.AbstractInstance;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wblacoe
 */
public class Instance extends AbstractInstance {

    protected static Pattern instancePattern = Pattern.compile("<instance index=\\\"(.*?)\\\" label=\\\"(.*?)\\\">");
    protected static Pattern sentencePattern = Pattern.compile("<sentence index=\\\"(.*?)\\\" variable=\\\"(.*?)\\\">");

    private int /*index,*/ labelIndex;
    private DepTree[] depSentences;
    private String[] variables;
    
    
    public Instance(){
        index = -1;
        labelIndex = -1;
        depSentences = new DepTree[5];
        variables = new String[5];
    }


    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public int getLabelIndex() {
        return labelIndex;
    }
    
    //index must be in [0;4]
    public DepTree getDepSentence(int index){
        return depSentences[index];
    }
    
    public DepTree getLabelDepSentence(){
        return getDepSentence(labelIndex);
    }

    public static Instance importFromReader(BufferedReader in) throws IOException{
        Instance instance = new Instance();
        
        String line = in.readLine();
        if(line == null) return null;
        Matcher matcher = instancePattern.matcher(line);
        if(matcher.find()){
            instance.setIndex(Integer.parseInt(matcher.group(1)));
            instance.setLabelIndex(Integer.parseInt(matcher.group(2)));
            
            while(true){
                line = in.readLine();
                if(line.equals("</instance>")) break;
                
                matcher = sentencePattern.matcher(line);
                if(matcher.find()){
                    int sentenceIndex = Integer.parseInt(matcher.group(1)) - 1;
                    String variable = matcher.group(2);
                    instance.variables[sentenceIndex] = variable;
                    
                    DepTree depSentence = DepTree.importFromReader(in, "</sentence>");
                    if(depSentence == null){
                        break;
                    }else{
                        instance.depSentences[sentenceIndex] = depSentence;
                    }
                }
            }
        }

        return instance;
    }
    
    
}

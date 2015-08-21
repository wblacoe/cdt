package experiment.dep.conll2015;

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

    protected static Pattern instancePattern = Pattern.compile("<instance id=\\\"(.*?)\\\" documentId=\\\"(.*?)\\\" connective=\\\"(.*?)\\\" sense=\\\"(.*?)\\\" type=\\\"(.*?)\\\">");

    public String documentId, sense;
    public DepTree[] arguments;
    
    
    public Instance(){
        index = -1;
        documentId = null;
        sense = null;
        arguments = new DepTree[2];
    }
    
    public static Instance importFromReader(BufferedReader in) throws IOException{
        Instance instance = new Instance();
        
        String line = in.readLine();
        if(line == null) return null;
        Matcher matcher = instancePattern.matcher(line);
        if(matcher.find()){
            instance.index = Integer.parseInt(matcher.group(1));
            instance.documentId = matcher.group(2);
            instance.sense = matcher.group(4);
            
            while(true){
                line = in.readLine();
                if(line.equals("</instance>")){
                    break;
                }else if(line.equals("<argument1>")){
                    DepTree depArgument = DepTree.importFromReader(in, "</argument1>");
                    instance.arguments[0] = depArgument;
                }else if(line.equals("<argument2>")){
                    DepTree depArgument = DepTree.importFromReader(in, "</argument2>");
                    instance.arguments[1] = depArgument;
                }
            }
        }

        return instance;
    }
    
    
}

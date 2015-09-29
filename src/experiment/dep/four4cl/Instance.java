package experiment.dep.four4cl;

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

    protected static Pattern msrpcPattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" paraphrastic=\\\"(.*?)\\\">"); //values: 0, 1
	protected static Pattern zeichnerPattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" entailing=\\\"(.*?)\\\">"); //values: true, false
	protected static Pattern sickPattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" similarity=\\\".*\\\" entailment=\\\"(.*?)\\\">"); //values: ENTAILMENT, CONTRADICTION, NEUTRAL
	protected static Pattern rw2012Pattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" label=\\\"(.*?)\\\">"); //values: BACKWARDS_CONTAINMENT, CONTAINMENT, PARAPHRASE, RELATED, UNRELATED

    public String label;
    public DepTree[] sentenceTrees;
    
    
    public Instance(){
        index = -1;
        label = null;
        sentenceTrees = new DepTree[2];
    }
	
    public static Instance importFromReader(BufferedReader in, int task) throws IOException{
        Instance instance = new Instance();
        
		Pattern instancePattern;
		switch(task){
			case Four4CL.MSRPC_TASK:
				instancePattern = msrpcPattern;
				break;
			case Four4CL.ZEICHNER_TASK:
				instancePattern = zeichnerPattern;
				break;
			case Four4CL.SICK_TASK:
				instancePattern = sickPattern;
				break;
			case Four4CL.RW2012_TASK:
				instancePattern = rw2012Pattern;
				break;
			default:
				return null;
		}
		
		String line = in.readLine();
        if(line == null) return null;
        Matcher matcher = instancePattern.matcher(line);
        if(matcher.find()){
            instance.index = Integer.parseInt(matcher.group(1));
            int sentenceIndex = Integer.parseInt(matcher.group(2));
            instance.label = matcher.group(3);
			DepTree sentenceTree = DepTree.importFromReader(in, "</s>");
			instance.sentenceTrees[sentenceIndex - 1] = sentenceTree;
		}

		line = in.readLine();
        if(line == null) return null;
        matcher = instancePattern.matcher(line);
        if(matcher.find()){
            instance.index = Integer.parseInt(matcher.group(1));
            int sentenceIndex = Integer.parseInt(matcher.group(2));
            instance.label = matcher.group(3);
			DepTree sentenceTree = DepTree.importFromReader(in, "</s>");
			instance.sentenceTrees[sentenceIndex - 1] = sentenceTree;
		}

        return instance;
    }
    
    
}

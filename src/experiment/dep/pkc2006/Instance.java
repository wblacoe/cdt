package experiment.dep.pkc2006;

import corpus.dep.converter.DepNode;
import corpus.dep.converter.DepTree;
import experiment.AbstractInstance;
import experiment.dep.Vocabulary;
import featureExtraction.FeatureVector;

/**
 *
 * @author wblacoe
 */
public class Instance extends AbstractInstance {

	public static int instanceIndex = 0;
    String noun, verb, relation;
	public FeatureVector fv;
    
    public Instance(){
        index = -1;
		noun = "";
		verb = "";
		relation = "";
		fv = null;
    }
	
	public DepTree toDepTree(){
		
		//remove me
		if(Vocabulary.getTargetWord(noun) == null || Vocabulary.getTargetWord(noun).getLexicalRepresentation() == null || Vocabulary.getTargetWord(verb) == null || Vocabulary.getTargetWord(verb).getLexicalRepresentation() == null) return null;
		
		DepTree depTree = new DepTree("#" + index, false);
		
		DepNode nounNode, verbNode;
		int nounNumber, verbNumber;
		if(relation.equals("nsubj")){
			nounNumber = 1;
			verbNumber = 2;
		}else{
			nounNumber = 2;
			verbNumber = 1;
		}
		
		nounNode = new DepNode(noun, "", nounNumber, verbNumber, relation + "-1");
		verbNode = new DepNode(verb, "", verbNumber, 0, "null");
		
		nounNode.addDepArc(verbNode, relation + "-1", false);
		verbNode.addDepArc(nounNode, relation, false);

		depTree.addNode(nounNumber, nounNode);
		depTree.addNode(verbNumber, verbNode);
		depTree.setRootNode(verbNumber);
		
		return depTree;
	}

	public static Instance importFromString(String line){
		Instance instance = new Instance();
		
		instance.index = instanceIndex;
		instance.fv = new FeatureVector(instanceIndex);
		instanceIndex++;
		
		String[] entries = line.split("\t");
		instance.verb = entries[0];
		instance.noun = entries[1];
		instance.relation = (entries[2].equals("A0") ? "nsubj" : "dobj");
		Double expected = Double.parseDouble(entries[3]);
		instance.fv.setValue("expected", expected);
		
		return instance;
	}
   
}
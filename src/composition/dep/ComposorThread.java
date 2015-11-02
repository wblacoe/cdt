package composition.dep;

import cdt.Helper;
import corpus.dep.converter.DepArc;
import corpus.dep.converter.DepNode;
import corpus.dep.converter.DepTree;
import experiment.Dataset;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import innerProduct.InnerProductsCache;
import java.util.HashMap;
import linearAlgebra.value.LinearCombinationMatrix;
import numberTypes.NNumber;
import numberTypes.NNumberVector;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

/**
 *
 * @author wblacoe
 */
public class ComposorThread implements Runnable {

    private final double log2 = Math.log(2);
    
    private String name;
    private Composor composor;
    private HashMap<String, DepTree> indexDepTreeMap;
    private HashMap<String, LinearCombinationMatrix> treeRepresentations;
    private InnerProductsCache ipc;
	private Dataset dataset;
    
    public ComposorThread(String name, Composor composor, HashMap<String, DepTree> indexDepTreeMap, InnerProductsCache ipc, Dataset dataset){
        this.name = name;
        this.composor = composor;
        this.indexDepTreeMap = indexDepTreeMap;
        treeRepresentations = new HashMap<>();
        this.ipc = ipc;
		this.dataset = dataset;
    }
    
    /*public ComposorThread(String name, Composor composor, String key, DepTree depTree, InnerProductsCache ipc){
        this.name = name;
        this.composor = composor;
        integerDepTreeMap = new HashMap<>();
        integerDepTreeMap.put(key, depTree);
        treeRepresentations = new HashMap<>();
        this.ipc = ipc;
    }
	*/
	
	public String getName(){
		return name;
	}
    
    private NNumber similarity(int twIndex1, int twIndex2){
        String word1 = Vocabulary.getTargetWord(twIndex1).getWord();
        String word2 = Vocabulary.getTargetWord(twIndex2).getWord();
        
        //NNumber ip12 = ipc.getInnerProduct(twIndex1, twIndex2, true);
        NNumber ip12 = ipc.getInnerProduct(word1, word2, true);
        if(ip12 == null) return null;
        //NNumber ip11 = ipc.getInnerProduct(twIndex1, twIndex1, true);
        NNumber ip11 = ipc.getInnerProduct(word1, word1, true);
        if(ip11 == null) return null;
        //NNumber ip22 = ipc.getInnerProduct(twIndex2, twIndex2, true);
        NNumber ip22 = ipc.getInnerProduct(word2, word2, true);
        if(ip22 == null) return null;
        
        NNumber similarity = ip12.multiply(ip11.multiply(ip22).sqrt().reciprocal());
        return similarity;
    }

    private void analyse(String index, String phrase, NNumberVector[] vectors, LinearCombinationMatrix[] matrices, InnerProductsCache ipc){
        int card = 0;
        double[] sums = new double[3];
        for(int i=0; i<Vocabulary.getSize(); i++){
            NNumber weight = vectors[0].getWeight(i);
            if(weight != null && !weight.isZero()){
                card++;
                for(int j=0; j<3; j++){
                    NNumber ww = vectors[j].getWeight(i);
                    if(ww != null && !ww.isZero()){
                        double value = ww.getDoubleValue();
                        sums[j] += value;
                    }
                }
            }
        }
        double[] entropies = new double[3];
        for(int i=0; i<Vocabulary.getSize(); i++){
            NNumber weight = vectors[0].getWeight(i);
            if(weight != null && !weight.isZero()){
                for(int j=0; j<3; j++){
                    NNumber ww = vectors[j].getWeight(i);
                    if(ww != null && !ww.isZero()){
                        double value = ww.getDoubleValue() / sums[j];
                        entropies[j] += -value * Math.log(value) / log2;
                    }
                }
            }
        }
        
		
		double[] renyi2Entropies = new double[3];
		for(int j=0; j<3; j++){
			renyi2Entropies[j] = matrices[j].getRenyi2Entropy(ipc);
		}
        
		String[] labels = new String[]{ "weights.sum", "weights.ent", "sims.sum", "sims.ent", "weightedSims.sum", "weightedSims.ent", "dep.renyi", "head.renyi", "phrase.renyi" };
		double[] values = new double[]{ sums[0], entropies[0], sums[1], entropies[1], sums[2], entropies[2], renyi2Entropies[0], renyi2Entropies[1], renyi2Entropies[2] };
		
		experiment.dep.pkc2006.Instance instance = (experiment.dep.pkc2006.Instance) dataset.getInstance(Integer.parseInt(index));
		//String s = "[ComposorThread] (" + name + ") \"" + phrase + "\": card=" + card;
		for(int i=0; i<labels.length; i++){
			//s += ", " + labels[i] + "=" + values[i];
			instance.fv.setValue(labels[i], values[i]);
			for(int j=0; j<labels.length; j++){
				if(i!=j){
					//s += ", " + labels[i] + "/" + labels[j] + "=" + (values[i] / values[j]);
					instance.fv.setValue(labels[i] + "/" + labels[j], values[i] / values[j]);
				}
			}
		}
		//Helper.report(s);
    }
    
    
    private LinearCombinationMatrix applyContext(String index, DepNode headNode, DepRelationCluster drc, LinearCombinationMatrix dependentRepresentation, boolean headHasLexicalRepresentation){
        
        //System.out.println("head has repr = " + headHasLexicalRepresentation); //DEBUG
        
        NNumberVector partialTraceDiagonalVector = dependentRepresentation.getPartialTraceDiagonalVector(drc.getModeIndex());
        //System.out.println("part(" + " " + dependentRepresentation + ") = " + partialTraceVector); //DEBUG
        partialTraceDiagonalVector.keepOnlyTopNWeights(20); //restore me

        //multiply the weights for alternative heads by their similarities with given head
        NNumberVector weightedSimilaritiesVector = new NNumberVector(Vocabulary.getSize());
        NNumberVector similaritiesVector = new NNumberVector(Vocabulary.getSize());
        
        //start the dop to be returned by adding 1 at the head's target word index
        TargetWord head = Vocabulary.getTargetWord(headNode.getWord());
        int headTargetWordIndex = head.getIndex();
        weightedSimilaritiesVector.setWeight(headTargetWordIndex, NNumber.one());
        
        //if there is a head representation, multiply weights by similarities
        if(headHasLexicalRepresentation){
            //String s = "similarities: "; //DEBUG
            //go through all target words
            for(int i=0; i<Vocabulary.getSize(); i++){
                NNumber weight = partialTraceDiagonalVector.getWeight(i);
                if(weight != null && !weight.isZero()){
                    NNumber similarity = similarity(headTargetWordIndex, i);
                    if(similarity != null && !similarity.isZero()){
                        //s += "sim(" + headNode.getWord() + ", " + Vocabulary.getTargetWord(i).getWord() + ") = " + similarity + " "; //DEBUG
                        weightedSimilaritiesVector.add(i, weight.multiply(similarity));
                        similaritiesVector.add(i, similarity);
                    }
                }
            }
            //System.out.println(s); //DEBUG
        }

        //otherwise use weights not multiplied by similarities (because head representation is replaced by identity matrix)
        LinearCombinationMatrix dop = new LinearCombinationMatrix(weightedSimilaritiesVector);
        dop.setName(headNode.getWord() + "-" + dependentRepresentation.getName());
        
        //analysis
		LinearCombinationMatrix normalisedDop = dop.getCopy();
		normalisedDop.normalize(true);
        analyse(
			index,
			headNode.getWord() + "-" + dependentRepresentation.getName(),
				new NNumberVector[]{
					partialTraceDiagonalVector,
					similaritiesVector,
					weightedSimilaritiesVector 
				},
				new LinearCombinationMatrix[]{
					dependentRepresentation,
					new LinearCombinationMatrix(Vocabulary.getTargetWord(headTargetWordIndex)),
					normalisedDop
				},
				ipc
		);
        
        return dop;
    }

    private LinearCombinationMatrix getRepresentation(String index, DepNode node){
        
        TargetWord tw = Vocabulary.getTargetWord(node.getWord());
        LinearCombinationMatrix dop;
        boolean nodeHasLexicalRepresentation;

        //check that the ldop exists and is non-zero
        if(tw.hasLexicalRepresentation() && !tw.getLexicalRepresentation().isZero()){
            //return a one hot linear combination matrix
            dop = new LinearCombinationMatrix(tw);
            nodeHasLexicalRepresentation = true;
        }else{
            dop = null;
            nodeHasLexicalRepresentation = false;
        }

        //if given node has at least one dependent
        if(!node.isLeaf()){
            //go through all dependents
            for(DepArc depArc : node.getNeighbourArcSet()){
                if(depArc.drc.isFromHeadToDependent()){
                    DepNode dependentNode = depArc.neighbourNode;
                    LinearCombinationMatrix dependentNodeRepresentation = getRepresentation(index, dependentNode);
                    //only process dependent that have a representation
                    if(dependentNodeRepresentation != null && !dependentNodeRepresentation.isZero()){
                        LinearCombinationMatrix ac = applyContext(index, node, depArc.drc, dependentNodeRepresentation, nodeHasLexicalRepresentation);
                        if(dop == null){
                            dop = ac;
                        }else if(ac != null){
                            //String s = dop + " + ac(" + dependentNode.getWord() + ") " + ac;
                            dop.add(ac);
                            //Helper.report("[ComposorThread] (" + name + ") " + s + " = " + dop); //DEBUG
                            //Helper.report("[ComposorThread] (" + name + ") #" + index + " composing <" + node.getWord() + ", " + depArc.drc.getName() + ", " + dependentNode.getWord() + ">");
                        }
                    }
                }
            }
            if(dop != null && !dop.isZero()) dop.normalize(true);
        }

		node.setRepresentation(dop);
        //Helper.report("[ComposorThread] (" + name + ") #" + delme + " repr(" + node.getWord() + ") = " + dop); //DEBUG
        
        return dop;
    }
    
    private LinearCombinationMatrix getRepresentation(String index, DepTree depTree){
            return getRepresentation(index, depTree.getRootNode());
    }
    
    public void composeTrees(){
		LinearCombinationMatrix m;
        for(String index : indexDepTreeMap.keySet()){
			//save root node representation
            //LinearCombinationMatrix treeRepresentation = getRepresentation(index, integerDepTreeMap.get(index));
            //treeRepresentation.setName(index);
            //treeRepresentations.put(index, treeRepresentation);
			
			//save root and sub-root nodes' representations
			getRepresentation(index, indexDepTreeMap.get(index));
			DepTree depTree = indexDepTreeMap.get(index);
			m = depTree.getRootNode().getRepresentation();
			m.setName(index);
			treeRepresentations.put(index, m);
			
			/* save representations of subroot nodes for later
			int i=0;
			for(DepNode dependent : depTree.getRootNode().getDependents()){
				m = dependent.getRepresentation();
				if(m != null && !m.isZero()){
					m.setName(index + "." + i);
					treeRepresentations.put(index + "." + i, m);
					i++;
				}
			}
			*/
			
			Helper.report("[ComposorThread] (" + name + ") ...Finished composing sentence #" + index);
        }
    }

    @Override
	public void run() {
		composeTrees();
		composor.reportComposorThreadDone(this, treeRepresentations, ipc);
    }
    
}

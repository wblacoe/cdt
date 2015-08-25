package composition.dep;

import corpus.dep.converter.DepArc;
import corpus.dep.converter.DepNode;
import corpus.dep.converter.DepTree;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import innerProduct.InnerProductsCache;
import java.util.HashMap;
import linearAlgebra.Matrix;
import linearAlgebra.value.LinearCombinationMatrix;
import numberTypes.NNumber;
import numberTypes.NNumberVector;
import space.dep.DepRelationCluster;

/**
 *
 * @author wblacoe
 */
public class ComposorThread implements Runnable {

    private Composor composor;
    private HashMap<Integer, DepTree> integerDepTreeMap;
    private HashMap<Integer, Matrix> treeRepresentations;
    private InnerProductsCache ipc;
    
    public ComposorThread(HashMap<Integer, DepTree> integerDepTreeMap, InnerProductsCache ipc){
        this.integerDepTreeMap = integerDepTreeMap;
        treeRepresentations = new HashMap<>();
        this.ipc = ipc;
    }
    
    /*private NNumber lookUpFrobeniusInnerProduct(String word1, String word2){
        String key = word1.compareTo(word2) <= 0 ? word1 + "\t" + word2 : word2 + "\t" + word1;
        return frobeniusInnerProducts.get(key);
    }
    
    private NNumber getFrobeniusInnerProduct(TargetWord tw1, TargetWord tw2){
        String word1 = tw1.getWord();
        String word2 = tw2.getWord();
        NNumber ip = lookUpFrobeniusInnerProduct(tw1.getWord(), tw2.getWord());
        if(ip == null){
            ValueMatrix m1 = (ValueMatrix) tw1.getRepresentation();
            ValueMatrix m2 = (ValueMatrix) tw2.getRepresentation();
            ip = m1.innerProduct(m2);
            //save for future use
            String key = word1.compareTo(word2) <= 0 ? word1 + "\t" + word2 : word2 + "\t" + word1;
            frobeniusInnerProducts.put(key, ip);
        }
        
        return ip;
    }
    
    //this can be changed
    private NNumber similarity(TargetWord tw1, TargetWord tw2){
        NNumber ip11 = getFrobeniusInnerProduct(tw1, tw1);
        NNumber ip12 = getFrobeniusInnerProduct(tw2, tw2);
        NNumber ip22 = getFrobeniusInnerProduct(tw1, tw2);
        
        NNumber similarity = ip12.multiply(ip11.multiply(ip22).reciprocal());
        
        return similarity;
    }
    */
    
    private NNumber similarity(int twIndex1, int twIndex2){
        NNumber ip12 = ipc.getInnerProduct(twIndex1, twIndex2, true);
        NNumber ip11 = ipc.getInnerProduct(twIndex1, twIndex1, true);
        NNumber ip22 = ipc.getInnerProduct(twIndex2, twIndex2, true);
        
        NNumber similarity = ip12.multiply(ip11.multiply(ip22).reciprocal());
        
        return similarity;
    }

    
    private LinearCombinationMatrix applyContext(DepNode headNode, DepRelationCluster drc, DepNode dependentNode){
        
        //start the dop to be returned by adding 1 at the head's target word index
        TargetWord head = Vocabulary.getTargetWord(headNode.getWord());
        int headTargetWordIndex = head.getIndex();
        LinearCombinationMatrix dop = new LinearCombinationMatrix();
        dop.setWeight(headTargetWordIndex, NNumber.one());
        
        //get the dependent's partial trace vector
        TargetWord dependent = Vocabulary.getTargetWord(dependentNode.getWord());
        LinearCombinationMatrix dependentRepresentation = (LinearCombinationMatrix) dependent.getRepresentation();
        NNumberVector partialTraceVector = dependentRepresentation.getPartialTraceVector(drc.getModeIndex());
        
        //multiply the weights for alternative heads by their similarities with given head
        NNumberVector weightedSimilaritiesVector = new NNumberVector(partialTraceVector.getLength());
        for(int i=0; i<partialTraceVector.getLength(); i++){
            NNumber weight = partialTraceVector.getWeight(i);
            if(weight != null && !weight.isZero()){
                //TargetWord alternateHead = Vocabulary.getTargetWord(i);
                //NNumber similarity = similarity(head, alternateHead);
                NNumber similarity = similarity(headTargetWordIndex, i);
                if(similarity != null && !similarity.isZero()){
                    weightedSimilaritiesVector.setWeight(i, weight.multiply(similarity));
                }
            }
        }
        
        //create a matrix from the weights-similarities vector
        dop.add(new LinearCombinationMatrix(weightedSimilaritiesVector));
        
        return dop;
    }

    private LinearCombinationMatrix getRepresentation(DepNode node){
        
        //if given node is a leaf
        if(node.isLeaf()){
            //return a linear combination matrix with one 1 and 0's otherwise
            LinearCombinationMatrix ldop = new LinearCombinationMatrix();
            int targetWordIndex = Vocabulary.getTargetWordIndex(node.getWord());
            ldop.setWeight(targetWordIndex, NNumber.one());
            return ldop;
            
        //if given node has at least one dependent
        }else{
            LinearCombinationMatrix dop = new LinearCombinationMatrix();
            for(DepArc depArc : node.getNeighbourArcSet()){
                if(depArc.drc.isFromHeadToDependent()){
                    dop.add(applyContext(node, depArc.drc, depArc.neighbourNode));
                }
            }
            dop.normalize(true);
            return dop;
        }
        
    }
    
    private LinearCombinationMatrix getRepresentation(DepTree depTree){
        return getRepresentation(depTree.getRootNode());
    }
    
    public void composeTrees(){
        for(Integer index : integerDepTreeMap.keySet()){
            LinearCombinationMatrix treeRepresentation = getRepresentation(integerDepTreeMap.get(index));
            treeRepresentations.put(index, treeRepresentation);
        }
    }

    @Override
	public void run() {
		composeTrees();
		composor.reportComposorThreadDone(this, treeRepresentations, ipc);
	}

}

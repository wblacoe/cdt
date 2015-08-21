package composition.dep;

import cdt.Helper;
import corpus.dep.converter.DepTree;
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.Matrix;
import numberTypes.NNumber;
import space.TensorSpace;

/**
 *
 * @author wblacoe
 */
public class Composor {

    private HashSet<Runnable> threads;
    private HashMap<Integer, Matrix> treeRepresentations;
    
    public Composor(){
        threads = new HashSet<>();
    }
    
    public synchronized HashMap<Integer, Matrix> composeTrees(HashMap<Integer, DepTree> depTrees){
        Helper.report("[Composor] Composing all sentences...");
        
        //divide dataset
        int amountOfCores = Helper.getAmountOfCores();
        HashMap<Integer, HashMap<Integer, DepTree>> allSubDatasets = new HashMap<>();
        for(int i=0; i<amountOfCores; i++) allSubDatasets.put(i, new HashMap<Integer, DepTree>());
        int j=0;
        for(Integer index : depTrees.keySet()){
            HashMap<Integer, DepTree> subDataset = allSubDatasets.get(j % amountOfCores);
            subDataset.put(index, depTrees.get(index));
            j++;
        }
        
        //run composor threads
		for(int i=0; i<amountOfCores; i++){
			ComposorThread thread = new ComposorThread(allSubDatasets.get(i), TensorSpace.frobeniusInnerProducts);
			threads.add(thread);
			(new Thread(thread)).start();
		}

		
		try{
			while(!threads.isEmpty()){
				wait();
			}
		}catch(InterruptedException ie){
			ie.printStackTrace();
		}
        
        Helper.report("[Composor] ...Finished composing " + j + " sentences");
        return treeRepresentations;
    }
    
    public synchronized void reportComposorThreadDone(ComposorThread thread, HashMap<Integer, Matrix> treeRepresentations, HashMap<String, NNumber> localFrobeniusInnerProducts){
        TensorSpace.frobeniusInnerProducts.putAll(localFrobeniusInnerProducts);
        threads.remove(thread);
        notify();
    }
    
}

package composition.dep;

import cdt.Helper;
import corpus.dep.converter.DepTree;
import innerProduct.InnerProductsCache;
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.Matrix;

/**
 *
 * @author wblacoe
 */
public class Composor {

    private HashSet<Runnable> threads;
    private HashMap<Integer, Matrix> treeRepresentations;
    private InnerProductsCache ipc;
    
    public Composor(InnerProductsCache ipc){
        threads = new HashSet<>();
        this.ipc = ipc;
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
			ComposorThread thread = new ComposorThread(allSubDatasets.get(i), ipc.getCopy());
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
    
    public synchronized void reportComposorThreadDone(ComposorThread thread, HashMap<Integer, Matrix> treeRepresentations, InnerProductsCache localIpc){
        ipc.integrate(localIpc);
        threads.remove(thread);
        notify();
    }
    
}

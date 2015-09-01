package composition.dep;

import cdt.Helper;
import corpus.dep.converter.DepTree;
import innerProduct.InnerProductsCache;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import linearAlgebra.Matrix;
import linearAlgebra.value.LinearCombinationMatrix;

/**
 *
 * @author wblacoe
 */
public class Composor {

    private HashSet<Runnable> threads;
    private HashMap<String, LinearCombinationMatrix> treeRepresentations;
    private InnerProductsCache ipc;
	private File sdopsFolder, innerProductsFile;
    
    public Composor(InnerProductsCache ipc, File sdopsFolder, File innerProductsFile){
        threads = new HashSet<>();
        treeRepresentations = new HashMap<>();
        this.ipc = ipc;
		this.sdopsFolder = sdopsFolder;
		this.innerProductsFile = innerProductsFile;
    }
    
    
    //divides set of trees into [amount of cores] many subsets
    public synchronized HashMap<String, LinearCombinationMatrix> composeTrees1(HashMap<String, DepTree> depTrees){
        Helper.report("[Composor] Composing all sentences...");
        
        //divide dataset
        int amountOfCores = Helper.getAmountOfCores();
        HashMap<Integer, HashMap<String, DepTree>> allSubDatasets = new HashMap<>();
        for(int i=0; i<amountOfCores; i++) allSubDatasets.put(i, new HashMap<String, DepTree>());
        int j=0;
        for(String index : depTrees.keySet()){
            HashMap<String, DepTree> subDataset = allSubDatasets.get(j % amountOfCores);
            subDataset.put(index, depTrees.get(index));
            j++;
        }
        
        //run composor threads
		for(int i=0; i<amountOfCores; i++){
			ComposorThread thread = new ComposorThread("thread" + i, this, allSubDatasets.get(i), ipc.getCopy());
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
        
        //Helper.report("[Composor] ...Finished composing " + j + " sentences");
        return treeRepresentations;
    }
	
	private void saveAllTreeRepresentationsToFile(File sdopsFile){
		Helper.report("[Composor] Saving " + treeRepresentations.size() + " SDops to " + sdopsFile.getAbsolutePath() + "...");
		
		try{
			BufferedWriter out = Helper.getFileWriter(sdopsFile);
			
			for(String key : treeRepresentations.keySet()){
				Matrix m = treeRepresentations.get(key);
				m.saveToWriter(out);
			}
			
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[Composor] ...Finished saving " + treeRepresentations.size() + " SDops to " + sdopsFile.getAbsolutePath());
	}
    
    //creates one composor thread per tree
    public synchronized HashMap<String, LinearCombinationMatrix> composeTrees(HashMap<String, DepTree> depTrees, int amountOfTreesPerThread, int saveSdopsEvery){
		Helper.report("[Composor] Composing " + depTrees.size() + " sentences...");
        
        /*String[] keys = new String[depTrees.size()];
		int i=0;
		for(String key : depTrees.keySet()){
			keys[i] = key;
			i++;
		}
		Arrays.sort(keys);
		*/
		
		Iterator<String> it = depTrees.keySet().iterator();

        int amountOfIOThreads = Helper.getAmountOfCores();
		int threadCounter = 0;
		int sdopsFileCounter = 0;
		//int sdopsCounter = 0;
		try{
			do{
				
				while(threads.size() < amountOfIOThreads && it.hasNext()){
					HashMap<String, DepTree> indexDepTreeMap = new HashMap<>();
					for(int i=0; i<amountOfTreesPerThread && it.hasNext(); i++){
						String index = it.next();
						indexDepTreeMap.put(index, depTrees.get(index));
					}
                    ComposorThread thread = new ComposorThread("thread" + threadCounter, this, indexDepTreeMap, ipc.getCopy());
					threads.add(thread);
					(new Thread(thread)).start();
					
					threadCounter++;
				}
				
				wait();
				if(treeRepresentations.size() >= saveSdopsEvery){
					//flush out all tree representations computed so far (clear up memory)
					saveAllTreeRepresentationsToFile(new File(sdopsFolder, "sdops." + sdopsFileCounter + ".gz"));
					//sdopsCounter += treeRepresentations.size();
					treeRepresentations.clear();
					sdopsFileCounter++;
					
					//update inner products file
					ipc.saveToFile(innerProductsFile);
				}
			}while(!threads.isEmpty() || it.hasNext());
		}catch(InterruptedException e){}
				
		
		Helper.report("[Composor] ...Finished composing " + /*sdopsCounter + "/" + depTrees.size() +*/ " sentences");
        return treeRepresentations;
	}
    
    public synchronized void reportComposorThreadDone(ComposorThread thread, HashMap<String, LinearCombinationMatrix> localTreeRepresentations, InnerProductsCache localIpc){
		Helper.report("[Composor] ...Finished composor thread (" + thread.getName() + ")");
        ipc.integrate(localIpc);
        treeRepresentations.putAll(localTreeRepresentations);
        threads.remove(thread);
        notify();
    }
    
}

package featureExtraction.fromDensityMatrices;

import cdt.Helper;
import experiment.AbstractInstance;
import experiment.Dataset;
import featureExtraction.AbstractFeatureVectorExtractor;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class FeatureVectorExtractor extends AbstractFeatureVectorExtractor {

	private HashSet<Runnable> threads;
    protected InnerProductsCache ipc;
	private FeatureVectorsCollection fvc;
	private ArrayList<String> featureList;

	public FeatureVectorExtractor(InnerProductsCache ipc, ArrayList<String> featureList) {
		name = "fromDensityMatrices";
		threads = new HashSet<>();
        this.ipc = ipc;
		fvc = new FeatureVectorsCollection();
		fvc.applyFeatureSelection(featureList);
		this.featureList = featureList;
    }

    
    @Override
    public synchronized FeatureVectorsCollection extract(Dataset dataset){

		/*Set<Integer> indicesSet = dataset.getIndicesSet();
		Integer[] indicesArray = new Integer[indicesSet.size()];
		int i = 0;
		for(Integer index : indicesSet) indicesArray[i++] = index;
		Arrays.sort(indicesArray);
		*/
		
		Iterator<Integer> it = dataset.getIndicesSet().iterator();
		int amountOfInstancesPerThread = 100;
		
		int amountOfIOThreads = Helper.getAmountOfCores();
		int threadCounter = 0;
		try{
			do{
				
				while(threads.size() < amountOfIOThreads && it.hasNext()){
					HashMap<Integer, AbstractInstance> indexInstanceMap = new HashMap<>();
					for(int i=0; i<amountOfInstancesPerThread && it.hasNext(); i++){
						Integer index = it.next();
						indexInstanceMap.put(index, dataset.getInstance(index));
					}
					FeatureVectorExtractorThread thread = new FeatureVectorExtractorThread(this, "#" + threadCounter, indexInstanceMap, ipc.getCopy(), featureList);
					threads.add(thread);
					(new Thread(thread)).start();
					
					threadCounter++;
				}
				
				wait();
			}while(!threads.isEmpty() || it.hasNext());
		}catch(InterruptedException e){}
		
		return fvc;
    }

		//
	protected synchronized void reportMatrixImporterDone(FeatureVectorExtractorThread thread, FeatureVectorsCollection fvc){
		Helper.report("[FeatureVectorExtractor] ...Finished feature vector extractor thread (" + thread.getName() + ")");
		try{
			this.fvc.append(fvc);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		threads.remove(thread);
		notify();
	}

}

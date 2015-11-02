package experiment.dep;

import cdt.Helper;
import composition.dep.Composor;
import corpus.dep.converter.DepNode;
import corpus.dep.converter.DepTree;
import experiment.AbstractInstance;
import experiment.Experiment;
import featureExtraction.AbstractFeatureVectorExtractor;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import io.CustomMatrixExporter;
import io.CustomMatrixImporter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import linearAlgebra.Matrix;
import linearAlgebra.value.LinearCombinationMatrix;

/**
 *
 * @author wblacoe
 */
public class DepExperiment extends Experiment {

    protected HashMap<String, Matrix> indexSdopsMap;

    public DepExperiment(){
        super();
        indexSdopsMap = new HashMap<>();
    }

    
    public void composeTrees(InnerProductsCache ipc, File sdopsFolder, File innerProductsFile, int amountOfTreesPerThread, int saveSdopsEvery){
        HashMap<String, DepTree> indexDepTreesMap = new HashMap<>();
        
        for(Integer instanceIndex : dataset.getIndicesSet()){
            AbstractInstance instance = dataset.getInstance(instanceIndex);
			if(instance.getClass().equals(experiment.dep.conll2015.Instance.class)){
				DepTree depTree1 = ((experiment.dep.conll2015.Instance) instance).arguments[0];
				DepTree depTree2 = ((experiment.dep.conll2015.Instance) instance).arguments[1];
				indexDepTreesMap.put(instanceIndex + ".1", depTree1);
				indexDepTreesMap.put(instanceIndex + ".2", depTree2);
			}else if(instance.getClass().equals(experiment.dep.four4cl.Instance.class)){
				DepTree depTree1 = ((experiment.dep.four4cl.Instance) instance).sentenceTrees[0];
				DepTree depTree2 = ((experiment.dep.four4cl.Instance) instance).sentenceTrees[1];
				indexDepTreesMap.put(instanceIndex + ".1", depTree1);
				indexDepTreesMap.put(instanceIndex + ".2", depTree2);
			}
        }
        
        Composor cmp = new Composor(ipc, sdopsFolder, innerProductsFile);
		indexSdopsMap.putAll(cmp.composeTrees(indexDepTreesMap, amountOfTreesPerThread, saveSdopsEvery));
    }
    
    public void saveSdopsToFiles(File sdopsFolder){
        CustomMatrixExporter me = new CustomMatrixExporter();
        me.exportMatricesToFiles(indexSdopsMap, sdopsFolder, 8);
    }
	
	public void importSdopsFromFiles(File sdopsFolder){
		CustomMatrixImporter mi = new CustomMatrixImporter(false);
		File[] sdopsFiles = sdopsFolder.listFiles();
		Arrays.sort(sdopsFiles);
		HashMap<String, Matrix> indexMatrixMap = mi.importMatricesFromFiles(sdopsFiles);
		indexSdopsMap.putAll(indexMatrixMap);
	}
	
	private void attachSdopsToSubRoots(String index, DepTree depTree){
		DepNode rootNode = depTree.getRootNode();
		int i=0;
		for(DepNode subRootNode : rootNode.getDependents()){
			//it does not matter if the attachment order is not the same as at composition time because the feature extraction operations are symmetric and associative
			LinearCombinationMatrix m = (LinearCombinationMatrix) indexSdopsMap.get(index + "." + i);
			subRootNode.setRepresentation(m);
			i++;
		}
	}
	
	public void attachSdopsToDataset(){
		Helper.report("[DepExperiment] Attaching matrices to dataset...");
		
		for(Integer index : dataset.getIndicesSet()){
			AbstractInstance instance = dataset.getInstance(index);
			
			if(instance.getClass().equals(experiment.dep.conll2015.Instance.class)){
				DepTree depTree1 = ((experiment.dep.conll2015.Instance) instance).arguments[0];
				DepTree depTree2 = ((experiment.dep.conll2015.Instance) instance).arguments[1];
				LinearCombinationMatrix sdop1 = (LinearCombinationMatrix) indexSdopsMap.get(index + ".1");
				LinearCombinationMatrix sdop2 = (LinearCombinationMatrix) indexSdopsMap.get(index + ".2");
				depTree1.getRootNode().setRepresentation(sdop1);
				attachSdopsToSubRoots(index + ".1", depTree1);
				depTree2.getRootNode().setRepresentation(sdop2);
				attachSdopsToSubRoots(index + ".2", depTree2);
			}else if(instance.getClass().equals(experiment.dep.four4cl.Instance.class)){
				DepTree depTree1 = ((experiment.dep.four4cl.Instance) instance).sentenceTrees[0];
				DepTree depTree2 = ((experiment.dep.four4cl.Instance) instance).sentenceTrees[1];
				LinearCombinationMatrix sdop1 = (LinearCombinationMatrix) indexSdopsMap.get(index + ".1");
				LinearCombinationMatrix sdop2 = (LinearCombinationMatrix) indexSdopsMap.get(index + ".2");
				depTree1.getRootNode().setRepresentation(sdop1);
				attachSdopsToSubRoots(index + ".1", depTree1);
				depTree2.getRootNode().setRepresentation(sdop2);
				attachSdopsToSubRoots(index + ".2", depTree2);
			}
		}
		
		Helper.report("[DepExperiment] ...Finished attaching matrices to dataset");
	}
	
	public FeatureVectorsCollection extractFeatureVectorsFromSdops(InnerProductsCache ipc, ArrayList<String> featureList){
		String[] namesOfExtractors = new String[]{ "fromDensityMatrices" };
		AbstractFeatureVectorExtractor ex = new AbstractFeatureVectorExtractor(namesOfExtractors, ipc, featureList);
		return ex.extract(dataset);
	}
    
}

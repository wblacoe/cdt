package experiment.dep.conll2015;

import cdt.Helper;
import experiment.Dataset;
import experiment.dep.DepExperiment;
import featureExtraction.FeatureVector;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import numberTypes.NNumber;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
public class Conll2015 extends DepExperiment {

    public Conll2015(){
        dataset = new Dataset();
    }
    
    public void importDataset(File datasetFile){
        Helper.report("[Conll2015] Importing dataset from " + datasetFile.getAbsolutePath() + "...");
        
        try{
            BufferedReader in = Helper.getFileReader(datasetFile);
            
            while(true){
                Instance instance = Instance.importFromReader(in);
                if(instance == null){
                    break;
                }else{
                    dataset.setInstance(instance.getIndex(), instance);
                }
            }
            
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Helper.report("[Conll2015] ...Finished importing dataset (" + dataset.getSize() + " instances) from " + datasetFile.getAbsolutePath());
    }
	
	public void exportFeatureVectorsCollectionToJsonFile(FeatureVectorsCollection fvc, File featureVectorsFile){
		Helper.report("[Conll2015] Exporting feature vectors to \"" + featureVectorsFile.getAbsolutePath() + "...");

		if(!featureVectorsFile.getParentFile().exists()) featureVectorsFile.getParentFile().mkdirs();
		
		int counter = 0;
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(featureVectorsFile));
			
			TreeMap<Integer, FeatureVector> indexFeatureVectorMap = fvc.getIndexFeatureVectorMap();
			ArrayList<String> featureNamesList = fvc.getFeatureNamesList();
			
			//go through all feature vectors
			for(Integer index : indexFeatureVectorMap.navigableKeySet()){
				Instance instance = (Instance) dataset.getInstance(index);
				
				out.write("{ ");
				
				String qf = "'QuantumFeatures': [";
				FeatureVector fv = fvc.getFeatureVector(index);
				boolean isFirst = true;
				for(String featureName : featureNamesList){
					qf += (isFirst ? "" : ", ") + fv.getValue(featureName);
					isFirst = false;
				}
				qf += "],";
				out.write(qf + " 'DocID': '" + instance.documentId + "', 'ID': '" + index + "' }\n");
				counter++;
			}
			
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[Conll2015] ...Finished exporting " + counter + " feature vectors of length " + fvc.getAmountOfFeatures());
	}

    
    public static void main(String[] args){
        
        //space
        //File projectFolder = new File("/local/falken-1");
        //File projectFolder = new File("/local/william/falken-1");
		File projectFolder = new File("/local/william");
        //File projectFolder = new File("/disk/scratch/william.banff");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/conll2015/space.conll2015");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        
        //gather and save joint counts
        //File jdopsFolder = new File(projectFolder, "experiments/conll2015/jdops");
		//exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        //File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        //DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        //int delta = 5000;
        //int ldopCardinality = 2000;
        //SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
        File ldopsFolder = new File(projectFolder, "experiments/conll2015/ldops");
        //exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
        
        //inner products cache
        File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/conll2015/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
        ipc.importFromFile(innerProductsFile);

        //experiment
        Conll2015 exp = new Conll2015();
		
		//composition
        File datasetFile = new File(projectFolder, "datasets/conll2015/trainAndDevAndTest.implicitOnly");
		//File datasetFile = new File(projectFolder, "datasets/conll2015/tiny");
        exp.importDataset(datasetFile);
        exp.importMatrices(ldopsFolder, false);
		File sdopsFolder = new File(projectFolder, "experiments/conll2015/sdops");
		//int saveSdopsEvery = 50;
		//int amountOfTreesPerThread = 100;
		//exp.composeTrees(ipc, sdopsFolder, innerProductsFile, amountOfTreesPerThread, saveSdopsEvery);
		//exp.saveSdopsToFiles(sdopsFolder);
		//ipc.saveToFile(innerProductsFile);

		//feature extraction
		exp.importSdopsFromFiles(sdopsFolder);
		exp.attachSdopsToDataset();
		File featureListFile = new File(projectFolder, "experiments/conll2015/featurelists/discourseRelations.featurelist");
		ArrayList<String> featureList = FeatureVectorsCollection.importFeatureNamesList(featureListFile);
		FeatureVectorsCollection fvc = exp.extractFeatureVectorsFromSdops(ipc, featureList);
		fvc.exportToFile(new File(projectFolder, "experiments/conll2015/discourseRelations.fvec"));
		exp.exportFeatureVectorsCollectionToJsonFile(fvc, new File(projectFolder, "experiments/conll2015/discourseRelations.json"));
		
    }
}
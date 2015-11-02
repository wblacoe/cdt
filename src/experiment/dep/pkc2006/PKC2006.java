package experiment.dep.pkc2006;

import cdt.Helper;
import experiment.Dataset;
import experiment.dep.DepExperiment;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import numberTypes.NNumber;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
public class PKC2006 extends DepExperiment {

    public PKC2006(){
        super();
    }
    
    public void importDataset(File datasetFile){
        Helper.report("[PKC2006] Importing dataset from " + datasetFile.getAbsolutePath() + "...");
        
        dataset = new Dataset();
        try{
            BufferedReader in = Helper.getFileReader(datasetFile);
            
			String line;
            while((line = in.readLine()) != null){
                Instance instance = Instance.importFromString(line);
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
        
        Helper.report("[PKC2006] ...Finished importing dataset (" + dataset.getSize() + " instances) from " + datasetFile.getAbsolutePath());
    }
	
	public void createArff(File arffFile){
		Helper.report("[PKC2006] Creating arff file...");

		//create feature vector collection
		FeatureVectorsCollection fvc = new FeatureVectorsCollection();
		int counter = 0;
		try{
			for(Integer index : dataset.getIndicesSet()){
				Instance instance = (Instance) dataset.getInstance(index);
				if(instance.toDepTree() != null){
					fvc.append(instance.getIndex(), instance.fv);
					counter++;
				}
			}
			fvc.setOutputFeature("expected");
		}catch(Exception e){}

		
		fvc.exportToArffFile(arffFile);
		
		Helper.report("[PKC2006] ...Finished creating arff file with " + counter + " instances");
	}
    
    public static void mainCorrectedAssociationFunction(String[] args){
        //space
        File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/msrscc/msrscc.space");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        
        //experiment
        PKC2006 exp = new PKC2006();

        //gather and save joint counts
        //File jdopsFolder = new File(projectFolder, "experiments/msrscc/jdops");
		//exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
        
		//associationate jdops to ldops
        /*File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        int delta = 10;
        int ldopCardinality = 2000;
        SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
		File ldopsFolder = new File(projectFolder, "experiments/msrscc/ldops");
		exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
		*/
        
        /*File ldopsFolderUgly = new File(projectFolder, "experiments/msrscc/ldops.ugly");
        File ldopsFolderPretty = new File(projectFolder, "experiments/msrscc/ldops.pretty");
		Helper.prettyPrint = false;
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolderUgly);
        Helper.prettyPrint = true;
        exp.saveMatrices(ldopsFolderPretty);
        exp.importMatrices(ldopsFolderUgly, true);
        */
		
		File ldopsFolder = new File(projectFolder, "experiments/msrscc/ldops");
		exp.importMatrices(ldopsFolder, true);
        
        File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/msrscc/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
		ipc.importFromFile(innerProductsFile);

        File datasetFile = new File(projectFolder, "datasets/pkc2006/dataset");
        exp.importDataset(datasetFile);
        
        File sdopsFolder = new File(projectFolder, "experiments/pkc2006/sdops");
        exp.composeTrees(ipc, sdopsFolder, innerProductsFile, 1, 1);
		
		File arffFile = new File(projectFolder, "experiments/pkc2006/available.arff");
		exp.createArff(arffFile);
    }
    
    public static void main(String[] args){
        mainCorrectedAssociationFunction(args);
    }

}

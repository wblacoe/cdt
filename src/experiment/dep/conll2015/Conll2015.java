package experiment.dep.conll2015;

import cdt.Helper;
import corpus.associationFunction.SppmiFunction;
import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.Dataset;
import experiment.dep.DepExperiment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
    
    public void run(){
        File datasetFile = new File("/local/william/datasets/conll2015/train.onlyImplicit");
        importDataset(datasetFile);
    }

    
    public static void main(String[] args){
        
        //space
        //File projectFolder = new File("/local/falken-1");
        File projectFolder = new File("/local/william");
        //File projectFolder = new File("/disk/scratch/william.banff");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/conll2015/space.conll2015");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        DepNeighbourhoodSpace.setFrobeniusInnerProductsFile(new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/conll2015/innerProducts.txt"));
        
        //experiment
        Conll2015 exp = new Conll2015();
        
        //gather and save joint counts
        File jdopsFolder = new File(projectFolder, "experiments/conll2015/jdops");
		exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        SppmiFunction sf = new SppmiFunction(dmc, 5000, 2000);
        File ldopsFolder = new File(projectFolder, "experiments/conll2015/ldops");
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
                
    }
    
}

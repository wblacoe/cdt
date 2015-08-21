package experiment.dep.msrscc;

import cdt.Helper;
import corpus.associationFunction.SppmiFunction;
import corpus.dep.converter.DepTree;
import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.dep.DepExperiment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import numberTypes.NNumber;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
public class Msrscc extends DepExperiment {

    public Msrscc(){

    }
    
    protected HashMap<Integer, DepTree> getIntegerDepTreeMap(){
        HashMap<Integer, DepTree> integerDepTreeMap = new HashMap<>();
        
        for(Integer index : dataset.getIndicesSet()){
            Instance instance = (Instance) dataset.getInstance(index);
            for(int i=1; i<=5; i++){
                DepTree depSentence = instance.getDepSentence(i);
                integerDepTreeMap.put(5 * (index-1) + i, depSentence);
            }
        }
        
        return integerDepTreeMap;
    }
    
    public void importDataset(File datasetFile){
        Helper.report("[Msrscc] Importing dataset from " + datasetFile.getAbsolutePath() + "...");
        
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
        
        Helper.report("[Msrscc] ...Finished importing dataset (" + dataset.getSize() + " instances) from " + datasetFile.getAbsolutePath());
    }
    
    public void run(){
        File datasetFile = new File("/local/william/datasets/msrscc/all.parsed");
        importDataset(datasetFile);
    }
    
    public static void main(String[] args){
        
        //space
        //File projectFolder = new File("/local/falken-1");
        //File projectFolder = new File("/local/william");
        File projectFolder = new File("/disk/scratch/william.banff");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/msrscc/space.msrscc");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        DepNeighbourhoodSpace.setFrobeniusInnerProductsFile(new File(projectFolder, "innerProducts.txt"));
        
        //experiment
        Msrscc exp = new Msrscc();
        
        //gather and save joint counts
        File jdopsFolder = new File(projectFolder, "experiments/msrscc/jdops");
		//exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        SppmiFunction sf = new SppmiFunction(dmc, 5000, 2000);
        File ldopsFolder = new File(projectFolder, "experiments/msrscc/ldops");
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
                
    }
    
}

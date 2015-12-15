package experiment.wordlevel.wordsim353;

import cdt.Helper;
import sprwikiwordrelatedness.Spearman;
import corpus.associationFunction.SppmiFunction;
import corpus.dep.marginalizer.DepMarginalCounts;
import corpus.dep.marginalizer.DepMarginalizer;
import experiment.AbstractInstance;
import experiment.Dataset;
import experiment.Experiment;
import innerProduct.InnerProductsCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Vector;
import numberTypes.NNumber;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
public class Wordsim353 extends Experiment{
    
    public Wordsim353(){
        dataset = new Dataset();
    }

    //import tab-separated dataset
    public void importDataset(File datasetFile){
        Helper.report("[Wordsim353] Importing dataset from " + datasetFile.getAbsolutePath() + "...");
        
        try{
            BufferedReader in = Helper.getFileReader(datasetFile);
            
            int index = 0;
            while(true){
                Instance instance = Instance.importFromReader(in);
                if(instance == null){
                    break;
                }else{
                    instance.setIndex(index);
                    dataset.setInstance(index, instance);
                    index++;
                }
            }
            
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Helper.report("[Wordsim353] ...Finished importing dataset (" + dataset.getSize() + " instances) from " + datasetFile.getAbsolutePath());
    }
    
    public void predict(InnerProductsCache ipc){
        for(Entry<Integer, AbstractInstance> entry : dataset.indexInstanceMap.entrySet()){
            Instance instance = (Instance) entry.getValue();
            NNumber ip = ipc.getInnerProduct(instance.word1, instance.word2, true);
            if(ip != null) instance.predicted = ip.getFloatValue();
            Helper.report("[Wordsim353] instance #" + instance.index + " \"" + instance.word1 + ", " + instance.word2 + "\": expected = " + instance.expected + ", predicted = " + instance.predicted); //DEBUG
        }
    }
    
    //Spearman implementation from https://code.google.com/p/sprwikiwordrelatedness/
    public double evaluate(){
        Vector<Double> X = new Vector<Double>();
        Vector<Double> Y = new Vector<Double>();
        
        for(Entry<Integer, AbstractInstance> entry : dataset.indexInstanceMap.entrySet()){
            Instance instance = (Instance) entry.getValue();
            if(instance.predicted != -1){
                X.add(1.0 * instance.expected);
                Y.add(1.0 * instance.predicted);
            }
        }
        
        double spearmanCorrelation = Spearman.GetCorrelation(X, Y);
        Helper.report("[Wordsim353] Spearman correlation = " + spearmanCorrelation);
        return spearmanCorrelation;
    }
   
    
    public static DepMarginalCounts getMarginalCounts(File marginalCountsFile){
        DepMarginalCounts dmc;
        
        if(marginalCountsFile.exists()){
            dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        }else{
            DepMarginalizer dm = new DepMarginalizer();
            dm.marginalize();
            dmc = dm.getMarginalCounts();
            dmc.saveToFile(marginalCountsFile);
        }
        
        return dmc;
    }
    
    public static void main(String[] args){
        
        if(args.length < 1){
            System.out.println("[Wordsim353] Path to project folder not specified!");
        }else{

            //project
            File projectFolder = new File(args[0]);
            System.out.println("Project folder is \"" + projectFolder.getAbsolutePath() + "\""); //DEBUG

            //space
            DepNeighbourhoodSpace.setProjectFolder(projectFolder);
            File spaceFile = new File(projectFolder, "space.txt");
            DepNeighbourhoodSpace.importFromFile(spaceFile); //creates context counts from corpus if not already present
            DepNeighbourhoodSpace.saveToFile(spaceFile);
            DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
            
            //experiment
            Wordsim353 exp = new Wordsim353();

            //gather and save joint counts (jdops = joint counts density operators)
            File jdopsFolder = new File(projectFolder, "jdops");
            exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);

            //associationate jdops to ldops (ldops = lexical density operators)
            Helper.setMinMarginalCount(1); //consider only marginal counts with at least this value
            File marginalCountsFile = new File(projectFolder, DepNeighbourhoodSpace.marginalCountsFilename);
            DepMarginalCounts dmc = getMarginalCounts(marginalCountsFile);
            int delta = 10; //add this value to all association values when creating ldops
            int ldopCardinality = 1000; //keep only the [ldop cardinality] highest positive entries in an ldop
            SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
            File ldopsFolder = new File(projectFolder, "ldops");
            exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
            exp.importMatrices(ldopsFolder, true);
            
            //optionally save matrices in pretty print format
            Helper.prettyPrint = true;
            File ldopsFolderPretty = new File(projectFolder, "ldops.pretty");
            exp.saveMatrices(ldopsFolderPretty);
            Helper.prettyPrint = false;
            
            //prepare a cache object for inner products
            File innerProductsFile = new File(projectFolder, "innerProducts.txt");
            InnerProductsCache ipc = new InnerProductsCache();
            ipc.importFromFile(innerProductsFile);

            //run experiment on data
            File datasetFile = new File(projectFolder, "wordsim353.txt");
            exp.importDataset(datasetFile);
            exp.predict(ipc);
            ipc.saveToFile(innerProductsFile);
            exp.evaluate();
            
        }

    }
    
}

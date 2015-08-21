package experiment.wordlevel.wordsim353;

import cdt.Helper;
import corpus.associationFunction.SppmiFunction;
import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.Dataset;
import experiment.dep.DepExperiment;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import linearAlgebra.BaseTensor;
import linearAlgebra.value.ValueBaseMatrix;
import linearAlgebra.value.ValueMatrix;
import linearAlgebra.value.ValuePlusMatrix;
import numberTypes.NNumber;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
public class Wordsim353 extends DepExperiment{

    private Dataset dataset;
    private HashMap<String, NNumber> similarityCache;
    
    public Wordsim353(){
        dataset = new Dataset();
        similarityCache = new HashMap<>();
    }

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
    
    
    public NNumber getSimilarity(TargetWord tw1, TargetWord tw2){
        String word1 = tw1.getWord();
        String word2 = tw2.getWord();
        String key = word1.compareTo(word2) <= 0 ? word1 + "_#_" + word2 : word2 + "_#_" + word1;
        
        NNumber existingSimilarity = similarityCache.get(key);
        if(existingSimilarity != null){
            return existingSimilarity;
        }else{
            ValueMatrix m1 = (ValueMatrix) tw1.getRepresentation();
            ValueMatrix m2 = (ValueMatrix) tw2.getRepresentation();
            NNumber similarity = m1.times(m2).trace();
            similarityCache.put(key, similarity);
            return similarity;
        }
    }
    
    public void printSimilarities(){
        
        //prepare
        for(int i=0; i<Vocabulary.getSize(); i++){
            TargetWord tw1 = Vocabulary.getTargetWord(i);
            if(tw1 == null || !tw1.hasRepresentation()) continue;
            System.out.println("<" + tw1.getWord() + ", " + tw1.getWord() + "> = " + getSimilarity(tw1, tw1));
        }
        
        //print
        for(int i=0; i<Vocabulary.getSize(); i++){
            TargetWord tw1 = Vocabulary.getTargetWord(i);
            if(tw1 == null || !tw1.hasRepresentation()) continue;
            for(int j=0; j<Vocabulary.getSize(); j++){
                TargetWord tw2 = Vocabulary.getTargetWord(j);
                if(tw2 == null || !tw2.hasRepresentation()) continue;
                NNumber selfSimilarity1 = getSimilarity(tw1, tw1);
                NNumber selfSimilarity2 = getSimilarity(tw2, tw2);
                System.out.println("sim(" + tw1.getWord() + ", " + tw2.getWord() + ") = " + getSimilarity(tw1, tw2).divide(selfSimilarity1.multiply(selfSimilarity2).sqrt()));
            }
        }
        
    }
    
    public void run(){
        
        //hyper-parameters
        String jointCountsFoldername = "experiments/wordsim353/jdops.tiny";
        int jdopsCardinality = 3000;
        double delta = 5000.0;
        File ldopsFolder = new File("/local/william/experiments/wordsim353/ldops.tiny");
        int ldopsCardinality = 1000;

        //create and save jdops
        //extractAndSaveJointCountsFromCorpus(jointCountsFoldername, 8);
        
        //import jdops and marginal counts, create ldops
        //importMatrices(jointCountsFoldername, jdopsCardinality);
        //applyAssociationFunctionAndSave(new SppmiFunction(), "preprocessed/ukwac.depParsed/marginalcounts.gz", delta, ldopsCardinality, ldopsFoldername);
        //Vocabulary.clear();
        
        //import ldops
        importMatrices(ldopsFolder, /*ldopsCardinality,*/ true);
        //printSimilarities();
        
        //need, equip, basketball
        ValueMatrix mNeed = (ValueMatrix) Vocabulary.getTargetWord("need").getRepresentation();
        ValueMatrix mEquip = (ValueMatrix) Vocabulary.getTargetWord("equip").getRepresentation();
        ValueMatrix mBasketball = (ValueMatrix) Vocabulary.getTargetWord("basketball").getRepresentation();
        
        System.out.println("need: " + mNeed.sumOfAllEntries());
        System.out.println("equip: " + mEquip.sumOfAllEntries());
        System.out.println("basketball: " + mBasketball.sumOfAllEntries());
        
        //System.out.println("need + equip: " + mNeed.plus(mEquip).sumOfAllEntries());
        //System.out.println("need + equip - equip: " + mNeed.plus(mEquip).plus(mEquip.invertEntries()).sumOfAllEntries());
        
        ValueMatrix m = new ValueMatrix(1);
        BaseTensor t = new BaseTensor();
        t.setDimensionAtMode(1, 123);
        ValueBaseMatrix bm = new ValueBaseMatrix(t, t, NNumber.create(0.25));
        m.setBaseMatrix(0, bm);
        
        ValuePlusMatrix mNeedSqrt = sqrt(m);
        //ValuePlusMatrix mNeedSqrt = sqrt(mNeed);
        System.out.println("sqrt: " + mNeedSqrt.getValueMatrix().sumOfAllEntries() + " " + mNeedSqrt.getIdentityFactor());
        System.out.println(mNeedSqrt.getValueMatrix());
        ValueMatrix mNeedTest = mNeedSqrt.getValueMatrix().times(mNeedSqrt.getValueMatrix());
        System.out.println("sqrt squared: " + mNeedTest.sumOfAllEntries());
        System.out.println(mNeedTest);
    }
    
    public ValuePlusMatrix sqrt(ValueMatrix m){
        
        m.reduceCardinality(2);
        m.normalize();
        
        m.setSorting(Helper.NOT_SORTED);
        //m = m.times(NNumber.create(100)); //really?
        //ValueMatrix aa = m.getCopy();
        //aa.setName("aa");
        ValuePlusMatrix a = new ValuePlusMatrix(m, NNumber.create(0));
        //ValueMatrix cc = m.getCopy();
        //cc.setName("cc");
        ValuePlusMatrix c = new ValuePlusMatrix(m.getCopy(), NNumber.create(-1));
        System.out.println("a: " + a.getValueMatrix() + " " + a.getIdentityFactor());
        System.out.println("c: " + c.getValueMatrix() + " " + c.getIdentityFactor());
        /*ValuePlusMatrix ac = a.times(c);
        System.out.println("ac: " + ac.getValueMatrix() + " " + ac.getIdentityFactor());
        ValuePlusMatrix aa = a.times(a);
        System.out.println("aa: " + aa.getValueMatrix() + " " + aa.getIdentityFactor());
        ValuePlusMatrix cc = c.times(c);
        System.out.println("cc: " + cc.getValueMatrix() + " " + cc.getIdentityFactor());
        */
        
        NNumber epsilon = NNumber.create(0.00001);
        NNumber minusHalf = NNumber.create(-0.5);
        NNumber quarter = NNumber.create(0.25);
        NNumber minusThree = NNumber.create(-3.0);
        int counter = 0;
        while(true){
            a = a.add(a.times(c).times(minusHalf));
            //a.getValueMatrix().reduceCardinality(2000);
            c = c.times(c).times(c.add(minusThree)).times(quarter);
            //c.getValueMatrix().reduceCardinality(2000);
            
            NNumber e1 = c.getIdentityFactor();
            NNumber e2 = c.getValueMatrix().sumOfAllEntries();
            //System.out.println("e: " + e1 + " " + e2);
            //System.out.println("c: " + c.getValueMatrix());
            System.out.println("a: " + a.getValueMatrix() + " " + a.getIdentityFactor() + " I");
            System.out.println("c: " + c.getValueMatrix() + " " + c.getIdentityFactor() + " I");
            if(/*e1.abs().compareTo(epsilon) <= 0 &&*/ e2.abs().compareTo(epsilon) <= 0){
                break;
            }
            System.out.println("counter: " + (++counter));
        }
        
        
        return a;
    }
    
    
    public static void main(String[] args){
        
        //space
        //File projectFolder = new File("/local/falken-1");
        File projectFolder = new File("/local/william");
        //File projectFolder = new File("/disk/scratch/william.banff");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/wordsim353/space.wordsim353");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        DepNeighbourhoodSpace.setFrobeniusInnerProductsFile(new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/wordsim353/innerProducts.txt"));
        
        //experiment
        Wordsim353 exp = new Wordsim353();
        
        //gather and save joint counts
        /*File jdopsFolder = new File(projectFolder, "experiments/wordsim353/jdops");
		//exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        SppmiFunction sf = new SppmiFunction(dmc, 5000, 2000);
        File ldopsFolder = new File(projectFolder, "experiments/wordsim353/ldops");
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
        */
        
        //experiment
        File datasetFile = new File(projectFolder, "datasets/wordsim353/trainAndTest");
        exp.importDataset(datasetFile);
        File ldopsFolder = new File(projectFolder, "experiments/wordsim353/ldops/done");
        exp.importMatrices(ldopsFolder, false);
        
        
    }
    
}

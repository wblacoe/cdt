package experiment;

import cdt.Helper;
import corpus.associationFunction.AssociationFunction;
import corpus.dep.converter.DepConverter;
import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import io.VocabularyMatrixExporter;
import io.VocabularyMatrixImporter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import linearAlgebra.Matrix;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.ValueMatrix;

/**
 *
 * @author wblacoe
 */
public class Experiment {

    protected TargetElements targetElements;
    protected Dataset dataset;
    
    protected static HashSet<Runnable> experimentThreads;

    public Experiment(){
        targetElements = null;
        dataset = null;
    }
    /*public Experiment(int amountOfTargetElements){
        this();
        targetElements = new TargetElements(amountOfTargetElements);
    }
    */
    
    
    public TargetElements getTargetElements(){
        return targetElements;
    }
    
    public Dataset getDataset(){
        return dataset;
    }
    
    public void extractAndSaveJointCountsFromCorpus(File outputFolder, String prefix, int amountOfOutputFiles, HashSet<String> targetWords){
        DepConverter dc = new DepConverter(targetWords);
        dc.extractJointCountsFromCorpus();
        VocabularyMatrixExporter me = new VocabularyMatrixExporter();
        me.exportMatricesToFiles(outputFolder, prefix, amountOfOutputFiles);
    }
    public void extractAndSaveJointCountsFromCorpus(File outputFolder, String prefix, int amountOfOutputFiles){
        extractAndSaveJointCountsFromCorpus(outputFolder, prefix, amountOfOutputFiles, null);
    }
    public void extractAndSaveJointCountsFromCorpus(File outputFolder, int amountOfOutputFiles, int amountOfTargetWordsPerPhase){
        int amountofTargetWords = TargetElements.getSize();
        int amountOfPhases = (int) Math.ceil(1.0 * amountofTargetWords / amountOfTargetWordsPerPhase);
        
        //go through all phases
        HashSet<String> targetWords = new HashSet<>();
        for(int i=0; i<amountOfPhases; i++){
            int j = 0; //counts the amount of target words used for this phase
            //collect target words for this phase
            while(j < amountOfTargetWordsPerPhase){
                TargetWord tw = Vocabulary.getTargetWord(i * amountOfTargetWordsPerPhase + j);
                if(tw == null) break;
                //System.out.println("Adding " + (i * amountOfTargetWordsPerPhase + j) + ":" + tw.getWord()); //DEBUG
                targetWords.add(tw.getWord());
                j++;
            }
            //run this phase
            Helper.report("[Experiment] Running phase " + (i+1) + "/" + amountOfPhases + "...");
            extractAndSaveJointCountsFromCorpus(outputFolder, "phase" + (i+1) + ".", amountOfOutputFiles, targetWords);
            Helper.report("[Experiment] ...Finished phase " + (i+1) + "/" + amountOfPhases);
            //clear all data from previous phase
            targetWords.clear();
            Vocabulary.removeAllRepresentations();
        }
    }
    
	//TODO count and report successes and other conditions
    public void importAssociationateAndSaveMatrices(File jdopsFolder, DepMarginalCounts dmc, AssociationFunction af, File ldopsFolder){
		File[] jdopsFiles = jdopsFolder.listFiles();
        for(File jdopsFile : jdopsFiles){
            Helper.report("Processing jdops file \"" + jdopsFile.getName() + "\"...");
            try{
                BufferedReader in = Helper.getFileReader(jdopsFile);
                File ldopsFile = new File(ldopsFolder, jdopsFile.getName());
                BufferedWriter out = Helper.getFileWriter(ldopsFile);

                int counter = 0;
                while(true){
                    Matrix m = Matrix.importFromReader(in);
                    if(m == null) break;
                    CountMatrix cm = (CountMatrix) m;
                    ValueMatrix vm = (ValueMatrix) af.compute(cm, m.getName());
                    if(vm == null){
                        //System.out.println("d"); //DEBUG
                    }else{
                        vm.saveToWriter(out);
                    }
                    counter++;
                    if(counter % 100 == 0) Helper.report("Finished processing " + counter + " matrices");
                }
                
                in.close();
                out.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            Helper.report("...Finished processing jdops file \"" + jdopsFile.getName());
        }
    }
    
    public void importMatrices(File folder, boolean normalize){
        VocabularyMatrixImporter mi = new VocabularyMatrixImporter(normalize);
        mi.importMatricesFromFiles(folder.listFiles());
    }
    
    /*public void importMatricesApplyAssociationFunctionAndSaveMatrices(File inputFolder, AssociationFunction af, boolean normalize, File outputFolder){
        MatrixImporter mi = new MatrixImporter(normalize);
        mi.importMatricesApplyAssociationFunctionAndSaveMatrices(inputFolder.listFiles(), af, outputFolder);
    }
    */
    
    public void saveMatrices(File outputFolder){
        VocabularyMatrixExporter me = new VocabularyMatrixExporter();
        me.exportMatricesToFiles(outputFolder, 8);
    }

}

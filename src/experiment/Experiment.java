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
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.Matrix;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.ValueMatrix;
import space.dep.DepNeighbourhoodSpace;

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
    
    public void extractAndSaveJointCountsFromCorpus(File outputFolder, String prefix, int amountOfOutputFilesPerPhase, HashSet<String> targetWords){
        DepConverter dc = new DepConverter(targetWords);
        dc.extractJointCountsFromCorpus(); //attaches jdops to vocabulary
        VocabularyMatrixExporter me = new VocabularyMatrixExporter();
        me.exportMatricesToFiles(outputFolder, prefix, amountOfOutputFilesPerPhase);
    }
    public void extractAndSaveJointCountsFromCorpus(File outputFolder, String prefix, int amountOfOutputFilesPerPhase){
        extractAndSaveJointCountsFromCorpus(outputFolder, prefix, amountOfOutputFilesPerPhase, null);
    }
    public void extractAndSaveJointCountsFromCorpus(File outputFolder, int amountOfOutputFilesPerPhase, int amountOfTargetWordsPerPhase){
        int amountofTargetWords = TargetElements.getSize();
        int amountOfPhases = (int) Math.ceil(1.0 * amountofTargetWords / amountOfTargetWordsPerPhase);
        
        //go through all phases
        HashSet<String> targetWords = new HashSet<>();
        for(int i=0; i<amountOfPhases; i++){
            int j = 0; //counts the amount of target words used for this phase
            //collect target words for this phase
            while(j < amountOfTargetWordsPerPhase){
                TargetWord tw = Vocabulary.getTargetWord(i * amountOfTargetWordsPerPhase + j);
                if(tw != null){
					//System.out.println("Adding " + (i * amountOfTargetWordsPerPhase + j) + ":" + tw.getWord()); //DEBUG
					targetWords.add(tw.getWord());
				}
                j++;
            }
            //run this phase
            Helper.report("[Experiment] Running phase " + (i+1) + "/" + amountOfPhases + "...");
            extractAndSaveJointCountsFromCorpus(outputFolder, "phase" + (i+1) + ".", amountOfOutputFilesPerPhase, targetWords);
            Helper.report("[Experiment] ...Finished phase " + (i+1) + "/" + amountOfPhases);
            //clear all data from previous phase
            targetWords.clear();
            Vocabulary.removeAllLexicalRepresentations();
        }
    }
    
    private HashMap<String, CountMatrix> importJdopsFromReader(BufferedReader in, int amount) throws IOException{
        HashMap<String, CountMatrix> jdops = new HashMap<>();
        for(int i=0; i<amount; i++){
            Matrix m = Matrix.importFromReader(in);
            if(m == null){
                break;
            }else{
                CountMatrix cm = (CountMatrix) m;
                //if(DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationClusterName("obj-1").hasContextWord(cm.getName())){ //DELME
                    jdops.put(cm.getName(), cm);
                //}
            }
        }
        return jdops;
    }
    
	//imports jdops from disk, applies association function, saves ldops to disk, attaches ldops to vocabulary
    public void importAssociationateAndSaveMatrices(File jdopsFolder, DepMarginalCounts dmc, AssociationFunction af, File ldopsFolder){
        //List<String> words = Arrays.asList(new String[]{ "see", "write", "buy", "kick", "book" }); //remove me
        int amountOfThreads = Math.min(10, Helper.getAmountOfCores());
        
		File[] jdopsFiles = jdopsFolder.listFiles();
        for(File jdopsFile : jdopsFiles){
            Helper.report("[Experiment] Processing jdops file \"" + jdopsFile.getName() + "\"...");
            int jdopsCounter = 0, ldopsCounter = 0;
            try{
                BufferedReader in = Helper.getFileReader(jdopsFile);
                File ldopsFile = new File(ldopsFolder, jdopsFile.getName());
                BufferedWriter out = Helper.getFileWriter(ldopsFile);

                int counter = 0;
                while(true){
                    //Matrix m = Matrix.importFromReader(in);
                    //if(m == null) break;
                    //CountMatrix cm = (CountMatrix) m;
                    //if(!words.contains(cm.getName())) continue; //remove me
                    HashMap<String, CountMatrix> jdops = importJdopsFromReader(in, amountOfThreads);
                    if(jdops.isEmpty()) break;
                    jdopsCounter += jdops.size();
                    HashMap<String, ValueMatrix> ldops = af.compute(jdops);
                    if(ldops != null){
                        for(String ldopName : ldops.keySet()){
                            ValueMatrix ldop = ldops.get(ldopName);
                            Vocabulary.getTargetWord(ldopName).setLexicalRepresentation(ldop);
                            ldop.saveToWriter(out);
                            counter++;
                            ldopsCounter++;
                            if(counter % 10 == 0) Helper.report("Finished computing " + counter + " LDops");
                        }
                    }
                }
                
                in.close();
                out.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            Helper.report("...Finished processing " + ldopsCounter + "/" + jdopsCounter + " jdops from \"" + jdopsFile.getName() + "\"");
        }
    }
    
    public void importMatrices(File folder, boolean normalize){
        VocabularyMatrixImporter mi = new VocabularyMatrixImporter(normalize);
        mi.importMatricesFromFiles(folder.listFiles());
    }
    
    public void saveMatrices(File outputFolder){
        VocabularyMatrixExporter me = new VocabularyMatrixExporter();
        me.exportMatricesToFiles(outputFolder, 1); //change this back to 8
    }

}

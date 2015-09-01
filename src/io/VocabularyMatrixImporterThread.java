package io;

import cdt.Helper;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import linearAlgebra.Matrix;
import linearAlgebra.value.ValueMatrix;

public class VocabularyMatrixImporterThread implements Runnable {

	private VocabularyMatrixImporter superior;
	private String name;
	private File inputFile;
    private boolean normalize;
	
	public VocabularyMatrixImporterThread(VocabularyMatrixImporter superior, String name, File inputFile, boolean normalize){
		this.superior = superior;
		this.name = name;
		this.inputFile = inputFile;
        this.normalize = normalize;
	}
    
    private int importMatrices(){
		Helper.report("[MatrixImporterThread] (" + name + ") Importing matrices from \"" + inputFile.getAbsolutePath() + "\"...");
		
		if(!inputFile.exists()){
			Helper.report("[MatrixImporterThread] (" + name + ") File " + inputFile.getAbsolutePath() + " does not exist!");
			return 0;
		}
		
		//go through all matrices in experiment
		int counter = 0;
		try{
			BufferedReader in = Helper.getFileReader(inputFile);
			
			while(true){
                Matrix m = Matrix.importFromReader(in);
                if(m == null) break;
				if(m instanceof ValueMatrix && normalize) ((ValueMatrix) m).normalize();
                counter++;
                TargetWord tw = Vocabulary.getTargetWord(m.getName());
                //System.out.println("Finished loading matrix \"" + m.getName() + "\", card=" + m.getCardinality() + "..."); //DEBUG
                if(tw != null){
                    tw.setLexicalRepresentation(m);
                }
                
                //sometimes useful
				//if(counter % 10 == 0 && counter > 0) Helper.report("[MatrixImporterThread] (" + name + ") ..." +  counter + " matrices have been imported...");
			}
						
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[MatrixImporterThread] (" + name + ") ...Finished importing " + counter + " matrices from \"" + inputFile.getAbsolutePath() + "\"");
        return counter;
	}
    
	@Override
	public void run(){
		int amount = importMatrices();
		superior.reportMatrixImporterDone(this, amount);
	}
}

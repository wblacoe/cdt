package io;

import cdt.Helper;
import corpus.associationFunction.AssociationFunction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import linearAlgebra.Matrix;

public class MatrixImporterThread implements Runnable {

	private MatrixImporter superior;
	private String name;
	private File inputFile, outputFile;
    private boolean normalize;
    private AssociationFunction af;
	
	public MatrixImporterThread(MatrixImporter superior, String name, File inputFile, AssociationFunction af, boolean normalize, File outputFile){
		this.superior = superior;
		this.name = name;
		this.inputFile = inputFile;
        this.normalize = normalize;
        this.af = af;
        this.outputFile = outputFile;
	}
    public MatrixImporterThread(MatrixImporter superior, String name, File inputFile, boolean normalize){
        this(superior, name, inputFile, null, normalize, null);
    }

    
    private void importMatricesApplyAssociationFunctionAndSaveMatrices(){
		Helper.report("[MatrixImporterThread] (" + name + ") Importing matrices from \"" + inputFile.getAbsolutePath() + "\"...");
		
		if(!inputFile.exists()){
			Helper.report("[MatrixImporterThread] (" + name + ") File " + inputFile.getAbsolutePath() + " does not exist!");
			return;
		}
		
		//go through all matrices in experiment
		int counter = 0;
		//boolean success;
		try{
			BufferedReader in = Helper.getFileReader(inputFile);
            BufferedWriter out = Helper.getFileWriter(outputFile);
			
			while(true){
                Matrix m = Matrix.importFromReader(in);
                if(m == null) break;
                counter++;
                /*TargetWord tw = Vocabulary.getTargetWord(m.getName());
                if(tw != null){
                    tw.setRepresentation(m);
                    //System.out.println("processing " + m); //DEBUG
                    m = af.compute(tw);
					tw.removeRepresentation();
                    if(m == null) continue;
                    m.saveToWriter(out);
                }
				*/
                
				if(counter % 10 == 0 && counter > 0) Helper.report("[MatrixImporterThread] (" + name + ") ..." +  counter + " matrices have been imported...");
			}
						
			in.close();
            out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[MatrixImporterThread] (" + name + ") ...Finished importing " + counter + " matrices from \"" + inputFile.getAbsolutePath() + "\"");
	}
    
	private void importMatrices(){
		Helper.report("[MatrixImporterThread] (" + name + ") Importing matrices from \"" + inputFile.getAbsolutePath() + "\"...");
		
		if(!inputFile.exists()){
			Helper.report("[MatrixImporterThread] (" + name + ") File " + inputFile.getAbsolutePath() + " does not exist!");
			return;
		}
		
		//go through all matrices in experiment
		int counter = 0;
		//boolean success;
		try{
			BufferedReader in = Helper.getFileReader(inputFile);
			
			String line;
			//while(true){
			while((line = in.readLine()) != null){
                //Matrix m = Matrix.importFromReader(in);
				//CountMatrix m = new CountMatrix(in);
				//if(m.getCardinality() == 0) break;
				//if(m == null) break;
                counter++;
                //TargetWord tw = Vocabulary.getTargetWord(m.getName());
                //if(tw != null){
                    //if(normalize) ((ValueMatrix) m).normalize();
                    //tw.setRepresentation(m); //undo
                //}
				//System.gc();
                
				if(counter % 10 == 0 && counter > 0) Helper.report("[MatrixImporterThread] (" + name + ") ..." +  counter + " matrices have been imported...");
			}
						
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[MatrixImporterThread] (" + name + ") ...Finished importing " + counter + " matrices from \"" + inputFile.getAbsolutePath() + "\"");
	}
	
	
	@Override
	public void run(){
        if(af == null && outputFile == null){
            importMatrices();
        }else{
            importMatricesApplyAssociationFunctionAndSaveMatrices();
        }
        
		superior.reportMatrixImporterDone(this);
	}
}

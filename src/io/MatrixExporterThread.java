package io;

import cdt.Helper;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import linearAlgebra.Matrix;

public class MatrixExporterThread implements Runnable {

	private MatrixExporter exporter;
	private String name;
	private HashSet<TargetWord> targetWordSet;
	private File outputFile;
	
	public MatrixExporterThread(MatrixExporter exporter, HashSet<TargetWord> targetWordSet, File outputFile){
		this.exporter = exporter;
		this.name = outputFile.getName();
		this.outputFile = outputFile;
		this.targetWordSet = targetWordSet;
	}
	
	private void exportMatrices(){
		Helper.report("[MatrixExporterThread] (" + name + ") Exporting matrices to \"" + outputFile.getAbsolutePath() + "\"...");
		
		if(outputFile.exists()){
			Helper.report("[MatrixExporterThread] (" + name + ") File " + outputFile.getAbsolutePath() + " already exists. Overwriting...");
			//return;
		}
		
		//go through all matrices in experiment
		int counter = 0;
		//boolean success;
		try{
			//BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFile)), "UTF-8"));
            BufferedWriter out = Helper.getFileWriter(outputFile);
			
			//for(Matrix m : matrixSet){
            for(TargetWord tw : targetWordSet){
                Matrix m = tw.getRepresentation();
                if(m == null) continue; //don't export useless matrices
                m.saveToWriter(out);
				//success = m.saveToWriter(out);
				//if(!success) Helper.report("[MatrixExporterThread] (" + name + ") There was a problem exporting after " + counter + " matrices");
				counter++;
			}
						
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[MatrixExporterThread] (" + name + ") ...Finished exporting " + counter + " matrices from \"" + outputFile.getAbsolutePath() + "\"");
	}
	
	
	@Override
	public void run(){
		exportMatrices();
		exporter.reportMatrixExporterThreadDone(this);
	}
}
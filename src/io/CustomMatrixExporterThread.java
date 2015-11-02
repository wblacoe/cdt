package io;

import cdt.Helper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import linearAlgebra.Matrix;

public class CustomMatrixExporterThread implements Runnable {

	private CustomMatrixExporter exporter;
	private String name;
	private HashSet<Matrix> matrixSet;
	private File outputFile;
	
	public CustomMatrixExporterThread(CustomMatrixExporter exporter, HashSet<Matrix> matrixSet, File outputFile){
		this.exporter = exporter;
		this.name = outputFile.getName();
		this.outputFile = outputFile;
		this.matrixSet = matrixSet;
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
            BufferedWriter out = Helper.getFileWriter(outputFile);
			
			for(Matrix m : matrixSet){
            //for(TargetWord tw : targetWordSet){
                //Matrix m = tw.getRepresentation();
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
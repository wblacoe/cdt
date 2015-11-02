package io;

import cdt.Helper;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.Matrix;

public class CustomMatrixImporter {

    private boolean normalize;
	private int amountOfImportedMatrices;
	private HashSet<Runnable> threads;
	private HashMap<String, Matrix> indexMatrixMap;
	
	public CustomMatrixImporter(boolean normalize){
        this.normalize = normalize;
		amountOfImportedMatrices = 0;
		threads = new HashSet<>();
		indexMatrixMap = new HashMap<>();
	}
	
	
	//import matrices with given names in given files
	public synchronized HashMap<String, Matrix> importMatricesFromFiles(File[] inputFiles){
		Helper.report("[MatrixImporter] Importing matrices from " + inputFiles.length + " files...");

        int amountOfIOThreads = Helper.getAmountOfCores();
		int fileCounter = 0;
		try{
			do{
				
				while(threads.size() < amountOfIOThreads && fileCounter < inputFiles.length){
					CustomMatrixImporterThread thread = new CustomMatrixImporterThread(this, inputFiles[fileCounter].getName(), inputFiles[fileCounter], normalize);
					threads.add(thread);
					(new Thread(thread)).start();
					
					fileCounter++;
				}
				
				wait();
			}while(!threads.isEmpty() || fileCounter < inputFiles.length);
		}catch(InterruptedException e){}
				
		
		Helper.report("[MatrixImporter] ...Finished importing " + amountOfImportedMatrices + " matrices from " + inputFiles.length + " files.");
		return indexMatrixMap;
	}
    
	//
	protected synchronized void reportMatrixImporterDone(CustomMatrixImporterThread thread, int amountOfImportedMatrices, HashMap<String, Matrix> indexMatrixMap){
        this.amountOfImportedMatrices += amountOfImportedMatrices;
		this.indexMatrixMap.putAll(indexMatrixMap);
		threads.remove(thread);
		notify();
	}
	
}

package io;

import cdt.Helper;
import java.io.File;
import java.util.HashSet;

public class VocabularyMatrixImporter {

    private boolean normalize;
	private int amountOfImportedMatrices;
	private HashSet<Runnable> threads;
	
	
	public VocabularyMatrixImporter(boolean normalize){
        this.normalize = normalize;
		amountOfImportedMatrices = 0;
		threads = new HashSet<>();
	}
	
	
	//import matrices with given names in given files
	public synchronized void importMatricesFromFiles(File[] inputFiles){
		Helper.report("[MatrixImporter] Importing matrices from " + inputFiles.length + " files...");

        int amountOfIOThreads = Helper.getAmountOfCores();
		int fileCounter = 0;
		try{
			do{
				
				while(threads.size() < amountOfIOThreads && fileCounter < inputFiles.length){
					VocabularyMatrixImporterThread thread = new VocabularyMatrixImporterThread(this, inputFiles[fileCounter].getName(), inputFiles[fileCounter], normalize);
					threads.add(thread);
					(new Thread(thread)).start();
					
					fileCounter++;
				}
				
				wait();
			}while(!threads.isEmpty() || fileCounter < inputFiles.length);
		}catch(InterruptedException e){}
				
		
		Helper.report("[MatrixImporter] ...Finished importing " + amountOfImportedMatrices + " matrices from " + inputFiles.length + " files.");
	}
    
	//
	public synchronized void reportMatrixImporterDone(VocabularyMatrixImporterThread thread, int amountOfImportedMatrices){
        this.amountOfImportedMatrices += amountOfImportedMatrices;
		threads.remove(thread);
		notify();
	}
	
}

package io;

import cdt.Helper;
import corpus.associationFunction.AssociationFunction;
import java.io.File;
import java.util.HashSet;

public class MatrixImporter {

    private boolean normalize;
	
	private HashSet<Runnable> threads;
	
	
	public MatrixImporter(boolean normalize){
        this.normalize = normalize;
		
		threads = new HashSet<>();
	}
	
	
    //import matrices with given names in given files
	public synchronized void importMatricesApplyAssociationFunctionAndSaveMatrices(File[] inputFiles, AssociationFunction af, File outputFolder){
		Helper.report("[MatrixImporter] Importing matrices from " + inputFiles.length + " files...");

		//int amountOfIOThreads = space.getIntegerParameter("amountofiothreads");
        int amountOfIOThreads = Helper.getAmountOfCores();
		int fileCounter = 0;
		try{
			do{
				
				while(threads.size() < amountOfIOThreads && fileCounter < inputFiles.length){
                    File outputFile = new File(outputFolder, fileCounter + ".gz");
					MatrixImporterThread thread = new MatrixImporterThread(this, inputFiles[fileCounter].getName(), inputFiles[fileCounter], af, normalize, outputFile);
					threads.add(thread);
					(new Thread(thread)).start();
					
					fileCounter++;
				}
				
				wait();
			}while(!threads.isEmpty() || fileCounter < inputFiles.length);
		}catch(InterruptedException e){}
				
		
		Helper.report("[MatrixImporter] ...Finished importing matrices from " + inputFiles.length + " files.");
	}
    
	//import matrices with given names in given files
	public synchronized void importMatricesFromFiles(File[] inputFiles){
		Helper.report("[MatrixImporter] Importing matrices from " + inputFiles.length + " files...");

		//int amountOfIOThreads = space.getIntegerParameter("amountofiothreads");
        int amountOfIOThreads = Helper.getAmountOfCores();
		int fileCounter = 0;
		try{
			do{
				
				while(threads.size() < amountOfIOThreads && fileCounter < inputFiles.length){
					MatrixImporterThread thread = new MatrixImporterThread(this, inputFiles[fileCounter].getName(), inputFiles[fileCounter], normalize);
					threads.add(thread);
					(new Thread(thread)).start();
					
					fileCounter++;
				}
				
				wait();
			}while(!threads.isEmpty() || fileCounter < inputFiles.length);
		}catch(InterruptedException e){}
				
		
		Helper.report("[MatrixImporter] ...Finished importing matrices from " + inputFiles.length + " files.");
	}
    
	//
	public synchronized void reportMatrixImporterDone(MatrixImporterThread thread){
		threads.remove(thread);
		notify();
	}
	
}

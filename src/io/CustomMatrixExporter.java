package io;

import cdt.Helper;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.Matrix;

public class CustomMatrixExporter {

	private HashSet<Runnable> exporterThreads;

	public CustomMatrixExporter(){
		exporterThreads = new HashSet<>();
	}
	
	
	//export matrices with given names to given amount of files in given folder
	//if outputNames is null, then use the names of all matrices
	public synchronized boolean exportMatricesToFiles(HashMap<String, Matrix> nameMatrixMap, File outputFolder, String prefix, Collection<String> matrixNames, int amountOfFiles){
		Helper.report("[MatrixExporter] Exporting matrices to up to " + amountOfFiles + " files in " + outputFolder.getAbsolutePath() + "...");
        
        if(prefix == null) prefix = "";
		
		if(!outputFolder.exists()) outputFolder.mkdirs(); //ensure that output folder exists
		//for(File file : outputFolder.listFiles()) file.delete(); //ensure that output folder is empty
		
		HashMap<Integer, HashSet<Matrix>> indexMatrixSetMap = new HashMap<>();
		for(int i=0; i<amountOfFiles; i++) indexMatrixSetMap.put(i, new HashSet<Matrix>());
		
		int counter = 0;
		if(matrixNames == null) matrixNames = nameMatrixMap.keySet(); //if no matrix names are specified use the names of all saved matrices
		for(String matrixName : matrixNames){
			Matrix m = nameMatrixMap.get(matrixName);
			m.setName(matrixName); //jut to make sure
			indexMatrixSetMap.get(counter % amountOfFiles).add(m);
			counter++;
		}
		
		int fileCounter = 0;
		for(int i=0; i<amountOfFiles; i++){
			//HashSet<TargetWord> targetWordSet = indexMatrixSetMap.get(i);
            HashSet<Matrix> matrixSet = indexMatrixSetMap.get(i);
			//don't run useless exporterThreads
			if(!matrixSet.isEmpty()){
				File outputFile = new File(outputFolder, prefix + i + ".gz");
				CustomMatrixExporterThread thread = new CustomMatrixExporterThread(this, matrixSet, outputFile);
				exporterThreads.add(thread);
				(new Thread(thread)).start();
				fileCounter++;
			}
		}
				
		try{
			while(!exporterThreads.isEmpty()){
				wait();
			}
		}catch(InterruptedException e){}
		
		Helper.report("[MatrixExporter] ...Finished Exporting " + counter + " matrices to " + fileCounter + " files in " + outputFolder.getAbsolutePath() + "...");
		return true;
	}
	//export all matrices to given amount of files in given folder
	/*public boolean exportMatricesToFiles(HashMap<String, Matrix> nameMatrixMap, File outputFolder, int amountOfFiles){
		return exportMatricesToFiles(
			nameMatrixMap,
			outputFolder,
			null,
			amountOfFiles
		);
	}
    */
	public synchronized void reportMatrixExporterThreadDone(CustomMatrixExporterThread thread){
		exporterThreads.remove(thread);
		notify();
	}

    public synchronized boolean exportMatricesToFiles(HashMap<String, Matrix> nameMatrixMap, File outputFolder, Collection<String> matrixNames, int amountOfFiles){
        return exportMatricesToFiles(nameMatrixMap, outputFolder, "", matrixNames, amountOfFiles);
    }

    public synchronized boolean exportMatricesToFiles(HashMap<String, Matrix> nameMatrixMap, File outputFolder, int amountOfFiles){
        return exportMatricesToFiles(nameMatrixMap, outputFolder, "", null, amountOfFiles);
    }

}

package featureExtraction;

import cdt.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class FeatureVectorsCollection {

	private ArrayList<String> featureNamesList;
	private TreeMap<Integer, FeatureVector> indexFeatureVectorMap;
	
	public FeatureVectorsCollection(){
		featureNamesList = new ArrayList<String>();
		indexFeatureVectorMap = new TreeMap<Integer, FeatureVector>();
	}
	public FeatureVectorsCollection(File fvecFile){
		this();
		importFromFile(fvecFile);
	}
	
	
	public FeatureVector getFeatureVector(int index){
		return indexFeatureVectorMap.get(index);
	}
	
	public ArrayList<String> getFeatureNamesList(){
		return featureNamesList;
	}
	
	public TreeMap<Integer, FeatureVector> getIndexFeatureVectorMap(){
		return indexFeatureVectorMap;
	}
	
	//adds this feature to the collection's schema (without adding any values to its feature vectors)
	public void addFeature(String featureName){
		featureNamesList.add(featureName);
	}
	
	public boolean hasFeature(String featureName){
		return featureNamesList.contains(featureName);
	}
	
	public boolean isEmpty(){
		return featureNamesList.isEmpty() && indexFeatureVectorMap.isEmpty();
	}
	
	public void setValue(int index, String featureName, Double value){
		if(!featureNamesList.contains(featureName)) featureNamesList.add(featureName);
		indexFeatureVectorMap.get(index).setValue(featureName, value);
	}
	
	public void setValues(int index, String[] featureNames, Double[] values){
		indexFeatureVectorMap.get(index).setValues(featureNamesList, values);
	}
	
	public void setValues(int index, ArrayList<String> featureNames, Double[] values){
		indexFeatureVectorMap.get(index).setValues(featureNames, values);
	}
	
	public void applyFeatureSelection(ArrayList<String> newFeatureNamesList){
		for(FeatureVector fv : indexFeatureVectorMap.values()){
			fv.applyFeatureSelection(featureNamesList);
		}
		this.featureNamesList = newFeatureNamesList;
	}
	
	public void setOutputFeature(String featureName) throws Exception {
		if(!featureNamesList.contains(featureName)) throw new Exception("[FeatureVectorsCollection] Cannot assign \"" + featureName + "\" to be output feature. Not contained in schema!");
		featureNamesList.remove(featureName);
		featureNamesList.add(featureName);
	}

	public int getAmountOfFeatures(){
		return featureNamesList.size();
	}
	
	public int getSize(){
		return indexFeatureVectorMap.size();
	}
	
	public void clear(){
		featureNamesList.clear();
		indexFeatureVectorMap.clear();
	}
	
	public boolean schemaAlignsWith(FeatureVectorsCollection fvc){
		ArrayList<String> fvcFeatureNamesList = fvc.getFeatureNamesList();
		return featureNamesList.containsAll(fvcFeatureNamesList) && fvcFeatureNamesList.containsAll(featureNamesList);
	}
	
	public boolean indicesAlignWith(FeatureVectorsCollection fvc){
		return indexFeatureVectorMap.keySet().containsAll(fvc.getIndexFeatureVectorMap().keySet()) && fvc.getIndexFeatureVectorMap().keySet().containsAll(indexFeatureVectorMap.keySet());
	}
	
	public void append(FeatureVectorsCollection fvc) throws Exception {
		if(isEmpty()){
			//take over the schema of given feature vectors collection
			featureNamesList = fvc.getFeatureNamesList();
		}else{
			//check that given feature vector features align with existing ones
			if(!schemaAlignsWith(fvc)) throw new Exception("[FeatureVectorsCollection] Schemas do not match!");
		}
		
		//go through all feature vectors and append them
		for(Integer index : fvc.getIndexFeatureVectorMap().navigableKeySet()){
			if(getFeatureVector(index) != null) throw new Exception("[FeatureVectorsCollection] A feature vector with index " + index + " already exists!");
			indexFeatureVectorMap.put(index, fvc.getFeatureVector(index));
		}
	}
	
	public boolean schemaAlignsWith(FeatureVector fv){
		Collection<String> fvFeatureNamesList = fv.getUnorderedFeatureNamesList();
		return featureNamesList.containsAll(fvFeatureNamesList) && fvFeatureNamesList.containsAll(featureNamesList);
	}
	
	public void append(int index, FeatureVector fv) throws Exception {
		if(isEmpty()){
			//take over the schema from first feature vector to be appended
			for(String featureName : fv.getUnorderedFeatureNamesList()) featureNamesList.add(featureName);
		}else{
			//check that given feature vector features align with existing ones
			if(fv.getSize() != getAmountOfFeatures()) throw new Exception("[FeatureVectorsCollection] Size of feature vector does not match size of schema!");
			if(!schemaAlignsWith(fv)) throw new Exception("[FeatureVectorsCollection] Schema of feature vector does not align with schema of existing feature vectors!");
		}
		
		indexFeatureVectorMap.put(index, fv);
	}
	
	public void integrate(FeatureVectorsCollection fvc) throws Exception {
		if(isEmpty()){
			append(fvc);
		}else{
			if(getSize() != fvc.getSize()) throw new Exception("[FeatureVectorsCollection] Sizes do not match!");
			if(!indicesAlignWith(fvc)) throw new Exception("[FeatureVectorsCollection] Indices do not align!");

			//go through all feature vectors and integrate them
			for(Integer index : fvc.getIndexFeatureVectorMap().navigableKeySet()){
				getFeatureVector(index).integrate(fvc.getFeatureVector(index));
			}
			//extend schema
			for(String featureName : fvc.getFeatureNamesList()){
				if(!hasFeature(featureName)) featureNamesList.add(featureName);
			}
		}
	}
	
	public void appendOrIntegrate(FeatureVectorsCollection fvc) throws Exception {
		if(isEmpty() || schemaAlignsWith(fvc)){
			append(fvc);
		}else if(getSize() == fvc.getSize() && indicesAlignWith(fvc)){
			integrate(fvc);
		}else{
			throw new Exception("[FeatureVectorsCollection] Given feature vectors collection cannot be appended or integrated to this one!");
		}
	}

	//first column: index, last column: output feature
	private void importFromFile(File fvecFile){
		Helper.report("[FeatureVectorsCollection] Importing feature vectors from \"" + fvecFile.getAbsolutePath() + "\"...");
		
		//remove schema and all existing feature vectors
		this.clear();
		try{
			BufferedReader in = new BufferedReader(new FileReader(fvecFile));
	
			//read one vector per line (delimiter=space)
			String line;
			while((line = in.readLine()) != null){
				
				//skip empty lines
				if(line.isEmpty() || line.startsWith("#")) continue;
				
				//read a feature name
				if(line.startsWith("@")){
					featureNamesList.add(line.substring(1));
					continue;
				}
				
				//read a feature vector
				String[] entries = line.split(" ");
				int D = entries.length-1;
				
				//get index
				int index = Integer.parseInt(entries[0]); //first entry is index
				
				//get coefficients
				Double[] doubleArray = new Double[D];
				for(int d=0; d<D; d++){
					if(entries[d+1].equals("null")) entries[d+1] = "0.0";
					Double value = Double.parseDouble(entries[d+1]); //coefficients start after index
					if(value == null || Double.isNaN(value) || Double.isInfinite(value)) value = 0.0;
					doubleArray[d] = value;
				}
				
				FeatureVector fv = new FeatureVector(index);
				fv.setValues(featureNamesList, doubleArray);
				indexFeatureVectorMap.put(index, fv);
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[FeatureVectorsCollection] ...Finished importing " + this.getSize() + " feature vectors of length " + this.getAmountOfFeatures());
	}
	
	public void filter(Collection<Integer> indicesToKeep){
		ArrayList<Integer> toRemove = new ArrayList<>();
		for(Integer index : indexFeatureVectorMap.navigableKeySet()){
			if(!indicesToKeep.contains(index)) toRemove.add(index);
		}
		for(Integer index : toRemove){
			indexFeatureVectorMap.remove(index);
		}
	}
	
	public void exportToFile(File featureVectorsFile){
		Helper.report("[FeatureVectorsCollection] Exporting feature vectors to \"" + featureVectorsFile.getAbsolutePath() + "...");

		if(!featureVectorsFile.getParentFile().exists()) featureVectorsFile.getParentFile().mkdirs();
		
		int counter = 0;
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(featureVectorsFile));
			
			//write the schema
			for(String featureName : featureNamesList){
				out.write("@" + featureName + "\n");
			}
			
			//go through all feature vectors
			for(Integer index : indexFeatureVectorMap.navigableKeySet()){
				FeatureVector fv = indexFeatureVectorMap.get(index);
				Double[] doubleArray = fv.toDoubleArray(featureNamesList);
				out.write("" + index);
				for(int i=0; i<doubleArray.length; i++){
					out.write(" " + doubleArray[i]);
				}
				out.write("\n");
				counter++;
			}
			
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[FeatureVectorsCollection] ...Finished exporting " + counter + " feature vectors of length " + this.getAmountOfFeatures());
	}

	//import feature names list for feature selection
	public static ArrayList<String> importFeatureNamesList(File featureNamesListFile) {
		Helper.report("[FeatureVectorsCollection] Importing feature names list from \"" + featureNamesListFile.getAbsolutePath() + "\"...");
		
		ArrayList<String> importedFeatureNamesList = new ArrayList<String>();
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(featureNamesListFile));

			String line;
			while((line = in.readLine()) != null){
				if(line.isEmpty() || line.startsWith("%")) continue;
				importedFeatureNamesList.add(line);
			}

			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		Helper.report("[FeatureVectorsCollection] ...Finished importing feature names list (length is " +  importedFeatureNamesList.size() + ").");
		return importedFeatureNamesList;
	}
	
}

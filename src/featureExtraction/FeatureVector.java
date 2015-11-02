package featureExtraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

public class FeatureVector {

	private int index;
	private HashMap<String, Double> featureNameValueMap;
		
	public FeatureVector(int index){
		this.index = index;
		featureNameValueMap = new HashMap<String, Double>();
	}
	public FeatureVector(){
		this(-1);
	}
	
	
	public int getIndex(){
		return index;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public int getSize(){
		return featureNameValueMap.size();
	}
	
	public HashMap<String, Double> getFeatureNameValueMap(){
		return featureNameValueMap;
	}
	
	public Double getValue(String featureName){
		return featureNameValueMap.get(featureName);
	}
	
	public Double clean(Double d){
		if(d == null || Double.isInfinite(d) || Double.isNaN(d)) return 0.0;
		else return d;
	}
	public void setValue(String featureName, Double value){
		featureNameValueMap.put(featureName, clean(value));
	}
	
	public boolean hasValue(String featureName){
		return featureNameValueMap.containsKey(featureName);
	}
	
	public void setValues(String[] featureNames, Double[] values){
		for(int i=0; i<featureNames.length; i++){
			String featureName = featureNames[i];
			Double value = values[i];
			setValue(featureName, value);
		}
	}
	
	public void setValues(ArrayList<String> featureNames, Double[] values){
		for(int i=0; i<featureNames.size(); i++){
			String featureName = featureNames.get(i);
			Double value = values[i];
			setValue(featureName, value);
		}
	}
	
	public void integrate(FeatureVector fv){
		if(fv != null && !fv.isEmpty()){
			for(Entry<String, Double> entry : fv.getFeatureNameValueMap().entrySet()){
				setValue(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void addSuffixToFeatureNames(String suffix){
		HashMap<String, Double> newFeatureNameValueMap = new HashMap<String, Double>();
		for(Entry<String, Double> entry : featureNameValueMap.entrySet()){
			newFeatureNameValueMap.put(entry.getKey() + suffix, entry.getValue());
		}
		featureNameValueMap = newFeatureNameValueMap;
	}
	
	public boolean isEmpty(){
		return featureNameValueMap.isEmpty();
	}

	public void applyFeatureSelection(ArrayList<String> featureNamesList){
		HashMap<String, Double> newFeatureNamesValueMap = new HashMap<String, Double>();
		for(int i=0; i<featureNamesList.size(); i++){
			String featureName = featureNamesList.get(i);
			newFeatureNamesValueMap.put(featureName, featureNameValueMap.get(featureName));
		}
		featureNameValueMap = newFeatureNamesValueMap;
	}
	
	public Double[] toDoubleArray(ArrayList<String> featureNamesList){
		Double[] doubleArray = new Double[featureNamesList.size()];
		
		for(int i=0; i<featureNamesList.size(); i++){
			String featureName = featureNamesList.get(i);
			doubleArray[i] = featureNameValueMap.get(featureName);
		}
		
		return doubleArray;
	}
	
	public String toStringDetailed(ArrayList<String> featureNamesList){
		String s = "";
		
		for(String featureName : featureNamesList){
			s += featureName + ": " + featureNameValueMap.get(featureName) + "\n";
		}
		
		return s;
	}
	
	public Collection<String> getUnorderedFeatureNamesList(){
		return featureNameValueMap.keySet();
	}

	public String toStringDetailed(){
		String s = "";
		
		for(String featureName : getUnorderedFeatureNamesList()){
			s += featureName + ": " + featureNameValueMap.get(featureName) + "\n";
		}
		
		return s;
	}
		
}
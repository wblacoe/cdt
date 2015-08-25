package featureExtraction.fromVector;

import cdt.Helper;
import corpus.dep.converter.DepTree;
import experiment.AbstractInstance;
import experiment.Dataset;
import featureExtraction.AbstractFeatureVectorExtractor;
import featureExtraction.FeatureVector;
import featureExtraction.FeatureVectorsCollection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FeatureVectorExtractor extends AbstractFeatureVectorExtractor {

	private ArrayList<Integer> indices;
    private HashMap<Integer, Double[]> indexSentence1VectorMap;
	private HashMap<Integer, Double[]> indexSentence2VectorMap;
	
	
	public FeatureVectorExtractor(){
        super();
		indices = new ArrayList<Integer>();
		indexSentence1VectorMap = new HashMap<Integer, Double[]>();
		indexSentence2VectorMap = new HashMap<Integer, Double[]>();
	}
    
    private Double innerProduct(Double[] v1, Double[] v2){
        Double ip = 0.0;
        
        for(int i=0; i<v1.length; i++){
            ip += v1[i] * v2[i];
        }
        
        return ip;
    }
	
	//features specified in the Kotlerman paper
	private FeatureVector getKotlermanFeatureVector(int index, Double[] v1, Double[] v2){
		FeatureVector fv = new FeatureVector(index);
		
		double epsilon = 0.000001;
		
		//kotlerman features
		double clarkeNumerator = 0;
		double linNumerator = 0;
		double weedsNumerator = 0;
		double denominator1 = 0, denominator2 = 0;
		
		for(int d=0; d<v1.length; d++){
			if(Math.abs(v1[d]) > epsilon || Math.abs(v2[d]) > epsilon){
				clarkeNumerator += Math.min(v1[d], v2[d]);
				linNumerator += v1[d] + v2[d];
				weedsNumerator += v1[d];
			}
			denominator1 += v1[d];
			denominator2 += v2[d];
		}
        
        double sim12 = innerProduct(v1, v2);
        double sim11 = innerProduct(v1, v1);
        double sim22 = innerProduct(v2, v2);
		double superFidelity12 = sim12 + Math.sqrt((1 - sim11) * (1 - sim22));
		fv.setValue("sim11", sim11);
		fv.setValue("sim22", sim22);
		fv.setValue("sim12", sim12);
		fv.setValue("sim12 * 2 / (sim11 + sim22)", sim12 * 2 / (sim11 + sim22));
		fv.setValue("sim12 / sim11", sim12 / sim11);
		fv.setValue("sim12 / sim22", sim12 / sim22);
		fv.setValue("super fidelity", superFidelity12);
		
		fv.setValue("clarke0", clarkeNumerator);
		fv.setValue("clarke1", clarkeNumerator / denominator1);
		fv.setValue("clarke2", clarkeNumerator / denominator2);
        fv.setValue("clarke3", clarkeNumerator / sim12);
			
		fv.setValue("lin0", linNumerator);
		fv.setValue("lin", linNumerator / (denominator1 + denominator2));
		fv.setValue("lin1", linNumerator / denominator1);
		fv.setValue("lin2", linNumerator / denominator2);
        fv.setValue("lin3", linNumerator / sim12);
			
		fv.setValue("weeds0", weedsNumerator);
		fv.setValue("weeds", weedsNumerator / (denominator1 + denominator2));
		fv.setValue("weeds1", weedsNumerator / denominator1);
		fv.setValue("weeds2", weedsNumerator / denominator2);
        fv.setValue("weeds3", weedsNumerator / sim12);
			
		fv.setValue("bal0", Math.sqrt(linNumerator * weedsNumerator));
		fv.setValue("bal", Math.sqrt(linNumerator * weedsNumerator) / (denominator1 + denominator2));
		fv.setValue("bal1", Math.sqrt(linNumerator * weedsNumerator) / denominator1);
		fv.setValue("bal2", Math.sqrt(linNumerator * weedsNumerator) / denominator2);
        fv.setValue("bal3", Math.sqrt(linNumerator * weedsNumerator) / sim12);
		
        return fv;
	}
	
	public void importVectors(File file){
		Helper.report("[FeatureVectorExtractor] Importing vectors from " + file.getAbsolutePath() + "...");
		
		int counter = 0;
		try{
			BufferedReader in = new BufferedReader(new FileReader(file));
			
			String line;
			while((line = in.readLine()) != null){
				String[] parts = line.split("\t");
				
				//part0: index
				int index = Integer.parseInt(parts[0]);
				indices.add(index);
				
				//part1: sentence1 vector
				String[] entries1 = parts[1].split(" ");
				Double[] vector1 = new Double[entries1.length];
				for(int i=0; i<vector1.length; i++){
					String entry = entries1[i];
					if(entry.isEmpty()) entry = "0.0";
					vector1[i] = Double.parseDouble(entry);
					if(vector1[i].isInfinite() || vector1[i].isNaN()) vector1[i] = 0.0;
				}
				indexSentence1VectorMap.put(index, vector1);
				
				//part1: sentence1 vector
				String[] entries2 = parts[2].split(" ");
				Double[] vector2 = new Double[entries2.length];
				for(int i=0; i<vector2.length; i++){
					String entry = entries2[i];
					if(entry.isEmpty()) entry = "0.0";
					vector2[i] = Double.parseDouble(entry);
					if(vector2[i].isInfinite() || vector2[i].isNaN()) vector2[i] = 0.0;
				}
				indexSentence2VectorMap.put(index, vector2);
				
				counter++;
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[FeatureVectorExtractor] ...Finished importing " + counter + " vectors from " + file.getAbsolutePath());
	}
	
    @Override
    public FeatureVectorsCollection extract(Dataset dataset){
        FeatureVectorsCollection fvc = new FeatureVectorsCollection();
        
        try{
            for(Integer index : dataset.getIndicesSet()){
                AbstractInstance instance = dataset.getInstance(index);
                int instanceIndex = instance.getIndex();
                FeatureVector fv = new FeatureVector(instanceIndex);

                if(instance instanceof experiment.dep.conll2015.Instance){
                    fv.setIndex(instanceIndex);
                    Double[] sentence1Vector = indexSentence1VectorMap.get(instanceIndex);
                    Double[] sentence2Vector = indexSentence2VectorMap.get(instanceIndex);
                    fv.integrate(getKotlermanFeatureVector(instanceIndex, sentence1Vector, sentence2Vector));

                }else if(instance instanceof experiment.dep.msrscc.Instance){
                    //TODO
                }

                fvc.append(instanceIndex, fv);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return fvc;
    }

}

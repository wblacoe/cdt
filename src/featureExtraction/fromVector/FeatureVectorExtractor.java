package featureExtraction.fromVector;

import cdt.Helper;
import featureExtraction.AbstractFeatureVectorExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FeatureVectorExtractor extends AbstractFeatureVectorExtractor {

	//private ArrayList<Integer> indices;
	//private HashMap<Integer, Double[]> indexSentence1VectorMap;
	//private HashMap<Integer, Double[]> indexSentence2VectorMap;
	
	
	public FeatureVectorExtractor(){
        super();
		//indices = new ArrayList<Integer>();
		//indexSentence1VectorMap = new HashMap<Integer, Double[]>();
		//indexSentence2VectorMap = new HashMap<Integer, Double[]>();
	}
	
	//features specified in the Kotlerman paper
	private Double[] getKotlermanVector(Double[] v1, Double[] v2){
		
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
		
		
		return new Double[]{
			clarkeNumerator,
			clarkeNumerator / denominator1,
			clarkeNumerator / denominator2,
			
			linNumerator,
			linNumerator / (denominator1 + denominator2),
			linNumerator / denominator1,
			linNumerator / denominator2,
			
			weedsNumerator,
			weedsNumerator / (denominator1 + denominator2),
			weedsNumerator / denominator1,
			weedsNumerator / denominator2,
			
			Math.sqrt(linNumerator * weedsNumerator),
			Math.sqrt(linNumerator * weedsNumerator) / (denominator1 + denominator2),
			Math.sqrt(linNumerator * weedsNumerator) / denominator1,
			Math.sqrt(linNumerator * weedsNumerator) / denominator2
		};
	}
	
	public void importVectors(File file){
		Helper.report("[KotlermanVectors] Importing vectors from " + file.getAbsolutePath() + "...");
		
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
		
		Helper.report("[KotlermanVectors] ...Finished importing " + counter + " vectors from " + file.getAbsolutePath());
	}
	
	public HashMap<Integer, Double[]> getFeatureVectors(){
		HashMap<Integer, Double[]> indexFeatureVectorsMap = new HashMap<Integer, Double[]>();
		
		//go through all sentence pair indices
		for(int index : indices){
			Double[] sentence1Vector = indexSentence1VectorMap.get(index);
			Double[] sentence2Vector = indexSentence2VectorMap.get(index);
			DepTreePair depTreePair = (DepTreePair) dataset.get(index);
			
			//similarities among sentence vectors (6)
			double sim11 = Operations.cosine(sentence1Vector, sentence1Vector);
			double sim22 = Operations.cosine(sentence2Vector, sentence2Vector);
			double sim12 = Operations.cosine(sentence1Vector, sentence2Vector);
			Double[] similarityVector = new Double[]{
				sim12 * 2 / (sim11 + sim22),
				sim12 / sim11,
				sim12 / sim22,
				sim11,
				sim22,
				sim12,
			};
			//small features: sentence lengths, sentence overlaps (4)
			Double[] superficialVector = getSuperficialVector(depTreePair);
			//kotlerman features for sentence vectors (15)
			Double[] kotlermanVector = getKotlermanVector(sentence1Vector, sentence2Vector);
			
			Double[] featureVector = Operations.concatenate(new Double[][]{
				similarityVector,
				superficialVector,
				kotlermanVector,
				new Double[]{ depTreePair.isEntailing() ? 1.0 : 0.0 }
			});
			indexFeatureVectorsMap.put(index, featureVector);
		}
		
		return indexFeatureVectorsMap;
	}

}

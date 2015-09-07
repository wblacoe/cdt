package featureExtraction.fromVectors;

import experiment.AbstractInstance;
import featureExtraction.FeatureVector;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import java.util.ArrayList;
import java.util.HashMap;

public class FeatureVectorExtractorThread implements Runnable {

	private FeatureVectorExtractor superior;
	private String name;
	private HashMap<Integer, AbstractInstance> indexInstanceMap;
	private ArrayList<String> featureList;

	public FeatureVectorExtractorThread(FeatureVectorExtractor superior, String name, HashMap<Integer, AbstractInstance> indexInstanceMap, ArrayList<String> featureList) {
		this.superior = superior;
		this.name = name;
		this.indexInstanceMap = indexInstanceMap;
		this.featureList = featureList;
    }
	public FeatureVectorExtractorThread(FeatureVectorExtractor superior, String name, Integer index, AbstractInstance instance, ArrayList<String> featureList) {
		this.superior = superior;
		this.name = name;
		indexInstanceMap = new HashMap<>();
		indexInstanceMap.put(index, instance);
		this.featureList = featureList;
    }
	
	
	public String getName(){
		return name;
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
	
	@Override
    public void run(){
        FeatureVectorsCollection fvc = new FeatureVectorsCollection();
        
        try{
            for(Integer instanceIndex : indexInstanceMap.keySet()){
                AbstractInstance instance = indexInstanceMap.get(instanceIndex);
                FeatureVector fv = null;

                /*if(instance instanceof experiment.dep.conll2015.Instance){
                    DepTree depTree1 = ((experiment.dep.conll2015.Instance) instance).arguments[0];
                    DepTree depTree2 = ((experiment.dep.conll2015.Instance) instance).arguments[1];
                    //fv = getKotlermanFeatureVector(instanceIndex, depTree1, depTree2);
					fv = getDiscourseRelationFeatureVector(instanceIndex, depTree1, depTree2);
					fv.setIndex(instanceIndex);
					fv.applyFeatureSelection(featureList);

                }else*/ if(instance instanceof experiment.rnnlm.Instance){
                    Double[] sentence1Vector = ((experiment.rnnlm.Instance) instance).vector1;
                    Double[] sentence2Vector = ((experiment.rnnlm.Instance) instance).vector2;
					fv = getKotlermanFeatureVector(instanceIndex, sentence1Vector, sentence2Vector);
					fv.setIndex(instanceIndex);
                }

                fvc.append(instanceIndex, fv);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

		fvc.applyFeatureSelection(featureList);
        superior.reportMatrixImporterDone(this, fvc);
    }

}
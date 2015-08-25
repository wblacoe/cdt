package featureExtraction;

import experiment.Dataset;
import innerProduct.InnerProductsCache;

public class AbstractFeatureVectorExtractor {

    private AbstractFeatureVectorExtractor[] extractors;
    
    public AbstractFeatureVectorExtractor(){
        
    }
    
	public AbstractFeatureVectorExtractor(String[] extractorsAsStrings, InnerProductsCache ipc){
        extractors = new AbstractFeatureVectorExtractor[extractorsAsStrings.length];
        for(int i=0; i<extractorsAsStrings.length; i++){
            switch(extractorsAsStrings[i]){
                case "fromSentences" :
                    extractors[i] = new featureExtraction.fromSentences.FeatureVectorExtractor(ipc);
                    break;
                case "fromVectors" :
                    extractors[i] = new featureExtraction.fromVector.FeatureVectorExtractor();
                    break;
                case "fromDensityMatrices" :
                    extractors[i] = new featureExtraction.fromDensityMatrices.FeatureVectorExtractor(ipc);
                    break;
                default :
                    extractors[i] = null;
            }
        }
	}
    
    public FeatureVectorsCollection extract(Dataset dataset){
        FeatureVectorsCollection totalFvc = new FeatureVectorsCollection();
        
        try{
            //let each extractor extract feature vectors collection, and collect in one collection
            for(int i=0; i<extractors.length; i++){
                FeatureVectorsCollection fvc = extractors[i].extract(dataset);
                totalFvc.integrate(fvc);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return totalFvc;
    }
    
}

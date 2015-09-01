package featureExtraction;

import cdt.Helper;
import experiment.Dataset;
import innerProduct.InnerProductsCache;
import java.util.ArrayList;

public class AbstractFeatureVectorExtractor {

	protected String name;
    private AbstractFeatureVectorExtractor[] extractors;
    
    protected AbstractFeatureVectorExtractor(){
        name = "abstract";
    }
	
	public String getName(){
		return name;
	}
    
	public AbstractFeatureVectorExtractor(String[] namesOfExtractors, InnerProductsCache ipc, ArrayList<String> featureList){
        extractors = new AbstractFeatureVectorExtractor[namesOfExtractors.length];
        for(int i=0; i<namesOfExtractors.length; i++){
            switch(namesOfExtractors[i]){
                case "fromSentences" :
                    extractors[i] = new featureExtraction.fromSentences.FeatureVectorExtractor(ipc, featureList);
                    break;
                case "fromVectors" :
                    extractors[i] = new featureExtraction.fromVectors.FeatureVectorExtractor(featureList);
                    break;
                case "fromDensityMatrices" :
                    extractors[i] = new featureExtraction.fromDensityMatrices.FeatureVectorExtractor(ipc, featureList);
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
				AbstractFeatureVectorExtractor extractor = extractors[i];
				Helper.report("[FeatureVectorExtractor] Extracting features using extractor \"" + extractor.getName() + "\"...");
                FeatureVectorsCollection fvc = extractor.extract(dataset);
                totalFvc.integrate(fvc);
				Helper.report("[FeatureVectorExtractor] ...Finished extracting features using extractor \"" + extractor.getName() + "\"");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return totalFvc;
    }
    
}

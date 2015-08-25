package featureExtraction.fromDensityMatrices;

import cdt.Helper;
import corpus.dep.converter.DepTree;
import experiment.AbstractInstance;
import experiment.Dataset;
import featureExtraction.AbstractFeatureVectorExtractor;
import featureExtraction.FeatureVector;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import linearAlgebra.value.LinearCombinationMatrix;
import linearAlgebra.value.ValueBaseMatrix;
import linearAlgebra.value.ValueMatrix;

public class FeatureVectorExtractor extends AbstractFeatureVectorExtractor {

	protected String name;
    protected InnerProductsCache ipc;

	public FeatureVectorExtractor(InnerProductsCache ipc) {
        this.ipc = ipc;
    }

    
    /*private float traceOfMin(ValueMatrix m1, HashSet<ValueBaseMatrix> m2){
        NNumber trace = null;
        
        for(int i=0; i<m1.getAmountOfNonNullBaseMatrices(); i++){
            ValueBaseMatrix bm1 = m1.getBaseMatrix(i);
            ValueBaseMatrix bm2 = 
        }
        
    }
    */
	
	private FeatureVector getKotlermanFeatureVector(int index, DepTree depTree1, DepTree depTree2){
		FeatureVector fv = new FeatureVector(index);
		
        //get value matrix at root (base matrices are sorted by dimension upon creating ValueMatrix)
        LinearCombinationMatrix l1 = (LinearCombinationMatrix) depTree1.getRootNode().getRepresentation();
        LinearCombinationMatrix l2 = (LinearCombinationMatrix) depTree2.getRootNode().getRepresentation();
		ValueMatrix r1 = (l1).toValueMatrix();
		ValueMatrix r2 = (l2).toValueMatrix();
        
        if(r1 == null){
			Helper.report("[FeatureVectorExtractor] (" + name + ") Matrix #" + index + ".1 is NULL! Skipping data point #" + index + ".");
			return null;
		}
		if(r2 == null){
			Helper.report("[FeatureVectorExtractor] (" + name + ") Matrix #" + index + ".2 is NULL! Skipping data point #" + index + ".");
			return null;
		}
        
        int i1 = 0, i2 = 0;
        ValueBaseMatrix bm1 = r1.getBaseMatrix(i1);
        ValueBaseMatrix bm2 = r1.getBaseMatrix(i2);
        double r1MinR2Trace = 0.0, linNumerator = 0.0, weedsNumerator = 0.0;
        while(true){
            int comparison = bm1.compareTo(bm2);
            if(comparison == 0){
                double value1 = bm1.getValue().getDoubleValue();
                double value2 = bm2.getValue().getDoubleValue();
                r1MinR2Trace += Math.min(value1, value2);
                linNumerator += value1 + value2;
                weedsNumerator = value1;
            }else if(comparison < 0){
                i1++;
                if(i1 >= r1.getCardinality()) break;
                bm1 = r1.getBaseMatrix(i1);
            }else if(comparison > 0){
                i2++;
                if(i2 >= r2.getCardinality()) break;
                bm2 = r2.getBaseMatrix(i2);
            }
        }
		
		

		//features based solely on matrices
		/*double sim12 = r1.times(r2).trace().getDoubleValue();
		double sim11 = r1.times(r1).trace().getDoubleValue();
		double sim22 = r2.times(r2).trace().getDoubleValue();
        */
        double sim12 = l1.innerProduct(l2, ipc).getDoubleValue();
        double sim11 = l1.innerProduct(l1, ipc).getDoubleValue();
        double sim22 = l2.innerProduct(l2, ipc).getDoubleValue();
		double superFidelity12 = sim12 + Math.sqrt((1 - sim11) * (1 - sim22));
		fv.setValue("sim11", sim11);
		fv.setValue("sim22", sim22);
		fv.setValue("sim12", sim12);
		fv.setValue("sim12 * 2 / (sim11 + sim22)", sim12 * 2 / (sim11 + sim22));
		fv.setValue("sim12 / sim11", sim12 / sim11);
		fv.setValue("sim12 / sim22", sim12 / sim22);
		fv.setValue("super fidelity", superFidelity12);

		//features from Kotlerman
		//Clarke (adapted)
		//Double r1MinR2Trace = r1.min(r2).trace(); //this by itself is symmetric
		fv.setValue("clarke0", r1MinR2Trace);
		fv.setValue("clarke1", r1MinR2Trace / sim11);
		fv.setValue("clarke2", r1MinR2Trace / sim22);
		fv.setValue("clarke3", r1MinR2Trace / superFidelity12);
		//LIN (adapted)
		/*double linNumerator = 0.0;
		double weedsNumerator = 0.0;
		//for (BaseMatrix r1Bm : r1.getBaseMatrices()) {
        for(int i=0; i<r1.getAmountOfNonNullBaseMatrices(); i++){
            BaseMatrix r1Bm = r1.getBaseMatrix(i);
			BaseMatrix r2Bm = r2.getBaseMatrixByDimension(r1Bm);
			if (r2Bm == null) {
				continue; //skip elements that r1 and r2 don't have in common
			}
			linNumerator += r1Bm.getValue() + r2Bm.getValue();
			weedsNumerator += r1Bm.getValue();
		}
        */
		fv.setValue("lin0", linNumerator);
		fv.setValue("lin", linNumerator / (sim11 + sim22));
		fv.setValue("lin1", linNumerator / sim11);
		fv.setValue("lin2", linNumerator / sim22);
		fv.setValue("lin3", linNumerator / superFidelity12);
		//Weeds Precision (adapted)
		fv.setValue("weeds0", weedsNumerator);
		fv.setValue("weeds", weedsNumerator / (sim11 + sim22));
		fv.setValue("weeds1", weedsNumerator / sim11);
		fv.setValue("weeds2", weedsNumerator / sim22);
		fv.setValue("weeds3", weedsNumerator / superFidelity12);
		//Bal Precision (adapted)
		fv.setValue("bal0", Math.sqrt(linNumerator * weedsNumerator));
		fv.setValue("bal", Math.sqrt(linNumerator * weedsNumerator) / (sim11 + sim22));
		fv.setValue("bal1", Math.sqrt(linNumerator * weedsNumerator) / sim11);
		fv.setValue("bal2", Math.sqrt(linNumerator * weedsNumerator) / sim22);
		fv.setValue("bal3", Math.sqrt(linNumerator * weedsNumerator) / superFidelity12);

		return fv;
	}

	
	/*public double getSimilarity(String word1, String word2, Matrix m1, Matrix m2) {
		String wordPair = (word1.compareTo(word2) <= 0 ? word1 + "\t" + word2 : word2 + "\t" + word1);
		Double existingSimilarity = localWordPairSimilarityMap.get(wordPair);
		//Double existingSimilarity = Helper.getCachedWordSimilarity(word1, word2);

		if (existingSimilarity != null) {
			return existingSimilarity;
		}else{
			double similarity;
			if(m1 == null){
				Helper.report("[FeatureVectorExtractorThread] (" + name + ") Can't compute similarity. Ldop for \"" + word1 + "\" is NULL!");
				similarity = 0.0;
			}else if(m2 == null){
				Helper.report("[FeatureVectorExtractorThread] (" + name + ") Can't compute similarity. Ldop for \"" + word2 + "\" is NULL!");
				similarity = 0.0;
			}else{			
				//similarity = m1.times(m2).trace();
				similarity = m1.similarity(m2, similarityMethod);
			}
			
			//Helper.cacheWordSimilarity(word1, word2, similarity);
			localWordPairSimilarityMap.put(wordPair, similarity);
			return similarity;
		}
	}
	
	private double getSimilarity(LabeledWord lw1, LabeledWord lw2){
		String word1 = lw1.getWord();
		String word2 = lw2.getWord();
		
		return getSimilarity(word1, word2, wordLdopMap.get(word1), wordLdopMap.get(word2));
	}
    */



	
	
	/*private FeatureVector extractFeatureVector(DepTreePair depTreePair){
		FeatureVector fv = new FeatureVector(depTreePair.getIndex());
		
		//superficial features
		fv.integrate(extractFeatureVectorSuperficial(depTreePair));
		
		//symmetric and directed similarity features
		//fv.integrate(extractFeatureVectorKotlerman(depTreePair));
		
		//alignment features from Illinois
		//fv.integrate(extractFeatureVectorIllinois(depTreePair));
		
		return fv;
	}
	
	private void extractAllFeatureVectors(){
		Helper.report("[FeatureVectorExtractor] (" + name + ") Extracting feature vectors for data points...");
		
		int counter = 0;
		try{
			for(DepTreePair datapoint : subDataset){
				//extract all possible feature values for this data point
				DepTreePair depTreePair = (DepTreePair) datapoint;
				FeatureVector fv = extractFeatureVector(depTreePair);
				featureVectorsCollection.append(depTreePair.getIndex(), fv);
				counter++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Helper.report("[FeatureVectorExtractor] (" + name + ") ...Finished extracting feature vectors for " + counter + " data points.");
	}

	
	@Override
	public void run() {
		extractAllFeatureVectors();
		exp.reportFeatureVectorExtractorThreadDone(this, featureVectorsCollection, localWordPairSimilarityMap);
	}
    */
    
    
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
                    DepTree depTree1 = ((experiment.dep.conll2015.Instance) instance).arguments[0];
                    DepTree depTree2 = ((experiment.dep.conll2015.Instance) instance).arguments[1];
                    fv.integrate(getKotlermanFeatureVector(instanceIndex, depTree1, depTree2));

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

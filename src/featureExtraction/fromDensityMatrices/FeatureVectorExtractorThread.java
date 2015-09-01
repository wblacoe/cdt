package featureExtraction.fromDensityMatrices;

import cdt.Helper;
import corpus.dep.converter.DepNode;
import corpus.dep.converter.DepTree;
import experiment.AbstractInstance;
import featureExtraction.FeatureVector;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import java.util.ArrayList;
import java.util.HashMap;
import linearAlgebra.value.LinearCombinationMatrix;
import linearAlgebra.value.ValueBaseMatrix;
import linearAlgebra.value.ValueMatrix;
import numberTypes.NNumber;
import numberTypes.NNumberVector;

public class FeatureVectorExtractorThread implements Runnable {

	private FeatureVectorExtractor superior;
	private String name;
	private HashMap<Integer, AbstractInstance> indexInstanceMap;
    private InnerProductsCache ipc;
	private ArrayList<String> featureList;

	public FeatureVectorExtractorThread(FeatureVectorExtractor superior, String name, HashMap<Integer, AbstractInstance> indexInstanceMap, InnerProductsCache ipc, ArrayList<String> featureList) {
		this.superior = superior;
		this.name = name;
		this.indexInstanceMap = indexInstanceMap;
        this.ipc = ipc;
		this.featureList = featureList;
    }
	public FeatureVectorExtractorThread(FeatureVectorExtractor superior, String name, Integer index, AbstractInstance instance, InnerProductsCache ipc, ArrayList<String> featureList) {
		this.superior = superior;
		this.name = name;
		indexInstanceMap = new HashMap<>();
		indexInstanceMap.put(index, instance);
        this.ipc = ipc;
		this.featureList = featureList;
    }

	public String getName(){
		return name;
	}
    
	private FeatureVector getKotlermanFeatureVector(int index, DepTree depTree1, DepTree depTree2){
		FeatureVector fv = new FeatureVector(index);
		
        //get value matrix at root (base matrices are sorted by dimension upon creating ValueMatrix)
        LinearCombinationMatrix l1 = depTree1.getRootNode().getRepresentation();
        LinearCombinationMatrix l2 = depTree2.getRootNode().getRepresentation();
		ValueMatrix r1 = l1.toValueMatrix();
		ValueMatrix r2 = l2.toValueMatrix();
        
		//System.out.println("Comparing #" + l1.getName() + " with #" + l2.getName() + "..."); //DEBUG
		
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
        ValueBaseMatrix bm2 = r2.getBaseMatrix(i2);
        double r1MinR2Trace = 0.0, linNumerator = 0.0, weedsNumerator = 0.0, sim12 = 0.0, sim11 = 0.0, sim22 = 0.0;
		boolean bm1IsNew = true, bm2IsNew = true;
        while(true){
			
			//System.out.println("kotlerman i1=" + i1 + ", i2=" + i2 + ", bm1=" + ((BaseMatrix) bm1) + ", bm2=" + ((BaseMatrix) bm2)); //DEBUG
			
			NNumber bm1Value = bm1.getValue();
			NNumber bm2Value = bm2.getValue();
			
			if(bm1IsNew) sim11 += bm1Value.multiply(bm1Value).getDoubleValue();
			if(bm2IsNew) sim22 += bm2Value.multiply(bm2Value).getDoubleValue();
			bm1IsNew = false;
			bm2IsNew = false;
			
            int comparison = bm1.compareIgnoreValue(bm2);
            if(comparison == 0){
                double value1 = bm1Value.getDoubleValue();
                double value2 = bm2Value.getDoubleValue();
                r1MinR2Trace += Math.min(value1, value2);
                linNumerator += value1 + value2;
                weedsNumerator += value1;
				sim12 += value1 * value2;
				i1++;
                if(i1 >= r1.getCardinality()) break;
                bm1 = r1.getBaseMatrix(i1);
				bm1IsNew = true;
				i2++;
                if(i2 >= r2.getCardinality()) break;
                bm2 = r2.getBaseMatrix(i2);
				bm2IsNew = true;
            }else if(comparison < 0){
                i1++;
                if(i1 >= r1.getCardinality()) break;
                bm1 = r1.getBaseMatrix(i1);
				bm1IsNew = true;
            }else if(comparison > 0){
                i2++;
                if(i2 >= r2.getCardinality()) break;
                bm2 = r2.getBaseMatrix(i2);
				bm2IsNew = true;
            }
        }
		
		

		//features based solely on matrices
		/*double sim12 = r1.times(r2).trace().getDoubleValue();
		double sim11 = r1.times(r1).trace().getDoubleValue();
		double sim22 = r2.times(r2).trace().getDoubleValue();
        */
        /*double sim12 = l1.innerProduct(l2, ipc).getDoubleValue();
        double sim11 = l1.innerProduct(l1, ipc).getDoubleValue();
        double sim22 = l2.innerProduct(l2, ipc).getDoubleValue();
		*/
		double superFidelity12 = sim12 + Math.sqrt((1 - sim11) * (1 - sim22));
		
		//similarity computed by treating all base matrices as mutually orthogonal (except when equal)
		//i.e. ignoring overlapping but unequal pairs of base matrices
		fv.setValue("sim11", sim11);
		fv.setValue("sim22", sim22);
		fv.setValue("sim12", sim12);
		fv.setValue("sim12 * 2 / (sim11 + sim22)", sim12 * 2 / (sim11 + sim22));
		fv.setValue("sim12 / sim11", sim12 / sim11);
		fv.setValue("sim12 / sim22", sim12 / sim22);
		fv.setValue("super fidelity", superFidelity12);

		//features from Kotlerman
		fv.setValue("clarke0", r1MinR2Trace);
		fv.setValue("clarke1", r1MinR2Trace / sim11);
		fv.setValue("clarke2", r1MinR2Trace / sim22);
		fv.setValue("clarke3", r1MinR2Trace / superFidelity12);

		fv.setValue("lin0", linNumerator);
		fv.setValue("lin", linNumerator / (sim11 + sim22));
		fv.setValue("lin1", linNumerator / sim11);
		fv.setValue("lin2", linNumerator / sim22);
		fv.setValue("lin3", linNumerator / superFidelity12);

		
		fv.setValue("weeds0", weedsNumerator);
		fv.setValue("weeds", weedsNumerator / (sim11 + sim22));
		fv.setValue("weeds1", weedsNumerator / sim11);
		fv.setValue("weeds2", weedsNumerator / sim22);
		fv.setValue("weeds3", weedsNumerator / superFidelity12);

		fv.setValue("bal0", Math.sqrt(linNumerator * weedsNumerator));
		fv.setValue("bal", Math.sqrt(linNumerator * weedsNumerator) / (sim11 + sim22));
		fv.setValue("bal1", Math.sqrt(linNumerator * weedsNumerator) / sim11);
		fv.setValue("bal2", Math.sqrt(linNumerator * weedsNumerator) / sim22);
		fv.setValue("bal3", Math.sqrt(linNumerator * weedsNumerator) / superFidelity12);

		return fv;
	}

	
	private NNumberVector getSimilarityFeatureVector(ValueMatrix r1, ValueMatrix r2){
		NNumberVector similarityFeatureVector = new NNumberVector(28);
		if(r1 == null || r2 == null || r1.getCardinality() == 0 || r2.getCardinality() == 0) return similarityFeatureVector;
		
		int i1 = 0, i2 = 0;
        ValueBaseMatrix bm1 = r1.getBaseMatrix(i1);
        ValueBaseMatrix bm2 = r2.getBaseMatrix(i2);
        NNumber r1MinR2Trace = NNumber.zero(), linNumerator = NNumber.zero(), weedsNumerator = NNumber.zero(), sim12 = NNumber.zero(), sim11 = NNumber.zero(), sim22 = NNumber.zero();
		boolean bm1IsNew = true, bm2IsNew = true;
        while(true){
			
			//System.out.println("kotlerman i1=" + i1 + ", i2=" + i2 + ", bm1=" + ((BaseMatrix) bm1) + ", bm2=" + ((BaseMatrix) bm2)); //DEBUG
			
			NNumber bm1Value = bm1.getValue();
			NNumber bm2Value = bm2.getValue();
			
			if(bm1IsNew) sim11 = sim11.add(bm1Value.multiply(bm1Value));
			if(bm2IsNew) sim22 = sim22.add(bm2Value.multiply(bm2Value));
			bm1IsNew = false;
			bm2IsNew = false;
			
            int comparison = bm1.compareIgnoreValue(bm2);
            if(comparison == 0){
                NNumber value1 = bm1Value;
                NNumber value2 = bm2Value;
                r1MinR2Trace = r1MinR2Trace.add(value1.min(value2));
                linNumerator = linNumerator.add(value1).add(value2);
                weedsNumerator = weedsNumerator.add(value1);
				sim12 = sim12.add(value1.multiply(value2));
				i1++;
                if(i1 >= r1.getCardinality()) break;
                bm1 = r1.getBaseMatrix(i1);
				bm1IsNew = true;
				i2++;
                if(i2 >= r2.getCardinality()) break;
                bm2 = r2.getBaseMatrix(i2);
				bm2IsNew = true;
            }else if(comparison < 0){
                i1++;
                if(i1 >= r1.getCardinality()) break;
                bm1 = r1.getBaseMatrix(i1);
				bm1IsNew = true;
            }else if(comparison > 0){
                i2++;
                if(i2 >= r2.getCardinality()) break;
                bm2 = r2.getBaseMatrix(i2);
				bm2IsNew = true;
            }
        }
		NNumber sim12Avg = sim12.multiply(NNumber.create(2)).divide(sim11.add(sim22));
		NNumber superFidelity12 = sim12.add((sim11.invert().add(NNumber.one())).multiply(sim22.invert().add(NNumber.one())));
		
		//similarity computed by treating all base matrices as mutually orthogonal (except when equal)
		//i.e. ignoring overlapping but unequal pairs of base matrices
		similarityFeatureVector.setWeight(0, sim12);
		similarityFeatureVector.setWeight(1, sim12Avg);
		similarityFeatureVector.setWeight(2, sim12.divide(sim11));
		similarityFeatureVector.setWeight(3, sim12.divide(sim22));
		similarityFeatureVector.setWeight(4, superFidelity12);

		//features from Kotlerman
		similarityFeatureVector.setWeight(5, r1MinR2Trace);
		similarityFeatureVector.setWeight(6, r1MinR2Trace.divide(sim11));
		similarityFeatureVector.setWeight(7, r1MinR2Trace.divide(sim22));
		similarityFeatureVector.setWeight(8, r1MinR2Trace.divide(superFidelity12));

		similarityFeatureVector.setWeight(9, linNumerator);
		similarityFeatureVector.setWeight(10, linNumerator.divide(sim11.add(sim22)));
		similarityFeatureVector.setWeight(11, linNumerator.divide(sim11));
		similarityFeatureVector.setWeight(12, linNumerator.divide(sim22));
		similarityFeatureVector.setWeight(13, linNumerator.divide(superFidelity12));

		similarityFeatureVector.setWeight(14, weedsNumerator);
		similarityFeatureVector.setWeight(15, weedsNumerator.divide(sim11.add(sim22)));
		similarityFeatureVector.setWeight(16, weedsNumerator.divide(sim11));
		similarityFeatureVector.setWeight(17, weedsNumerator.divide(sim22));
		similarityFeatureVector.setWeight(18, weedsNumerator.divide(superFidelity12));

		similarityFeatureVector.setWeight(19, linNumerator.multiply(weedsNumerator).sqrt());
		similarityFeatureVector.setWeight(20, linNumerator.multiply(weedsNumerator).sqrt().divide(sim11.add(sim22)));
		similarityFeatureVector.setWeight(21, linNumerator.multiply(weedsNumerator).sqrt().divide(sim11));
		similarityFeatureVector.setWeight(22, linNumerator.multiply(weedsNumerator).sqrt().divide(sim22));
		similarityFeatureVector.setWeight(23, linNumerator.multiply(weedsNumerator).sqrt().divide(superFidelity12));

		//combination of symmetric and asymmetric features
		similarityFeatureVector.setWeight(24, sim12Avg.multiply(r1MinR2Trace));
		similarityFeatureVector.setWeight(25, sim12Avg.multiply(linNumerator));
		similarityFeatureVector.setWeight(26, sim12Avg.multiply(weedsNumerator));
		similarityFeatureVector.setWeight(27, sim12Avg.multiply(linNumerator.multiply(weedsNumerator).sqrt()));
		
		return similarityFeatureVector;
	}
	
	//returns a list of value matrices
	//first is for the root node of given tree, all further matrices are for sub-root nodes
	private ArrayList<ValueMatrix> getRootAndSubRootValueMatrices(DepTree depTree){
		ArrayList<ValueMatrix> rootAndSubRootValueMatrices = new ArrayList<>();
		
		DepNode rootNode = depTree.getRootNode();
		LinearCombinationMatrix l = rootNode.getRepresentation();
		if(l == null){
			rootAndSubRootValueMatrices.add(null);
		}else{
			l.normalize(true);
			ValueMatrix rootValueMatrix = l.toValueMatrix();
			rootAndSubRootValueMatrices.add(rootValueMatrix);
		}
		
		int i=0;
		for(DepNode subRootNode : rootNode.getDependents()){
			LinearCombinationMatrix m = subRootNode.getRepresentation();
			if(m != null && !m.isZero()){
				ValueMatrix subRootValueMatrix = m.toValueMatrix();
				rootAndSubRootValueMatrices.add(subRootValueMatrix);
				i++;
			}
		}
		
		return rootAndSubRootValueMatrices;
	}
	
	private NNumberVector getRootsAndSubRootsFeatureVector(DepTree depTree1, DepTree depTree2){
		ArrayList<ValueMatrix> valueMatrices1 = getRootAndSubRootValueMatrices(depTree1);
		ArrayList<ValueMatrix> valueMatrices2 = getRootAndSubRootValueMatrices(depTree2);
		
		//get feature vector for roots pair
		NNumberVector rootsFeatureVector = getSimilarityFeatureVector(valueMatrices1.get(0), valueMatrices2.get(0));
		int length = rootsFeatureVector.getLength();
		
		//get feature vectors for all sub-roots pairs
		ArrayList<NNumberVector> allSubRootsFeatureVectors = new ArrayList<>();
		for(int i1=1; i1<valueMatrices1.size(); i1++){
			ValueMatrix m1 = valueMatrices1.get(i1);
			for(int i2=1; i2<valueMatrices2.size(); i2++){
				ValueMatrix m2 = valueMatrices2.get(i2);
				NNumberVector subRootsFeatureVector = getSimilarityFeatureVector(m1, m2);
				allSubRootsFeatureVectors.add(subRootsFeatureVector);
			}
		}

		NNumberVector subRootsAggregatedFeatureVectors;
		if(allSubRootsFeatureVectors.isEmpty()){
			subRootsAggregatedFeatureVectors = new NNumberVector(length * 3);
			
		}else{
			//aggregate all feature vectors for sub-roots pairs into one sub-roots feature vector per aggregation type
			NNumberVector subRootsMinFeatureVector = new NNumberVector(length);
			NNumberVector subRootsMaxFeatureVector = new NNumberVector(length);
			NNumberVector subRootsAverageFeatureVector = new NNumberVector(length);
			for(NNumberVector subRootsFeatureVectors : allSubRootsFeatureVectors){
				for(int i=0; i<length; i++){
					NNumber weight = subRootsFeatureVectors.getWeight(i);
					//min
					if(subRootsMinFeatureVector.getWeight(i) == null || weight.compareTo(subRootsMinFeatureVector.getWeight(i)) < 0){
						subRootsMinFeatureVector.setWeight(i, weight);
					}
					//max
					if(subRootsMaxFeatureVector.getWeight(i) == null || weight.compareTo(subRootsMaxFeatureVector.getWeight(i)) > 0){
						subRootsMaxFeatureVector.setWeight(i, weight);
					}
				}
				//average
				subRootsAverageFeatureVector.add(subRootsFeatureVectors);
			}
			//average
			subRootsAverageFeatureVector.multiply(NNumber.create(allSubRootsFeatureVectors.size()).reciprocal());

			subRootsAggregatedFeatureVectors = subRootsMinFeatureVector.concatenate(subRootsMaxFeatureVector).concatenate(subRootsAverageFeatureVector);
		}
		
		//concatenate
		return rootsFeatureVector.concatenate(subRootsAggregatedFeatureVectors);
	}
	
	private FeatureVector getDiscourseRelationFeatureVector(int index, DepTree depTree1, DepTree depTree2){
		FeatureVector fv = new FeatureVector(index);
		
		NNumberVector discourseRelationFeatureVector = getRootsAndSubRootsFeatureVector(depTree1, depTree2);
		
		String[] featureNames = new String[]{ "sim12", "sim12 * 2 / (sim11 + sim22)", "sim12 / sim11", "sim12 / sim22", "super fidelity", "clarke0", "clarke1", "clarke2", "clarke3", "lin0", "lin", "lin1", "lin2", "lin3", "weeds0", "weeds", "weeds1", "weeds2", "weeds3", "bal0", "bal", "bal1", "bal2", "bal3", "simAvg * clarke0", "simAvg * lin0", "simAvg * weeds0", "simAvg * bal0" };
		String[] situations = new String[]{ "roots", "subRoots.min", "subRoots.max", "subRoots.avg" };
		int i=0;
		for(String situation : situations){
			for(String featureName : featureNames){
				NNumber weight = discourseRelationFeatureVector.getWeight(i);
				double weightAsDouble;
				if(weight == null || weight.isInfinite() || weight.isNaN()){
					weightAsDouble = 0.0;
				}else{
					weightAsDouble = weight.getDoubleValue();
				}
				fv.setValue(featureName + " (" + situation + ")", weightAsDouble);
				i++;
			}
		}

		return fv;
	}
	
    
    @Override
    public void run(){
        FeatureVectorsCollection fvc = new FeatureVectorsCollection();
		fvc.applyFeatureSelection(featureList);
        
        try{
            for(Integer instanceIndex : indexInstanceMap.keySet()){
                AbstractInstance instance = indexInstanceMap.get(instanceIndex);
                FeatureVector fv = null;

                if(instance instanceof experiment.dep.conll2015.Instance){
                    DepTree depTree1 = ((experiment.dep.conll2015.Instance) instance).arguments[0];
                    DepTree depTree2 = ((experiment.dep.conll2015.Instance) instance).arguments[1];
                    //fv = getKotlermanFeatureVector(instanceIndex, depTree1, depTree2);
					fv = getDiscourseRelationFeatureVector(instanceIndex, depTree1, depTree2);
					fv.setIndex(instanceIndex);
					fv.applyFeatureSelection(featureList);

                }else if(instance instanceof experiment.dep.msrscc.Instance){
                    //TODO
                }

                fvc.append(instanceIndex, fv);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        superior.reportMatrixImporterDone(this, fvc);
    }

}

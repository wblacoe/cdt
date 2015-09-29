package corpus.associationFunction;

import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.dep.Vocabulary;
import linearAlgebra.BaseTensor;
import linearAlgebra.Matrix;
import linearAlgebra.count.CountBaseMatrix;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.ValueBaseMatrix;
import linearAlgebra.value.ValueMatrix;
import numberTypes.NNumber;
import numberTypes.NNumberVector;
import space.dep.ContextWord;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

/**
 *
 * @author wblacoe
 */
public class SppmiFunction extends AssociationFunction {

    private DepMarginalCounts dmc;
    private double delta;
    private int maxCardinality;
    private double logOneMillion;
    private long totalCountOfNeighbourhoods;
    private double logTotalCountOfNeighbourhoods;

    public SppmiFunction(DepMarginalCounts dmc, double delta, int maxCardinality){
		super();
        this.dmc = dmc;
        this.delta = delta;
        this.maxCardinality = maxCardinality;
        logOneMillion = Math.log(1e6);
	    totalCountOfNeighbourhoods = dmc.getCorpusTotalCount();
		logTotalCountOfNeighbourhoods = Math.log(totalCountOfNeighbourhoods / 1e6) + logOneMillion;

    }

    
    private double getValues(ValueMatrix vm, BaseTensor leftBaseTensor, BaseTensor rightBaseTensor, DepRelationCluster drc, int modeIndex){
        
        //compute value for pmi
        double value = 0.0;
        Long sumContextWordCount = dmc.getTotalContextWordCount(drc.getName());
        //left base tensor
        int contextWordDimensionIndexL = leftBaseTensor.getDimensionAtMode(modeIndex);
        if(contextWordDimensionIndexL > 0){ //skip dummy context word
            ContextWord cwL = drc.getContextWord(contextWordDimensionIndexL);
            String contextWordL = cwL.getWord();
            Long contextWordCountL = dmc.getContextWordCount(drc.getName(), contextWordL);
            if(contextWordCountL != null && sumContextWordCount != null){
                value +=
                    Math.log(sumContextWordCount / 1e6) //log of sum of counts of all context words in this subspace
                    - Math.log(contextWordCountL / 1e6); //log of count of this context word in this subspace
            }
        }
        //right base tensor
        int contextWordDimensionIndexR = rightBaseTensor.getDimensionAtMode(modeIndex);
        if(contextWordDimensionIndexR > 0){ //skip dummy context word
            ContextWord cwR = drc.getContextWord(contextWordDimensionIndexR);
            String contextWordR = cwR.getWord();
            Long contextWordCountR = dmc.getContextWordCount(drc.getName(), contextWordR);
            if(contextWordCountR != null && sumContextWordCount != null){
                value +=
                    Math.log(sumContextWordCount / 1e6) //log of sum of counts of all context words in this subspace
                    - Math.log(contextWordCountR / 1e6); //log of count of this context word in this subspace
            }
        }
        
        
        //compute partial trace diagonal vector
        NNumberVector[] partialTraceDiagonalVectors = vm.getPartialTraceDiagonalVectors();
        //#if(partialTraceDiagonalVectors == null) partialTraceDiagonalVectors = new NNumberVector[DepNeighbourhoodSpace.getOrder()];
        if(partialTraceDiagonalVectors == null) partialTraceDiagonalVectors = new NNumberVector[DepNeighbourhoodSpace.getOrder() + 1];
        //#NNumberVector partialTraceDiagonalVector = partialTraceDiagonalVectors[modeIndex - 1];
        NNumberVector partialTraceDiagonalVector = partialTraceDiagonalVectors[modeIndex];
        if(partialTraceDiagonalVector == null) partialTraceDiagonalVector = new NNumberVector(Vocabulary.getSize());
        
        NNumber ip = leftBaseTensor.innerProduct(rightBaseTensor);
        if(ip != null){
            NNumber weight = ip.multiply(value);
            int dimension = contextWordDimensionIndexL == 0 ? contextWordDimensionIndexR : contextWordDimensionIndexL;
			//ignore weight for the dummy word
			if(dimension > 0){
				int vocabularyIndex = drc.getContextWord(dimension).getVocabularyIndex();
				partialTraceDiagonalVector.add(vocabularyIndex, weight);
				//#partialTraceDiagonalVectors[modeIndex - 1] = partialTraceDiagonalVector;
                partialTraceDiagonalVectors[modeIndex] = partialTraceDiagonalVector;
			}
        }
        vm.setPartialTraceDiagonalVectors(partialTraceDiagonalVectors);
		
        
        return value;
    }
    
    @Override
    public Matrix compute(CountMatrix jointCountsDop, String targetWord){
        Matrix r = null;
        
        if(targetWord != null){
            
            //has the target word been observed at all?
            Long targetWordCount = dmc.getTargetWordCount(targetWord);
            //if no, save an empty ldop, and move on to the next target word
            if(targetWordCount != null){

                //log of probability of target word
                double pmi =
                    Math.log(dmc.getTotalTargetWordCount() / 1e6) + logOneMillion //log of sum of all target word counts
                    - Math.log(targetWordCount); //log of target word count

                ValueMatrix sppmiMatrix = new ValueMatrix(jointCountsDop.getCardinality() * 2); //reduce cardinality later
                sppmiMatrix.setName(jointCountsDop.getName());
                int count = 0;

                for(CountBaseMatrix bm : jointCountsDop.getCountBaseMatrices().values()){
                    pmi += Math.log(bm.getCount()) //log of joint count (of neighbourhood)
                           - logTotalCountOfNeighbourhoods; //log of total count (of neighbourhoods)

                    BaseTensor leftBaseTensor = bm.getLeftBaseTensor();
                    BaseTensor rightBaseTensor = bm.getRightBaseTensor();
                    for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
                        DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);
                        pmi += getValues(sppmiMatrix, leftBaseTensor, rightBaseTensor, drc, m); //this also computes the matrix' partial trace diagonal vector
                    }
                    
                    //only keep matrix elements with a positive pmi value
                    if(pmi + delta > 0){
                        ValueBaseMatrix vbm = new ValueBaseMatrix(leftBaseTensor, rightBaseTensor, NNumber.create(pmi + delta));
                        sppmiMatrix.setBaseMatrix(count, vbm);
                        count++;
                        if(!vbm.isDiagonal()){
                            sppmiMatrix.setBaseMatrix(count, vbm.transpose());
                            count++;
                        }
                    }

                }

				sppmiMatrix.normalizePartialTraceDiagonalVectors();
                sppmiMatrix.removeNullBaseMatrices();
                sppmiMatrix.reduceCardinality(Math.min(maxCardinality, sppmiMatrix.getCardinality()));
                //if(space.getBooleanParameter("normalizeldops")) pmiLdop.normalizeSelf();
                //wordLdopMap.put(targetWord, pmiLdop);
				//System.out.println("normalising " + sppmiMatrix); //DEBUG
                sppmiMatrix.normalize(); //divide by trace
                //targetWord.setRepresentation(sppmiMatrix);
                
                //System.out.println("c"); //DEBUG
                r = sppmiMatrix;
                
                //System.out.println("[SppmiFunction] ...Finished computing pmi Ldop for \"" + targetWord + "\"...");
            }
        }
        
        return r;
    }
    
    /*@Override
    //apply pmi association function to elements of matrix
	public void compute(){
		Helper.report("[SppmiFunction] Computing sppmi Ldops...");

		int counter = 0;
		for(int i=0; i<TargetElements.getSize(); i++){
            TargetWord targetWord = Vocabulary.getTargetWord(i);
            ValueMatrix sppmiMatrix = (ValueMatrix) compute((CountMatrix) targetWord.getLexicalRepresentation(), targetWord.getWord());
            targetWord.setLexicalRepresentation(sppmiMatrix);
            counter++;
        }
		
		//Helper.report("[SppmiFunction] ...Finished computing " + counter + " sppmi Ldops.");
	}
	*/
    
}

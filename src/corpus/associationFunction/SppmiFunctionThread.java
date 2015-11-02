package corpus.associationFunction;

import cdt.Helper;
import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.dep.Vocabulary;
import linearAlgebra.BaseTensor;
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
public class SppmiFunctionThread implements Runnable{
    
    private SppmiFunction superior;
    private DepMarginalCounts dmc;
    private double delta;
    private int maxCardinality;
    private CountMatrix jointCountsDop;
    private double logOneMillion;
    private long totalCountOfNeighbourhoods;
    private double logTotalCountOfNeighbourhoods;

    public SppmiFunctionThread(SppmiFunction superior, DepMarginalCounts dmc, double delta, int maxCardinality, CountMatrix jointCountsDop){
        this.superior = superior;
        this.dmc = dmc;
        this.delta = delta;
        this.maxCardinality = maxCardinality;
        this.jointCountsDop = jointCountsDop;
        logOneMillion = Math.log(1e6);
	    totalCountOfNeighbourhoods = dmc.getCorpusTotalCount();
		logTotalCountOfNeighbourhoods = Math.log(totalCountOfNeighbourhoods / 1e6) + logOneMillion;

    }

    
    private double getPmiIncrementForMode(ValueMatrix vm, BaseTensor leftBaseTensor, BaseTensor rightBaseTensor, DepRelationCluster drc, int modeIndex, int amountOfInstantiatedRelations){
        
        double pmiIncrement = 0.0;
        
        //compute value for pmi
        Long sumContextWordCount = dmc.getTotalContextWordCount(drc.getName());
        
        //left base tensor
        int contextWordDimensionIndexL = leftBaseTensor.getDimensionAtMode(modeIndex);
        if(contextWordDimensionIndexL > 0){ //skip dummy context word
            ContextWord cwL = drc.getContextWord(contextWordDimensionIndexL);
            String contextWordL = cwL.getWord();
            Long contextWordCountL = dmc.getContextWordCount(drc.getName(), contextWordL);
            if(contextWordCountL != null && sumContextWordCount != null){
                double logProbOfContextWord =
                    Math.log(sumContextWordCount / 1e6) //log of sum of counts of all context words in this subspace
                    - Math.log(contextWordCountL / 1e6); //log of count of this context word in this subspace
                pmiIncrement += logProbOfContextWord;
                //System.out.println("left log(#(R" + modeIndex + "=" + contextWordDimensionIndexL + ")=log("  + contextWordCountL + ")=" + Math.log(contextWordCountL)); //DEBUG
                //System.out.println("left log(#(R" + modeIndex + "=*)=log(" + sumContextWordCount + ")=" + Math.log(sumContextWordCount)); //DEBUG
            }
        }
        
        //right base tensor
        int contextWordDimensionIndexR = rightBaseTensor.getDimensionAtMode(modeIndex);
        if(contextWordDimensionIndexR > 0){ //skip dummy context word
            ContextWord cwR = drc.getContextWord(contextWordDimensionIndexR);
            String contextWordR = cwR.getWord();
            Long contextWordCountR = dmc.getContextWordCount(drc.getName(), contextWordR);
            if(contextWordCountR != null && sumContextWordCount != null){
                double logProbOfContextWord =
                    Math.log(sumContextWordCount / 1e6) //log of sum of counts of all context words in this subspace
                    - Math.log(contextWordCountR / 1e6); //log of count of this context word in this subspace
                pmiIncrement += logProbOfContextWord;
                //System.out.println("right log(#(R" + modeIndex + "=" + contextWordDimensionIndexR + ")=log(" + contextWordCountR + ")=" + Math.log(contextWordCountR)); //DEBUG
                //System.out.println("right log(#(R" + modeIndex + "=*)=log(" + sumContextWordCount + ")=" + Math.log(sumContextWordCount)); //DEBUG
            }
        }
        
        return pmiIncrement / amountOfInstantiatedRelations;
    }
    
    private void updatePartialTraceDiagonalVector(NNumberVector partialTraceDiagonalVector, ValueBaseMatrix vbm, int modeIndex){
        //compute partial trace diagonal vector
        BaseTensor leftBaseTensor = vbm.getLeftBaseTensor();
        BaseTensor rightBaseTensor = vbm.getRightBaseTensor();
        NNumber ip = leftBaseTensor.innerProduct(rightBaseTensor);
        if(ip != null){
            NNumber weight = ip.multiply(vbm.getValue());
            int contextWordDimensionIndexL = leftBaseTensor.getDimensionAtMode(modeIndex);
            int contextWordDimensionIndexR = rightBaseTensor.getDimensionAtMode(modeIndex);
            int dimension = contextWordDimensionIndexL == 0 ? contextWordDimensionIndexR : contextWordDimensionIndexL;
			//ignore weight for the dummy word
			if(dimension > 0){
                DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(modeIndex);
				int vocabularyIndex = drc.getContextWord(dimension).getVocabularyIndex();
				partialTraceDiagonalVector.add(vocabularyIndex, weight);
			}
        }
    }
    private void updatePartialTraceDiagonalVectors(ValueMatrix vm, ValueBaseMatrix vbm){
        NNumberVector[] partialTraceDiagonalVectors = vm.getPartialTraceDiagonalVectors();
        //#if(partialTraceDiagonalVectors == null) partialTraceDiagonalVectors = new NNumberVector[DepNeighbourhoodSpace.getOrder()];
        if(partialTraceDiagonalVectors == null) partialTraceDiagonalVectors = new NNumberVector[DepNeighbourhoodSpace.getOrder() + 1];

        for(int modeIndex=1; modeIndex<=DepNeighbourhoodSpace.getOrder(); modeIndex++){
            //#NNumberVector partialTraceDiagonalVector = partialTraceDiagonalVectors[modeIndex - 1];
            NNumberVector partialTraceDiagonalVector = partialTraceDiagonalVectors[modeIndex];
            if(partialTraceDiagonalVector == null) partialTraceDiagonalVector = new NNumberVector(Vocabulary.getSize());
            updatePartialTraceDiagonalVector(partialTraceDiagonalVector, vbm, modeIndex);
            //#partialTraceDiagonalVectors[modeIndex - 1] = partialTraceDiagonalVector;
            partialTraceDiagonalVectors[modeIndex] = partialTraceDiagonalVector;
        }
        
        vm.setPartialTraceDiagonalVectors(partialTraceDiagonalVectors);
    }
    
    public ValueMatrix compute(){
        //Helper.report("[SppmiFunctionThread] (" + jointCountsDop.getName() + ") Computing LDop for \"" + jointCountsDop.getName() + "\"...");
        
        ValueMatrix sppmiMatrix = null;
        
        String targetWord = null;
        if(jointCountsDop != null) targetWord = jointCountsDop.getName();
        
        if(targetWord != null){
            //System.out.println("word: " + targetWord); //DEBUG
            
            //has the target word been observed at all?
            Long targetWordCount = dmc.getTargetWordCount(targetWord);
            //if no, save an empty ldop, and move on to the next target word
            if(targetWordCount != null){

                //log of probability of target word
                double logProbOfTargetWord =
                    Math.log(dmc.getTotalTargetWordCount() / 1e6) + logOneMillion //log of sum of all target word counts
                    - Math.log(targetWordCount); //log of target word count
                //System.out.println("log(#(T=w))=log(" + targetWordCount + ")=" + Math.log(targetWordCount)); //DEBUG
                //System.out.println("log(#(T=*))=log(" + dmc.getTotalTargetWordCount() + ")=" + Math.log(dmc.getTotalTargetWordCount())); //DEBUG

                sppmiMatrix = new ValueMatrix(jointCountsDop.getCardinality() * 2); //reduce cardinality later
                sppmiMatrix.setName(jointCountsDop.getName());
                int count = 0;

                for(CountBaseMatrix bm : jointCountsDop.getCountBaseMatrices().values()){

                    Double pmi = 0.0;
                    BaseTensor leftBaseTensor = bm.getLeftBaseTensor();
                    BaseTensor rightBaseTensor = bm.getRightBaseTensor();
                    int amountOfInstantiatedRelations = leftBaseTensor.getAmountOfCertainModes() + rightBaseTensor.getAmountOfCertainModes();
                    //go through all modes
                    for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
                        DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);
                        //update pmi according to the context word probabilities from this mode for left and right base tensor simultaneously
                        pmi += getPmiIncrementForMode(sppmiMatrix, leftBaseTensor, rightBaseTensor, drc, m, amountOfInstantiatedRelations);
                    }
                    //System.out.println("amount of instantiated relations=" + amountOfInstantiatedRelations); //DEBUG
                    
                    double logProbOfDepNeighbourhood =
                        Math.log(bm.getCount()) //log of joint count (of neighbourhood)
                        - logTotalCountOfNeighbourhoods; //log of total count (of neighbourhoods)
                    pmi += logProbOfTargetWord + logProbOfDepNeighbourhood;
                        
                    //System.out.println("log(#(T=w,R1=c1,...,Rn=cn))=log(" + bm.getCount() + ")=" + Math.log(bm.getCount())); //DEBUG
                    //System.out.println("log(#(*,...,*))=log(" + totalCountOfNeighbourhoods + ")=" + logTotalCountOfNeighbourhoods); //DEBUG
                    //System.out.println("pmi=" + pmi); //DEBUG
                    
                    //shift the pmi value by delta
                    pmi += delta;
                    
                    //only keep matrix elements with a positive pmi value
                    if(pmi > 0){
                        ValueBaseMatrix vbm = new ValueBaseMatrix(leftBaseTensor, rightBaseTensor, NNumber.create(pmi));
                        updatePartialTraceDiagonalVectors(sppmiMatrix, vbm);
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
                
                //System.out.println("[SppmiFunction] ...Finished computing pmi Ldop for \"" + targetWord + "\"...");
            }
        }
        
        //Helper.report("[SppmiFunctionThread] (" + jointCountsDop.getName() + ") ...Finished computing LDop for \"" + jointCountsDop.getName() + "\"");
        return sppmiMatrix;
    }

    @Override
    public void run(){
        ValueMatrix vm = compute();
        superior.reportSppmiFunctionThreadDone(this, vm);
    }
    
}

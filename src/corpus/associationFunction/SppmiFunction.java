package corpus.associationFunction;

import cdt.Helper;
import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.TargetElements;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import linearAlgebra.BaseTensor;
import linearAlgebra.Matrix;
import linearAlgebra.count.CountBaseMatrix;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.ValueBaseMatrix;
import linearAlgebra.value.ValueMatrix;
import numberTypes.NNumber;
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

    @Override
    public Matrix compute(CountMatrix jointCountsDop, String targetWord){
        if(targetWord == null){
            //System.out.println("a"); //DEBUG
            return null;
        }else{
            
            //has the target word been observed at all?
            Long targetWordCount = dmc.getTargetWordCount(targetWord/*.getWord()*/);
            //if no, save an empty ldop, and move on to the next target word
            if(targetWordCount == null){
                //System.out.println("b"); //DEBUG
                return null;
            }else{

                //log of probability of target word
                double pmi =
                    Math.log(/*#subSpaceContextWordCountMap.get(null).get(null)*/ dmc.getTotalTargetWordCount() / 1e6) + logOneMillion //log of sum of all target word counts
                    - Math.log(targetWordCount); //log of target word count
                //System.out.println("Math.log(space.getTotalTargetWordCount() / 1e6) = " + Math.log(space.getTotalTargetWordCount() / 1e6));
                //System.out.println("- Math.log(targetWordCount) = " + - Math.log(targetWordCount));

                //CountMatrix jointCountsDop = (CountMatrix) targetWord.getRepresentation();
                ValueMatrix sppmiMatrix = new ValueMatrix(jointCountsDop.getCardinality() * 2); //reduce cardinality later
                sppmiMatrix.setName(jointCountsDop.getName());
                int count = 0;

                for(CountBaseMatrix bm : jointCountsDop.getCountBaseMatrices().values()){
                    pmi +=
                        Math.log(bm.getCount()) //log of joint count (of neighbourhood)
                        - logTotalCountOfNeighbourhoods; //log of total count (of neighbourhoods) //TODO, this is not yet assigned a value (in the process of extracting counts from the corpus). It can be ignored, though, because it is the same for all pmi values

                    //left base tensor
                    BaseTensor leftBaseTensor = bm.getLeftBaseTensor();
                    for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
                        DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);
                        int contextWordDimensionIndex = leftBaseTensor.getDimensionAtMode(m);
                        if(contextWordDimensionIndex == 0){
                            //TODO
                        }else{
                            ContextWord cw = drc.getContextWord(contextWordDimensionIndex);
                            String contextWord = cw.getWord();
                            Long sumContextWordCount = dmc.getTotalContextWordCount(drc.getName());
                            Long contextWordCount = dmc.getContextWordCount(drc.getName(), contextWord);
                            if(contextWordCount != null && sumContextWordCount != null){
                                //System.out.println("contextWordCount = " + contextWordCount);
                                pmi +=
                                    Math.log(sumContextWordCount / 1e6) //log of sum of counts of all context words in this subspace
                                    - Math.log(contextWordCount / 1e6); //log of count of this context word in this subspace
                                //System.out.println("sumContextWordCount = " + sumContextWordCount);
                                //System.out.println("contextWordCount = "  + contextWordCount);
                            }else{
                                //Helper.report("[SppmiFunction] word \"" + targetWord + "\" in <ket, subspace " + subSpace + ", context word: " + contextWord + "> has a 0 count!"); //DEBUG
                            }
                        }
                    }

                    //right base tensor
                    BaseTensor rightBaseTensor = bm.getRightBaseTensor();
                    for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
                        DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);
                        int contextWordDimensionIndex = rightBaseTensor.getDimensionAtMode(m);
                        if(contextWordDimensionIndex == 0){
                            //TODO
                        }else{
                            ContextWord cw = drc.getContextWord(contextWordDimensionIndex);
                            String contextWord = cw.getWord();
                            Long sumContextWordCount = dmc.getTotalContextWordCount(drc.getName());
                            Long contextWordCount = dmc.getContextWordCount(drc.getName(), contextWord);
                            if(contextWordCount != null && sumContextWordCount != null){
                                //System.out.println("contextWordCount = " + contextWordCount);
                                pmi +=
                                    Math.log(sumContextWordCount / 1e6) //log of sum of counts of all context words in this subspace
                                    - Math.log(contextWordCount / 1e6); //log of count of this context word in this subspace
                                //System.out.println("sumContextWordCount = " + sumContextWordCount);
                                //System.out.println("contextWordCount = "  + contextWordCount);
                            }else{
                                //Helper.report("[SppmiFunction] word \"" + targetWord + "\" in <ket, subspace " + subSpace + ", context word: " + contextWord + "> has a 0 count!"); //DEBUG
                            }
                        }
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

                //replace raw ldop by pmi ldop
                //#pmiLdop.activateMaintaince();
                sppmiMatrix.removeNullBaseMatrices();
                sppmiMatrix.reduceCardinality(Math.min(maxCardinality, sppmiMatrix.getCardinality()));
                //if(space.getBooleanParameter("normalizeldops")) pmiLdop.normalizeSelf();
                //wordLdopMap.put(targetWord, pmiLdop);
				//System.out.println("normalising " + sppmiMatrix); //DEBUG
                sppmiMatrix.normalize(); //divide by trace
                //targetWord.setRepresentation(sppmiMatrix);
                
                //System.out.println("c"); //DEBUG
                return sppmiMatrix;
                
                //System.out.println("[SppmiFunction] ...Finished computing pmi Ldop for \"" + targetWord + "\"...");
            }
        }
    }
    
    @Override
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
    
}

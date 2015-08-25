package linearAlgebra.value;

import experiment.TargetElements;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import innerProduct.InnerProductsCache;
import java.util.TreeMap;
import linearAlgebra.Matrix;
import numberTypes.NNumber;
import numberTypes.NNumberVector;
import space.dep.DepNeighbourhoodSpace;

/**
 * expresses linear combination of subordinate matrices.
 * weights are in array where indices correspond to target elements.
 * 
 * @author wblacoe
 */
public class LinearCombinationMatrix extends Matrix {
    
    //array indices correspond to target element indices
    private NNumberVector weights;
    
    public LinearCombinationMatrix(){
        //creates a zero matrix
        weights = new NNumberVector(Vocabulary.getSize());
    }
    public LinearCombinationMatrix(NNumberVector weights){
        this.weights = weights;
    }
    
    
    public NNumberVector getWeights(){
        return weights;
    }

    public void add(LinearCombinationMatrix m){
        weights.add(m.getWeights());
    }
    
    public void setWeight(int index, NNumber weight){
        weights.setWeight(index, weight);
    }
    
    public NNumber getWeight(int index){
        return weights.getWeight(index);
    }

    //returns linear combination of partial trace vectors of subordinate matrices
    public NNumberVector getPartialTraceVector(int modeIndex){
        NNumberVector partialTraceVector = new NNumberVector(TargetElements.getSize());
        
        for(int i=0; i<weights.getLength(); i++){
            NNumber weight = weights.getWeight(i);
            if(weight != null){
                TargetWord tw = Vocabulary.getTargetWord(i);
                NNumberVector twPartialTraceVector = ((ValueMatrix) tw.getRepresentation()).getPartialTraceVector(modeIndex);
                partialTraceVector.add(twPartialTraceVector);
            }
        }
        
        return partialTraceVector;
    }
    
    @Override
    public boolean isZero(){
        return weights.isZero();
    }
    
    public NNumber getTrace(boolean assumeLdopsAreNormalized){
        NNumber trace = NNumber.create(0);
        for(int i=0; i<weights.getLength(); i++){
            NNumber weight = weights.getWeight(i);
            if(weight == null || weight.isZero()) continue;
            
            if(assumeLdopsAreNormalized){
                trace.add(weight);
            }else{
                TargetWord tw = Vocabulary.getTargetWord(i);
                NNumber twTrace = ((ValueMatrix) tw.getRepresentation()).trace();
                trace.add(twTrace.multiply(weight));
            }
        }
        return trace;
    }
    
    public void normalize(boolean assumeLdopsAreNormalized){
        NNumber oneOverTrace = getTrace(assumeLdopsAreNormalized).reciprocal();
        for(int i=0; i<weights.getLength(); i++){
            NNumber weight = weights.getWeight(i);
            if(weight != null && !weight.isZero()){
                weights.setWeight(i, weight.multiply(oneOverTrace));
            }
        }
    }
    
    //reduces inner product of linear combination matrices to linear combination of inner products of lexical matrices
    public NNumber innerProduct(LinearCombinationMatrix given, InnerProductsCache ipc){
        NNumber ip = null;
        
        for(int i=0; i<Vocabulary.getSize(); i++){
            NNumber weight1 = weights.getWeight(i);
            if(weight1 == null || weight1.isZero()) continue;
            //ValueMatrix m1 = (ValueMatrix) Vocabulary.getTargetWord(i).getRepresentation();
            //TargetWord tw1 = Vocabulary.getTargetWord(i);
            for(int j=0; j<Vocabulary.getSize(); j++){
                NNumber weight2 = given.getWeight(j);
                if(weight2 == null || weight2.isZero()) continue;
                //ValueMatrix m2 = (ValueMatrix) Vocabulary.getTargetWord(j).getRepresentation();
                //TargetWord tw2 = Vocabulary.getTargetWord(j);
                //NNumber localIp = DepNeighbourhoodSpace.getFrobeniusInnerProduct(tw1.getWord(), tw2.getWord(), true);
                NNumber localIp = ipc.getInnerProduct(i, j, true);
                if(localIp == null || localIp.isZero()) continue;
                NNumber weightedLocalIp = weight1.multiply(weight2).multiply(localIp);
                if(ip == null){
                    ip = weightedLocalIp;
                }else{
                    ip = ip.add(weightedLocalIp);
                }
            }
        }
        
        return ip;
    }
    
    //returns a ValueMatrix
    //  - which is the sum of all lexical ValueMatrices weighted by these weights
    //  - whose base matrices are sorted by dimension ascendingly
    //  - whose cardinality is not restricted
    public ValueMatrix toValueMatrix(){
        TreeMap<ValueBaseMatrix, ValueBaseMatrix> collection = new TreeMap<>();
        
        for(int i=1; i<=Vocabulary.getSize(); i++){
            NNumber weight = weights.getWeight(i);
            if(weight == null || weight.isZero()) continue;
            
            TargetWord tw = Vocabulary.getTargetWord(i);
            ValueMatrix twMatrix = (ValueMatrix) tw.getRepresentation();
            for(int j=0; j<twMatrix.getAmountOfNonNullBaseMatrices(); j++){
                ValueBaseMatrix bm = twMatrix.getBaseMatrix(j);
                ValueBaseMatrix existingBm = collection.remove(bm);
                if(existingBm == null){
                    collection.put(bm, bm);
                }else{
                    ValueBaseMatrix newBm = new ValueBaseMatrix(bm.getLeftBaseTensor(), bm.getRightBaseTensor(), existingBm.getValue().add(bm.getValue()));
                    collection.put(newBm, newBm);
                }
            }
        }
        
        ValueMatrix m = new ValueMatrix(collection.size());
        int i=0;
        while(true){
            ValueBaseMatrix bm = collection.pollFirstEntry().getValue();
            if(bm == null) break;
            m.setBaseMatrix(i, bm);
            i++;
        }
        
        return m;
    }
    
}

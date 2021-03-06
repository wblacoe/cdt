package linearAlgebra.value;

import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import innerProduct.InnerProductsCache;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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

	private static final double log2 = Math.log(2);
	
    //array indices correspond to target element indices
    private NNumberVector weights;
    
    public LinearCombinationMatrix(){
        //creates a zero matrix
        weights = new NNumberVector(Vocabulary.getSize());
    }
    public LinearCombinationMatrix(NNumberVector weights){
        this.weights = weights;
    }
	//create a one-hot weights vector for the target word with given vocabulary index
	public LinearCombinationMatrix(TargetWord targetWord){
		this();
		setName(targetWord.getWord());
		weights.setWeight(targetWord.getIndex(), NNumber.one());
	}
	//create a one-hot weights vector for the target word with given word from vocabulary
	public LinearCombinationMatrix(String vocabularyWord){
		this();
		setName(vocabularyWord);
		Integer vocabularyIndex = Vocabulary.getTargetWordIndex(vocabularyWord);
		if(vocabularyIndex != null){
			weights.setWeight(vocabularyIndex, NNumber.one());
		}
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
    public NNumberVector getPartialTraceDiagonalVector(int modeIndex){
        NNumberVector partialTraceDiagonalVector = new NNumberVector(Vocabulary.getSize());
        
        for(int i=0; i<Vocabulary.getSize(); i++){
            NNumber weight = weights.getWeight(i);
            if(weight != null && !weight.isZero()){
                TargetWord tw = Vocabulary.getTargetWord(i);
                ValueMatrix vm = (ValueMatrix) tw.getLexicalRepresentation();
                if(vm != null){
                    NNumberVector twPartialTraceDiagonalVector = vm.getPartialTraceDiagonalVector(modeIndex);
                    partialTraceDiagonalVector.add(twPartialTraceDiagonalVector.times(weight));
                }
            }
        }
        
        return partialTraceDiagonalVector;
    }
    
    @Override
    public boolean isZero(){
        return weights.isZero();
    }
    
    public NNumber getTrace(boolean assumeLdopsAreNormalized){
        NNumber trace = null;
        for(int i=0; i<weights.getLength(); i++){
            NNumber weight = weights.getWeight(i);
            if(weight == null || weight.isZero()) continue;
            
            if(assumeLdopsAreNormalized){
                trace = (trace == null ? weight : trace.add(weight));
            }else{
                TargetWord tw = Vocabulary.getTargetWord(i);
                NNumber twTrace = ((ValueMatrix) tw.getLexicalRepresentation()).trace();
                trace = (trace == null ? twTrace.multiply(weight) : trace.add(twTrace.multiply(weight)));
            }
        }
        return trace;
    }
    
    public void normalize(boolean assumeLdopsAreNormalized){
		NNumber trace = getTrace(assumeLdopsAreNormalized);
		if(trace != null && !trace.isZero()){
			NNumber oneOverTrace = trace.reciprocal();
			for(int i=0; i<weights.getLength(); i++){
				NNumber weight = weights.getWeight(i);
				if(weight != null && !weight.isZero()){
					weights.setWeight(i, weight.multiply(oneOverTrace));
				}
			}
		}
    }
    
    //reduces inner product of linear combination matrices to linear combination of inner products of lexical matrices
    public NNumber innerProduct(LinearCombinationMatrix given, InnerProductsCache ipc){
        NNumber ip = null;
        
        for(int i=0; i<Vocabulary.getSize(); i++){
            NNumber weight1 = getWeight(i);
            if(weight1 == null || weight1.isZero()) continue;
            TargetWord tw1 = Vocabulary.getTargetWord(i);
            for(int j=0; j<Vocabulary.getSize(); j++){
                NNumber weight2 = given.getWeight(j);
                if(weight2 == null || weight2.isZero()) continue;
                TargetWord tw2 = Vocabulary.getTargetWord(j);
                //NNumber localIp = ipc.getInnerProduct(i, j, true);
                NNumber localIp = ipc.getInnerProduct(tw1.getWord(), tw2.getWord(), true);
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
        
        //save linear combination of partial trace diagonal vectors here
        NNumberVector[] partials = new NNumberVector[DepNeighbourhoodSpace.getOrder() + 1];
        for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
            partials[m] = new NNumberVector(Vocabulary.getSize());
        }
        
        for(int i=0; i<Vocabulary.getSize(); i++){
            NNumber weight = weights.getWeight(i);
            if(weight != null && !weight.isZero()){
				TargetWord tw = Vocabulary.getTargetWord(i);
				ValueMatrix twMatrix = (ValueMatrix) tw.getLexicalRepresentation();
				if(twMatrix != null && !twMatrix.isZero()){
					for(int j=0; j<twMatrix.getAmountOfNonNullBaseMatrices(); j++){
						//System.out.println("toValueMatrix i=" + i + ", j=" + j); //DEBUG
						ValueBaseMatrix bm = twMatrix.getBaseMatrix(j);
						ValueBaseMatrix existingBm = collection.remove(bm);
						if(existingBm == null){
							ValueBaseMatrix newBm = new ValueBaseMatrix(bm.getLeftBaseTensor(), bm.getRightBaseTensor(), bm.getValue().multiply(weight));
							collection.put(newBm, newBm);
						}else{
							ValueBaseMatrix newBm = new ValueBaseMatrix(bm.getLeftBaseTensor(), bm.getRightBaseTensor(), existingBm.getValue().add(bm.getValue().multiply(weight)));
							collection.put(newBm, newBm);
						}
					}
					for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
						partials[m].add(twMatrix.getPartialTraceDiagonalVector(m).times(weight));
					}
				}
			}
        }
        
        ValueMatrix vm = new ValueMatrix(collection.size());
        int i=0;
        /*while(true){
			Entry<ValueBaseMatrix, ValueBaseMatrix> entry = collection.pollFirstEntry();
			if(entry == null) continue;
            ValueBaseMatrix bm = entry.getValue();
            if(bm == null) break;
            m.setBaseMatrix(i, bm);
            i++;
        }
		*/
		for(ValueBaseMatrix bm : collection.keySet()){
			if(bm != null){
				vm.setBaseMatrix(i, bm);
				i++;
			}
		}
        vm.setName(name);
        
        for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
            vm.setPartialTraceDiagonalVectors(partials);
        }
        
        return vm;
    }
	
	public LinearCombinationMatrix getCopy(){
		return new LinearCombinationMatrix(weights.getCopy());
	}
	
	public double getRenyi2Entropy(InnerProductsCache ipc){
		return -Math.log(this.innerProduct(this, ipc).getDoubleValue()) / log2;
	}

    @Override
    public String toString(){
        return name + " " + weights.toString();
    }
 
    @Override
    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<matrix name=\"" + getName() + "\" type=\"linearcombination\" cardinality=\"\">\n");
        for(int i=0; i<Vocabulary.getSize(); i++){
            NNumber weight = getWeight(i);
            if(weight != null && !weight.isZero()){
                //s += Vocabulary.getTargetWord(i).getWord() + ": " + weight/*.getDoubleValue()*/ + ", ";
                out.write(i + "\t" + weight.getDoubleValue() + "\n");
            }
        }
        out.write("</matrix>\n");
    }
    
    public static LinearCombinationMatrix importFromReader(BufferedReader in) throws IOException{
        LinearCombinationMatrix m = new LinearCombinationMatrix();
        
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</matrix>")) break;
            
            String[] entries = line.split("\t");
            int twIndex = Integer.parseInt(entries[0]);
            NNumber weight = NNumber.create(Double.parseDouble(entries[1]));
            
            m.setWeight(twIndex, weight);
        }

        return m;
    }

}

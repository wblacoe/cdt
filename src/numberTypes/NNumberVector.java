package numberTypes;

import experiment.dep.Vocabulary;
import java.util.TreeSet;

/**
 *
 * @author wblacoe
 */
public class NNumberVector {

    private NNumber[] array; //null entries mean 0
    
    
    public NNumberVector(int length){
        array = new NNumber[length];
    }
    
    
    public NNumber[] getArray(){
        return array;
    }
    
    public int getLength(){
        return array.length;
    }
	
	public int getCardinality(){
		int card = 0;
		for(NNumber number : array) {
			if(number != null && !number.isZero()) card++;
		}
		return card;
	}
    
    public void add(NNumberVector v){
        NNumber[] givenArray = v.getArray();
        for(int i=0; i<array.length; i++){
            NNumber thisWeight = array[i];
            NNumber givenWeight = givenArray[i];
            if(thisWeight == null){
                if(givenWeight != null){
                    array[i] = givenWeight;
                }
            }else{
                if(givenWeight != null){
                    array[i] = thisWeight.add(givenWeight);
                }
            }
        }
    }
    
    public void add(int index, NNumber weight){
        NNumber existingWeight = array[index];
        if(existingWeight == null){
            array[index] = weight;
        }else{
            array[index] = existingWeight.add(weight);
        }
    }
	
	public void multiply(NNumber scalar){
		for(int i=0; i<array.length; i++){
			NNumber weight = array[i];
			if(weight != null && !weight.isZero()){
				array[i] = weight.multiply(scalar);
			}
		}
	}
	
	public NNumberVector times(NNumber scalar){
		NNumberVector v = new NNumberVector(this.getLength());

		for(int i=0; i<array.length; i++){
			NNumber weight = array[i];
			if(weight != null && !weight.isZero()){
				v.setWeight(i, weight.multiply(scalar));
			}
		}
		
		return v;
	}

    public void setWeight(int index, NNumber weight){
        array[index] = weight;
    }
    
    public NNumber getWeight(int index){
        return array[index];
    }
 
    public boolean isZero(){
        for(int i=0; i<array.length; i++){
            if(array[i] != null && !array[i].isZero()) return false;
        }
        return true;
    }
	
	public NNumber getL1Norm(){
		NNumber l1norm = null, weight = null;
		for(int i=0; i<array.length; i++){
			weight = array[i];
			if(weight != null && !weight.isZero()){
				if(l1norm == null){
					l1norm = weight;
				}else{
					l1norm = l1norm.add(weight);
				}
			}
		}
		
		return l1norm;
	}
	
	public void normalize(){
		NNumber l1norm = getL1Norm(), weight = null;
		for(int i=0; i<array.length; i++){
			weight = array[i];
			if(weight != null && !weight.isZero()){
				array[i] = weight.divide(l1norm);
			}
		}
	}
	
	public NNumberVector concatenate(NNumberVector v){
		int thisLength = getLength();
		NNumberVector c = new NNumberVector(thisLength+ v.getLength());
		
		for(int i=0; i<thisLength; i++){
			c.setWeight(i, array[i]);
		}
		for(int i=0; i<v.getLength(); i++){
			c.setWeight(thisLength + i, v.getWeight(i));
		}
		
		return c;
	}
    
    public NNumberVector getCopy(){
        NNumberVector v = new NNumberVector(this.getLength());
        for(int i=0; i<this.getLength(); i++){
            NNumber weight = this.getWeight(i);
            if(weight != null && !weight.isZero()){
                v.setWeight(i, weight);
            }
        }
        return v;
    }
    
    public void keepOnlyWeightsLargerOrEqualTo(NNumber threshold){
        for(int i=0; i<getLength(); i++){
            NNumber weight = getWeight(i);
            if(weight != null && weight.compareTo(threshold) < 0){
                setWeight(i, null);
            }
        }
    }
    
    public void keepOnlyTopNWeights(int n){
        TreeSet<NNumber> topNWeights = new TreeSet<>();
        for(int i=0; i<getLength(); i++){
            NNumber weight = getWeight(i);
            if(weight != null){
                topNWeights.add(weight);
                while(topNWeights.size() > n) topNWeights.pollFirst();
            }
        }
        keepOnlyWeightsLargerOrEqualTo(topNWeights.first());
    }
    
    @Override
    public String toString(){
        String s = "[";
        
        for(int i=0; i<array.length; i++){
            NNumber weight = array[i];
            if(weight != null && !weight.isZero()){
                s += Vocabulary.getTargetWord(i).getWord() + ": " + weight/*.getDoubleValue()*/ + ", ";
            }
        }
        
        return s + "]";
    }
    
}

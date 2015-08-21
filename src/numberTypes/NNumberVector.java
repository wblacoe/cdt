package numberTypes;

import experiment.dep.Vocabulary;

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
    
    public void add(NNumberVector v){
        NNumber[] givenArray = v.getArray();
        for(int i=0; i<Vocabulary.getSize(); i++){
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
            existingWeight = weight;
        }else{
            array[index] = existingWeight.add(weight);
        }
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
    
}

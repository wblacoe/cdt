package linearAlgebra.value;

import java.util.ArrayList;
import numberTypes.NNumber;

/**
 *
 * @author wblacoe
 */
public class DecomposedMatrix {

    protected ArrayList<NNumber> eigenValues;
    protected ArrayList<ValueTensor> eigenTensors;
    protected ArrayList<ValueMatrix> eigenMatrices;
    
    public DecomposedMatrix(){
        eigenValues = new ArrayList<>();
        eigenTensors = new ArrayList<>();
        eigenMatrices = new ArrayList<>();
    }
    
    public void addEigenComponent(NNumber eigenValue, ValueTensor eigenTensor, ValueMatrix eigenMatrix){
        eigenValues.add(eigenValue);
        eigenTensors.add(eigenTensor);
        eigenMatrices.add(eigenMatrix);
    }
    
    public void removeEigenComponent(int index){
        eigenValues.remove(index);
        eigenTensors.remove(index);
        eigenMatrices.remove(index);
    }
    
    public NNumber getEigenValue(int index){
        return eigenValues.get(index);
    }
    
    public ValueTensor getEigenTensor(int index){
        return eigenTensors.get(index);
    }
    
    public ValueMatrix getEigenMatrix(int index){
        return eigenMatrices.get(index);
    }
    
}

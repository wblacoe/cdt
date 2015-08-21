package linearAlgebra.value;

import linearAlgebra.Matrix;
import numberTypes.NNumber;

/**
 * m = valueMatrix + identitiyFactor * I
 * 
 * @author wblacoe
 */
public class ValuePlusMatrix extends Matrix {

    private ValueMatrix valueMatrix;
    private NNumber identityFactor;
    
    public ValuePlusMatrix(int cardinality) {
        super();
        valueMatrix = new ValueMatrix(cardinality);
        identityFactor = NNumber.create(0);
    }
    public ValuePlusMatrix(ValueMatrix valueMatrix, NNumber identitfyFactor){
        this.valueMatrix = valueMatrix;
        this.identityFactor = identitfyFactor;
    }
    
    public void setValueMatrix(ValueMatrix vm){
        valueMatrix = vm;
    }
    public ValueMatrix getValueMatrix(){
        return valueMatrix;
    }
    
    public void setIdentitityFactor(NNumber n){
        identityFactor = n;
    }
    public NNumber getIdentityFactor(){
        return identityFactor;
    }
    
    
    public ValuePlusMatrix add(ValuePlusMatrix m){
        ValuePlusMatrix sum = new ValuePlusMatrix(getCardinality());
        sum.setValueMatrix(valueMatrix.plus(m.getValueMatrix()));
        sum.setIdentitityFactor(identityFactor.add(m.getIdentityFactor()));
        return sum;
    }
    
    public ValuePlusMatrix add(ValueMatrix m){
        ValuePlusMatrix sum = new ValuePlusMatrix(
            valueMatrix.plus(m),
            identityFactor
        );
        return sum;
    }
    
    public ValuePlusMatrix add(NNumber n){
        ValuePlusMatrix sum = new ValuePlusMatrix(
            valueMatrix,
            identityFactor.add(n)
        );
        return sum;
    }
        
    
    public ValuePlusMatrix times(ValuePlusMatrix m){
        ValueMatrix m1 = valueMatrix.times(m.getValueMatrix());
        //System.out.println("m1: " + m1 + ", m1.1: " + valueMatrix + ", m1.2: " + m.getValueMatrix());
        ValueMatrix m2 = valueMatrix.times(m.getIdentityFactor());
        //System.out.println("m2: " + m2);
        ValueMatrix m3 = m.getValueMatrix().times(identityFactor);
        //System.out.println("m3: " + m3);
        
        ValueMatrix newM = m1.plus(m2);
        //System.out.println("m1 + m2: " + newM);
        newM = newM.plus(m3);
        //System.out.println("(m1 + m2) + m3: " + newM);
        
        ValuePlusMatrix product = new ValuePlusMatrix(
            newM,
            identityFactor.multiply(m.getIdentityFactor())
        );
        
        return product;
    }
    
    public ValuePlusMatrix times1(ValuePlusMatrix m){
        ValuePlusMatrix product = new ValuePlusMatrix(
            (valueMatrix.times(m.getValueMatrix())).plus(valueMatrix.times(m.getIdentityFactor())).plus(m.getValueMatrix().times(identityFactor)),
            identityFactor.multiply(m.getIdentityFactor())
        );
        return product;
    }
    
    public ValuePlusMatrix times(NNumber n){
        ValuePlusMatrix product = new ValuePlusMatrix(
            valueMatrix.times(n),
            identityFactor.multiply(n)
        );
        return product;
    }
    
}

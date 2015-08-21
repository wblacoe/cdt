package linearAlgebra.value;

import java.io.BufferedWriter;
import java.io.IOException;
import linearAlgebra.BaseMatrix;
import linearAlgebra.BaseTensor;
import numberTypes.NNumber;
import space.TensorSpace;

/**
 *
 * @author wblacoe
 */
public class ValueBaseMatrix extends BaseMatrix {
    
   	private NNumber value;
	
	public ValueBaseMatrix(){
		super();
		//value = NNumber.zero();
        value = null;
	}
	public ValueBaseMatrix(NNumber value){
		super();
		this.value = value;
	}
	public ValueBaseMatrix(BaseTensor btLeft, BaseTensor btRight){
		super(btLeft, btRight);
	}
	public ValueBaseMatrix(BaseTensor btLeft, BaseTensor btRight, NNumber value){
		super(btLeft, btRight);
		this.value = value;
	}
	
	public void add(NNumber n){
        if(value == null){
            value = n;
        }else{
            value = value.add(n);
        }
	}
    
    public void setValue(NNumber n){
        value = n;
    }
    
	public NNumber getValue(){
		return value;
	}
    
    public boolean isZero(){
        //return value.isZero();
        return value == null;
    }
    
    public void times(NNumber factor){
        value = value.multiply(factor);
    }
    
    public ValueBaseTensor times(ValueBaseTensor t){
        NNumber ip = rightBaseTensor.innerProduct(t);
        if(ip == null){
            return null;
        }else{
            return new ValueBaseTensor(getLeftBaseTensor(), ip.multiply(value).multiply(t.getValue()));
        }
    }
    
    public ValueBaseMatrix times(ValueBaseMatrix bm){
        if(bm == null) return null;
        
        NNumber ipInside = rightBaseTensor.innerProduct(bm.getLeftBaseTensor());
        //if(ipInside.isZero()){
        if(ipInside == null){
            //return new ValueBaseMatrix();
            return null;
        }else{
            NNumber newValue = ipInside.multiply(value).multiply(bm.getValue());
            return new ValueBaseMatrix(leftBaseTensor, bm.getRightBaseTensor(), newValue);
        }
    }
    
    //<m1, m2> = Tr(m1*m2^T)
    public NNumber innerProduct(ValueBaseMatrix bm){
        
        //left
        NNumber ipLeft = leftBaseTensor.innerProduct(bm.getLeftBaseTensor());
        //if(ipLeft.isZero()) return ipLeft;
        if(ipLeft == null) return null;
        
        //right
        NNumber ipRight = rightBaseTensor.innerProduct(bm.getRightBaseTensor());
        //if(ipRight.isZero()) return ipRight;
        if(ipRight == null) return null;
        
        return ipLeft.multiply(ipRight).multiply(value).multiply(bm.getValue());
    }
    
    @Override
    public ValueBaseMatrix transpose(){
        return new ValueBaseMatrix(rightBaseTensor, leftBaseTensor, value);
    }
    
    @Override
    //sort by dimension first, then by value
    public int compareTo(Object o){
        int compareBm = super.compareTo(o);
        if(compareBm != 0){
            return compareBm;
        }else{
            ValueBaseMatrix bm = (ValueBaseMatrix) o;
            NNumber givenValue = bm.getValue();
            if(value == null){
                if(givenValue == null){
                    return 0;
                }else if(givenValue.isPositive()){
                    return -1;
                }else{
                    return 1;
                }
            }else{
                if(givenValue != null){
                    return value.compareTo(givenValue);
                }else if(value.isPositive()){
                    return 1;
                }else{
                    return -1;
                }
            }
            
        }
    }
    
	@Override
	public boolean equals(Object o){
		return /*(o instanceof ValueBaseMatrix) &&*/ compareTo((ValueBaseMatrix) o) == 0;
	}

    //@Override
    public void exportToWriter(BufferedWriter out) throws IOException{
        //out.write(value.getDoubleValue() + "\t" + leftBaseTensor.toString() + "\t" + rightBaseTensor.toString() + "\n");
        out.write(value.getDoubleValue() + "\t");
        leftBaseTensor.saveToWriter(out);
        out.write("\t");
        rightBaseTensor.saveToWriter(out);
        out.write("\n");
        //+ leftBaseTensor.toPrettyString() + "\t" + rightBaseTensor.toPrettyString() + "\n");
    }
    
    @Override
	public String toString(){
		String s = "" + value + "*" + super.toString();
		return s;
	}

    
    public static void main(String[] args){
        TensorSpace.setOrder(5);
		TensorSpace.setDimensionality(300);
		TensorSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

        ValueBaseMatrix bm1 = new ValueBaseMatrix(NNumber.one());
		bm1.setValue(bm1.getValue().invert());
		bm1.getLeftBaseTensor().setDimensionAtMode(1, 5);
		
		//BaseTensor bt2 = new BaseTensor(NNumber.one());
		ValueBaseMatrix bm2 = new ValueBaseMatrix(NNumber.create(0.3123f));
		//bm2.setValue(btm.getValue().invert());
        bm2.getRightBaseTensor().setDimensionAtMode(2, 7);
		
		System.out.println("bm1 = " + bm1);
		System.out.println("bm2 = " + bm2);
		System.out.println("<bm1,bm2> = " + bm1.innerProduct(bm2));

    }
    
}

package linearAlgebra.value;

import java.io.BufferedWriter;
import java.io.IOException;
import linearAlgebra.BaseTensor;
import numberTypes.NNumber;
import space.TensorSpace;

public class ValueBaseTensor extends BaseTensor {

	private NNumber value;
	
	public ValueBaseTensor(){
		super();
		value = NNumber.one();
	}
	public ValueBaseTensor(int mode, int dimension, NNumber value){
		super();
		modeDimensionArray[mode] = dimension;
		this.value = value;
	}
	public ValueBaseTensor(NNumber value){
		super();
		this.value = value;
	}
    public ValueBaseTensor(BaseTensor bt){
        super(bt.getModeDimensionArray(), bt.getModeIsDimensionCertainArray(), bt.getModeAndOnlyCertainDimension());
    }
    public ValueBaseTensor(BaseTensor bt, NNumber value){
        super(bt.getModeDimensionArray(), bt.getModeIsDimensionCertainArray(), bt.getModeAndOnlyCertainDimension());
        this.value = value;
    }
    
    
    public NNumber getValue(){
		return value;
	}
	public void setValue(NNumber n){
		value = n;
	}

    public boolean isZero(){
        return value.isZero();
    }
	
	public NNumber innerProduct(ValueBaseTensor bt){
		
		NNumber innerProductWithoutValues = super.innerProduct(bt);
        if(innerProductWithoutValues == null /*|| innerProductWithoutValues.isZero()*/){
            return null;
        }else{
            return innerProductWithoutValues.multiply(value).multiply(bt.getValue());
        }
	}
    
    @Override
    public int compareTo(Object o){
        int compareBt = super.compareTo(o);
        if(compareBt != 0){
            return compareBt;
        }else{
            ValueBaseTensor bt = (ValueBaseTensor) o;
            return value.compareTo(bt.getValue());
        }
    }
    
    //@Override
    public void exportToWriter(BufferedWriter out) throws IOException{
        //out.write(value.getDoubleValue() + "\t" + toString() + "\n");
        out.write(value.getDoubleValue() + "\t" + toPrettyString() + "\n");
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
		//Helper.setNumberType(NNumber.LOG_FLOAT);
		//Helper.setNumberType(NNumber.RATIONAL);
		//Helper.setNumberType(NNumber.COMPLEX);
		
		ValueBaseTensor bt1 = new ValueBaseTensor();
		bt1.setValue(bt1.getValue().invert());
		bt1.setDimensionAtMode(1, 5);
		//bt1.setDimensionAtMode(2, 7);
		
		//BaseTensor bt2 = new BaseTensor(NNumber.one());
		ValueBaseTensor bt2 = new ValueBaseTensor(NNumber.create(0.3123f));
		//bt2.setValue(bt2.getValue().invert());
		
		System.out.println("bt1 = " + bt1);
		System.out.println("bt2 = " + bt2);
		System.out.println("<bt1,bt2> = " + bt1.innerProduct(bt2));
	}

}

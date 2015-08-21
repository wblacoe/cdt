package numberTypes;

import cdt.Helper;
import space.TensorSpace;

/**
 * number = dimensionality^exponent
 * @author wblacoe
 */
public class NLogFloat extends NNumber {

    private float exponent;

	public NLogFloat(boolean ignoreme, float exponent){
		this.exponent = exponent;
	}
    public NLogFloat(){
		this(true, Float.NEGATIVE_INFINITY);
    }
    public NLogFloat(int n){
        this(true, (float) Math.log10(n));
    }
    public NLogFloat(float n){
        this(true, (float) Math.log10(n));
    }
    public NLogFloat(double n){
        this(true, (float) Math.log10(n));
    }
    public NLogFloat(String nString){
        this(Double.parseDouble(nString));
    }
	
    
	public float getExponent(){
		return exponent;
	}
    
    @Override
    public boolean isZero(){
        return exponent == Float.NEGATIVE_INFINITY;
    }
    
    @Override
    public NNumber add(NNumber n) {
		NLogFloat r = (NLogFloat) n;
		return new NLogFloat(
			(float) (
				Math.log(
					(this.getDoubleValue() + n.getDoubleValue())
					/ TensorSpace.getDimensionalityLog()
				)
			)
		);
    }

    @Override
    public NNumber multiply(NNumber n) {
		NLogFloat r = (NLogFloat) n;
		float givenExponent = r.getExponent();
		NLogFloat product = new NLogFloat(true, exponent + givenExponent);
		System.out.println("" + this + "*" + r + "=" + product); //DEBUG
		
        return product;
    }
    
    @Override
    public NNumber multiply(int n){
		float nLog = (float) (Math.log(n) / TensorSpace.getDimensionalityLog());
        return new NLogFloat(true, exponent + nLog);
    }
    
    @Override
    public NNumber multiply(float n){
        float nLog = (float) (Math.log(n) / TensorSpace.getDimensionalityLog());
        return new NLogFloat(true, exponent + nLog);
    }
    
    @Override
    public NNumber multiply(double n){
        float nLog = (float) (Math.log(n) / TensorSpace.getDimensionalityLog());
        return new NLogFloat(true, exponent + nLog);
    }
 
	@Override
	public double getDoubleValue(){
		return Math.pow(TensorSpace.getDimensionality(), exponent);
	}
    
    @Override
    public int compareTo(Object o){
        if(!(o instanceof NLogFloat)) throw new IllegalArgumentException("[NLogFloat] Bad comparison: Wrong type");
        NLogFloat n = (NLogFloat) o;
        return Float.compare(exponent, n.getExponent());
    }
	
    @Override
    public String toString(){
		return "" + TensorSpace.getDimensionality() + "^" + exponent;
    }
    
}
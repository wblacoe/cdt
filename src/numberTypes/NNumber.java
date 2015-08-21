package numberTypes;

import space.TensorSpace;

/**
 *
 * @author wblacoe
 */
public class NNumber implements Comparable {
	
	public static final int COMPLEX = 1;
	public static final int CUSTOM_BASE_FLOAT = 2;
	public static final int DOUBLE = 3;
	public static final int FLOAT = 4;
	public static final int LOG_DOUBLE = 5;
	public static final int LOG_FLOAT = 6;
	public static final int LOG_RATIONAL = 7;
	public static final int RATIONAL = 8;
	
	/*public static NNumber zero(){
		switch(Helper.getNumberType()){
			case COMPLEX : return new NComplex();
			case CUSTOM_BASE_FLOAT : return new NCustomBaseFloat();
			case FLOAT : return new NFloat();
			case LOG_FLOAT : return new NLogFloat();
			case RATIONAL : return new NRational();
			default : return null;
		}
	}
    */
	
	public static NNumber one(){
		switch(TensorSpace.getNumberType()){
			case COMPLEX : return new NComplex(1);
			case CUSTOM_BASE_FLOAT : return new NCustomBaseFloat(1); //TODO: optimise this
			case FLOAT : return new NFloat(1);
			case LOG_FLOAT : return new NLogFloat(1);
			case RATIONAL : return new NRational(1);
			default : return null;
		}
	}
	
	public static NNumber create(int n){
		switch(TensorSpace.getNumberType()){
			case COMPLEX : return new NComplex(n);
			case CUSTOM_BASE_FLOAT : return new NCustomBaseFloat(n);
			case FLOAT : return new NFloat(n);
			case LOG_FLOAT : return new NLogFloat(n);
			case RATIONAL : return new NRational(n);
			default : return null;
		}
	}
	public static NNumber create(float n){
		switch(TensorSpace.getNumberType()){
			case COMPLEX : return new NComplex(n);
			case CUSTOM_BASE_FLOAT : return new NCustomBaseFloat(n);
			case FLOAT : return new NFloat(n);
			case LOG_FLOAT : return new NLogFloat(n);
			case RATIONAL : return new NRational(n);
			default : return null;
		}
	}
	public static NNumber create(double n){
		switch(TensorSpace.getNumberType()){
			case COMPLEX : return new NComplex(n);
			case CUSTOM_BASE_FLOAT : return new NCustomBaseFloat(n);
			case FLOAT : return new NFloat(n);
			case LOG_FLOAT : return new NLogFloat(n);
			case RATIONAL : return new NRational(n);
			default : return null;
		}
	}
    public static NNumber createDimensionalitySqrtToThePowerOf(int e){
        	switch(TensorSpace.getNumberType()){
			case CUSTOM_BASE_FLOAT : return new NCustomBaseFloat(true, 1.0f, e);
			default : return create(Math.pow(TensorSpace.getDimensionalitySqrt(), e));
		}
	}
    
    public int logBaseDimensionalitySqrtToInt(){
        double valueLog =  Math.log(getDoubleValue()) / TensorSpace.getDimensionalitySqrtLog();
        return ((int) valueLog);
    }
    
	public boolean isZero(){
        return false;
    }
    
    public boolean isPositive(){
        return false;
    }
    
    public boolean isNegative(){
        return false;
    }
    
    public NNumber add(NNumber n){
		return null;
	}
    public NNumber multiply(NNumber n){
		return null;
	}
    public NNumber multiply(int n){
		return null;
	}
    public NNumber multiply(float n){
		return null;
	}
    public NNumber multiply(double n){
		return null;
	}

	public NNumber invert(){
		return multiply(-1);
	}
    public NNumber reciprocal(){
        return NNumber.create(Math.pow(getDoubleValue(), -1));
    }
    public NNumber divide(NNumber n){
        return multiply(n.reciprocal());
    }
    public NNumber sqrt(){
        if(isNegative()){
            return null;
        }else{
            return NNumber.create(Math.pow(getDoubleValue(), 0.5));
        }
    }
    public NNumber abs(){
        if(isPositive()){
            return this;
        }else{
            return invert();
        }
    }
	public double getDoubleValue(){
		return Double.NaN;
	}
    
    /*public static NNumber min(NNumber n1, NNumber n2){
            switch(TensorSpace.getNumberType()){
			case CUSTOM_BASE_FLOAT : return NCustomBaseFloat.min(n1, n2);
			default : return null;
		}
    }
    public static NNumber max(NNumber n1, NNumber n2){
            switch(TensorSpace.getNumberType()){
			case CUSTOM_BASE_FLOAT : return NCustomBaseFloat.min(n1, n2);
			default : return null;
		}
    }
    */

	/*public int round(double n){
		double a = Math.abs(n);
		int i = (int) a;
		double r = a - ((double) i);
		if(r < 0.5){
			return n<0 ? -i : i;
		}else{
			return n<0 ? -(i+1) : i+1;
		}
	}
	*/

    @Override
    public int compareTo(Object o){
        return 0;
    }
    
	@Override
	public String toString(){
		return "" + getDoubleValue();
	}
	

	public static void main(String[] args){
		TensorSpace.setDimensionality(300);
		TensorSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
		NNumber o = NNumber.one();
		System.out.println(o);
	}
	
}

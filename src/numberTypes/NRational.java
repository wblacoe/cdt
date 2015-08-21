package numberTypes;

import space.TensorSpace;

/**
 * number = numerator / denominator
 * @author wblacoe
 */
public class NRational extends NNumber {
    
    private int numerator, denominator;

	//standard value is 0
    public NRational(int numerator, int denominator){
        this.numerator = numerator;
        this.denominator = denominator;
    }
    public NRational(){
        this(0, TensorSpace.getDimensionality()); //change standard denominator?
    }
    public NRational(int n){
        this();
        numerator = n * denominator;
    }
    public NRational(float n){
        this();
        numerator = (int) (n * denominator);
    }
    public NRational(double n){
        this();
        numerator = (int) (n * denominator);
    }
    public NRational(String nString){
        this(Double.parseDouble(nString));
    }

    
    public int getNumerator(){
        return numerator;
    }
    public int getDenominator(){
        return denominator;
    }
    
    @Override
    public boolean isZero(){
        return numerator == 0;
    }
    
    @Override
    public NNumber add(NNumber n) {
        return new NRational(numerator + ((NRational) n).getNumerator(), denominator);
    }

    @Override
    public NNumber multiply(NNumber n) {
        NRational r = (NRational) n;
        return new NRational((int) (numerator * r.getNumerator() / ((float) denominator)), denominator);
    }
    
    @Override
    public NNumber multiply(int n){
        return new NRational(numerator * n, denominator);
    }
    
    @Override
    public NNumber multiply(float n){
        return new NRational((int) (numerator * n), denominator);
    }
    
    @Override
    public NNumber multiply(double n){
        return new NRational((int) (numerator * n), denominator);
    }
 
    @Override
    public double getDoubleValue(){
        return ((double) numerator) / denominator;
    }
	
    @Override
    public int compareTo(Object o){
        if(!(o instanceof NRational)) throw new IllegalArgumentException("[NRational] Bad comparison: Wrong type");
        NRational n = (NRational) o;
        //assume this and given NRational have the same denominator
        return Integer.compare(numerator, n.getNumerator());
    }
    
    @Override
    public String toString(){
		return "" + numerator + "/" + denominator;
    }
	
}
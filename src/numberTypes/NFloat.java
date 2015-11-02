package numberTypes;

/**
 *
 * @author wblacoe
 */
public class NFloat extends NNumber {
    
    private final float number;

	//standard value is 0
    public NFloat(){
        number = 0.0f;
    }
    public NFloat(int n){
        number = n;
    }
    public NFloat(float n){
        number = n;
    }
    public NFloat(double n){
        number = (float) n;
    }
    public NFloat(String nString){
        number = Float.parseFloat(nString);
    }

    
    public float getNumber(){
        return number;
    }
    
    @Override
    public boolean isZero(){
        return number == 0;
    }
    
    @Override
    public NNumber add(NNumber n) {
        return new NFloat(number + ((NFloat) n).getNumber());
    }

    @Override
    public NNumber multiply(NNumber n) {
        return new NFloat(number * ((NFloat) n).getNumber());
    }

    @Override
    public NNumber multiply(int n) {
        return new NFloat(number * n);
    }

    @Override
    public NNumber multiply(float n) {
        return new NFloat(number * n);
    }

    @Override
    public NNumber multiply(double n) {
        return new NFloat(number * ((float) n));
    }

	@Override
	public double getDoubleValue(){
		return number;
	}
    
    @Override
    public int compareTo(Object o){
        if(!(o instanceof NFloat)) throw new IllegalArgumentException("[NFloat] Bad comparison: Wrong type");
        NFloat n = (NFloat) o;
        return Float.compare(number, n.getNumber());
    }
	
    @Override
    public String toString(){
        return "" + number;
    }
    
}

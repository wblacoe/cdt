package linearAlgebra.count;

//used for counting

import linearAlgebra.BaseTensor;

public class CountBaseTensor extends BaseTensor {

	private int count;
	
	public CountBaseTensor(){
		count = 0;
	}
	public CountBaseTensor(int count){
		this.count = count;
	}
	
	public void add(int n){
		count += n;
	}
	public int getCount(){
		return count;
	}
    
    public CountBaseTensor getCopy(){
        CountBaseTensor copy = new CountBaseTensor(count);
        copy.setModeDimensionArray(modeDimensionArray);
        copy.setModeIsDimensionCertainArray(modeIsDimensionCertainArray);
        copy.setModeandOnlyCertainDimension(modeAndOnlyCertainDimension);
        return copy;
    }
	
}

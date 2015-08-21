package linearAlgebra.count;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import linearAlgebra.Tensor;

//should a max cardinality be set?
public class CountTensor extends Tensor {

	private HashMap<CountBaseTensor, CountBaseTensor> countBaseTensors;
	
	public CountTensor(){
		super();
		countBaseTensors = new HashMap<>();
	}
	
	
	public void add(CountBaseTensor bt){
		CountBaseTensor existingBt = countBaseTensors.get(bt);
		if(existingBt == null){
			countBaseTensors.put(bt, bt);
		}else{
			existingBt.add(bt.getCount());
		}
	}
    
    public void add(CountTensor t){
        for(CountBaseTensor bt : t.getCountBaseTensors()){
            add(bt);
        }
    }
	
	public int getCardinality(){
		return countBaseTensors.size();
	}
	
	public Collection<CountBaseTensor> getCountBaseTensors(){
		return countBaseTensors.keySet();
	}
	
	public CountMatrix outerProductOnlyUpperRightTriangle(CountTensor t){
		CountMatrix result = new CountMatrix();
		
		for(CountBaseTensor thisBt : getCountBaseTensors()){
			for(CountBaseTensor givenBt : t.getCountBaseTensors()){
                int comparison = thisBt.compareTo(givenBt);
				if(comparison <= 0) result.add(
					new CountBaseMatrix(
						thisBt,
						givenBt,
						thisBt.getCount() * givenBt.getCount()
					)
				);
			}
		}
		
		return result;
	}

	public CountMatrix outerProduct(CountTensor t){
		CountMatrix result = new CountMatrix();
		
		for(CountBaseTensor thisBt : getCountBaseTensors()){
			for(CountBaseTensor givenBt : t.getCountBaseTensors()){
				result.add(
					new CountBaseMatrix(
						thisBt,
						givenBt,
						thisBt.getCount() * givenBt.getCount()
					)
				);
			}
		}
		
		return result;
	}
    
	
	public void importFromReader(BufferedReader in) throws IOException{
		//TODO
	}
			
	public void exportToWriter(BufferedWriter out) throws IOException{
		//TODO
	}
	
}

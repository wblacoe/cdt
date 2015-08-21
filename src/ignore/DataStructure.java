package ignore;

import numberTypes.NNumber;
import java.util.ArrayList;

public class DataStructure {

	private ArrayList<NNumber> values;
	
	public DataStructure(){
		values = new ArrayList<>();
	}
	
	public void append(NNumber value){
		values.add(value);
	}
	public NNumber getValueAt(int i){
		return values.get(i);
	}
	public int getSize(){
		return values.size();
	}
	public DataStructure add(DataStructure ds){
		DataStructure result = new DataStructure();
		for(int i=0; i<Math.min(getSize(), ds.getSize()); i++){
			NNumber thisValue = getValueAt(i);
			NNumber dsValue = ds.getValueAt(i);
			if(thisValue != null && dsValue != null){
				result.append(thisValue.add(dsValue));
			}else{
				break;
			}
		}
		return result;
	}
	
	@Override
	public String toString(){
		String s = "";
		for(int i=0; i<values.size(); i++){
			s += " " + getValueAt(i);
		}
		return s;
	}
	
}

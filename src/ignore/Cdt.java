package ignore;

import numberTypes.NFloat;

/**
 *
 * @author wblacoe
 */
public class Cdt {

	public Cdt(){
		DataStructure ds1 = new DataStructure();
		ds1.append(new NFloat(0.5f));
		ds1.append(new NFloat(2));
		ds1.append(new NFloat(3));
		
		DataStructure ds2 = new DataStructure();
		ds2.append(new NFloat(10));
		ds2.append(new NFloat(20));
		//ds2.append(new NFloat(30));
		
		System.out.println(ds1);
		System.out.println(ds2);
		System.out.println(ds1.add(ds2));
	}
	
    public static void main(String[] args) {
        Cdt cdt = new Cdt();
    }
    
}

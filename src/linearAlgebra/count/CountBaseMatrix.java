package linearAlgebra.count;

import java.io.BufferedWriter;
import java.io.IOException;
import linearAlgebra.BaseMatrix;
import linearAlgebra.BaseTensor;

public class CountBaseMatrix extends BaseMatrix{

	private int count;
	
	public CountBaseMatrix(){
		super();
		count = 0;
	}
	public CountBaseMatrix(int count){
		super();
		this.count = count;
	}
	public CountBaseMatrix(BaseTensor btLeft, BaseTensor btRight){
		super(btLeft, btRight);
	}
	public CountBaseMatrix(BaseTensor btLeft, BaseTensor btRight, int count){
		super(btLeft, btRight);
		this.count = count;
	}
	/*public CountBaseMatrix(String line){
		this();
		
		String[] entries = line.split("\t");
        count = Integer.parseInt(entries[0]);
		leftBaseTensor = new BaseTensor(entries[1]);
        rightBaseTensor = new BaseTensor(entries[2]);
	}
	*/
	
	public void add(int n){
		count += n;
	}
	public int getCount(){
		return count;
	}
    
    public CountBaseMatrix transpose(){
        return new CountBaseMatrix(rightBaseTensor, leftBaseTensor, count);
    }
    
    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write(count + "\t");
        getLeftBaseTensor().saveToWriter(out);
        out.write("\t");
        getRightBaseTensor().saveToWriter(out);
        out.write("\n");
    }
    
    public static CountBaseMatrix importFromString(String line){
        CountBaseMatrix bm = new CountBaseMatrix();
        String[] entries = line.split("\t");
        int count = Integer.parseInt(entries[0]);
        BaseTensor leftBt = BaseTensor.importFromString(entries[1]);
        BaseTensor rightBt = BaseTensor.importFromString(entries[2]);
        bm.add(count);
        bm.setLeftBaseTensor(leftBt);
        bm.setRightBaseTensor(rightBt);
        
        return bm;
    }
    
    /*@Override
    public int hashCode(){
        return 7 * leftBaseTensor.hashCode() + 13 * rightBaseTensor.hashCode();
    }
    */
    
    @Override
    public boolean equals(Object o){
        CountBaseMatrix bm = (CountBaseMatrix) o;
        return leftBaseTensor.equals(bm.getLeftBaseTensor()) && rightBaseTensor.equals(bm.getRightBaseTensor());
    }

    @Override
	public String toString(){
		String s = "" + count + "*" + super.toString();
		return s;
	}

}

package linearAlgebra;

import cdt.Helper;
import numberTypes.NNumber;
import space.TensorSpace;

public class BaseMatrix implements Comparable {

    protected BaseTensor leftBaseTensor, rightBaseTensor;
	
    //initialise with completely uncertain left and right base tensors
	public BaseMatrix(){
        leftBaseTensor = new BaseTensor();
        rightBaseTensor = new BaseTensor();
	}
	public BaseMatrix(BaseTensor leftBaseTensor, BaseTensor rightBaseTensor){
        this.leftBaseTensor = leftBaseTensor;
        this.rightBaseTensor = rightBaseTensor;
	}
	
    public void setLeftBaseTensor(BaseTensor bt){
        leftBaseTensor = bt;
    }
    
    public BaseTensor getLeftBaseTensor(){
        return leftBaseTensor;
    }
    
    public void setRightBaseTensor(BaseTensor bt){
        rightBaseTensor = bt;
    }
    
    public BaseTensor getRightBaseTensor(){
        return rightBaseTensor;
    }
    
	public void makeAbsolutelyUncertain(){
		//for(int i=0; i<modeDimensionArray.length; i++) modeDimensionArray[i] = -1;
        leftBaseTensor.makeAbsolutelyUncertain();
        rightBaseTensor.makeAbsolutelyUncertain();
	}
	
	public boolean isAbsolutelyUncertain(){
        return leftBaseTensor.isAbsolutelyUncertain() && rightBaseTensor.isAbsolutelyUncertain();
	}
    
    //only consider this pair of base tensors if they are equal
    public NNumber innerProductQuick(BaseMatrix bm){
        if(this.compareTo(bm) == 0){
            return NNumber.one();
        }else{
            //return NNumber.zero();
            return null;
        }
    }
    
    //also consider pairs of unequal base tensors if their inner product is not zero
    public NNumber innerProductSlow(BaseMatrix bm){

        //left
        NNumber ipLeft = leftBaseTensor.innerProductSlow(bm.getLeftBaseTensor());
        if(ipLeft.isZero()) return ipLeft;
        
        //right
        NNumber ipRight = rightBaseTensor.innerProductSlow(bm.getRightBaseTensor());
        if(ipRight.isZero()) return ipRight;
        
        return ipLeft.multiply(ipRight);
    }
    
    public NNumber innerProduct(BaseMatrix bm){
        if(TensorSpace.getSpeedOfBaseMatrixInnerProduct() == Helper.QUICK){
            return innerProductQuick(bm);
        }else{
            return innerProductSlow(bm);
        }
    }

    public BaseMatrix transpose(){
        return new BaseMatrix(rightBaseTensor, leftBaseTensor);
    }
    
    public boolean isDiagonal(){
        return leftBaseTensor.compareTo(rightBaseTensor) == 0;
    }
    
    
    /*
    //only consider this pair of base matrices if they are equal
    public NNumber innerProduct(BaseMatrix bm){
        if(this.compareTo(bm) == 0){
            return NNumber.one();
        }else{
            return NNumber.zero();
        }
    }
    */
	
    //compare by dimension
    @Override
    public int compareTo(Object o){
        //if(!(o instanceof BaseMatrix)) throw new IllegalArgumentException("[BaseMatrix] Bad comparison: Wrong type");
		BaseMatrix bm = (BaseMatrix) o;
        int compareLeftBaseTensors = leftBaseTensor.compareTo(bm.getLeftBaseTensor());
        if(compareLeftBaseTensors != 0){
            return compareLeftBaseTensors;
        }else{
            return rightBaseTensor.compareTo(bm.getRightBaseTensor());
        }
    }
	
	@Override
	public boolean equals(Object o){
		return /*(o instanceof BaseMatrix) &&*/ compareTo((BaseMatrix) o) == 0;
	}

    @Override
    public String toString(){
        return "[" + leftBaseTensor.toString() + "][" + rightBaseTensor.toString() + "]";
    }
    
    public String toPrettyString(){
        return leftBaseTensor.toPrettyString() + rightBaseTensor.toPrettyString();
    }
 
    public static BaseMatrix importFromString(String s){
        return null;
    }

}
package linearAlgebra;

import cdt.Helper;
import java.io.BufferedWriter;
import java.io.IOException;
import numberTypes.NNumber;
import space.TensorSpace;
import space.dep.ContextWord;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

//Generic base tensor responsible for marking the modes and dimensions
//dimension 0 means uncertain!
public class BaseTensor implements Comparable{

	protected int[] modeDimensionArray; //assigns a dimension number to each mode number. 0 is dummy vector.
    protected boolean[] modeIsDimensionCertainArray; //true iff dimension is > 0
    protected int[] modeAndOnlyCertainDimension; //if all is uncertain this is [0, 0], if one mode is certain this is [mode, dimension], otherwise this is null
	
	public BaseTensor(){
		int order = TensorSpace.getOrder();
		modeDimensionArray = new int[order]; //initially filled with zero
        modeIsDimensionCertainArray = new boolean[order]; //initially filled with false
        modeAndOnlyCertainDimension = new int[2]; //initially filled with zero
	}
    public BaseTensor(int[] modeDimensionArray, boolean[] modeIsDimensionCertainArray, int[] modeAndOnlyCertainDimension){
        this.modeDimensionArray = modeDimensionArray;
        this.modeIsDimensionCertainArray = modeIsDimensionCertainArray;
        this.modeAndOnlyCertainDimension = modeAndOnlyCertainDimension;
    }
	/*public BaseTensor(String s){
		this();
		
		String entries[] = s.split(" "); //mode entries
        for(String entry : entries){
            String[] modeAndDimension = entry.split(":");
            int mode = Integer.parseInt(modeAndDimension[0]);
            int dimension = Integer.parseInt(modeAndDimension[1]);
            setDimensionAtMode(mode, dimension);
        }
	}
	*/
	
	public int getOrder(){
		return modeDimensionArray.length;
	}
	
    //mode should be in [1;order]
	public int getDimensionAtMode(int mode){
		return modeDimensionArray[mode - 1];
	}
	
    //mode should be in [1;order]
    //certain dimensions should be in [1;dimensionality], uncertain dimension is 0
	public void setDimensionAtMode(int mode, int dimension){
		modeDimensionArray[mode - 1] = dimension;
        modeIsDimensionCertainArray[mode - 1] = (dimension != 0);
        
        //if not more than one mode has a certain dimension
        if(modeAndOnlyCertainDimension != null){
            //if no modes have a certain dimension
            if(modeAndOnlyCertainDimension[0] == 0 && modeAndOnlyCertainDimension[1] == 0){
                //store the given mode and dimension
                modeAndOnlyCertainDimension[0] = mode;
                modeAndOnlyCertainDimension[1] = dimension;
            //if the given mode is the certain one
            }else if(modeAndOnlyCertainDimension[0] == mode){
                //update the dimension
                modeAndOnlyCertainDimension[1] = dimension;
            //if one mode was certain up until now
            }else{
                //this array should no longer be used
                modeAndOnlyCertainDimension = null;
            }
        }
	}
    
    public void setModeDimensionArray(int[] modeDimensionArray){
        this.modeDimensionArray = modeDimensionArray;
    }
    public int[] getModeDimensionArray(){
        return modeDimensionArray;
    }
    
    public void setModeIsDimensionCertainArray(boolean[] modeIsDimensionCertainArray){
        this.modeIsDimensionCertainArray = modeIsDimensionCertainArray;
    }
    public boolean[] getModeIsDimensionCertainArray(){
        return modeIsDimensionCertainArray;
    }
    
    public void setModeandOnlyCertainDimension(int[] modeAndOnlyCertainDimension){
        this.modeAndOnlyCertainDimension = modeAndOnlyCertainDimension;
    }
    public int[] getModeAndOnlyCertainDimension(){
        return modeAndOnlyCertainDimension;
    }
    
    //mode should be in [1;order]
    public boolean isDimensionCertainAtMode(int mode){
        return modeIsDimensionCertainArray[mode - 1];
    }
	
	
	public void makeAbsolutelyUncertain(){
		for(int m=1; m<=getOrder(); m++){
            modeDimensionArray[m - 1] = 0;
            modeIsDimensionCertainArray[m - 1] = false;
            modeAndOnlyCertainDimension = new int[2];
        }
	}
	
	public boolean isAbsolutelyUncertain(){
		for(int m=1; m<=getOrder(); m++){
			if(modeDimensionArray[m - 1] != 0) return false;
		}
		return true;
	}
	
    
    //only consider this pair of base tensors if they are equal
    public NNumber innerProductQuick(BaseTensor bt){
        if(this.compareTo(bt) == 0){
            return NNumber.one();
        }else{
            //return NNumber.zero();
            return null;
        }
    }
    
    //also consider pairs of unequal base tensors if their inner product is not zero
    //instead of returning 0 this returns null
    public NNumber innerProductSlow(BaseTensor bt){ //TODO: speed this up using modeOnly...
//if(bt == null) System.out.println("!!!");
        int amountOfModesWithAbsoluteUncertainty = 0;
        
        //if both tensors have at most one certain mode
        int[] givenModeAndOnlyCertainDimension = bt.getModeAndOnlyCertainDimension();
        if(modeAndOnlyCertainDimension != null && givenModeAndOnlyCertainDimension != null){
            //if first tensor is totally uncertain
            if(modeAndOnlyCertainDimension[0] == 0){
                //if second tensor is totally uncertain
                if(givenModeAndOnlyCertainDimension[0] == 0){
                    amountOfModesWithAbsoluteUncertainty = 0;
                //if second tensor has one certain mode
                }else{
                    amountOfModesWithAbsoluteUncertainty = 1;
                }
            //if first tensor has one certain mode
            }else{
                //if second tensor is totally uncertain
                if(givenModeAndOnlyCertainDimension[0] == 0){
                    amountOfModesWithAbsoluteUncertainty = 1;
                //if second tensor has one certain mode
                }else{
                    //if first and second tensors are certain in the same mode
                    if(modeAndOnlyCertainDimension[0] == givenModeAndOnlyCertainDimension[0]){
                        //if first tensor's dimension is the same as second tensor's dimension
                        if(modeAndOnlyCertainDimension[1] == givenModeAndOnlyCertainDimension[1]){
                            amountOfModesWithAbsoluteUncertainty = 0;
                        //if first tensor's dimension in not the same as second tensor's dimension
                        }else{
                            return null;
                        }
                    //if first and second tensors are certain in different modes
                    }else{
                        amountOfModesWithAbsoluteUncertainty = 2;
                    }
                }
            }
            
            
            
            /*if(modeAndOnlyCertainDimension[0] == givenModeAndOnlyCertainDimension[0]){
                if(modeAndOnlyCertainDimension[1] == givenModeAndOnlyCertainDimension[1]){
                    amountOfModesWithAbsoluteUncertainty = 0;
                }else if(modeAndOnlyCertainDimension[1] < givenModeAndOnlyCertainDimension[1]){
                    return -1;
                }else{
                    return 1;
                }
            }else{
                amountOfModesWithAbsoluteUncertainty = 2;
            }
            */
            
        }else{
            
            //go through all modes
            for(int m=1; m<=getOrder(); m++){
                boolean thisIsDimensionCertainAtMode = isDimensionCertainAtMode(m);
                boolean givenIsDimensionCertainAtMode = bt.isDimensionCertainAtMode(m);
                //(1) if both dimension are uncertain, do nothing
                if(thisIsDimensionCertainAtMode || givenIsDimensionCertainAtMode){
                    //(2) if exactly one dimension is uncertain
                    if(thisIsDimensionCertainAtMode != givenIsDimensionCertainAtMode){
                        amountOfModesWithAbsoluteUncertainty++;
                    //(3) if both dimensions are certain
                    }else{
                        int thisDimension = this.getDimensionAtMode(m);
                        int givenDimension = bt.getDimensionAtMode(m);
                        if(thisDimension != givenDimension){
                            return null; //instead of NNumber.zero()
                        }
                    }
                }
            }
        }
        
        return NNumber.createDimensionalitySqrtToThePowerOf(-amountOfModesWithAbsoluteUncertainty);
    }
    
    public NNumber innerProduct(BaseTensor bt){
        if(TensorSpace.getSpeedOfBaseTensorInnerProduct() == Helper.QUICK){
            return innerProductQuick(bt);
        }else{
            return innerProductSlow(bt);
        }
    }
	
	//sort by dimensions
	@Override
	public int compareTo(Object o){
		//if(!(o instanceof BaseTensor)) throw new IllegalArgumentException("[BaseTensor] Bad comparison: Wrong type");
		BaseTensor bt = (BaseTensor) o;
		//if(this.getOrder() != bt.getOrder()) throw new IllegalArgumentException("[BaseTensor] Bad comparison: Unequal amount of modes");
        
        //if both tensors have at most one certain mode
        int[] givenModeAndOnlyCertainDimension = bt.getModeAndOnlyCertainDimension();
        if(modeAndOnlyCertainDimension != null && givenModeAndOnlyCertainDimension != null){
            if(modeAndOnlyCertainDimension[0] == givenModeAndOnlyCertainDimension[0]){
                if(modeAndOnlyCertainDimension[1] == givenModeAndOnlyCertainDimension[1]){
                    //System.out.println(toString() + "\t" + bt.toString() + "\ta");
                    return 0;
                }else if(modeAndOnlyCertainDimension[1] < givenModeAndOnlyCertainDimension[1]){
                    //System.out.println(toString() + "\t" + bt.toString() + "\tb");
                    return -1;
                }else{
                    //System.out.println(toString() + "\t" + bt.toString() + "\tc");
                    return 1;
                }
            }else if(modeAndOnlyCertainDimension[0] < givenModeAndOnlyCertainDimension[0]){
                //System.out.println(toString() + "\t" + bt.toString() + "\td");
                return -1;
            }else{
                //System.out.println(toString() + "\t" + bt.toString() + "\te");
                return 1;
            }
            
        }else{
        
            for(int m=1; m<=getOrder(); m++){
                if(isDimensionCertainAtMode(m) || bt.isDimensionCertainAtMode(m)){
                    int thisDimension = this.getDimensionAtMode(m);
                    int givenDimension = bt.getDimensionAtMode(m);
                    if(thisDimension < givenDimension){
                        //System.out.println(toString() + "\t" + bt.toString() + "\tf");
                        return -1;
                    }
                    if(thisDimension > givenDimension){
                        //System.out.println(toString() + "\t" + bt.toString() + "\tg");
                        return 1;
                    }
                }
            }
        }
        
        //System.out.printlnln("this: [" + givenModeAndOnlyCertainDimension[0] + ", " + givenModeAndOnlyCertainDimension[1] + "], given: [" + givenModeAndOnlyCertainDimension[0] + ", " + givenModeAndOnlyCertainDimension[1] + "]");
		
        //System.out.println(toString() + "\t" + bt.toString() + "\th");
		return 0;
	}
    
    @Override
    public String toString(){
        String s = "";
        for(int m=1; m<=getOrder(); m++){
            if(isDimensionCertainAtMode(m)){
                int d = getDimensionAtMode(m);
                s += m + ":" + d + " ";
            }
		}
        return s;
    }
    
    public String toPrettyString(){
        String s = "";
        for(int m=1; m<=getOrder(); m++){
            if(isDimensionCertainAtMode(m)){
                int d = getDimensionAtMode(m);
                s += TensorSpace.getModeObject(m).getName() + ":" + ((ContextWord) TensorSpace.getModeObject(m).getDimensionObject(d)).getWord() + " ";
                //s += m + ":" + d + " ";
            }
		}
        return s;
    }
    
	@Override
	public boolean equals(Object o){
		return /*(o instanceof BaseTensor) &&*/ compareTo((BaseTensor) o) == 0;
	}

    public static BaseTensor importFromString(String s){
        BaseTensor bt = new BaseTensor();
		String entries[] = s.split(" "); //mode entries
        for(String entry : entries){
            String[] modeAndDimension = entry.split(":");
            int mode = Integer.parseInt(modeAndDimension[0]);
            int dimension = Integer.parseInt(modeAndDimension[1]);
            bt.setDimensionAtMode(mode, dimension);
        }
        return bt;
	}
    
    public void saveToWriter(BufferedWriter out) throws IOException{
        String s = "";
        for(int m=1; m<=getOrder(); m++){
            int d = getDimensionAtMode(m);
            if(d > 0){
                if(Helper.prettyPrint){
                    DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);
                    s += drc.getName() + ":" + drc.getContextWord(d).getWord() + " ";
                }else{
                    s += m + ":" + d + " ";
                }
            }
        }
        out.write(s);
    }
	
    public static void main(String[] args){
        int[] x = new int[5];
        boolean[] y = new boolean[5];
        for(int i=0; i<5; i++){
            System.out.println("x[" + i + "] = " + x[i]);
            System.out.println("y[" + i + "] = " + y[i]);
        }
    }
    
}

package space;

import cdt.Helper;
import java.io.File;

public class TensorSpace {
    
    protected static Mode[] modeObjectsArray = null;
    protected static String name = null;
    protected static Boolean hasDimensions = null;

    /*public TensorSpace(){
        modeObjectsArray = null;
        name = null;
    }
    */
    
    
    public static void setName(String s){
        name = s;
    }
    public static String getName(){
        return name;
    }
    
    //modeIndex should be in [1;order]
    public static void setModeObject(int modeIndex, Mode modeObject){
        //#modeObjectsArray[modeIndex - 1] = modeObject;
        modeObjectsArray[modeIndex] = modeObject;
    }
    //modeIndex should be in [1;order]
    public static Mode getModeObject(int modeIndex){
        //#return modeObjectsArray[modeIndex - 1];
        return modeObjectsArray[modeIndex];
    }
    
    //warning: this deletes previously existing modeObjectsArray
    public static void setOrder(int order){
        //#modeObjectsArray = new Mode[order];
        modeObjectsArray = new Mode[order + 1];
    }
    public static int getOrder(){
        //#return modeObjectsArray.length;
        return modeObjectsArray.length - 1;
    }
    
    public static boolean hasDimensions(){
        if(hasDimensions != null){
            return hasDimensions;
        }else if(modeObjectsArray == null){
            hasDimensions = false;
            return false;
        }else{
            for(int m=1; m<=getOrder(); m++){
                Mode mode = getModeObject(m);
                if(mode == null){
                    hasDimensions = false;
                    return false;
                }else{
                    Dimension[] dimensionObjectsArray = mode.getDimensionObjectsArray();
                    if(dimensionObjectsArray == null){
                        hasDimensions = false;
                        return false;
                    }else{
                        //System.out.println("Mode " + m); //DEBUG
                        for(int d=1; d<=getDimensionality(); d++){
                            Dimension dimension = dimensionObjectsArray[d];
                            if(dimension == null){
                                hasDimensions = false;
                                return false;
                            }else{
                                int dimensionIndex = dimension.getDimensionIndex();
                                if(dimensionIndex != -1){
                                    //System.out.println("Dimension " + dimensionIndex); //DEBUG
                                    hasDimensions = true;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        hasDimensions = false;
        return false;
    }
    //public static void setHasDimensions(boolean b){
        //hasDimensions = b;
    //}
    
    //modeIndex should be in [1;order]
    //certain dimensions should be in [1;dimensionality], uncertain dimension is 0
    public static void setDimensionObject(int modeIndex, int dimensionIndex, Dimension d){
        modeObjectsArray[modeIndex - 1].setDimensionObject(dimensionIndex, d);
    }
    //modeIndex should be in [1;order]
    //certain dimensions should be in [1;dimensionality], uncertain dimension is 0
    public static Dimension getDimensionObject(int mode, int dimension){
        return modeObjectsArray[mode - 1].getDimensionObject(dimension);
    }

    
    protected static int dimensionality;
	protected static double dimensionalityLog, dimensionalitySqrt, dimensionalitySqrtLog;
	protected static int numberType;
    
	public static void setDimensionality(int n){
		dimensionality = n;
		dimensionalityLog = Math.log(n);
		dimensionalitySqrt = Math.sqrt(dimensionality);
		dimensionalitySqrtLog = dimensionalityLog / 2;
	}
	public static int getDimensionality(){
		return dimensionality;
	}
	public static double getDimensionalityLog(){
		return dimensionalityLog;
	}
	public static double getDimensionalitySqrt(){
		return dimensionalitySqrt;
	}
	public static double getDimensionalitySqrtLog(){
		return dimensionalitySqrtLog;
	}
    
	public static void setNumberType(int nt){
		numberType = nt;
	}
	public static int getNumberType(){
		return numberType;
	}


    protected static final int speedOfNCustomBaseFloatAddition = Helper.SLOW;
    protected static final int speedOfBaseTensorInnerProduct = Helper.SLOW;
    protected static final int speedOfValueTensorInnerProduct = Helper.SLOW;
    protected static final int speedOfBaseMatrixInnerProduct = Helper.SLOW;
    protected static final int speedOfValueMatrixInnerProduct = Helper.SLOW;

    public static int getSpeedOfNCustomBaseFloatAddition(){
        return speedOfNCustomBaseFloatAddition;
    }
    
    public static int getSpeedOfBaseTensorInnerProduct(){
        return speedOfBaseTensorInnerProduct;
    }
    
    public static int getSpeedOfValueTensorInnerProduct(){
        return speedOfValueTensorInnerProduct;
    }
    
    public static int getSpeedOfBaseMatrixInnerProduct(){
        return speedOfBaseMatrixInnerProduct;
    }
    
    public static int getSpeedOfValueMatrixInnerProduct(){
        return speedOfValueMatrixInnerProduct;
    }
    
    public static File projectFolder = null;
    
    public static void setProjectFolder(File f){
        projectFolder = f;
    }
    public static File getProjectFolder(){
        return projectFolder;
    }
    
}
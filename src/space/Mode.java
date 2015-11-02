package space;

public class Mode {
    
    protected Dimension[] dimensionObjectsArray;
    protected int modeIndex;
    protected String name;

    public Mode(){
        dimensionObjectsArray = null;
        modeIndex = -1;
        name = null;
    }
    public Mode(int dimensionality){
        this();
        setDimensionality(dimensionality);
    }
    
    
    public Dimension[] getDimensionObjectsArray(){
        return dimensionObjectsArray;
    }
    
    public void setModeIndex(int modeIndex){
        //#this.modeIndex = modeIndex - 1;
        this.modeIndex = modeIndex;
    }
    public int getModeIndex(){
        //#return modeIndex + 1;
        return modeIndex;
    }
    
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    
    //warning: this deletes previously existing dimensionObjectsArray
    public void setDimensionality(int dimensionality){
        //#dimensionObjectsArray = new Dimension[dimensionality];
        dimensionObjectsArray = new Dimension[dimensionality + 1];
    }
    public int getDimensionality(){
        if(dimensionObjectsArray != null){
            //#return dimensionObjectsArray.length;
            return dimensionObjectsArray.length - 1;
        }else{
            return TensorSpace.getDimensionality();
        }
    }
        
    //dimensionIndex should be in [1;dimensionality]
    public void setDimensionObject(int dimensionIndex, Dimension d){
        //#dimensionObjectsArray[dimensionIndex - 1] = d;
        dimensionObjectsArray[dimensionIndex] = d;
    }
    
    //dimensionIndex should be in [1;dimensionality]
    public Dimension getDimensionObject(int dimensionIndex){
        //#return dimensionObjectsArray[dimensionIndex - 1];
        return dimensionObjectsArray[dimensionIndex];
    }

    @Override
    public String toString(){
        String s = "";
        for(int i=1; i<=dimensionObjectsArray.length; i++){
            s += "mode:" + i + "\n";
            s += dimensionObjectsArray[i].toString() + "\n";
        }
        return s;
    }
    
}

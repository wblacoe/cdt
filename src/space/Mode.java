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
        dimensionObjectsArray = new Dimension[dimensionality];
    }
    
    
    public Dimension[] getDimensionObjectsArray(){
        return dimensionObjectsArray;
    }
    
    public void setModeIndex(int modeIndex){
        this.modeIndex = modeIndex - 1;
    }
    public int getModeIndex(){
        return modeIndex + 1;
    }
    
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    
    //warning: this deletes previously existing dimensionObjectsArray
    public void setDimensionality(int dimensionality){
        dimensionObjectsArray = new Dimension[dimensionality];
    }
    public int getDimensionality(){
        if(dimensionObjectsArray != null){
            return dimensionObjectsArray.length;
        }else{
            return TensorSpace.getDimensionality();
        }
    }
        
    //dimensionIndex should be in [1;dimensionality]
    public void setDimensionObject(int dimensionIndex, Dimension d){
        dimensionObjectsArray[dimensionIndex - 1] = d;
    }
    
    //dimensionIndex should be in [1;dimensionality]
    public Dimension getDimensionObject(int dimensionIndex){
        return dimensionObjectsArray[dimensionIndex - 1];
    }

    @Override
    public String toString(){
        String s = "";
        for(int i=0; i<dimensionObjectsArray.length; i++){
            s += "mode:" + i + "\n";
            s += dimensionObjectsArray[i].toString() + "\n";
        }
        return s;
    }
    
}

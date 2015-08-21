package space;

import java.util.regex.Pattern;

/**
 *
 * @author wblacoe
 */
public class Dimension {

    protected static final Pattern dimensionPattern = Pattern.compile("<dimension index=\\\"(.*?)\\\">(.*?)</dimension>");
    protected int dimensionIndex;

    public Dimension(){
        dimensionIndex = -1;
    }
    public Dimension(int dimensionIndex){
        this.dimensionIndex = dimensionIndex;
    }
    
    public void setDimensionIndex(int dimensionIndex){
        this.dimensionIndex = dimensionIndex;
    }
    
    public int getDimensionIndex(){
        return dimensionIndex;
    }
    
    @Override
    public String toString(){
        return "dimension:" + dimensionIndex;
    }
    
}

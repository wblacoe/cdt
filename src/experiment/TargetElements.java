package experiment;

/**
 *
 * @author wblacoe
 */
public class TargetElements {

    protected static TargetElement[] targetElementsArray = new TargetElement[0];
    
    /*public TargetElements(int amountOfTargetElements){
        targetElementsArray = new TargetElement[amountOfTargetElements];
    }
    */

    public static void setTargetElement(int index, TargetElement te){
        targetElementsArray[index] = te;
    }
    
    public static TargetElement getTargetElement(int index){
        if(index >= getSize()){
            return null;
        }else{
            return targetElementsArray[index];
        }
    }
    
    public static void setSize(int size){
        targetElementsArray = new TargetElement[size];
    }
    public static int getSize(){
        return targetElementsArray.length;
    }
    
    public static void clear(){
        setSize(0);
    }
    
}

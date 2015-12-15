package corpus;

/**
 *
 * @author wblacoe
 */
public class Corpus {

    private static String name;
    private static String format; //dep, srl, ...
    private static String folderName; //relative to the project folder
    private static int amountOfDocuments = -1; //process all documents in corpus files
    private static int minAmountOfNeighboursWhenCounting = 1; //consider dep neighbourhoods with at least one neighbour
    private static long totalWordCount;
    
    
    public static void setName(String s){
        name = s;
    }
    public static String getName(){
        return name;
    }
    
    public static void setFormat(String s){
        format = s;
    }
    public static String getFormat(){
        return format;
    }

    public static void setFolderName(String s){
        folderName = s;
    }
    public static String getFolderName(){
        return folderName;
    }
    
    public static void setAmountOfDocuments(int n){
        amountOfDocuments = n;
    }
    public static int getAmountOfDocuments(){
        return amountOfDocuments;
    }

    public static void setMinAmountOfNeighboursWhenCounting(int n){
        minAmountOfNeighboursWhenCounting = n;
    }
    public static int getMinAmountOfNeighboursWhenCounting(){
        return minAmountOfNeighboursWhenCounting;
    }
    
    /*public static void setTotalWordCount(long n){
        totalWordCount = n;
    }
    public static long getTotalWordCount(){
        return totalWordCount;
    }
    */
    
    public static String getString(){
        //String s = "Corpus \"" + name + "\":\ntotal word count=" + totalWordCount;
        String s = name;
        return s;
    }
    
}

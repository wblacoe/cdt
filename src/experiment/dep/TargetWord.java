package experiment.dep;

import java.io.BufferedReader;
import java.io.IOException;
import experiment.TargetElement;
import java.io.BufferedWriter;
import linearAlgebra.Matrix;

/**
 *
 * @author wblacoe
 */
public class TargetWord extends TargetElement {
    
    private String word;
    //private int marginalCount;
    private Matrix representation;
    
    public TargetWord(int index){
        super(index);
        word = null;
        //marginalCount = -1;
        representation = null;
    }
    public TargetWord(int index, String word){
        super(index);
        this.word = word;
    }
    
    public void setWord(String word){
        this.word = word;
    }
    public String getWord(){
        return word;
    }
    
    /*public void setMarginalCount(int marginalCount){
        this.marginalCount = marginalCount;
    }
    public int getMarginalCount(){
        return marginalCount;
    }
    */
    
    public void setRepresentation(Matrix representation){
        this.representation = representation;
    }
    public Matrix getRepresentation(){
        return representation;
    }
    public boolean hasRepresentation(){
        return representation != null;
    }
    public void removeRepresentation(){
        setRepresentation(null);
    }
    
    public void importFromString(String s){
        //TODO
    }
    
    public void importFromReader(BufferedReader in) throws IOException{
        String s = in.readLine();
        importFromString(s);
    }
    
    @Override
    public String toString(){
        return word;
    }
    
    @Override
    public int hashCode(){
        return word.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        return word.equals(((TargetWord) o).getWord());
    }
    
    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<targetelement index=\"" + getIndex() + "\">" + getWord() + "</targetelement>\n");
    }
}

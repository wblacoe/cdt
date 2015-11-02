package linearAlgebra;

import cdt.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.LinearCombinationMatrix;
import linearAlgebra.value.ValueMatrix;

public class Matrix {

    protected static Pattern matrixPattern = Pattern.compile("<matrix name=\\\"(.*?)\\\" type=\\\"(.*?)\\\" cardinality=\\\"(.*?)\\\">");
    
    protected String name;
    
    public Matrix(){
        name = "?";
    }
    
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    
    public int getCardinality(){
        return 0;
    }
    
    public void saveToWriter(BufferedWriter out) throws IOException{}
    
    public boolean isZero(){
        return true;
    }
    
    public static Matrix importFromReader(BufferedReader in) throws IOException{
        Matrix m = null;
        
        String line;
        while((line = in.readLine()) != null){
            
            if(line.startsWith("<matrix")){
                Matcher matcher = matrixPattern.matcher(line);
                if(matcher.find()){
                    String matrixName = matcher.group(1);
                    String matrixType = matcher.group(2);
                    if(matrixType.equals("count")){
                        m = CountMatrix.importFromReader(in);
                    }else if(matrixType.equals("value")){
                        int cardinality = Integer.parseInt(matcher.group(3));
                        m = ValueMatrix.importFromReader(in, cardinality);
                    }else if(matrixType.equals("linearcombination")){
						m = LinearCombinationMatrix.importFromReader(in);
					}
                    if(m != null) m.setName(matrixName);
                    //System.out.println(line + ", name: " + matcher.group(1) + ", card: " + matcher.group(3) + ", type: " + matcher.group(2)); //DEBUG
                    break;
                }
            }
            
        }

        return m;
    }
    
    public void saveToFile(File file){
        try{
            BufferedWriter out = Helper.getFileWriter(file);
            saveToWriter(out);
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}

package innerProduct;

import cdt.Helper;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Set;
import linearAlgebra.value.ValueMatrix;
import numberTypes.NNumber;

/**
 *
 * @author wblacoe
 */
public class InnerProductsCache {

    private HashMap<String, Integer> wordTargetWordIndexMap;
    private HashMap<SimpleEntry<Integer, Integer>, NNumber> innerProducts;
    private HashMap<String, String> hyperparameters;
    
    public InnerProductsCache(){
        wordTargetWordIndexMap = new HashMap<>();
        for(int i=0; i<Vocabulary.getSize(); i++){
            String word = Vocabulary.getTargetWord(i).getWord();
            wordTargetWordIndexMap.put(word, i);
        }
        
        innerProducts = new HashMap<>();
        hyperparameters = new HashMap<>();
    }
    
    
    public void setHyperParameter(String key, String value){
        hyperparameters.put(key, value);
    }
    
    public String getHyperParameter(String key){
        return hyperparameters.get(key);
    }
    
    public Set<SimpleEntry<Integer, Integer>> getKeys(){
        return innerProducts.keySet();
    }
    
    /*public void setinnerProductsFile(File innerProductsFile){
        this.innerProductsFile = innerProductsFile;
    }
    
    public File getInnerProductsFile(){
        return innerProductsFile;
    }
    */
    
    
    public void setInnerProduct(int twIndex1, int twIndex2, NNumber ip){
        int i1, i2;
        if(twIndex1 <= twIndex2){
            i1 = twIndex1;
            i2 = twIndex2;
        }else{
            i1 = twIndex2;
            i2 = twIndex1;
        }

        SimpleEntry<Integer, Integer> indexPair = new SimpleEntry<>(i1, i2);
        innerProducts.put(indexPair, ip);
    }
    
    public void setInnerProduct(String word1, String word2, NNumber ip){
        setInnerProduct(wordTargetWordIndexMap.get(word1), wordTargetWordIndexMap.get(word2), ip);
    }
    
    public NNumber getInnerProduct(int twIndex1, int twIndex2, boolean computeIfNull){
        int i1, i2;
        if(twIndex1 <= twIndex2){
            i1 = twIndex1;
            i2 = twIndex2;
        }else{
            i1 = twIndex2;
            i2 = twIndex1;
        }
        
        SimpleEntry<Integer, Integer> indexPair = new SimpleEntry<>(i1, i2);
        NNumber existingIp = innerProducts.get(indexPair);
        if(existingIp == null){
            if(!computeIfNull) return null;
            TargetWord tw1 = Vocabulary.getTargetWord(i1);
            TargetWord tw2 = Vocabulary.getTargetWord(i2);
            ValueMatrix m1 = (ValueMatrix) tw1.getRepresentation();
            ValueMatrix m2 = (ValueMatrix) tw2.getRepresentation();
            existingIp = m1.innerProduct(m2);
            setInnerProduct(i1, i2, existingIp);
        }
        return existingIp;

    }
    
    public NNumber getInnerProduct(String word1, String word2, boolean computeIfNull){
        String key = word1.compareTo(word2) <= 0 ? word1 + "\t" + word2 : word2 + "\t" + word1;
        int twIndex1 = wordTargetWordIndexMap.get(word1);
        int twIndex2 = wordTargetWordIndexMap.get(word2);
        return getInnerProduct(twIndex1, twIndex2, computeIfNull);
    }
    
    private void importHyperParameters(BufferedReader in) throws IOException{
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</hyperparameters>")) break;
            
            String[] entries = line.split("\t");
            String key = entries[0];
            String value = entries[1];
            setHyperParameter(key, value);
        }
    }
    
    private void importInnerProducts(BufferedReader in) throws IOException{
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</innerproducts>")) break;
            
            String[] entries = line.split("\t");
            Integer twIndex1 = Integer.parseInt(entries[0]);
            Integer twIndex2 = Integer.parseInt(entries[1]);
            float ip = Float.parseFloat(entries[2]);
            setInnerProduct(twIndex1, twIndex2, NNumber.create(ip));
        }
    }
    
    //assumes that number type has been defined
    public void importFromFile(File innerProductsFile){
        try{
            if(innerProductsFile != null && innerProductsFile.exists()){
                BufferedReader in = Helper.getFileReader(innerProductsFile);

                String line;
                while((line = in.readLine()) != null){
                    if(line.startsWith("<hyperparameters")){
                        importHyperParameters(in);
                    }else if(line.startsWith("<innerproducts")){
                        importInnerProducts(in);
                    }
                }

                in.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void saveToFile(File innerProductsFile){
        try{
            if(innerProductsFile != null){
                BufferedWriter out = Helper.getFileWriter(innerProductsFile);
                    out.write("<hyperparameters>\n");
                    for(String key : hyperparameters.keySet()){
                        out.write(key + "\t" + hyperparameters.get(key) + "\n");
                    }
                    out.write("</hyperparameters>\n");
                    
                    out.write("<innerproducts>\n");
                    for(SimpleEntry<Integer, Integer> ipPair : getKeys()){
                        int twIndex1 = ipPair.getKey();
                        int twIndex2 = ipPair.getValue();
                        NNumber ip = getInnerProduct(twIndex1, twIndex2, false);
                        if(ip != null){
                            out.write(twIndex1 + "\t" + twIndex2 + "\t" + ip.getDoubleValue() + "\n");
                        }
                    }
                    out.write("</innerproducts>");
                out.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void integrate(InnerProductsCache ipc){
        for(SimpleEntry<Integer, Integer> ipPair : ipc.getKeys()){
            int twIndex1 = ipPair.getKey();
            int twIndex2 = ipPair.getValue();
            NNumber ip = ipc.getInnerProduct(twIndex1, twIndex2, false);
            if(ip != null){
                setInnerProduct(twIndex1, twIndex2, ip);
            }
        }
    }
    
    public InnerProductsCache getCopy(){
        InnerProductsCache ipc = new InnerProductsCache();
        
        for(String key : hyperparameters.keySet()){
            ipc.setHyperParameter(key, getHyperParameter(key));
        }
     
        for(SimpleEntry<Integer, Integer> ipPair : getKeys()){
            int twIndex1 = ipPair.getKey();
            int twIndex2 = ipPair.getValue();
            NNumber ip = ipc.getInnerProduct(twIndex1, twIndex2, false);
            ipc.setInnerProduct(twIndex1, twIndex2, ip);
        }
        
        return ipc;
    }
    
}

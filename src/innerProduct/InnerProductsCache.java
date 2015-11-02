package innerProduct;

import cdt.Helper;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
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
    //private HashMap<SimpleEntry<Integer, Integer>, NNumber> innerProducts;
    private HashMap<String, NNumber> innerProducts;
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
    
    /*public Set<SimpleEntry<Integer, Integer>> getKeys(){
        return innerProducts.keySet();
    }
    */
    
    public Set<String> getKeys(){
        return innerProducts.keySet();
    }
    
    /*public void setInnerProduct(int twIndex1, int twIndex2, NNumber ip){
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
    */
    
    public void setInnerProduct(String word1, String word2, NNumber ip){
        //setInnerProduct(wordTargetWordIndexMap.get(word1), wordTargetWordIndexMap.get(word2), ip);
        
        String key = (word1.compareTo(word2) < 0 ? word1 + "_#_" + word2 : word2 + "_#_" + word1);
        innerProducts.put(key, ip);
    }
    
    /*public NNumber getInnerProduct(int twIndex1, int twIndex2, boolean computeIfNull){
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
        if(existingIp != null){
            System.out.println("using existing ip <" + Vocabulary.getTargetWord(i1).getWord() + ", " + Vocabulary.getTargetWord(i2).getWord() + "> = " + existingIp);
            return existingIp;
        }else{
            if(!computeIfNull) return null;
            TargetWord tw1 = Vocabulary.getTargetWord(i1);
            if(tw1 == null) return null;
            TargetWord tw2 = Vocabulary.getTargetWord(i2);
            if(tw2 == null) return null;
            ValueMatrix m1 = (ValueMatrix) tw1.getLexicalRepresentation();
            if(m1 == null) return null;
            ValueMatrix m2 = (ValueMatrix) tw2.getLexicalRepresentation();
            if(m2 == null) return null;
            NNumber ip = m1.innerProduct(m2);
            if(ip == null) return null;
            setInnerProduct(i1, i2, ip);
            
            System.out.println("computing new ip <" + Vocabulary.getTargetWord(i1).getWord() + ", " + Vocabulary.getTargetWord(i2).getWord() + "> = " + ip);
            return ip;
        }
    }
    */
    
    public synchronized NNumber getInnerProduct(String word1, String word2, boolean computeIfNull){
        String key = (word1.compareTo(word2) < 0 ? word1 + "_#_" + word2 : word2 + "_#_" + word1);
        NNumber ip = innerProducts.get(key);
        
        
        if(ip == null && computeIfNull){
            ValueMatrix m1 = (ValueMatrix) Vocabulary.getTargetWord(word1).getLexicalRepresentation();
            if(m1 == null) return null;
            ValueMatrix m2 = (ValueMatrix) Vocabulary.getTargetWord(word2).getLexicalRepresentation();
            if(m2 == null) return null;
            ip = m1.innerProduct(m2);
			//System.out.println("tr(" + word1 + ")=" + m1.trace().getDoubleValue() + ", tr(" + word2 + ")=" + m2.trace().getDoubleValue() + ", <" + word1 + ", " + word2 + "> = " + ip.getDoubleValue()); //DEBUG
            
            innerProducts.put(key, ip);
            //System.out.println("computing new ip <" + word1 + ", " + word2 + "> = " + ip); //DEBUG
            return ip;
        }else{
            //System.out.println("using existing ip <" + word1 + ", " + word2 + "> = " + ip); //DEBUG
            return ip;
        }
        
        //int twIndex1 = wordTargetWordIndexMap.get(word1);
        //int twIndex2 = wordTargetWordIndexMap.get(word2);
        //return getInnerProduct(twIndex1, twIndex2, computeIfNull);
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
    
    private int importInnerProducts(BufferedReader in) throws IOException{
        int counter = 0;
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</innerproducts>")) break;
            
            String[] entries = line.split("\t");
            if(entries.length == 2){
				String key = entries[0];
				float ip = Float.parseFloat(entries[1]);
				innerProducts.put(key, NNumber.create(ip));
				counter++;
			}
        }
        
        return counter;
    }
    
    //assumes that number type has been defined
    public void importFromFile(File innerProductsFile){
        Helper.report("[InnerProductsCache] Importing inner products from file...");
        
        int counter = 0;
        try{
            if(innerProductsFile != null && innerProductsFile.exists()){
                BufferedReader in = Helper.getFileReader(innerProductsFile);

                String line;
                while((line = in.readLine()) != null){
                    if(line.startsWith("<hyperparameters")){
                        importHyperParameters(in);
                    }else if(line.startsWith("<innerproducts")){
                        counter = importInnerProducts(in);
                    }
                }

                in.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Helper.report("[InnerProductsCache] ...Finished importing " + counter + " inner products from file");
    }
    
    public void saveToFile(File innerProductsFile){
        Helper.report("[InnerProductsCache] Saving inner products to file...");
        
        int counter = 0;
        try{
            if(innerProductsFile != null){
                BufferedWriter out = Helper.getFileWriter(innerProductsFile);
                    out.write("<hyperparameters>\n");
                    for(String key : hyperparameters.keySet()){
                        out.write(key + "\t" + hyperparameters.get(key) + "\n");
                    }
                    out.write("</hyperparameters>\n");
                    
                    out.write("<innerproducts>\n");
                    //for(SimpleEntry<Integer, Integer> ipPair : getKeys()){
                    for(String key : getKeys()){
                        //int twIndex1 = ipPair.getKey();
                        //int twIndex2 = ipPair.getValue();
                        //NNumber ip = getInnerProduct(twIndex1, twIndex2, false);
                        NNumber ip = innerProducts.get(key);
                        if(ip != null){
                            //out.write(twIndex1 + "\t" + twIndex2 + "\t" + ip.getDoubleValue() + "\n");
                            out.write(key + "\t" + ip.getDoubleValue() + "\n");
                            counter++;
                        }
                    }
                    out.write("</innerproducts>");
                out.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        Helper.report("[InnerProductsCache] ...Finished saving " + counter + " inner products to file");
    }
    
    public void integrate(InnerProductsCache ipc){
        //for(SimpleEntry<Integer, Integer> ipPair : ipc.getKeys()){
        for(String key : ipc.getKeys()){
            //int twIndex1 = ipPair.getKey();
            //int twIndex2 = ipPair.getValue();
            //NNumber ip = ipc.getInnerProduct(twIndex1, twIndex2, false);
            NNumber ip = ipc.innerProducts.get(key);
            if(ip != null){
                //setInnerProduct(twIndex1, twIndex2, ip);
                innerProducts.put(key, ip);
            }
        }
    }
    
    public InnerProductsCache getCopy(){
        InnerProductsCache ipc = new InnerProductsCache();
        
        for(String key : hyperparameters.keySet()){
            ipc.setHyperParameter(key, getHyperParameter(key));
        }
     
        //for(SimpleEntry<Integer, Integer> ipPair : getKeys()){
        for(String key : getKeys()){
            //int twIndex1 = ipPair.getKey();
            //int twIndex2 = ipPair.getValue();
            //NNumber ip = ipc.getInnerProduct(twIndex1, twIndex2, false);
            NNumber ip = innerProducts.get(key);
            //ipc.setInnerProduct(twIndex1, twIndex2, ip);
            ipc.innerProducts.put(key, ip);
        }
        
        return ipc;
    }
 
}

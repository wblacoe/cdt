package innerProduct;

import cdt.Helper;
import experiment.dep.TargetWord;
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
    
    public Set<String> getKeys(){
        return innerProducts.keySet();
    }

    public void setInnerProduct(String word1, String word2, NNumber ip){
        String key = (word1.compareTo(word2) < 0 ? word1 + "\t" + word2 : word2 + "\t" + word1);
        innerProducts.put(key, ip);
    }
    
    public synchronized NNumber getInnerProduct(String word1, String word2, boolean computeIfNull){
        String key = (word1.compareTo(word2) < 0 ? word1 + "\t" + word2 : word2 + "\t" + word1);
        NNumber ip = innerProducts.get(key);
        
        
        if(ip == null && computeIfNull){
            TargetWord tw1 = Vocabulary.getTargetWord(word1);
            if(tw1 == null) return null;
            ValueMatrix m1 = (ValueMatrix) tw1.getLexicalRepresentation();
            if(m1 == null) return null;
            TargetWord tw2 = Vocabulary.getTargetWord(word2);
            if(tw2 == null) return null;
            ValueMatrix m2 = (ValueMatrix) tw2.getLexicalRepresentation();
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
            if(entries.length == 3){
				String key = entries[0] + "\t" + entries[1];
				float ip = Float.parseFloat(entries[2]);
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
                    for(String key : getKeys()){
                        NNumber ip = innerProducts.get(key);
                        if(ip != null){
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
        for(String key : ipc.getKeys()){
            NNumber ip = ipc.innerProducts.get(key);
            if(ip != null){
                innerProducts.put(key, ip);
            }
        }
    }
    
    public InnerProductsCache getCopy(){
        InnerProductsCache ipc = new InnerProductsCache();
        
        for(String key : hyperparameters.keySet()){
            ipc.setHyperParameter(key, getHyperParameter(key));
        }
     
        for(String key : getKeys()){
            NNumber ip = innerProducts.get(key);
            ipc.innerProducts.put(key, ip);
        }
        
        return ipc;
    }
 
}

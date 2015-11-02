package experiment;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author wblacoe
 */
public class Dataset {
    
    public HashMap<Integer, AbstractInstance> indexInstanceMap;
    
    public Dataset(){
        indexInstanceMap = new HashMap<>();
    }
    
    public boolean hasInstance(Integer index){
        return indexInstanceMap.containsKey(index);
    }
    
    public void setInstance(Integer index, AbstractInstance instance){
        indexInstanceMap.put(index, instance);
    }
    
    public synchronized AbstractInstance getInstance(Integer index){
        return indexInstanceMap.get(index);
    }

    public Set<Integer> getIndicesSet(){
        return indexInstanceMap.keySet();
    }
    
    public int getSize(){
        return indexInstanceMap.size();
    }
    
    public void clear(){
        indexInstanceMap.clear();
    }

}

package corpus.associationFunction;

import corpus.dep.marginalizer.DepMarginalCounts;
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.ValueMatrix;

/**
 *
 * @author wblacoe
 */
public class SppmiFunction extends AssociationFunction {

    private DepMarginalCounts dmc;
    private double delta;
    private int maxCardinality;
    private HashSet<Runnable> threads;
    private HashMap<String, ValueMatrix> ldops;

    public SppmiFunction(DepMarginalCounts dmc, double delta, int maxCardinality){
		super();
        this.dmc = dmc;
        this.delta = delta;
        this.maxCardinality = maxCardinality;
        threads = new HashSet<>();
        ldops = new HashMap<>();
    }
    
    public SppmiFunction(){
        super();
    }
    
    @Override
    public synchronized HashMap<String, ValueMatrix> compute(HashMap<String, CountMatrix> jdops){
        threads.clear();
        ldops.clear();
        
        for(String dopName : jdops.keySet()){
            CountMatrix jdop = jdops.get(dopName);
            SppmiFunctionThread thread = new SppmiFunctionThread(this, dmc, delta, maxCardinality, jdop);
            threads.add(thread);
            (new Thread(thread)).start();
        }
        
        try{
            while(!threads.isEmpty()){
                wait();
            }
		}catch(InterruptedException e){}
		
        return ldops;
    }
    
    public synchronized void reportSppmiFunctionThreadDone(SppmiFunctionThread thread, ValueMatrix vm){
        threads.remove(thread);
        if(vm != null){
            ldops.put(vm.getName(), vm);
        }
        notify();
    }
    
}

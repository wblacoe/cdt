package linearAlgebra.count;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import linearAlgebra.Matrix;

public class CountMatrix extends Matrix{

	protected TreeMap<CountBaseMatrix, CountBaseMatrix> countBaseMatrices;
	
	public CountMatrix(){
		super();
		countBaseMatrices = new TreeMap<>();
	}

	/*public CountMatrix(BufferedReader in) throws IOException {
		this();
		
        String line;
        while((line = in.readLine()) != null){
            
            if(line.startsWith("<matrix")){
                Matcher matcher = matrixPattern.matcher(line);
                if(matcher.find()){
                    String matrixName = matcher.group(1);
                    //String matrixType = matcher.group(2);
                    setName(matrixName);
					while((line = in.readLine()) != null){
						if(line.equals("</matrix>")) break;
						CountBaseMatrix bm = new CountBaseMatrix(line);
						CountBaseMatrix existingBm = countBaseMatrices.get(bm);
						if(existingBm == null){
							countBaseMatrices.put(bm, bm);
						}else{
							existingBm.add(bm.getCount());
						}
					}

                    //System.out.println(line + ", name: " + matcher.group(1) + ", card: " + matcher.group(3) + ", type: " + matcher.group(2)); //DEBUG
                    break;
                }
            }
            
        }		
	}
	*/

	
	public void add(CountBaseMatrix bm){
		CountBaseMatrix existingBm = countBaseMatrices.get(bm);
		if(existingBm == null){
			countBaseMatrices.put(bm, bm);
		}else{
			existingBm.add(bm.getCount());
            //countBaseMatrices.remove(existingBm);
            //CountBaseMatrix newBm = new CountBaseMatrix(existingBm.getLeftBaseTensor(), existingBm.getRightBaseTensor(), existingBm.getCount() + bm.getCount());
            //countBaseMatrices.put(newBm, newBm);
		}
	}
    
    public synchronized void add(CountMatrix m){
        for(CountBaseMatrix bm : m.getCountBaseMatrices().keySet()){
            add(bm);
        }
    }
	
    @Override
	public int getCardinality(){
		return countBaseMatrices.size();
	}
    
    @Override
    public boolean isZero(){
        return getCardinality() == 0;
    }
    
    public TreeMap<CountBaseMatrix, CountBaseMatrix> getCountBaseMatrices(){
        return countBaseMatrices;
    }
	
	public void reduceCardinality(int cardinality){
		TreeSet<CountBaseMatrix> sortedCountBaseMatrices = new TreeSet<>(new Comparator(){
			@Override
			public int compare(Object o1, Object o2) {
				CountBaseMatrix bm1 = (CountBaseMatrix) o1;
				CountBaseMatrix bm2 = (CountBaseMatrix) o2;
				if(bm1.getCount() < bm2.getCount()){
					return -1;
				}else if(bm1.getCount() > bm2.getCount()){
					return 1;
				}else{
					return bm1.compareTo(bm2);
				}
			}
		});
		
		sortedCountBaseMatrices.addAll(countBaseMatrices.keySet());
		countBaseMatrices.clear();
		for(int i=0; i<cardinality; i++){
			CountBaseMatrix bm = sortedCountBaseMatrices.pollLast();
			if(bm == null) break;
			countBaseMatrices.put(bm, bm);
		}
	}
	
    @Override
    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<matrix name=\"" + getName() + "\" type=\"count\" cardinality=\"" + getCardinality() + "\">\n");
        for(CountBaseMatrix bm : countBaseMatrices.values()){
            bm.saveToWriter(out);
        }
        out.write("</matrix>\n");
    }
    
    public static CountMatrix importFromReader(BufferedReader in) throws IOException{
        CountMatrix m = new CountMatrix();
        
        String line;
        
        while((line = in.readLine()) != null){
            if(line.equals("</matrix>")) break;
            CountBaseMatrix bm = CountBaseMatrix.importFromString(line);
            m.add(bm);
        }
        
        return m;
    }

    @Override
    public String toString(){
        String s = "value matrix, name=\"" + name + "\", card=" + getCardinality() + "\n";
        int i=0;
        for(Iterator<CountBaseMatrix> iter=countBaseMatrices.descendingKeySet().iterator(); iter.hasNext() && i<5; i++){
            s += iter.next() + "\n";
        }
        return s + "...";
    }

}

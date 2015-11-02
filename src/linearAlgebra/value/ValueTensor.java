package linearAlgebra.value;

import cdt.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import linearAlgebra.BaseTensor;
import linearAlgebra.Tensor;
import numberTypes.NNumber;
import space.TensorSpace;

public class ValueTensor extends Tensor {

    private int sorting;
    private NNumber norm;
    
    //this matrix is the sum of these base tensors
    private ValueBaseTensor[] valueBaseTensors;
    
    public ValueTensor(int cardinality){
        super();
        sorting = Helper.NOT_SORTED;
        valueBaseTensors = new ValueBaseTensor[cardinality];
        norm = null;
    }
    
    public int getSorting(){
        return sorting;
    }
    
    public int getCardinality(){
        return valueBaseTensors.length;
    }
    
    public void setBaseTensor(int index, ValueBaseTensor bt){
        valueBaseTensors[index] = bt;
    }
    
    public ValueBaseTensor getBaseTensor(int index){
        return valueBaseTensors[index];
    }
    
    public boolean isZero(){
        for(int i=0; i<getCardinality(); i++){
            ValueBaseTensor bt = valueBaseTensors[i];
            if(bt != null && !bt.isZero()){
                return false;
            }
        }
        return true;
    }
    
    public void computeNorm(){
        for(int i1=0; i1<getCardinality(); i1++){
            ValueBaseTensor bt1 = valueBaseTensors[i1];
            for(int i2=0; i2<getCardinality(); i2++){
                ValueBaseTensor bt2 = valueBaseTensors[i2];
                NNumber x = bt1.innerProduct(bt2);
                //System.out.println("*** <" + bt1 + ", " + bt2 + "> = " + x); //DEBUG
                if(x != null){
                    if(norm == null){
                        norm = x;
                    }else{
                        norm = norm.add(x);
                    }
                }
            }
        }
    }
    
    public NNumber getNorm(){
        if(norm == null){
            computeNorm();
        }
        return norm;
    }
    
    public void normalize(){
        computeNorm();
        if(norm != null){
            multiply(norm.reciprocal());
        }
    }
    
    public void multiply(NNumber factor){
        for(int i=0; i<getCardinality(); i++){
            ValueBaseTensor bt = valueBaseTensors[i];
            bt.setValue(bt.getValue().multiply(factor));
        }
    }

    
    //sum over products of pairs of base tensors with equal combinations of dimensions per mode
    public NNumber innerProductQuick(ValueTensor t){
        
        //NNumber ip = NNumber.zero();
        NNumber ip = null;
        
        if(sorting == Helper.SORTED_BY_DIMENSION){
            
            int i1 = 0, i2 = 0;
            ValueBaseTensor bt1, bt2;
            while(i1 < getCardinality() && i2 < t.getCardinality()){
                bt1 = getBaseTensor(i1);
                bt2 = t.getBaseTensor(i2);
                int compareBt = ((BaseTensor) bt1).compareTo((BaseTensor) bt2);
                if(compareBt == 0){
                    NNumber x = bt1.getValue().multiply(bt2.getValue());
                    //System.out.println("*** <" + bt1 + ", " + bt2 + "> = " + x); //DEBUG
                    if(ip == null){
                        ip = x;
                    }else{
                        ip = ip.add(x);
                    }
                    i1++;
                    i2++;
                }else if(compareBt < 0){
                    i1++;
                }else{
                    i2++;
                }
            }
            
        }else{
        
            //sort base tensors by dimension
            TreeSet<ValueBaseTensor> sortedBaseTensors1 = new TreeSet<>();
            for(int i=0; i<getCardinality(); i++) sortedBaseTensors1.add(valueBaseTensors[i]);

            TreeSet<ValueBaseTensor> sortedBaseTensors2 = new TreeSet<>();
            for(int i=0; i<t.getCardinality(); i++) sortedBaseTensors2.add(t.getBaseTensor(i));

            //jointly iterate through both sorted lists
            Iterator<ValueBaseTensor> iterator1 = sortedBaseTensors1.iterator();
            Iterator<ValueBaseTensor> iterator2 = sortedBaseTensors2.iterator();

            if(iterator1.hasNext() && iterator2.hasNext()){
                ValueBaseTensor bt1 = iterator1.next();
                ValueBaseTensor bt2 = iterator2.next();
                while(true){
                    int compareBt = ((BaseTensor) bt1).compareTo((BaseTensor) bt2);
                    if(compareBt == 0){
                        NNumber x = bt1.getValue().multiply(bt2.getValue());
                        //System.out.println("*** <" + bt1 + ", " + bt2 + "> = " + x); //DEBUG
                        ip = ip.add(x);
                        if(!iterator1.hasNext() || !iterator2.hasNext()) break;
                        bt1 = iterator1.next();
                        bt2 = iterator2.next();
                    }else if(compareBt < 0){
                        if(!iterator1.hasNext()) break;
                        bt1 = iterator1.next();
                    }else{
                        if(!iterator2.hasNext()) break;
                        bt2 = iterator2.next();
                    }
                }
            }
            
        }
        
        return ip;
    }
    
    //sum over products of pairs of all base tensors with all base tensors
    public NNumber innerProductSlow(ValueTensor t){
        //NNumber ip = NNumber.zero();
        NNumber ip = null;
        
        for(int i1=0; i1<getCardinality(); i1++){
            ValueBaseTensor bt1 = getBaseTensor(i1);
            for(int i2=0; i2<t.getCardinality(); i2++){
                ValueBaseTensor bt2 = t.getBaseTensor(i2);
                NNumber x = bt1.innerProduct(bt2);
                //System.out.println("*** <" + bt1 + ", " + bt2 + "> = " + x); //DEBUG
                if(ip == null){
                    ip = x;
                }else if(x != null){
                    ip = ip.add(x);
                }
            }
        }
        
        return ip;
    }
    
    public NNumber innerProduct(ValueTensor t){
        if(TensorSpace.getSpeedOfValueTensorInnerProduct() == Helper.QUICK){
            return innerProductQuick(t);
        }else{
            return innerProductSlow(t);
        }
    }

    
    public void reduceCardinality(int cardinality){
        
        if(cardinality >= getCardinality()) System.out.println("[ValueTensor] This is not a reduction of cardinality."); //DEBUG
        
        //define sorting data structure
		TreeSet<ValueBaseTensor> sortedValueBaseTensors = new TreeSet<>(new Comparator(){
			@Override
			public int compare(Object o1, Object o2) {
				ValueBaseTensor bt1 = (ValueBaseTensor) o1;
				ValueBaseTensor bt2 = (ValueBaseTensor) o2;
				int diffValue = bt1.getValue().compareTo(bt2.getValue());
				if(diffValue != 0){
					return diffValue;
				}else{
					return bt1.compareTo(bt2);
				}
			}
		});

        //sort base matrices
        for(int i=0; i<getCardinality(); i++){
            sortedValueBaseTensors.add(valueBaseTensors[i]);
        }
        
        //keep only the [cardinality] largets base matrices
        ValueBaseTensor[] newValueBaseTensors = new ValueBaseTensor[cardinality];
		for(int i=0; i<cardinality; i++){
			ValueBaseTensor bt = sortedValueBaseTensors.pollLast();
			if(bt == null) break;
			newValueBaseTensors[i] = bt;
		}
        
        //replace array of base matrices
        sorting = Helper.SORTED_BY_VALUE;
        valueBaseTensors = newValueBaseTensors;
	}
    
    public void sortByValue(){
        reduceCardinality(getCardinality());
    }

    public void importFromReader(BufferedReader in) throws IOException{
        //TODO
        sorting = Helper.SORTED_BY_DIMENSION;
    }
    
    public void exportToWriter(BufferedWriter out) throws IOException{
        out.write("" + getCardinality() + "\n");
        for(int i=0; i<getCardinality(); i++){
            ValueBaseTensor bt = valueBaseTensors[i];
            //out.write(bm.getValue().getDoubleValue() + "\t" + bm.getLeftBaseTensor().toString() + "\t" + bm.getRightBaseTensor().toString() + "\n");
            //out.write(bt.getValue().getDoubleValue() + "\t"); // + bt.toString() + "\n");
            bt.exportToWriter(out);
        }

    }

    @Override
    public String toString(){
        String s = "value tensor, card=" + getCardinality() + ", norm=" /*+ getNorm()*/ + "\n";
        for(int i=0; i<Math.min(5, getCardinality()); i++){
            s += getBaseTensor(i) + "\n";
        }
        return s + "...";
    }

    
    public static void main(String[] args){
        TensorSpace.setOrder(5);
		TensorSpace.setDimensionality(300);
		TensorSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
		
		ValueBaseTensor bt1 = new ValueBaseTensor();
		bt1.setValue(bt1.getValue().invert());
		bt1.setDimensionAtMode(1, 5);
		//bt1.setDimensionAtMode(2, 7);
		
		//BaseTensor bt2 = new BaseTensor(NNumber.one());
		ValueBaseTensor bt2 = new ValueBaseTensor(NNumber.create(0.3123f));
		//bt2.setValue(bt2.getValue().invert());
        
        ValueTensor t1 = new ValueTensor(2);
        t1.setBaseTensor(0, bt2);
        t1.setBaseTensor(1, bt1);
        
        ValueTensor t2 = new ValueTensor(1);
        t2.setBaseTensor(0, bt1);
        
        ValueTensor t3 = new ValueTensor(1);
        t3.setBaseTensor(0, bt2);
		
		System.out.println("bt1 = " + bt1);
		System.out.println("bt2 = " + bt2);
		System.out.println("<bt1,bt2> = " + bt1.innerProduct(bt2));
        System.out.println("t1 = bt1 + bt2");
        System.out.println("t2 = bt1");
        System.out.println("t3 = bt2");
        System.out.println("<t1,t1> = " + t1.innerProduct(t1));
        System.out.println("<t2,t2> = " + t2.innerProduct(t2));
        System.out.println("<t3,t3> = " + t3.innerProduct(t3));
        System.out.println("<t1,t2> = " + t1.innerProduct(t2));
        System.out.println("<t2,t3> = " + t2.innerProduct(t3));
        System.out.println("<t1,t3> = " + t1.innerProduct(t3));
        System.out.println("<t2,t3> + <t3,t3> = " + t2.innerProduct(t3).add(t3.innerProduct(t3)));
	}
    
}

package linearAlgebra.value;

import cdt.Helper;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import linearAlgebra.BaseMatrix;
import linearAlgebra.BaseTensor;
import linearAlgebra.Matrix;
import numberTypes.NNumber;
import numberTypes.NNumberVector;
import space.TensorSpace;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

/**
 * .
 * 
 * should be sorted by value descendingly, and all null base matrices should be at the end of the array
 * 
 * @author wblacoe
 */
public class ValueMatrix extends Matrix {
 
    protected static Pattern partialTracePattern = Pattern.compile("<mode name=\\\"(.*?)\\\" index=\\\"(.*?)\\\" dimensionality=\\\"(.*?)\\\" cardinality=\\\"(.*?)\\\">");
    
    
    private int sorting;
    private NNumberVector[] partialTraceDiagonalVectors; //uses target word (vocabulary) indices
    
    //this matrix is the sum of these base matrices
    private ValueBaseMatrix[] valueBaseMatrices; //uses context word indices
    
    
    public ValueMatrix(int cardinality){
        super();
        sorting = Helper.NOT_SORTED;
        //sorting = Helper.SORTED_BY_DIMENSION;
        valueBaseMatrices = new ValueBaseMatrix[cardinality];
        //trace = null;
        partialTraceDiagonalVectors = null;
    }
    
    public void setSorting(int n){
        sorting = n;
    }
    
    public int getSorting(){
        return sorting;
    }
    
    @Override
    public int getCardinality(){
        return valueBaseMatrices.length;
    }
    
    public void setBaseMatrix(int index, ValueBaseMatrix bm){
        valueBaseMatrices[index] = bm;
    }
    
    public ValueBaseMatrix getBaseMatrix(int index){
        return valueBaseMatrices[index];
    }
    
    //warning: this deletes all previously saved value base matrices
    public void setCardinality(int cardinality){
        valueBaseMatrices = new ValueBaseMatrix[cardinality];
    }
    
    public ValueBaseMatrix[] getValueBaseMatrices(){
        return valueBaseMatrices;
    }
    
    public NNumberVector[] getPartialTraceDiagonalVectors(){
        return partialTraceDiagonalVectors;
    }
    
    public void setPartialTraceDiagonalVectors(NNumberVector[] partialTraceDiagonalVectors){
        this.partialTraceDiagonalVectors = partialTraceDiagonalVectors;
    }

    
    @Override
    public boolean isZero(){
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = valueBaseMatrices[i];
            if(bm != null && !bm.isZero()){
                return false;
            }
        }
        return true;
    }
    
    public NNumber trace(){
		NNumber trace = null;
        for(ValueBaseMatrix bm : valueBaseMatrices){
            if(bm == null) continue;
            NNumber ip = bm.getLeftBaseTensor().innerProduct(bm.getRightBaseTensor());
            if(ip != null){
                NNumber value = ip.multiply(bm.getValue());
                if(trace == null){
                    trace = value;
                }else{
                    trace = trace.add(value);
                }
            }
        }
		return trace;
    }
    
    /*public NNumber getTrace(){
        if(trace == null){
            computeTrace();
        }
        return trace;
    }
	*/
    
    public void normalize(){
        NNumber trace = trace();
        if(trace != null){
            multiply(trace.reciprocal());
        }
    }
    
    public void multiply(NNumber factor){
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = valueBaseMatrices[i];
            bm.setValue(bm.getValue().multiply(factor));
        }
    }
    
    
    //sum over products of pairs of base matrices with equal combinations of dimensions per mode
    public NNumber innerProductQuick(ValueMatrix m){
        
        //NNumber ip = NNumber.zero();
        NNumber ip = null;
        
        if(sorting == Helper.SORTED_BY_DIMENSION){
            
            int i1 = 0, i2 = 0;
            ValueBaseMatrix bm1, bm2;
            while(i1 < getCardinality() && i2 < m.getCardinality()){
                bm1 = getBaseMatrix(i1);
                bm2 = m.getBaseMatrix(i2);
                int compareBm = ((BaseMatrix) bm1).compareTo((BaseMatrix) bm2);
                if(compareBm == 0){
                    NNumber x = bm1.getValue().multiply(bm2.getValue());
                    //System.out.println("*** <" + bm1 + ", " + bm2 + "> = " + x); //DEBUG
                    if(ip == null){
                        ip = x;
                    }else{
                        ip = ip.add(x);
                    }
                    i1++;
                    i2++;
                }else if(compareBm < 0){
                    i1++;
                }else{
                    i2++;
                }
            }
            
        }else{
        
            //sort base tensors by dimension
            TreeSet<ValueBaseMatrix> sortedBaseMatrices1 = new TreeSet<>();
            for(int i=0; i<getCardinality(); i++) sortedBaseMatrices1.add(valueBaseMatrices[i]);

            TreeSet<ValueBaseMatrix> sortedBaseMatrices2 = new TreeSet<>();
            for(int i=0; i<m.getCardinality(); i++) sortedBaseMatrices2.add(m.getBaseMatrix(i));

            //jointly iterate through both sorted lists
            Iterator<ValueBaseMatrix> iterator1 = sortedBaseMatrices1.iterator();
            Iterator<ValueBaseMatrix> iterator2 = sortedBaseMatrices2.iterator();

            if(iterator1.hasNext() && iterator2.hasNext()){
                ValueBaseMatrix bm1 = iterator1.next();
                ValueBaseMatrix bm2 = iterator2.next();
                while(true){
                    int compareBm = ((BaseMatrix) bm1).compareTo((BaseMatrix) bm2);
                    if(compareBm == 0){
                        NNumber x = bm1.getValue().multiply(bm2.getValue());
                        //System.out.println("*** <" + bt1 + ", " + bt2 + "> = " + x); //DEBUG
                        if(ip == null){
                            ip = x;
                        }else{
                            ip = ip.add(x);
                        }
                        if(!iterator1.hasNext() || !iterator2.hasNext()) break;
                        bm1 = iterator1.next();
                        bm2 = iterator2.next();
                    }else if(compareBm < 0){
                        if(!iterator1.hasNext()) break;
                        bm1 = iterator1.next();
                    }else{
                        if(!iterator2.hasNext()) break;
                        bm2 = iterator2.next();
                    }
                }
            }
            
        }
        
        if(ip == null){
            return null;
        }else{
            return ip;
        }
    }

    
    //sum over products of pairs of all base matrices with all base matrices
    public NNumber innerProductSlow(ValueMatrix m){
        NNumber ip = null;
        
        for(int i1=0; i1<getCardinality(); i1++){
            ValueBaseMatrix bm1 = getBaseMatrix(i1);
            for(int i2=0; i2<m.getCardinality(); i2++){
                ValueBaseMatrix bm2 = m.getBaseMatrix(i2);
                NNumber x = bm1.innerProduct(bm2);
                if(ip == null){
                    ip = x;
                }else if(x != null){
                    ip = ip.add(x);
                }
            }
        }
        
        if(ip == null){
            return null;
        }else{
            return ip;
        }
    }
    
    //<m1, m2> = Tr(m1*m2^T)
    public NNumber innerProduct(ValueMatrix m){
        //if(TensorSpace.getSpeedOfValueMatrixInnerProduct() == Helper.QUICK){
            //return innerProductQuick(m);
        //}else{
            return innerProductSlow(m);
        //}
    }

    public ValueTensor times(ValueTensor t){
        
        //define sorting data structure. sort base tensors by dimensions
		TreeMap<BaseTensor, BaseTensor> baseTensorsSortedByDimension = new TreeMap<>(new Comparator(){
            /*@Override
            public int compare(Object o1, Object o2) {
                BaseTensor bt1 = (BaseTensor) o1;
                BaseTensor bt2 = (BaseTensor) o2;
                return bt1.compareTo(bt2);
            }
            */
            @Override
            public int compare(Object o1, Object o2){
                BaseTensor bt1 = (BaseTensor) o1;
                BaseTensor bt2 = (BaseTensor) o2;
                int[] modeAndOnlyCertainDimension1 = bt1.getModeAndOnlyCertainDimension();
                int[] modeAndOnlyCertainDimension2 = bt2.getModeAndOnlyCertainDimension();
                if(modeAndOnlyCertainDimension1 != null && modeAndOnlyCertainDimension2 != null){
                    if(modeAndOnlyCertainDimension1[0] == modeAndOnlyCertainDimension2[0]){
                        if(modeAndOnlyCertainDimension1[1] == modeAndOnlyCertainDimension2[1]){
                            return 0;
                        }else if(modeAndOnlyCertainDimension1[1] < modeAndOnlyCertainDimension2[1]){
                            return -1;
                        }else{
                            return 1;
                        }
                    }else if(modeAndOnlyCertainDimension1[0] < modeAndOnlyCertainDimension2[0]){
                        return -1;
                    }else{
                        return 1;
                    }

                }else{

                    for(int m=1; m<=bt1.getOrder(); m++){
                        if(bt1.isDimensionCertainAtMode(m) || bt2.isDimensionCertainAtMode(m)){
                            int dimension1 = bt1.getDimensionAtMode(m);
                            int dimension2 = bt2.getDimensionAtMode(m);
                            if(dimension1 < dimension2) return -1;
                            if(dimension2 > dimension1) return 1;
                        }
                    }
                }

                //System.out.println("this: [" + givenModeAndOnlyCertainDimension[0] + ", " + givenModeAndOnlyCertainDimension[1] + "], given: [" + givenModeAndOnlyCertainDimension[0] + ", " + givenModeAndOnlyCertainDimension[1] + "]");

                return 0;
            }
        });
        
        //go through all pairs of base matrices
        //int a = 0, b = 0;
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = getBaseMatrix(i);
            for(int j=0; j<t.getCardinality(); j++){
                ValueBaseTensor bt = t.getBaseTensor(j);
                ValueBaseTensor newBt = bm.times(bt);
                if(newBt != null){
                    ValueBaseTensor existingBt = (ValueBaseTensor) baseTensorsSortedByDimension.get(newBt);
                    if(existingBt == null){
                        //a++;
                        baseTensorsSortedByDimension.put(newBt, newBt);
                    }else{
                        //b++;
                        ValueBaseTensor sumBt = new ValueBaseTensor(bm.getLeftBaseTensor(), existingBt.getValue().add(newBt.getValue()));
                        baseTensorsSortedByDimension.put(sumBt, sumBt);
                    }
                }
            }
        }
        //System.out.println("a = " + a + ", b = " + b);
        
        
        //define sorting data structure. sort first by value, then by dimension
        TreeSet<ValueBaseTensor> baseTensorsSortedByValue = new TreeSet<>(new Comparator(){
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
        
        
        for(BaseTensor bt : baseTensorsSortedByDimension.keySet()){
            baseTensorsSortedByValue.add((ValueBaseTensor) bt);
        }

        //save the [cardinality] top base tensors in the resulting tensor
        int newCard = Math.min(getCardinality(), baseTensorsSortedByValue.size());
        ValueTensor newT = new ValueTensor(newCard);
        for(int i=0; i<newCard; i++){
            ValueBaseTensor bt = baseTensorsSortedByValue.pollLast();
            newT.setBaseTensor(i, bt);
        }
        
        return newT;
    }
    
    public ValueMatrix times(ValueMatrix m){
        
        //define sorting data structure. sort base matrices by dimensions
		TreeMap<BaseMatrix, BaseMatrix> baseMatricesSortedByDimension = new TreeMap<>(new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                ValueBaseMatrix bm1 = (ValueBaseMatrix) o1;
                ValueBaseMatrix bm2 = (ValueBaseMatrix) o2;
                int compareLeftBaseTensors = bm1.getLeftBaseTensor().compareTo(bm2.getLeftBaseTensor());
                if(compareLeftBaseTensors != 0){
                    return compareLeftBaseTensors;
                }else{
                    return bm1.getRightBaseTensor().compareTo(bm2.getRightBaseTensor());
                }
            }
        });
        
        //go through all pairs of base matrices
        //int a = 0, b = 0;
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix thisBm = getBaseMatrix(i);
            if(thisBm == null) continue;
            for(int j=0; j<m.getCardinality(); j++){
                ValueBaseMatrix givenBm = m.getBaseMatrix(j);
                if(givenBm == null) continue;
                ValueBaseMatrix newBm = thisBm.times(givenBm);
                //don't add zeros (warning: this might lead to the product cardinality not being fully used)
                if(newBm != null && !newBm.getValue().isZero()){
                    //System.out.println("adding " + newBm); //DEBUG
                    //store new base matrix (even if another base matrix with the same dimensions is already stored here)
                    //BaseMatrix x = new BaseMatrix(newBm.getLeftBaseTensor(), newBm.getRightBaseTensor());
                    ValueBaseMatrix existingBm = (ValueBaseMatrix) baseMatricesSortedByDimension.get(newBm);
                    if(existingBm == null){
                        //a++;
                        baseMatricesSortedByDimension.put(newBm, newBm);
                    }else{
                        //b++;
                        //replace existing bm rather than just change its value
                        ValueBaseMatrix sumBm = new ValueBaseMatrix(existingBm.getLeftBaseTensor(), existingBm.getRightBaseTensor(), existingBm.getValue().add(newBm.getValue()));
                        baseMatricesSortedByDimension.put(sumBm, sumBm);
                    }
                }
            }
        }
        //System.out.println("a = " + a + ", b = " + b);
        
        
        //define sorting data structure. sort first by value, then by dimension
        TreeSet<ValueBaseMatrix> baseMatricesSortedByValue = new TreeSet<>(new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                ValueBaseMatrix bm1 = (ValueBaseMatrix) o1;
                ValueBaseMatrix bm2 = (ValueBaseMatrix) o2;
				int diffValue = bm1.getValue().compareTo(bm2.getValue());
				if(diffValue != 0){
					return diffValue;
				}else{
                    int compareLeftBaseTensors = bm1.getLeftBaseTensor().compareTo(bm2.getLeftBaseTensor());
                    if(compareLeftBaseTensors != 0){
                        return compareLeftBaseTensors;
                    }else{
                        return bm1.getRightBaseTensor().compareTo(bm2.getRightBaseTensor());
                    }
				}
            }
        });
            
            
            
        for(BaseMatrix bm : baseMatricesSortedByDimension.keySet()){
            baseMatricesSortedByValue.add((ValueBaseMatrix) bm);
        }

        //save the [cardinality] top base matrices in the resulting matrix
        ValueMatrix newM = new ValueMatrix(Math.min(getCardinality(), baseMatricesSortedByValue.size()));
        for(int i=0; i<newM.getCardinality(); i++){
            ValueBaseMatrix bm = baseMatricesSortedByValue.pollLast();
            newM.setBaseMatrix(i, bm);
        }
        
        return newM;
    }
    
    public ValueMatrix times(NNumber n){
        if(n.isZero()) return new ValueMatrix(0);
        
        ValueMatrix product = new ValueMatrix(getCardinality());
        
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = getBaseMatrix(i);
            if(bm != null){
                //System.out.println("matrix \"" + name + "\" bm #" + i + ": " + bm + " times " + n);
                product.setBaseMatrix(i, new ValueBaseMatrix(bm.getLeftBaseTensor(), bm.getRightBaseTensor(), bm.getValue().multiply(n)));
            }
        }
        
        return product;
    }
    
    public ValueMatrix getCopy(){
        ValueMatrix m = new ValueMatrix(getCardinality());
        for(int i=0; i<getCardinality(); i++){
            m.setBaseMatrix(i, valueBaseMatrices[i]);
        }
        return m;
    }
    
    public ValueTensor getUnderlyingOuterProductTensor(){
        ValueTensor t = new ValueTensor(1);
        t.setBaseTensor(0, new ValueBaseTensor(NNumber.one()));
        //System.out.println("totally uncertain tensor: " + t);
        
        NNumber oldNorm, norm = NNumber.create(20);
        
        while(true){
            oldNorm = norm;
            t = times(t);
            norm = t.getNorm();
            //System.out.println("ratio = " + oldNorm + " / " + norm);
            NNumber ratio = oldNorm.multiply(norm.reciprocal());
            //System.out.println("card=" + t.getCardinality() + ", norm=" + norm + ", ratio=" + ratio);
            if(Math.abs(ratio.abs().getDoubleValue() - 1) < 0.01){
                break;
            }
            t.normalize();
        }
        
        t.normalize();
        return t;
    }
   
    public ValueMatrix decompose(){
        ValueMatrix m = this.getCopy();
        //System.out.println("m: " + m);
        m.normalize();
        //System.out.println("norm(m): " + m);
        NNumber oldTrace, tr = NNumber.create(-1);
        while(true){
            m = m.times(m);
            oldTrace = tr;
            tr = m.trace();
            NNumber ratio = tr.multiply(oldTrace.reciprocal());
            //System.out.println("card=" + m.getCardinality() + ", trace=" + tr + ", ratio=" + ratio);
            //System.out.println(m);
            if(Math.abs(ratio.abs().getDoubleValue() - 1) < 0.01){
                break;
            }
            //NNumber tr = m.getTrace().multiply(m.getGlobalFactor());
            //System.out.println("card = " + m.getCardinality() + ", Tr(m*m) = " + tr);
            m.normalize();
            //System.out.println("Tr = " + m.getTrace().multiply(m.getGlobalFactor()));
        }
        
        m.normalize();
        return m;
    }
    
    public void reduceCardinality(int cardinality){
        
        cardinality = Math.min(cardinality, getCardinality());
        
		TreeSet<ValueBaseMatrix> sortedValueBaseMatrices = new TreeSet<>(new Comparator(){
			@Override
			public int compare(Object o1, Object o2) {
				ValueBaseMatrix bm1 = (ValueBaseMatrix) o1;
				ValueBaseMatrix bm2 = (ValueBaseMatrix) o2;
                int valueComparison = bm1.getValue().compareTo(bm2.getValue());
				if(valueComparison != 0){
					return valueComparison;
				}else{
					return bm1.compareTo(bm2);
				}
			}
		});
		
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = valueBaseMatrices[i];
            if(bm != null){
				sortedValueBaseMatrices.add(bm);
			}
        }
        setCardinality(cardinality);
		ValueBaseMatrix bm;
		for(int i=0; (bm = sortedValueBaseMatrices.pollLast()) != null && i<cardinality; i++){
			setBaseMatrix(i, bm);
		}
	}
    
    public int getAmountOfNullBaseMatrices(){
        int a = 0;
        for(int i=0; i<getCardinality(); i++){
            if(getBaseMatrix(i) == null) a++;
        }
        return a;
    }
    
    public int getAmountOfNonNullBaseMatrices(){
        return getCardinality() - getAmountOfNullBaseMatrices();
    }
    
    public void removeNullBaseMatrices(){
        ValueBaseMatrix[] newValueBaseMatrices = new ValueBaseMatrix[getAmountOfNonNullBaseMatrices()];
        int i=0;
        for(ValueBaseMatrix vbm : valueBaseMatrices){
            if(vbm != null){
                newValueBaseMatrices[i] = vbm;
                i++;
            }
        }
        valueBaseMatrices = newValueBaseMatrices;
    }
    
    public ValueMatrix plusUnsorted(ValueMatrix m){
        int thisCardinality = getCardinality();
        int givenCardinality = m.getCardinality();
        ValueMatrix sum = new ValueMatrix(thisCardinality + givenCardinality);

        HashMap<BaseMatrix, Integer> baseMatrixSumIndexMap = new HashMap<>();
        
        for(int i=0; i<thisCardinality; i++){
            ValueBaseMatrix vbm = valueBaseMatrices[i];
            sum.setBaseMatrix(i, vbm);
            baseMatrixSumIndexMap.put((BaseMatrix) vbm, i);
        }

        int j=0;
        for(int i=0; i<givenCardinality; i++){
            ValueBaseMatrix bm = m.getBaseMatrix(i);
            
            Integer existingIndex = baseMatrixSumIndexMap.get((BaseMatrix) bm);
            if(existingIndex == null){
                sum.setBaseMatrix(thisCardinality + j, bm);
                j++;
            }else{
                ValueBaseMatrix vbm = sum.getBaseMatrix(existingIndex);
                vbm.setValue(vbm.getValue().add(bm.getValue()));
            }
        }
        
        sum.reduceCardinality(Math.max(thisCardinality, givenCardinality));
        return sum;
    }
    
    public ValueMatrix plusSorted(ValueMatrix m){
        int thisCardinality = getCardinality();
        int givenCardinality = m.getCardinality();
        int sumCardinality = Math.max(thisCardinality, givenCardinality);
        ValueMatrix sum = new ValueMatrix(sumCardinality);
        
        HashMap<BaseMatrix, Integer> baseMatrixSumIndexMap = new HashMap<>();
        
        int thisIndex = 0, givenIndex = 0, sumIndex = 0;
        ValueBaseMatrix thisBm = getBaseMatrix(thisIndex);
        ValueBaseMatrix givenBm = getBaseMatrix(givenIndex);
        while(true){
            
            if(thisBm.getValue().compareTo(givenBm.getValue()) >= 0){
                Integer existingIndex = baseMatrixSumIndexMap.get(thisBm);
                if(existingIndex == null){
                    sum.setBaseMatrix(sumIndex, thisBm);
                    baseMatrixSumIndexMap.put(thisBm, sumIndex);
                    sumIndex++;
                    thisIndex++;
                    thisBm = getBaseMatrix(thisIndex);
                }else{
                    sum.getBaseMatrix(existingIndex).add(thisBm.getValue());
                }
            }else{
                Integer existingIndex = baseMatrixSumIndexMap.get(givenBm);
                if(existingIndex == null){
                    sum.setBaseMatrix(sumIndex, givenBm);
                    baseMatrixSumIndexMap.put(givenBm, sumIndex);
                    sumIndex++;
                    givenIndex++;
                    givenBm = getBaseMatrix(givenIndex);
                }else{
                    ValueBaseMatrix vbm = sum.getBaseMatrix(existingIndex);
                    vbm.setValue(vbm.getValue().add(givenBm.getValue()));
                }
            }
            
            if(sumIndex >= sumCardinality) break;
        }
        
        return sum;
    }
    
    public ValueMatrix plus(ValueMatrix m){
        int thisCardinality = getCardinality();
        int givenCardinality = m.getCardinality();
        ValueMatrix sum = new ValueMatrix(thisCardinality + givenCardinality - getAmountOfNullBaseMatrices() - m.getAmountOfNullBaseMatrices());
        //System.out.println(thisCardinality + " + " + givenCardinality + " = " + sum.getCardinality()); //DEBUG
        
        int j=0;
        for(int i=0; i<thisCardinality; i++){
            ValueBaseMatrix bm = getBaseMatrix(i);
            if(bm != null) sum.setBaseMatrix(j++, bm);
            //System.out.println("(from this) sum.setBaseMatrix(" + i + ", " + getBaseMatrix(i) + ");");
        }

        for(int i=0; i<givenCardinality; i++){
            ValueBaseMatrix bm = m.getBaseMatrix(i);
            if(bm != null) sum.setBaseMatrix(j++, m.getBaseMatrix(i));
            //System.out.println("(from given) sum.setBaseMatrix(" + (thisCardinality + i) + ", " + m.getBaseMatrix(i) + ");");
        }

        return sum;
    }
    
    public ValueMatrix plusDynamic(ValueMatrix m){
        if(getSorting() == Helper.SORTED_BY_VALUE && m.getSorting() == Helper.SORTED_BY_VALUE){
            return plusSorted(m);
        }else{
            return plusUnsorted(m);
        }
    }
    
    public ValueMatrix plus1(ValueMatrix m){
        //define sorting data structure. sort base matrices by dimensions
		TreeMap<BaseMatrix, BaseMatrix> baseMatricesSortedByDimension = new TreeMap<>(new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                ValueBaseMatrix bm1 = (ValueBaseMatrix) o1;
                ValueBaseMatrix bm2 = (ValueBaseMatrix) o2;
                int compareLeftBaseTensors = bm1.getLeftBaseTensor().compareTo(bm2.getLeftBaseTensor());
                if(compareLeftBaseTensors != 0){
                    return compareLeftBaseTensors;
                }else{
                    return bm1.getRightBaseTensor().compareTo(bm2.getRightBaseTensor());
                }
            }
        });
        
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = getBaseMatrix(i);
            baseMatricesSortedByDimension.put(bm, bm);
        }
        
        for(int i=0; i<m.getCardinality(); i++){
            ValueBaseMatrix bm = m.getBaseMatrix(i);
            ValueBaseMatrix existingBm = (ValueBaseMatrix) baseMatricesSortedByDimension.get(bm);
            if(existingBm == null){
                baseMatricesSortedByDimension.put(bm, bm);
            }else{
                //replace existing bm rather than just change its value
                ValueBaseMatrix sumBm = new ValueBaseMatrix(existingBm.getLeftBaseTensor(), existingBm.getRightBaseTensor(), existingBm.getValue().add(bm.getValue()));
                baseMatricesSortedByDimension.put(sumBm, sumBm);
            }
        }
        
        //go through all pairs of base matrices
        //int a = 0, b = 0; //DEBUG
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix thisBm = getBaseMatrix(i);
            for(int j=0; j<m.getCardinality(); j++){
                ValueBaseMatrix givenBm = m.getBaseMatrix(j);
                ValueBaseMatrix newBm = thisBm.times(givenBm);
                //don't add zeros (warning: this might lead to the product cardinality not being fully used)
                if(newBm != null){
                    //store new base matrix (even if another base matrix with the same dimensions is already stored here)
                    //BaseMatrix x = new BaseMatrix(newBm.getLeftBaseTensor(), newBm.getRightBaseTensor());
                    ValueBaseMatrix existingBm = (ValueBaseMatrix) baseMatricesSortedByDimension.get(newBm);
                    if(existingBm == null){
                        //a++; //DEBUG
                        baseMatricesSortedByDimension.put(newBm, newBm);
                    }else{
                        //b++; //DEBUG
                        //replace existing bm rather than just change its value
                        ValueBaseMatrix sumBm = new ValueBaseMatrix(existingBm.getLeftBaseTensor(), existingBm.getRightBaseTensor(), existingBm.getValue().add(newBm.getValue()));
                        baseMatricesSortedByDimension.put(sumBm, sumBm);
                    }
                }
            }
        }
        
        
        //define sorting data structure. sort first by value, then by dimension
        TreeSet<ValueBaseMatrix> baseMatricesSortedByValue = new TreeSet<>(new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                ValueBaseMatrix bm1 = (ValueBaseMatrix) o1;
                ValueBaseMatrix bm2 = (ValueBaseMatrix) o2;
				int diffValue = bm1.getValue().compareTo(bm2.getValue());
				if(diffValue != 0){
					return diffValue;
				}else{
                    int compareLeftBaseTensors = bm1.getLeftBaseTensor().compareTo(bm2.getLeftBaseTensor());
                    if(compareLeftBaseTensors != 0){
                        return compareLeftBaseTensors;
                    }else{
                        return bm1.getRightBaseTensor().compareTo(bm2.getRightBaseTensor());
                    }
				}
            }
        });
            
            
            
        for(BaseMatrix bm : baseMatricesSortedByDimension.keySet()){
            baseMatricesSortedByValue.add((ValueBaseMatrix) bm);
        }

        //save the [cardinality] top base matrices in the resulting matrix
        ValueMatrix newM = new ValueMatrix(getCardinality());
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = baseMatricesSortedByValue.pollLast();
            newM.setBaseMatrix(i, bm);
        }
        
        return newM;
    }
    
    public NNumber sumOfAllEntries(){
        //System.out.println("name: " + name + ", card: " + getCardinality()); //DEBUG
        NNumber n = NNumber.create(0);
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = getBaseMatrix(i);
            if(bm != null) n = n.add(bm.getValue());
        }
        return n;
    }
    
    public ValueMatrix invertEntries(){
        ValueMatrix inv = new ValueMatrix(getCardinality());
        inv.setSorting(Helper.NOT_SORTED);
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = getBaseMatrix(i);
            inv.setBaseMatrix(i, new ValueBaseMatrix(bm.getLeftBaseTensor(), bm.getRightBaseTensor(), bm.getValue().invert()));
        }
        return inv;
    }
    
    //modeIndex is in [1;order]
    //returns a vector which is the diagonal of the (first-order) matrix which results from tracing out all modes except the one with given mode index
    //this vector assigns each (occuring) target word in the vocabulary a weight. thus its indices are in [0;vocabulary_size]
    public NNumberVector getPartialTraceDiagonalVector(int modeIndex){

        //ensure that array of partial trace diagonal vectors is initialised
        //#if(partialTraceDiagonalVectors == null) partialTraceDiagonalVectors = new NNumberVector[DepNeighbourhoodSpace.getOrder()];
        if(partialTraceDiagonalVectors == null) partialTraceDiagonalVectors = new NNumberVector[DepNeighbourhoodSpace.getOrder() + 1];

        //if this partial trace diagonal vector has been computed before
        //#if(partialTraceDiagonalVectors[modeIndex - 1] != null) return partialTraceDiagonalVectors[modeIndex - 1];
        if(partialTraceDiagonalVectors[modeIndex] != null) return partialTraceDiagonalVectors[modeIndex];
        
        //start a new partial trace diagonal vector
        NNumberVector partialTraceDiagonalVector = new NNumberVector(Vocabulary.getSize());
        
		//compute new partial trace diagonal vector from base matrices currently contained in this ldop
		DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(modeIndex);
        for(ValueBaseMatrix bm : valueBaseMatrices){
            if(bm == null) continue;
            NNumber ip = bm.getLeftBaseTensor().innerProduct(bm.getRightBaseTensor());
            if(ip != null){
                NNumber weight = ip.multiply(bm.getValue()); //weight need not be normalised since it comes from a normalised ldop
                
                int dimensionLeft = bm.getLeftBaseTensor().getDimensionAtMode(modeIndex);
                int dimensionRight = bm.getRightBaseTensor().getDimensionAtMode(modeIndex);
                int dimension = dimensionLeft == 0 ? dimensionRight : dimensionLeft;
                
                if(dimension > 0){ //ignore dummy word
                    int vocabularyIndex = drc.getContextWord(dimension).getVocabularyIndex();
                    partialTraceDiagonalVector.add(vocabularyIndex, weight);
                }
            }
        }
        
        //save partial trace vector for later
        //#partialTraceDiagonalVectors[modeIndex - 1] = partialTraceDiagonalVector;
        partialTraceDiagonalVectors[modeIndex] = partialTraceDiagonalVector;
		
        
        return partialTraceDiagonalVector;
    }
	
	public void normalizePartialTraceDiagonalVectors(){
		if(partialTraceDiagonalVectors != null){
			for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
				//#NNumberVector partialTraceDiagonalVector = partialTraceDiagonalVectors[m - 1];
                NNumberVector partialTraceDiagonalVector = partialTraceDiagonalVectors[m];
				if(partialTraceDiagonalVector != null){
					partialTraceDiagonalVector.normalize();
				}
			}
		}
	}
    
    protected static NNumberVector importPartialTraceDiagonalVector(BufferedReader in) throws IOException{
        NNumberVector v = new NNumberVector(Vocabulary.getSize());
        
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</mode>")){
                break;
            }else{
                String[] entries = line.split(":");
                int vocabularyIndex;
                if(Helper.prettyRead){
                    vocabularyIndex = Vocabulary.getTargetWordIndex(entries[0]);
                }else{
                    vocabularyIndex = Integer.parseInt(entries[0]);
                }
                NNumber weight = NNumber.create(Double.parseDouble(entries[1]));
                v.setWeight(vocabularyIndex, weight);
            }
        }
        
        return v;
    }
    
    protected static NNumberVector[] importPartialTraceDiagonalVectors(BufferedReader in) throws IOException{
        //HashMap<Integer, NNumberVector> modeIndexPartialTraceDiagonalVectorMap = new HashMap<>();
        
        //int currentModeIndex = -1;
        //NNumberVector currentVector = null;
        
        NNumberVector[] ptdv = new NNumberVector[DepNeighbourhoodSpace.getOrder() + 1];
        
        String line;
        while((line = in.readLine()) != null){
            
            if(line.startsWith("<mode")){
                Matcher matcher = partialTracePattern.matcher(line);
                if(matcher.find()){
                    int modeIndex = Integer.parseInt(matcher.group(2));
                    NNumberVector v = importPartialTraceDiagonalVector(in);
                    ptdv[modeIndex] = v;
                }
            }else if(line.equals("</partialtracediagonals>")){
                break;
            }
        
        }
            
            /*if(line.startsWith("<mode")){
                Matcher matcher = partialTracePattern.matcher(line);
                if(matcher.find()){
                    currentModeIndex = Integer.parseInt(matcher.group(2));
                    currentVector = new NNumberVector(Vocabulary.getSize());
                }
            }else if(line.equals("</mode>") && currentModeIndex != -1){
                modeIndexPartialTraceDiagonalVectorMap.put(currentModeIndex, currentVector.getCopy());
                currentModeIndex = -1;
            }else if(currentModeIndex > -1){
                String[] entries = line.split(":");
                if(entries.length == 2){
                    int vocabularyIndex;
                    if(Helper.prettyRead){
                        vocabularyIndex = Vocabulary.getTargetWordIndex(entries[0]);
                    }else{
                        vocabularyIndex = Integer.parseInt(entries[0]);
                    }
                    NNumber weight = NNumber.create(Double.parseDouble(entries[1]));
                    if(currentVector != null) currentVector.setWeight(vocabularyIndex, weight);
                }
            }else if(line.equals("</partialtracediagonals>")){
                break;
            }
        }
        
        NNumberVector[] ptdv = null;
        if(!modeIndexPartialTraceDiagonalVectorMap.isEmpty()){
            //#ptdv = new NNumberVector[DepNeighbourhoodSpace.getOrder()];
            ptdv = new NNumberVector[DepNeighbourhoodSpace.getOrder() + 1];
            for(Integer modeIndex : modeIndexPartialTraceDiagonalVectorMap.keySet()){
                ptdv[modeIndex] = modeIndexPartialTraceDiagonalVectorMap.get(modeIndex);
            }
        }
        */
        
        return ptdv;
    }
    
    protected static void importBaseMatricesFromReader(ValueMatrix m, int cardinality, BufferedReader in) throws IOException{
        
        boolean isSortedByValue = true;
        NNumber lastValue = null;
        
        String line;
        int i=0;
        while((line = in.readLine()) != null){
            if(line.equals("</basematrices>")){
                if(isSortedByValue) m.setSorting(Helper.SORTED_BY_VALUE);
                break;
            }else{
                String[] entries = line.split("\t"); //value \t left base tensor \t right base tensor
                if(entries.length == 3 && i < cardinality){
                    NNumber value = NNumber.create(Float.parseFloat(entries[0]));
                    if(isSortedByValue && lastValue != null && value.compareTo(lastValue) == 1){
                        isSortedByValue = false;
                    }else{
                        lastValue = value;
                    }
                    BaseTensor leftBt = BaseTensor.importFromString(entries[1]);
                    BaseTensor rightBt = BaseTensor.importFromString(entries[2]);
                    ValueBaseMatrix bm = new ValueBaseMatrix(leftBt, rightBt, value);
                    m.setBaseMatrix(i, bm);
                    i++;
                }
            }
        }
        
    }
    
    public static ValueMatrix importFromReader(BufferedReader in, int cardinality) throws IOException{
        ValueMatrix m = new ValueMatrix(cardinality);
        
        String line;
        while((line = in.readLine()) != null){
            if(line.startsWith("<partialtracediagonals")){
                m.partialTraceDiagonalVectors = importPartialTraceDiagonalVectors(in);
            }else if(line.startsWith("<basematrices")){
                importBaseMatricesFromReader(m, cardinality, in);
            }else if(line.equals("</matrix>")){
                break;
            }
        }
        
        return m;
    }
    
    public static ValueMatrix importFromReader(BufferedReader in) throws IOException{
        ValueMatrix m = null;
        
        String line;
        while((line = in.readLine()) != null){
            
            if(line.startsWith("<matrix")){
                Matcher matcher = matrixPattern.matcher(line);
                if(matcher.find()){
                    String matrixType = matcher.group(2);
                    if(matrixType.equals("value")){
                        int cardinality = Integer.parseInt(matcher.group(3));
                        m = ValueMatrix.importFromReader(in, cardinality);
                    }
					String name = matcher.group(1);
                    if(name != null) m.setName(name);
                }
            }
            
        }

        return m;

    }
    
	protected void savePartialTraceVectorsToWriter(BufferedWriter out) throws IOException{
		boolean prettyPrint = Helper.prettyPrint;
        out.write("<partialtracediagonals>\n");
        for(int m=1; m<=DepNeighbourhoodSpace.getOrder(); m++){
			DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationCluster(m);
            NNumberVector partialTraceDiagonalVector = getPartialTraceDiagonalVector(m);
			out.write("<mode name=\"" + drc.getName() + "\" index=\"" + m + "\" dimensionality=\"" + DepNeighbourhoodSpace.getDimensionality() + "\" cardinality=\"" + partialTraceDiagonalVector.getCardinality() + "\">\n");
            //go through all target words
			for(int i=0; i<Vocabulary.getSize(); i++){
				NNumber weight = partialTraceDiagonalVector.getWeight(i);
				if(weight != null && !weight.isZero()){
					if(prettyPrint){
						out.write(Vocabulary.getTargetWord(i).getWord() + ":" + weight.getDoubleValue() + "\n");
					}else{
						out.write(i + ":" + weight.getDoubleValue() + "\n");
					}
				}
			}
			out.write("</mode>\n");
        }
        out.write("</partialtracediagonals>\n");
	}
	
    @Override
    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<matrix name=\"" + getName() + "\" type=\"value\" cardinality=\"" + getCardinality() + "\">\n");
        savePartialTraceVectorsToWriter(out);
        out.write("<basematrices>\n");
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = valueBaseMatrices[i];
            bm.exportToWriter(out);
        }
        out.write("</basematrices>\n");
        out.write("</matrix>\n");
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
	
    @Override
    public String toString(){
        String s = "value matrix, name=\"" + name + "\", card=" + getCardinality() + ", trace=" + trace() + "\npartials.cardPerMode:";
        for(int i=1; i<=DepNeighbourhoodSpace.getOrder(); i++){
            s += " " + i + ":" + partialTraceDiagonalVectors[i].getCardinality() + ",";
        }
        for(int i=0; i<Math.min(5, getCardinality()); i++){
            s += getBaseMatrix(i) + "\n";
        }
        return s + "...";
    }
    
    
    public static void main1(String[] args){
        TensorSpace.setOrder(5);
		TensorSpace.setDimensionality(300);
		TensorSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

        ValueBaseMatrix bm1 = new ValueBaseMatrix(NNumber.one());
		bm1.setValue(bm1.getValue().invert());
		bm1.getLeftBaseTensor().setDimensionAtMode(1, 5);
		
		//BaseTensor bt2 = new BaseTensor(NNumber.one());
		ValueBaseMatrix bm2 = new ValueBaseMatrix(NNumber.create(0.3123f));
		//bm2.setValue(btm.getValue().invert());
        //bm2.getRightBaseTensor().setDimensionAtMode(2, 7);
		
        ValueMatrix m1 = new ValueMatrix(2);
        m1.setBaseMatrix(0, bm2);
        m1.setBaseMatrix(1, bm1);
        
        ValueMatrix m2 = new ValueMatrix(1);
        m2.setBaseMatrix(0, bm1);
        
		System.out.println("bm1 = " + bm1);
		System.out.println("bm2 = " + bm2);
        System.out.println("<bm1,bm1> = " + bm1.innerProduct(bm1));
        System.out.println("<bm2,bm2> = " + bm2.innerProduct(bm2));
        System.out.println("<bm1,bm2> = " + bm1.innerProduct(bm2));
        System.out.println("m1 = bm1 + bm2");
        System.out.println("m2 = bm1");
        System.out.println("<m1, m1> = " + m1.innerProduct(m1));
        System.out.println("<m2, m2> = " + m2.innerProduct(m2));
        System.out.println("<m1, m2> = " + m1.innerProduct(m2));

    }

    public static void main2(String[] args){
        TensorSpace.setOrder(5);
		TensorSpace.setDimensionality(300);
		TensorSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        
        try{
            BufferedReader in = new BufferedReader(new FileReader("/local/such"));
            BufferedWriter out = new BufferedWriter(new FileWriter("/local/such2"));
            
            ValueMatrix m = ValueMatrix.importFromReader(in);
            System.out.println("such, " + m);
            
            ValueMatrix m2 = m.times(m);
            m2.saveToWriter(out);
            System.out.println("such * such, " + m2);
            
            in.close();
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
    
    public static void main3(String[] args){
        TensorSpace.setOrder(5);
		TensorSpace.setDimensionality(300);
		TensorSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        
        try{
            BufferedReader in = new BufferedReader(new FileReader("/local/such"));
            BufferedWriter out = new BufferedWriter(new FileWriter("/local/such2"));
            
            ValueMatrix m = ValueMatrix.importFromReader(in);
            System.out.println("such, " + m);
            
            NNumber ip1 = m.innerProduct(m);
            System.out.println("ip1 = " + ip1);
            System.out.println("ip1^-1 = " + ip1.reciprocal());
            System.out.println("sqrt(ip1) = " + ip1.sqrt());
            System.out.println("ip * ip^-1 = " + ip1.multiply(ip1.reciprocal()));
            System.out.println("1/sqrt(ip1) = " + ip1.sqrt().reciprocal());
            System.out.println("sqrt(1/ip1) = " + ip1.reciprocal().sqrt());
            
            //m.multiply(ip1.sqrt().reciprocal());
            //m.multiply(ip1.sqrt().reciprocal());
            m.multiply(ip1.reciprocal().sqrt());
            NNumber ip2 = m.innerProduct(m);
            System.out.println("ip2 = " + ip2);
            
            in.close();
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args){
        try{
            BufferedReader spaceIn = new BufferedReader(new FileReader("/local/Dropbox/VeraAndWilliam/dep-5up5down.space"));
            DepNeighbourhoodSpace.importFromReader(spaceIn);
            spaceIn.close();
            
            DepNeighbourhoodSpace.setDimensionality(300);
            DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
            
            BufferedReader in = new BufferedReader(new FileReader("/local/Dropbox/VeraAndWilliam/speech"));
            BufferedWriter outT = new BufferedWriter(new FileWriter("/local/Dropbox/VeraAndWilliam/speech.1t"));
            BufferedWriter outM = new BufferedWriter(new FileWriter("/local/Dropbox/VeraAndWilliam/speech.1m"));
            
            ValueMatrix m = ValueMatrix.importFromReader(in);
            ValueMatrix m1 = m.decompose();
            m1.saveToWriter(outM);
            ValueTensor t1 = m1.getUnderlyingOuterProductTensor();
            t1.exportToWriter(outT);
            
            in.close();
            outT.close();
            outM.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

}










/*
    public ValueMatrix times(ValueMatrix m){
        int productCardinality = getCardinality() * m.getCardinality();
        ValueMatrix product = new ValueMatrix(productCardinality);
        
        //collect base matrices for product in here
        TreeMap<Integer, ArrayList<ValueBaseMatrix>> exponentBaseMatricesMap = new TreeMap<>();
        
        int potentialCardinality=0;
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix thisBm = getBaseMatrix(i);
            for(int j=0; j<m.getCardinality(); j++){
                ValueBaseMatrix givenBm = m.getBaseMatrix(j);
                ValueBaseMatrix newBm = thisBm.times(givenBm);
                //don't add zeros (warning: this might lead to the product cardinality not being fully used)
                //if(!newBm.isZero()){
                if(newBm != null){
                    product.setBaseMatrix(potentialCardinality, newBm);
                    int exponent = newBm.getValue().logBaseDimensionalitySqrtToInt();
                    ArrayList<ValueBaseMatrix> baseMatricesMap = exponentBaseMatricesMap.get(exponent);
                    if(baseMatricesMap == null){
                        baseMatricesMap = new ArrayList<>();
                        exponentBaseMatricesMap.put(exponent, baseMatricesMap);
                    }
                    baseMatricesMap.add(newBm);
                    potentialCardinality++;
                }
            }
        }
        
        //reduce cardinality (sort all entries)
        product.reduceCardinality(getCardinality());
        
        return product;
    }


    public void reduceCardinality(int cardinality){

        //if(cardinality >= getCardinality()) System.out.println("[ValueMatrix] This is not a reduction of cardinality."); //DEBUG
        
        //define sorting data structure
		TreeSet<ValueBaseMatrix> sortedValueBaseMatrices = new TreeSet<>(new Comparator(){
			@Override
			public int compare(Object o1, Object o2) {
				ValueBaseMatrix bm1 = (ValueBaseMatrix) o1;
				ValueBaseMatrix bm2 = (ValueBaseMatrix) o2;
				int diffValue = bm1.getValue().compareTo(bm2.getValue());
				if(diffValue != 0){
					return diffValue;
				}else{
					return bm1.compareTo(bm2);
				}
			}
		});

        //sort base matrices
        for(int i=0; i<getCardinality(); i++){
            ValueBaseMatrix bm = valueBaseMatrices[i];
            if(bm != null){
                sortedValueBaseMatrices.add(valueBaseMatrices[i]);
            }
        }
        
        //keep only the [cardinality] largets base matrices
        ValueBaseMatrix[] newValueBaseMatrices = new ValueBaseMatrix[cardinality];
		for(int i=0; i<cardinality; i++){
			ValueBaseMatrix bm = sortedValueBaseMatrices.pollLast();
			if(bm == null) break;
			newValueBaseMatrices[i] = bm;
		}
        
        //replace array of base matrices
        sorting = Helper.SORTED_BY_VALUE;
        valueBaseMatrices = newValueBaseMatrices;
	}
    
    public void sortByValue(){
        reduceCardinality(getCardinality());
    }
*/
 
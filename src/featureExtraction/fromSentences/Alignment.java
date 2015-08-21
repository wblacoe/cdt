package featureExtraction.fromSentences;

import java.util.LinkedList;

//expects items to be added in reverse order
public class Alignment {

	private LinkedList<AlignmentPair> alignmentPairList;
	private LinkedList<Object> unalignedItemsList1, unalignedItemsList2;
	
	public Alignment(){
		alignmentPairList = new LinkedList<AlignmentPair>();
		unalignedItemsList1 = new LinkedList<Object>();
		unalignedItemsList2 = new LinkedList<Object>();
	}
	
	
	public LinkedList<AlignmentPair> getAlingmentPairList(){
		return alignmentPairList;
	}
	
	public LinkedList<Object> getUnalignedItemList1(){
		return unalignedItemsList1;
	}
	
	public LinkedList<Object> getUnalignedItemList2(){
		return unalignedItemsList2;
	}
	
	public void addAlignmentPair(Object object1, Object object2){
		alignmentPairList.addFirst(new AlignmentPair(object1, object2));
	}
	
	public void addUnalignedItem1(Object object){
		unalignedItemsList1.addFirst(object);
	}
	
	public void addUnalignedItem2(Object object){
		unalignedItemsList2.addFirst(object);
	}
	
	@Override
	public String toString(){
		String s = "aligned: {";
		for(AlignmentPair ap : alignmentPairList){
			s += ap.toString() + " ";
		}
		s+="}\nunaligned1: {";
		for(Object item : unalignedItemsList1){
			s += item.toString() + " ";
		}
		s+="}\nunaligned2: {";
		for(Object item : unalignedItemsList2){
			s += item.toString() + " ";
		}
		s += "}";
		
		return s;
	}
	
}

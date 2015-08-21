package featureExtraction.fromSentences;

public class AlignmentPair {

	private Object object1, object2;
	
	public AlignmentPair(Object object1, Object object2){
		this.object1 = object1;
		this.object2 = object2;
	}
	
	
	@Override
	public String toString(){
		return "<" + object1 + ", " + object2 + ">";
	}
	
}

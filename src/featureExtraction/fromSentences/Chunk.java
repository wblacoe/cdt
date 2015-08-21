package featureExtraction.fromSentences;

import java.util.ArrayList;

public class Chunk {

	private String type;
	private ArrayList<LabeledWord> labeldWordList;
	
	public Chunk(String type){
		this.type = type;
		labeldWordList = new ArrayList<LabeledWord>();
	}
	
	
	public String getType(){
		return type;
	}
	
	public int getSize(){
		return labeldWordList.size();
	}
	
	public void addLabeledWord(LabeledWord lw){
		labeldWordList.add(lw);
	}
	
	public ArrayList<LabeledWord> getLabeledWordList(){
		return labeldWordList;
	}
	
	/*public String toStringWithNoSpaces(){
		String s = "[" + type;
		for(WordWithPosTag wp : wordWithPosTagList){
			s += "_" + wp;
		}
		s += "]";
		
		return s;
	}
	*/
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof Chunk)) return false;
		
		Chunk c = (Chunk) o;
		if(!type.equals(c.getType())) return false;
		
		ArrayList<LabeledWord> cLabeledWordList = c.getLabeledWordList();
		if(labeldWordList.size() != cLabeledWordList.size()) return false;
		
		for(int i=0; i<labeldWordList.size(); i++){
			if(!labeldWordList.get(i).equals(cLabeledWordList.get(i))) return false;
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
		hash = 71 * hash + (this.labeldWordList != null ? this.labeldWordList.hashCode() : 0);
		return hash;
	}
	
	@Override
	public String toString(){
		String s = "[" + type;
		for(LabeledWord lw : labeldWordList){
			s += " " + lw.getWord();
		}
		s += "]";
		
		return s;
	}
	
}

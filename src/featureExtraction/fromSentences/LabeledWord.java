package featureExtraction.fromSentences;

public class LabeledWord {

	private String word, posTag, chunkType;
	
	public LabeledWord(String word, String posTag, String chunkType){
		this.word = word;
		this.posTag = posTag;
		this.chunkType = chunkType;
	}
	
	public String getWord(){
		return word;
	}
	
	public String getPosTag(){
		return posTag;
	}
	
	public String getChunkType(){
		return chunkType;
	}
	
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof LabeledWord)) return false;
		LabeledWord lw = (LabeledWord) o;
		
		return word.equals(lw.getWord()) && posTag.equals(lw.posTag) && chunkType.equals(lw.getChunkType());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + (this.word != null ? this.word.hashCode() : 0);
		hash = 17 * hash + (this.posTag != null ? this.posTag.hashCode() : 0);
		hash = 17 * hash + (this.chunkType != null ? this.chunkType.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString(){
		//return "(" + posTag + " " + word + ")";
		//return word;
		return "[" + chunkType + " " + word + "]";
	}
}

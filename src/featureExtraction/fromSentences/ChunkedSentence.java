package featureExtraction.fromSentences;

import LBJ2.nlp.SentenceSplitter;
import LBJ2.nlp.Word;
import LBJ2.nlp.WordSplitter;
import LBJ2.nlp.seg.PlainToTokenParser;
import LBJ2.parse.Parser;
import cdt.Helper;
import edu.illinois.cs.cogcomp.lbj.chunk.Chunker;
import java.util.ArrayList;

public class ChunkedSentence {

	private String rawSentence;
	private String[] wordArray;
	private ArrayList<LabeledWord> labeledWordList;
	private ArrayList<Chunk> chunkList;
	
	public ChunkedSentence(String rawSentence){
		this.rawSentence = rawSentence;
		wordArray = rawSentence.split(" ");
		labeledWordList = new ArrayList<LabeledWord>();
		chunkList = new ArrayList<Chunk>();
	}

	
	public String[] getWordArray(){
		return wordArray;
	}
	
	public int getLength(){
		return wordArray.length;
	}
	
	public void addChunk(Chunk c){
		chunkList.add(c);
	}
	
	public void createChunks(){
		Helper.report("[ChunkedSentence] Chunking sentence \"" + rawSentence + "\"..."); //DEBUG
		
		Chunker chunker = new Chunker();
		Parser parser =
		  new PlainToTokenParser(
			  new WordSplitter(
				new SentenceSplitter(
					new String[]{ rawSentence }
				)
			)
		);
		String previous = "";

		Chunk currentChunk = null;
		for(Word w = (Word) parser.next(); w != null; w = (Word) parser.next()){
			String prediction = chunker.discreteValue(w);
			if(prediction.equals("O")) continue; //skip words outside of any chunks
			String chunkType = prediction.substring(2);
			LabeledWord lw = new LabeledWord(w.form, w.partOfSpeech, chunkType);
			labeledWordList.add(lw);
			
			if (prediction.startsWith("B-") || prediction.startsWith("I-") && !previous.endsWith(prediction.substring(2))){
				currentChunk = new Chunk(chunkType);
				addChunk(currentChunk);
			}
			
			currentChunk.addLabeledWord(lw);

			previous = prediction;
		}
		
		Helper.report("[ChunkedSentence] ...Finished chunking sentence \"" + this.toString() + "\""); //DEBUG
	}
	
	public ArrayList<Chunk> getChunkList(){
		return chunkList;
	}
	
	public Chunk[] getChunkArray(){
		Chunk[] chunkArray = new Chunk[chunkList.size()];
		
		int i=0;
		for(Chunk c : chunkList){
			chunkArray[i] = c;
			i++;
		}
		
		return chunkArray;
	}
	
	public String[] getChunkTypeArray(){
		String[] chunkTypeArray = new String[chunkList.size()];
		
		int i=0;
		for(Chunk c : chunkList){
			chunkTypeArray[i] = c.getType();
			i++;
		}
		
		return chunkTypeArray;
	}

	public LabeledWord[] getLabeledWordArray(){
		LabeledWord[] labeledWordArray = new LabeledWord[labeledWordList.size()];
		
		int i=0;
		for(LabeledWord lw : labeledWordList){
			labeledWordArray[i] = lw;
			i++;
		}
		
		return labeledWordArray;
	}

	@Override
	public String toString(){
		String s = "";
		for(Chunk c : chunkList){
			s += c.toString();
		}
		
		return s;
	}
	
	
	public static void main(String[] args){
		ChunkedSentence cs1 = new ChunkedSentence("A brown and white dog is running through the tall grass");
		cs1.createChunks();
		System.out.println(cs1);
		
		ChunkedSentence cs2 = new ChunkedSentence("A brown and white dog is moving through the wild grass");
		cs2.createChunks();
		System.out.println(cs2);
	}
	
}

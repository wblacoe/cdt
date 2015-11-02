package featureExtraction.fromSentences;

import featureExtraction.FeatureVector;
import java.util.ArrayList;
import java.util.LinkedList;

public class Illinois {

	public FeatureVector featureVector;
	
	public Illinois(){
		featureVector = new FeatureVector(-1);
	}
	
	
	private double getSimilarity(LabeledWord lw1, LabeledWord lw2){
		return Math.random();
	}
	
	/*private boolean isSimpleMatch(LabeledWord lw1, LabeledWord lw2){
		return getSimilarity(lw1, lw2) > 0.2;
	}
	
	private boolean isStrictMatch(LabeledWord lw1, LabeledWord lw2){
		return lw1.getChunkType().equals(lw2.getChunkType()) && isSimpleMatch(lw1, lw2);
	}
	*/
	
	public void createFeatureVector(String sentence1, String sentence2){
		//chunking
		ChunkedSentence cs1 = new ChunkedSentence(sentence1);
		cs1.createChunks();
		ChunkedSentence cs2 = new ChunkedSentence(sentence2);
		cs2.createChunks();
		
		//aligning
		Aligner aligner;
		aligner = new Aligner(cs1.getWordArray(), cs2.getWordArray());
		Alignment wordsAlignment = aligner.needlemanWunschAndGetAlignment();
		aligner = new Aligner(cs1.getLabeledWordArray(), cs2.getLabeledWordArray());
		Alignment labeledWordsAlignment = aligner.needlemanWunschAndGetAlignment();
		aligner = new Aligner(cs1.getChunkArray(), cs2.getChunkArray());
		Alignment chunkAlignment = aligner.needlemanWunschAndGetAlignment();
		aligner = new Aligner(cs1.getChunkTypeArray(), cs2.getChunkTypeArray());
		Alignment chunkTypeAlignment = aligner.needlemanWunschAndGetAlignment();
		
		//compute features and save in feature vector
		int s1NumberOfWords = cs1.getLength();
		int s2NumberOfWords = cs2.getLength();
		boolean isS1Longer = s1NumberOfWords > s2NumberOfWords;
		featureVector.setValue("s1 #words", 1.0 * s1NumberOfWords);
		featureVector.setValue("s2 #words", 1.0 * s2NumberOfWords);
		featureVector.setValue("sLonger #words", 1.0 * (isS1Longer ? s1NumberOfWords : s2NumberOfWords));
		featureVector.setValue("sShorter #words", 1.0 * (isS1Longer ? s2NumberOfWords : s1NumberOfWords));
		
		int numberOfAlignedWords = wordsAlignment.getAlingmentPairList().size();
		featureVector.setValue("#aligned words", 1.0 * numberOfAlignedWords);
		featureVector.setValue("#aligned words / s1 #words", 1.0 * numberOfAlignedWords / s1NumberOfWords);
		featureVector.setValue("#aligned words / s2 #words", 1.0 * numberOfAlignedWords / s2NumberOfWords);
		featureVector.setValue("#aligned words / sLonger #words", 1.0 * numberOfAlignedWords / (isS1Longer ? s1NumberOfWords : s2NumberOfWords));
		featureVector.setValue("#aligned words / sShorter #words", 1.0 * numberOfAlignedWords / (isS1Longer ? s2NumberOfWords : s1NumberOfWords));
		
		LinkedList<Object> s1UnalignedChunkList = chunkAlignment.getUnalignedItemList1();
		LinkedList<Object> s2UnalignedChunkList = chunkAlignment.getUnalignedItemList2();
		int s1NumberOfUnalignedChunks = s1UnalignedChunkList.size();
		int s2NumberOfUnalignedChunks = s2UnalignedChunkList.size();
		
		double s12AverageUnalignedChunkSize = 0.0;
		int s1MaxUnalignedChunkSize = Integer.MIN_VALUE;
		int s1MinUnalignedChunkSize = Integer.MAX_VALUE;
		double s1AverageUnalignedChunkSize = 0.0;
		for(Object unalignedChunk : s1UnalignedChunkList){
			int unalignedChunkSize = ((Chunk) unalignedChunk).getSize();
			if(unalignedChunkSize > s1MaxUnalignedChunkSize) s1MaxUnalignedChunkSize = unalignedChunkSize;
			if(unalignedChunkSize < s1MinUnalignedChunkSize) s1MinUnalignedChunkSize = unalignedChunkSize;
			s1AverageUnalignedChunkSize += unalignedChunkSize;
			s12AverageUnalignedChunkSize += unalignedChunkSize;
		}
		s1AverageUnalignedChunkSize /= s1NumberOfUnalignedChunks;
		
		int s2MaxUnalignedChunkSize = Integer.MIN_VALUE;
		int s2MinUnalignedChunkSize = Integer.MAX_VALUE;
		double s2AverageUnalignedChunkSize = 0.0;
		for(Object unalignedChunk : s2UnalignedChunkList){
			int unalignedChunkSize = ((Chunk) unalignedChunk).getSize();
			if(unalignedChunkSize > s2MaxUnalignedChunkSize) s2MaxUnalignedChunkSize = unalignedChunkSize;
			if(unalignedChunkSize < s2MinUnalignedChunkSize) s2MinUnalignedChunkSize = unalignedChunkSize;
			s2AverageUnalignedChunkSize += unalignedChunkSize;
			s12AverageUnalignedChunkSize += unalignedChunkSize;
		}
		s2AverageUnalignedChunkSize /= s2NumberOfUnalignedChunks;
		
		int s12MaxUnalignedChunkSize = Math.max(s1MaxUnalignedChunkSize, s2MaxUnalignedChunkSize);
		int s12MinUnalignedChunkSize = Math.min(s1MinUnalignedChunkSize, s2MinUnalignedChunkSize);
		s12AverageUnalignedChunkSize /= (s1NumberOfUnalignedChunks + s2NumberOfUnalignedChunks);
		
		featureVector.setValue("s1 max unaligned chunk length", 1.0 * s1MaxUnalignedChunkSize);
		featureVector.setValue("s1 min unaligned chunk length", 1.0 * s1MinUnalignedChunkSize);
		featureVector.setValue("s1 average unaligned chunk length", s1AverageUnalignedChunkSize);
		featureVector.setValue("s2 max unaligned chunk length", 1.0 * s2MaxUnalignedChunkSize);
		featureVector.setValue("s2 min unaligned chunk length", 1.0 * s2MinUnalignedChunkSize);
		featureVector.setValue("s2 average unaligned chunk length", s2AverageUnalignedChunkSize);
		featureVector.setValue("s12 max unaligned chunk length", 1.0 * s12MaxUnalignedChunkSize);
		featureVector.setValue("s12 min unaligned chunk length", 1.0 * s12MinUnalignedChunkSize);
		featureVector.setValue("s12 average unaligned chunk length", s12AverageUnalignedChunkSize);
		featureVector.setValue("sLonger max unaligned chunk length", 1.0 * (isS1Longer ? s1MaxUnalignedChunkSize : s2MaxUnalignedChunkSize));
		featureVector.setValue("sLonger min unaligned chunk length", 1.0 * (isS1Longer ? s1MinUnalignedChunkSize : s2MinUnalignedChunkSize));
		featureVector.setValue("sLonger average unaligned chunk length", (isS1Longer ? s1AverageUnalignedChunkSize : s2AverageUnalignedChunkSize));
		featureVector.setValue("sShorter max unaligned chunk length", 1.0 * (isS1Longer ? s2MaxUnalignedChunkSize : s1MaxUnalignedChunkSize));
		featureVector.setValue("sShorter min unaligned chunk length", 1.0 * (isS1Longer ? s2MinUnalignedChunkSize : s1MinUnalignedChunkSize));
		featureVector.setValue("sShorter average unaligned chunk length", (isS1Longer ? s2AverageUnalignedChunkSize : s1AverageUnalignedChunkSize));

		featureVector.setValue("s1 #unaligned chunks", 1.0 * s1NumberOfUnalignedChunks);
		featureVector.setValue("s2 #unaligned chunks", 1.0 * s2NumberOfUnalignedChunks);
		featureVector.setValue("sLonger #unaligned chunks", 1.0 * (isS1Longer ? s1NumberOfUnalignedChunks : s2NumberOfUnalignedChunks));
		featureVector.setValue("sShorter #unaligned chunks", 1.0 * (isS1Longer ? s2NumberOfUnalignedChunks : s1NumberOfUnalignedChunks));

		LinkedList<Object> s1UnalignedLabeledWordList = labeledWordsAlignment.getUnalignedItemList1();
		LinkedList<Object> s2UnalignedLabeledWordList = labeledWordsAlignment.getUnalignedItemList2();
		int s1NumberOfUnalignedLabeledWords = s1UnalignedLabeledWordList.size();
		int s2NumberOfUnalignedLabeledWords = s2UnalignedLabeledWordList.size();

		featureVector.setValue("s1 #unaligned labeled words", 1.0 * s1NumberOfUnalignedLabeledWords);
		featureVector.setValue("s2 #unaligned labeled words", 1.0 * s2NumberOfUnalignedLabeledWords);
		featureVector.setValue("sLonger #unaligned labeled words", 1.0 * (isS1Longer ? s1NumberOfUnalignedLabeledWords : s2NumberOfUnalignedLabeledWords));
		featureVector.setValue("sShorter #unaligned labeled words", 1.0 * (isS1Longer ? s2NumberOfUnalignedLabeledWords : s1NumberOfUnalignedLabeledWords));
		
		int numberOfSimpleMatchedLabeledWords = 0; //i.e. amount of matches among simple pairs
		double maxSimpleMatchedLabeledWordsSimilarity = Integer.MIN_VALUE;
		double minSimpleMatchedLabeledWordsSimilarity = Integer.MAX_VALUE;
		double averageSimpleMatchedLabeledWordsSimilarity = 0.0;
		int numberOfStrictMatchedLabeledWords = 0; //i.e. amount of matches among strict pairs
		ArrayList<int[]> simpleMatchedLabeledWordsArcs = new ArrayList<int[]>(); //each entry (i,j) is an arc connecting the i-th object with the j-th
		double maxStrictMatchedLabeledWordsSimilarity = Integer.MIN_VALUE;
		double minStrictMatchedLabeledWordsSimilarity = Integer.MAX_VALUE;
		double averageStrictMatchedLabeledWordsSimilarity = 0.0;
		ArrayList<int[]> strictMatchedLabeledWordsArcs = new ArrayList<int[]>(); //each entry (i,j) is an arc connecting the i-th object with the j-th
		double similarityThreshold = 0.01; //TODO check this
		
		for(int i=0; i<s1UnalignedLabeledWordList.size(); i++){
			for(int j=0; j<s2UnalignedLabeledWordList.size(); j++){
				LabeledWord lw1 = (LabeledWord) s1UnalignedLabeledWordList.get(i);
				LabeledWord lw2 = (LabeledWord) s1UnalignedLabeledWordList.get(j);
				double similarity = getSimilarity(lw1, lw2);
				
				//simple case
				if(similarity > maxSimpleMatchedLabeledWordsSimilarity) maxSimpleMatchedLabeledWordsSimilarity = similarity;
				if(similarity < minSimpleMatchedLabeledWordsSimilarity) minSimpleMatchedLabeledWordsSimilarity = similarity;
				averageSimpleMatchedLabeledWordsSimilarity += similarity;
				if(similarity > similarityThreshold){
					numberOfSimpleMatchedLabeledWords++;
					simpleMatchedLabeledWordsArcs.add(new int[]{ j, i });
				}
				
				//strict case
				if(lw1.getChunkType().equals(lw2.getChunkType())){
					if(similarity > maxStrictMatchedLabeledWordsSimilarity) maxStrictMatchedLabeledWordsSimilarity = similarity;
					if(similarity < minStrictMatchedLabeledWordsSimilarity) minStrictMatchedLabeledWordsSimilarity = similarity;
					averageStrictMatchedLabeledWordsSimilarity += similarity;
					if(similarity > similarityThreshold){
						numberOfStrictMatchedLabeledWords++;
						strictMatchedLabeledWordsArcs.add(new int[]{ j, i });
					}
				}
			}
		}
		averageSimpleMatchedLabeledWordsSimilarity /= (s1NumberOfUnalignedLabeledWords * s2NumberOfUnalignedLabeledWords);
		averageStrictMatchedLabeledWordsSimilarity /= (s1NumberOfUnalignedLabeledWords * s2NumberOfUnalignedLabeledWords);
		
		//how often do the arcs (arc1[0], arc1[1])-(arc2[0], arc2[1]) cross?
		int numberOfSimpleMatchedLabeledWordsArcCrossings = 0;
		int numberOfSimpleMatchedLabeledWordsArcs = 0;
		for(int i=0; i<simpleMatchedLabeledWordsArcs.size(); i++){
			int[] arc1 = simpleMatchedLabeledWordsArcs.get(i);
			for(int j=0; j<i; j++){
				int[] arc2 = simpleMatchedLabeledWordsArcs.get(j);
				if(arc2[0] < arc1[1] && arc1[0] < arc2[1]) numberOfSimpleMatchedLabeledWordsArcCrossings++;
				numberOfSimpleMatchedLabeledWordsArcs++;
			}
		}
		int numberOfStrictMatchedLabeledWordsArcCrossings = 0;
		int numberOfStrictMatchedLabeledWordsArcs = 0;
		for(int i=0; i<strictMatchedLabeledWordsArcs.size(); i++){
			int[] arc1 = strictMatchedLabeledWordsArcs.get(i);
			for(int j=0; j<i; j++){
				int[] arc2 = strictMatchedLabeledWordsArcs.get(j);
				if(arc2[0] < arc1[1] && arc1[0] < arc2[1]) numberOfStrictMatchedLabeledWordsArcCrossings++;
				numberOfStrictMatchedLabeledWordsArcs++;
			}
		}
		
		featureVector.setValue("#matched unaligned labeled words (simple matching)", 1.0 * numberOfSimpleMatchedLabeledWords);
		featureVector.setValue("#matched unaligned labeled words / s1 #unaligned labeled words(simple matching)", 1.0 * numberOfSimpleMatchedLabeledWords / s1NumberOfUnalignedLabeledWords);
		featureVector.setValue("#matched unaligned labeled words / s2 #unaligned labeled words(simple matching)", 1.0 * numberOfSimpleMatchedLabeledWords / s2NumberOfUnalignedLabeledWords);
		featureVector.setValue("#matched unaligned labeled words / sLonger #unaligned labeled words(simple matching)", 1.0 * numberOfSimpleMatchedLabeledWords / (isS1Longer ? s1NumberOfUnalignedLabeledWords : s2NumberOfUnalignedLabeledWords));
		featureVector.setValue("#matched unaligned labeled words / sShorter #unaligned labeled words(simple matching)", 1.0 * numberOfSimpleMatchedLabeledWords / (isS1Longer ? s2NumberOfUnalignedLabeledWords : s1NumberOfUnalignedLabeledWords));
		featureVector.setValue("max matched unaligned labeled words similarity (simple matching)", maxSimpleMatchedLabeledWordsSimilarity);
		featureVector.setValue("min matched unaligned labeled words similarity (simple matching)", minSimpleMatchedLabeledWordsSimilarity);
		featureVector.setValue("average matched unaligned labeled words similarity (simple matching)", averageSimpleMatchedLabeledWordsSimilarity);
		featureVector.setValue("#crossings among unaligned chunk matchings (simple matching)", 1.0 * numberOfSimpleMatchedLabeledWordsArcCrossings);
		featureVector.setValue("#crossings among unaligned chunk matchings (simple matching)", 1.0 * numberOfSimpleMatchedLabeledWordsArcCrossings / numberOfSimpleMatchedLabeledWordsArcs);
		
		featureVector.setValue("#matched unaligned labeled words (strict matching)", 1.0 * numberOfStrictMatchedLabeledWords);
		featureVector.setValue("#matched unaligned labeled words / s1 #unaligned labeled words(strict matching)", 1.0 * numberOfStrictMatchedLabeledWords / s1NumberOfUnalignedLabeledWords);
		featureVector.setValue("#matched unaligned labeled words / s2 #unaligned labeled words(strict matching)", 1.0 * numberOfStrictMatchedLabeledWords / s2NumberOfUnalignedLabeledWords);
		featureVector.setValue("#matched unaligned labeled words / sLonger #unaligned labeled words(strict matching)", 1.0 * numberOfStrictMatchedLabeledWords / (isS1Longer ? s1NumberOfUnalignedLabeledWords : s2NumberOfUnalignedLabeledWords));
		featureVector.setValue("#matched unaligned labeled words / sShorter #unaligned labeled words(strict matching)", 1.0 * numberOfStrictMatchedLabeledWords / (isS1Longer ? s2NumberOfUnalignedLabeledWords : s1NumberOfUnalignedLabeledWords));
		featureVector.setValue("max matched unaligned labeled words similarity (strict matching)", maxStrictMatchedLabeledWordsSimilarity);
		featureVector.setValue("min matched unaligned labeled words similarity (strict matching)", minStrictMatchedLabeledWordsSimilarity);
		featureVector.setValue("average matched unaligned labeled words similarity (strict matching)", averageStrictMatchedLabeledWordsSimilarity);
		featureVector.setValue("#crossings among unaligned chunk matchings (strict matching)", 1.0 * numberOfStrictMatchedLabeledWordsArcCrossings);
		featureVector.setValue("#crossings among unaligned chunk matchings (strict matching)", 1.0 * numberOfStrictMatchedLabeledWordsArcCrossings / numberOfSimpleMatchedLabeledWordsArcs);

		int s1NumberOfChunkLabels = cs1.getChunkArray().length;
		int s2NumberOfChunkLabels = cs2.getChunkArray().length;
		int numberOfMatchingChunkLabels = chunkTypeAlignment.getAlingmentPairList().size();
		int s1NumberOfUnalignedChunkLabels = chunkTypeAlignment.getUnalignedItemList1().size();
		int s2NumberOfUnalignedChunkLabels = chunkTypeAlignment.getUnalignedItemList2().size();
		
		featureVector.setValue("s1 #chunk labels", 1.0 * s1NumberOfChunkLabels);
		featureVector.setValue("s2 #chunk labels", 1.0 * s2NumberOfChunkLabels);
		featureVector.setValue("sLonger #chunk labels", 1.0 * (isS1Longer ? s1NumberOfChunkLabels : s2NumberOfChunkLabels));
		featureVector.setValue("sShorter #chunk labels", 1.0 * (isS1Longer ? s2NumberOfChunkLabels : s1NumberOfChunkLabels));
		featureVector.setValue("s1 #unaligned chunk labels", 1.0 * s1NumberOfUnalignedChunkLabels);
		featureVector.setValue("s2 #unaligned chunk labels", 1.0 * s2NumberOfUnalignedChunkLabels);
		featureVector.setValue("sLonger #unaligned chunk labels", 1.0 * (isS1Longer ? s1NumberOfUnalignedChunkLabels : s2NumberOfUnalignedChunkLabels));
		featureVector.setValue("sShorter #unaligned chunk labels", 1.0 * (isS1Longer ? s2NumberOfUnalignedChunkLabels : s1NumberOfUnalignedChunkLabels));
		featureVector.setValue("s1 #unaligned chunk labels / s1 #chunk labels", 1.0 * s1NumberOfUnalignedChunkLabels / s1NumberOfChunkLabels);
		featureVector.setValue("s2 #unaligned chunk labels / s2 #chunk labels", 1.0 * s2NumberOfUnalignedChunkLabels / s2NumberOfChunkLabels);
		featureVector.setValue("sLonger #unaligned chunk labels / sLonger #chunk labels", 1.0 * (isS1Longer ? s1NumberOfUnalignedChunkLabels / s1NumberOfChunkLabels : s2NumberOfUnalignedChunkLabels / s2NumberOfChunkLabels));
		featureVector.setValue("sShorter #unaligned chunk labels / sShorter #chunk labels", 1.0 * (isS1Longer ? s2NumberOfUnalignedChunkLabels / s2NumberOfChunkLabels : s1NumberOfUnalignedChunkLabels / s1NumberOfChunkLabels));
		featureVector.setValue("#matched chunk labels", 1.0 * numberOfMatchingChunkLabels);
		featureVector.setValue("#matched chunk labels / s1 #chunk labels", 1.0 * numberOfMatchingChunkLabels / s1NumberOfChunkLabels);
		featureVector.setValue("#matched chunk labels / s2 #chunk labels", 1.0 * numberOfMatchingChunkLabels / s2NumberOfChunkLabels);
		featureVector.setValue("#matched chunk labels / sLonger #chunk labels", 1.0 * (isS1Longer ? numberOfMatchingChunkLabels / s1NumberOfChunkLabels : numberOfMatchingChunkLabels / s2NumberOfChunkLabels));
		featureVector.setValue("#matched chunk labels / sShorter #chunk labels", 1.0 * (isS1Longer ? numberOfMatchingChunkLabels / s2NumberOfChunkLabels : numberOfMatchingChunkLabels / s1NumberOfChunkLabels));
	}
	
	
	
	public static void main(String[] args){
		String s1 = "a brown and white dog is running through the tall grass";
		String s2 = "a brown and white dog is moving through the grass";
		String s3 = "a brown and white dog is moving through the wild grass";
		
		Illinois il = new Illinois();
		il.createFeatureVector(s1, s2);
		System.out.println(il.featureVector.toStringDetailed());
		
		il = new Illinois();
		il.createFeatureVector(s1, s3);
		System.out.println(il.featureVector.toStringDetailed());
	}
	
}

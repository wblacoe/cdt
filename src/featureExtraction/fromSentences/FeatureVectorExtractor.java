package featureExtraction.fromSentences;

import cdt.Helper;
import corpus.dep.converter.DepTree;
import experiment.AbstractInstance;
import experiment.Dataset;
import featureExtraction.AbstractFeatureVectorExtractor;
import featureExtraction.FeatureVector;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import numberTypes.NNumber;

public class FeatureVectorExtractor extends AbstractFeatureVectorExtractor {

    private String name;
    protected HashSet<String> negationWordSet;
    private InnerProductsCache ipc;
	
	public FeatureVectorExtractor(InnerProductsCache ipc){
        super();
        negationWordSet = new HashSet<>();
        importNegationWords(null); //TODO
        this.ipc = ipc;
	}
    
    //import negation words from negation words list file
	private void importNegationWords(File negationWordsListFile){
		if(negationWordsListFile.exists()){
            try{
                BufferedReader in = Helper.getFileReader(negationWordsListFile);
                
                String line;
                while((line = in.readLine()) != null){
                    line = line.trim();
                    negationWordSet.add(line);
                }
                
                in.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            if(negationWordSet != null){
                Helper.report("[FeatureVectorExtractor] Imported " + negationWordsListFile + " negation words from \"" + negationWordsListFile.getAbsolutePath() + "\".");
            }else{
                Helper.report("[FeatureVectorExtractor] There was a problem while importing negation words from \"" + negationWordsListFile.getAbsolutePath() + "\".");
            }
        }else{
            Helper.report("[FeatureVectorExtractor] Negation word list file does not exist!");
        }
	}
    
    private boolean sentenceContainsNegationWord(String[] sentence){
		for(String word : sentence){
			if(negationWordSet.contains(word)){
				return true;
			}
		}
		return false;
	}
    
    private double getUnigramOverlap(String[] sentence1, String[] sentence2, boolean normalize) {

		HashMap<String, Integer> wordCountMap1 = new HashMap<String, Integer>();
		HashMap<String, Integer> wordCountMap2 = new HashMap<String, Integer>();

		for(String word : sentence1){
			Integer existingCount = wordCountMap1.get(word);
			if(existingCount == null){
				wordCountMap1.put(word, 1);
			}else{
				wordCountMap1.put(word, existingCount + 1);
			}
		}

		for(String word : sentence2){
			Integer existingCount = wordCountMap2.get(word);
			if(existingCount == null){
				wordCountMap2.put(word, 1);
			}else{
				wordCountMap2.put(word, existingCount + 1);
			}
		}

		int unigramOverlap = 0;
		int sentenceUnionSize = 0;
		for (String word : wordCountMap1.keySet()) {
			Integer existingCount2 = wordCountMap2.get(word);
			if (existingCount2 != null && existingCount2 > 0) {
				unigramOverlap += Math.min(wordCountMap1.get(word), existingCount2);
				sentenceUnionSize += Math.max(wordCountMap1.get(word), wordCountMap2.get(word));
			}else{
				sentenceUnionSize += wordCountMap1.get(word);
			}
		}
		double unigramOverlapNormalized = 1.0 * unigramOverlap / sentenceUnionSize;

		return (normalize ? unigramOverlapNormalized : 1.0 * unigramOverlap);
	}
	
	private double getBigramOverlap(String[] sentence1, String[] sentence2, boolean normalize) {

		HashMap<String, Integer> wordCountMap1 = new HashMap<String, Integer>();
		HashMap<String, Integer> wordCountMap2 = new HashMap<String, Integer>();

		for (int i = 0; i < sentence1.length - 1; i++) {
			String bigram = sentence1[i] + "_" + sentence1[i+1];
			Integer existingCount = wordCountMap1.get(bigram);
			if (existingCount == null) {
				wordCountMap1.put(bigram, 1);
			} else {
				wordCountMap1.put(bigram, existingCount + 1);
			}
		}

		for (int i = 0; i < sentence2.length - 1; i++) {
			String bigram = sentence2[i] + "_" + sentence2[i+1];
			Integer existingCount = wordCountMap2.get(bigram);
			if (existingCount == null) {
				wordCountMap2.put(bigram, 1);
			} else {
				wordCountMap2.put(bigram, existingCount + 1);
			}
		}

		int bigramOverlap = 0;
		int sentenceUnionSize = 0;
		for (String bigram : wordCountMap1.keySet()) {
			Integer existingCount2 = wordCountMap2.get(bigram);
			if (existingCount2 != null && existingCount2 > 0) {
				bigramOverlap += Math.min(wordCountMap1.get(bigram), existingCount2);
				sentenceUnionSize += Math.max(wordCountMap1.get(bigram), wordCountMap2.get(bigram));
			}else{
				sentenceUnionSize += wordCountMap1.get(bigram);
			}
		}
		double bigramOverlapNormalized = 1.0 * bigramOverlap / sentenceUnionSize;

		return (normalize ? bigramOverlapNormalized : 1.0 * bigramOverlap);
	}
    
    //this can be changed
    public double similarity(String word1, String word2){
        NNumber ip11 = ipc.getInnerProduct(word1, word1, true);
        NNumber ip22 = ipc.getInnerProduct(word2, word2, true);
        NNumber ip12 = ipc.getInnerProduct(word1, word2, true);
        
        NNumber similarity = ip12.multiply(ip11.multiply(ip22).reciprocal());
        
        return similarity.getDoubleValue();
    }


    
    //private FeatureVector extract(int index, DepTree depTree1, DepTree depTree2){
    private FeatureVector getChunkingFeatureVector(int index, String sentence1, String sentence2){
		Helper.report("[FeatureVectorExtractorThread] (" + name + ") Extracting Illinois features for #" + index); //DEBUG
		FeatureVector fv = new FeatureVector(index);
		
        //String[] sentence1AsArray = depTree1.getSentenceAsArray();
		//String sentence1 = sentence1AsArray[0];
		//for(int i=1; i<sentence1AsArray.length; i++) sentence1 += " " + sentence1AsArray[i];
        //String[] sentence2AsArray = depTree2.getSentenceAsArray();
		//String sentence2 = sentence2AsArray[0];
		//for(int i=1; i<sentence2AsArray.length; i++) sentence2 += " " + sentence2AsArray[i];
				
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
		fv.setValue("s1 #words", 1.0 * s1NumberOfWords);
		fv.setValue("s2 #words", 1.0 * s2NumberOfWords);
		fv.setValue("sLonger #words", 1.0 * (isS1Longer ? s1NumberOfWords : s2NumberOfWords));
		fv.setValue("sShorter #words", 1.0 * (isS1Longer ? s2NumberOfWords : s1NumberOfWords));
		
		int numberOfAlignedWords = wordsAlignment.getAlingmentPairList().size();
		fv.setValue("#aligned words", 1.0 * numberOfAlignedWords);
		fv.setValue("#aligned words / s1 #words", 1.0 * numberOfAlignedWords / s1NumberOfWords);
		fv.setValue("#aligned words / s2 #words", 1.0 * numberOfAlignedWords / s2NumberOfWords);
		fv.setValue("#aligned words / sLonger #words", 1.0 * numberOfAlignedWords / (isS1Longer ? s1NumberOfWords : s2NumberOfWords));
		fv.setValue("#aligned words / sShorter #words", 1.0 * numberOfAlignedWords / (isS1Longer ? s2NumberOfWords : s1NumberOfWords));
		
		LinkedList<Object> s1UnalignedChunkList = chunkAlignment.getUnalignedItemList1();
		LinkedList<Object> s2UnalignedChunkList = chunkAlignment.getUnalignedItemList2();
		int s1NumberOfUnalignedChunks = s1UnalignedChunkList.size();
		int s2NumberOfUnalignedChunks = s2UnalignedChunkList.size();
		
		int s1MaxUnalignedChunkSize;
		int s1MinUnalignedChunkSize;
		double s1AverageUnalignedChunkSize = 0.0;
		int s2MaxUnalignedChunkSize;
		int s2MinUnalignedChunkSize;
		double s2AverageUnalignedChunkSize = 0.0;
		double s12AverageUnalignedChunkSize = 0.0;
		
		if(s1NumberOfUnalignedChunks == 0){
			s1MaxUnalignedChunkSize = 0;
			s1MinUnalignedChunkSize = 0;
		}else{
			s1MaxUnalignedChunkSize = Integer.MIN_VALUE;
			s1MinUnalignedChunkSize = Integer.MAX_VALUE;
			for(Object unalignedChunk : s1UnalignedChunkList){
				int unalignedChunkSize = ((Chunk) unalignedChunk).getSize();
				if(unalignedChunkSize > s1MaxUnalignedChunkSize) s1MaxUnalignedChunkSize = unalignedChunkSize;
				if(unalignedChunkSize < s1MinUnalignedChunkSize) s1MinUnalignedChunkSize = unalignedChunkSize;
				s1AverageUnalignedChunkSize += unalignedChunkSize;
				s12AverageUnalignedChunkSize += unalignedChunkSize;
			}
			s1AverageUnalignedChunkSize /= s1NumberOfUnalignedChunks;
		}
		
		if(s2NumberOfUnalignedChunks == 0){
			s2MaxUnalignedChunkSize = 0;
			s2MinUnalignedChunkSize = 0;
		}else{
			s2MaxUnalignedChunkSize = Integer.MIN_VALUE;
			s2MinUnalignedChunkSize = Integer.MAX_VALUE;
			for(Object unalignedChunk : s2UnalignedChunkList){
				int unalignedChunkSize = ((Chunk) unalignedChunk).getSize();
				if(unalignedChunkSize > s2MaxUnalignedChunkSize) s2MaxUnalignedChunkSize = unalignedChunkSize;
				if(unalignedChunkSize < s2MinUnalignedChunkSize) s2MinUnalignedChunkSize = unalignedChunkSize;
				s2AverageUnalignedChunkSize += unalignedChunkSize;
				s12AverageUnalignedChunkSize += unalignedChunkSize;
			}
			s2AverageUnalignedChunkSize /= s2NumberOfUnalignedChunks;
		}
		
		int s12MaxUnalignedChunkSize = Math.max(s1MaxUnalignedChunkSize, s2MaxUnalignedChunkSize);
		int s12MinUnalignedChunkSize = Math.min(s1MinUnalignedChunkSize, s2MinUnalignedChunkSize);
		if(s1NumberOfUnalignedChunks + s2NumberOfUnalignedChunks > 0) s12AverageUnalignedChunkSize /= (s1NumberOfUnalignedChunks + s2NumberOfUnalignedChunks);
		
		fv.setValue("s1 max unaligned chunk length", 1.0 * s1MaxUnalignedChunkSize);
		fv.setValue("s1 min unaligned chunk length", 1.0 * s1MinUnalignedChunkSize);
		fv.setValue("s1 average unaligned chunk length", s1AverageUnalignedChunkSize);
		fv.setValue("s2 max unaligned chunk length", 1.0 * s2MaxUnalignedChunkSize);
		fv.setValue("s2 min unaligned chunk length", 1.0 * s2MinUnalignedChunkSize);
		fv.setValue("s2 average unaligned chunk length", s2AverageUnalignedChunkSize);
		fv.setValue("s12 max unaligned chunk length", 1.0 * s12MaxUnalignedChunkSize);
		fv.setValue("s12 min unaligned chunk length", 1.0 * s12MinUnalignedChunkSize);
		fv.setValue("s12 average unaligned chunk length", s12AverageUnalignedChunkSize);
		fv.setValue("sLonger max unaligned chunk length", 1.0 * (isS1Longer ? s1MaxUnalignedChunkSize : s2MaxUnalignedChunkSize));
		fv.setValue("sLonger min unaligned chunk length", 1.0 * (isS1Longer ? s1MinUnalignedChunkSize : s2MinUnalignedChunkSize));
		fv.setValue("sLonger average unaligned chunk length", (isS1Longer ? s1AverageUnalignedChunkSize : s2AverageUnalignedChunkSize));
		fv.setValue("sShorter max unaligned chunk length", 1.0 * (isS1Longer ? s2MaxUnalignedChunkSize : s1MaxUnalignedChunkSize));
		fv.setValue("sShorter min unaligned chunk length", 1.0 * (isS1Longer ? s2MinUnalignedChunkSize : s1MinUnalignedChunkSize));
		fv.setValue("sShorter average unaligned chunk length", (isS1Longer ? s2AverageUnalignedChunkSize : s1AverageUnalignedChunkSize));

		fv.setValue("s1 #unaligned chunks", 1.0 * s1NumberOfUnalignedChunks);
		fv.setValue("s2 #unaligned chunks", 1.0 * s2NumberOfUnalignedChunks);
		fv.setValue("sLonger #unaligned chunks", 1.0 * (isS1Longer ? s1NumberOfUnalignedChunks : s2NumberOfUnalignedChunks));
		fv.setValue("sShorter #unaligned chunks", 1.0 * (isS1Longer ? s2NumberOfUnalignedChunks : s1NumberOfUnalignedChunks));

		LinkedList<Object> s1UnalignedLabeledWordList = labeledWordsAlignment.getUnalignedItemList1();
		LinkedList<Object> s2UnalignedLabeledWordList = labeledWordsAlignment.getUnalignedItemList2();
		int s1NumberOfUnalignedLabeledWords = s1UnalignedLabeledWordList.size();
		int s2NumberOfUnalignedLabeledWords = s2UnalignedLabeledWordList.size();

		fv.setValue("s1 #unaligned labeled words", 1.0 * s1NumberOfUnalignedLabeledWords);
		fv.setValue("s2 #unaligned labeled words", 1.0 * s2NumberOfUnalignedLabeledWords);
		fv.setValue("sLonger #unaligned labeled words", 1.0 * (isS1Longer ? s1NumberOfUnalignedLabeledWords : s2NumberOfUnalignedLabeledWords));
		fv.setValue("sShorter #unaligned labeled words", 1.0 * (isS1Longer ? s2NumberOfUnalignedLabeledWords : s1NumberOfUnalignedLabeledWords));
		
		
		double maxSimpleMatchedLabeledWordsSimilarity, minSimpleMatchedLabeledWordsSimilarity, maxStrictMatchedLabeledWordsSimilarity, minStrictMatchedLabeledWordsSimilarity;
		double averageSimpleMatchedLabeledWordsSimilarity = 0.0;
		double averageStrictMatchedLabeledWordsSimilarity = 0.0;
		
		int numberOfSimpleMatchedLabeledWords = 0; //i.e. amount of matches among simple pairs
		int numberOfSimpleMatchedLabeledWordsArcCrossings = 0;
		int numberOfSimpleMatchedLabeledWordsArcs = 0;
		int numberOfStrictMatchedLabeledWords = 0; //i.e. amount of matches among strict pairs
		int numberOfStrictMatchedLabeledWordsArcCrossings = 0;
		int numberOfStrictMatchedLabeledWordsArcs = 0;
		
		if(s1UnalignedLabeledWordList.size() == 0 || s2UnalignedLabeledWordList.size() == 0){
			maxSimpleMatchedLabeledWordsSimilarity = 0;
			minSimpleMatchedLabeledWordsSimilarity = 0;
			maxStrictMatchedLabeledWordsSimilarity = 0;
			minStrictMatchedLabeledWordsSimilarity = 0;
		}else{
			maxSimpleMatchedLabeledWordsSimilarity = Integer.MIN_VALUE;
			minSimpleMatchedLabeledWordsSimilarity = Integer.MAX_VALUE;
			ArrayList<int[]> simpleMatchedLabeledWordsArcs = new ArrayList<int[]>(); //each entry (i,j) is an arc connecting the i-th object with the j-th
			maxStrictMatchedLabeledWordsSimilarity = Integer.MIN_VALUE;
			minStrictMatchedLabeledWordsSimilarity = Integer.MAX_VALUE;
			ArrayList<int[]> strictMatchedLabeledWordsArcs = new ArrayList<int[]>(); //each entry (i,j) is an arc connecting the i-th object with the j-th
			double similarityThreshold = 0.000001; //TODO check this

			for(int i=0; i<s1UnalignedLabeledWordList.size(); i++){
                LabeledWord lw1 = (LabeledWord) s1UnalignedLabeledWordList.get(i);
                //TargetWord tw1 = depTree1.getSentenceAsTargetWordArray()[i];
				for(int j=0; j<s2UnalignedLabeledWordList.size(); j++){	
					LabeledWord lw2 = (LabeledWord) s2UnalignedLabeledWordList.get(j);
                    //TargetWord tw2 = depTree2.getSentenceAsTargetWordArray()[j];
                    double similarity = similarity(lw1.getWord(), lw2.getWord());

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
			for(int i=0; i<simpleMatchedLabeledWordsArcs.size(); i++){
				int[] arc1 = simpleMatchedLabeledWordsArcs.get(i);
				for(int j=0; j<i; j++){
					int[] arc2 = simpleMatchedLabeledWordsArcs.get(j);
					if(arc2[0] < arc1[1] && arc1[0] < arc2[1]) numberOfSimpleMatchedLabeledWordsArcCrossings++;
					numberOfSimpleMatchedLabeledWordsArcs++;
				}
			}
			for(int i=0; i<strictMatchedLabeledWordsArcs.size(); i++){
				int[] arc1 = strictMatchedLabeledWordsArcs.get(i);
				for(int j=0; j<i; j++){
					int[] arc2 = strictMatchedLabeledWordsArcs.get(j);
					if(arc2[0] < arc1[1] && arc1[0] < arc2[1]) numberOfStrictMatchedLabeledWordsArcCrossings++;
					numberOfStrictMatchedLabeledWordsArcs++;
				}
			}
		}

		int s1NumberOfUnalignedLabeledWordsSimpleNormalized = (s1NumberOfUnalignedLabeledWords == 0 ? 0 : (numberOfSimpleMatchedLabeledWords / s1NumberOfUnalignedLabeledWords));
		int s2NumberOfUnalignedLabeledWordsSimpleNormalized = (s2NumberOfUnalignedLabeledWords == 0 ? 0 : (numberOfSimpleMatchedLabeledWords / s2NumberOfUnalignedLabeledWords));
		
		fv.setValue("#matched unaligned labeled words (simple matching)", 1.0 * numberOfSimpleMatchedLabeledWords);
		fv.setValue("#matched unaligned labeled words / s1 #unaligned labeled words (simple matching)", 1.0 * s1NumberOfUnalignedLabeledWordsSimpleNormalized);
		fv.setValue("#matched unaligned labeled words / s2 #unaligned labeled words (simple matching)", 1.0 * s2NumberOfUnalignedLabeledWordsSimpleNormalized);
		fv.setValue("#matched unaligned labeled words / sLonger #unaligned labeled words (simple matching)", 1.0 * (isS1Longer ? s1NumberOfUnalignedLabeledWordsSimpleNormalized : s2NumberOfUnalignedLabeledWordsSimpleNormalized));
		fv.setValue("#matched unaligned labeled words / sShorter #unaligned labeled words (simple matching)", 1.0 * (isS1Longer ? s2NumberOfUnalignedLabeledWordsSimpleNormalized : s1NumberOfUnalignedLabeledWordsSimpleNormalized));
		fv.setValue("max matched unaligned labeled words similarity (simple matching)", maxSimpleMatchedLabeledWordsSimilarity);
		fv.setValue("min matched unaligned labeled words similarity (simple matching)", minSimpleMatchedLabeledWordsSimilarity);
		fv.setValue("average matched unaligned labeled words similarity (simple matching)", averageSimpleMatchedLabeledWordsSimilarity);
		fv.setValue("#crossings among unaligned chunk matchings (simple matching)", 1.0 * numberOfSimpleMatchedLabeledWordsArcCrossings);
		fv.setValue("#crossings among unaligned chunk matchings normalized (simple matching)", (numberOfSimpleMatchedLabeledWordsArcs == 0 ? 0 : 1.0 * numberOfSimpleMatchedLabeledWordsArcCrossings / numberOfSimpleMatchedLabeledWordsArcs));
		
		int s1NumberOfUnalignedLabeledWordsStrictNormalized = (s1NumberOfUnalignedLabeledWords == 0 ? 0 : (numberOfStrictMatchedLabeledWords / s1NumberOfUnalignedLabeledWords));
		int s2NumberOfUnalignedLabeledWordsStrictNormalized = (s2NumberOfUnalignedLabeledWords == 0 ? 0 : (numberOfStrictMatchedLabeledWords / s2NumberOfUnalignedLabeledWords));
		
		fv.setValue("#matched unaligned labeled words (strict matching)", 1.0 * numberOfStrictMatchedLabeledWords);
		fv.setValue("#matched unaligned labeled words / s1 #unaligned labeled words (strict matching)", 1.0 * s1NumberOfUnalignedLabeledWordsStrictNormalized);
		fv.setValue("#matched unaligned labeled words / s2 #unaligned labeled words (strict matching)", 1.0 * s2NumberOfUnalignedLabeledWordsStrictNormalized);
		fv.setValue("#matched unaligned labeled words / sLonger #unaligned labeled words (strict matching)", 1.0 * (isS1Longer ? s1NumberOfUnalignedLabeledWordsStrictNormalized : s2NumberOfUnalignedLabeledWordsStrictNormalized));
		fv.setValue("#matched unaligned labeled words / sShorter #unaligned labeled words (strict matching)", 1.0 * (isS1Longer ? s2NumberOfUnalignedLabeledWordsStrictNormalized : s1NumberOfUnalignedLabeledWordsStrictNormalized));
		fv.setValue("max matched unaligned labeled words similarity (strict matching)", maxStrictMatchedLabeledWordsSimilarity);
		fv.setValue("min matched unaligned labeled words similarity (strict matching)", minStrictMatchedLabeledWordsSimilarity);
		fv.setValue("average matched unaligned labeled words similarity (strict matching)", averageStrictMatchedLabeledWordsSimilarity);
		fv.setValue("#crossings among unaligned chunk matchings (strict matching)", 1.0 * numberOfStrictMatchedLabeledWordsArcCrossings);
		fv.setValue("#crossings among unaligned chunk matchings normalized (strict matching)", (numberOfSimpleMatchedLabeledWordsArcs == 0 ? 0 : 1.0 * numberOfStrictMatchedLabeledWordsArcCrossings / numberOfSimpleMatchedLabeledWordsArcs));

		int s1NumberOfChunkLabels = cs1.getChunkArray().length;
		int s2NumberOfChunkLabels = cs2.getChunkArray().length;
		int numberOfMatchingChunkLabels = chunkTypeAlignment.getAlingmentPairList().size();
		int s1NumberOfUnalignedChunkLabels = chunkTypeAlignment.getUnalignedItemList1().size();
		int s2NumberOfUnalignedChunkLabels = chunkTypeAlignment.getUnalignedItemList2().size();
		
		fv.setValue("s1 #chunk labels", 1.0 * s1NumberOfChunkLabels);
		fv.setValue("s2 #chunk labels", 1.0 * s2NumberOfChunkLabels);
		fv.setValue("sLonger #chunk labels", 1.0 * (isS1Longer ? s1NumberOfChunkLabels : s2NumberOfChunkLabels));
		fv.setValue("sShorter #chunk labels", 1.0 * (isS1Longer ? s2NumberOfChunkLabels : s1NumberOfChunkLabels));
		fv.setValue("s1 #unaligned chunk labels", 1.0 * s1NumberOfUnalignedChunkLabels);
		fv.setValue("s2 #unaligned chunk labels", 1.0 * s2NumberOfUnalignedChunkLabels);
		fv.setValue("sLonger #unaligned chunk labels", 1.0 * (isS1Longer ? s1NumberOfUnalignedChunkLabels : s2NumberOfUnalignedChunkLabels));
		fv.setValue("sShorter #unaligned chunk labels", 1.0 * (isS1Longer ? s2NumberOfUnalignedChunkLabels : s1NumberOfUnalignedChunkLabels));
		fv.setValue("s1 #unaligned chunk labels / s1 #chunk labels", 1.0 * s1NumberOfUnalignedChunkLabels / s1NumberOfChunkLabels);
		fv.setValue("s2 #unaligned chunk labels / s2 #chunk labels", 1.0 * s2NumberOfUnalignedChunkLabels / s2NumberOfChunkLabels);
		fv.setValue("sLonger #unaligned chunk labels / sLonger #chunk labels", 1.0 * (isS1Longer ? s1NumberOfUnalignedChunkLabels / s1NumberOfChunkLabels : s2NumberOfUnalignedChunkLabels / s2NumberOfChunkLabels));
		fv.setValue("sShorter #unaligned chunk labels / sShorter #chunk labels", 1.0 * (isS1Longer ? s2NumberOfUnalignedChunkLabels / s2NumberOfChunkLabels : s1NumberOfUnalignedChunkLabels / s1NumberOfChunkLabels));
		fv.setValue("#matched chunk labels", 1.0 * numberOfMatchingChunkLabels);
		fv.setValue("#matched chunk labels / s1 #chunk labels", 1.0 * numberOfMatchingChunkLabels / s1NumberOfChunkLabels);
		fv.setValue("#matched chunk labels / s2 #chunk labels", 1.0 * numberOfMatchingChunkLabels / s2NumberOfChunkLabels);
		fv.setValue("#matched chunk labels / sLonger #chunk labels", 1.0 * (isS1Longer ? numberOfMatchingChunkLabels / s1NumberOfChunkLabels : numberOfMatchingChunkLabels / s2NumberOfChunkLabels));
		fv.setValue("#matched chunk labels / sShorter #chunk labels", 1.0 * (isS1Longer ? numberOfMatchingChunkLabels / s2NumberOfChunkLabels : numberOfMatchingChunkLabels / s1NumberOfChunkLabels));
		
		
		return fv;
	}
    
    
    
    
    private ArrayList<String> getNumberTokens(DepTree depTree){
		ArrayList<String> tokens = depTree.getUnlemmatizedSentenceAsList();
		ArrayList<String> posTags = depTree.getPosTagsAsList();
		ArrayList<String> numberTokens = new ArrayList<String>();
		
		for(int i=0; i<posTags.size(); i++){
			if(posTags.get(i).equals("CD")){
				numberTokens.add(tokens.get(i));
			}
		}
		
		return numberTokens;
	}
    
    private FeatureVector getSuperficialFeatureVector(int index, DepTree depTree1, DepTree depTree2){
		FeatureVector fv = new FeatureVector(index);
		
		//superficial features
		//String[][] sentences = getSentencesForDatapoint(depTreePair);
		String[] sentence1 = depTree1.getSentenceAsArray();
		String[] sentence2 = depTree2.getSentenceAsArray();
		double diffSentenceLength = Math.abs(sentence1.length - sentence2.length);
		double diffSentenceLengthNormalized = 2.0 * diffSentenceLength / (sentence1.length + sentence2.length);
		
		ArrayList<String> numberTokens1 = getNumberTokens(depTree1);
		ArrayList<String> numberTokens2 = getNumberTokens(depTree2);
		boolean noNumbers = numberTokens1.isEmpty() && numberTokens2.isEmpty();
		boolean noNumbersOrSameNumbers, sameNumbers, numbersOfOneSentenceProperlyContainedInOtherSentence;
		if(noNumbers){
			noNumbersOrSameNumbers = true;
			sameNumbers = false;
			numbersOfOneSentenceProperlyContainedInOtherSentence = false;
		}else{
			boolean numberTokens1ContainsNumberTokens2 = numberTokens1.containsAll(numberTokens2);
			boolean numberTokens2ContainsNumberTokens1 = numberTokens2.containsAll(numberTokens1);
			sameNumbers = numberTokens1ContainsNumberTokens2 && numberTokens2ContainsNumberTokens1;
			noNumbersOrSameNumbers = sameNumbers;
			numbersOfOneSentenceProperlyContainedInOtherSentence = numberTokens1ContainsNumberTokens2 || numberTokens2ContainsNumberTokens1;
		}
		
		boolean s1ContainsNegationWord = sentenceContainsNegationWord(sentence1);
		boolean s2ContainsNegationWord = sentenceContainsNegationWord(sentence2);
		
		fv.setValue("s1 contains negation word", s1ContainsNegationWord ? 1.0 : 0.0);
		fv.setValue("s2 contains negation word", s2ContainsNegationWord ? 1.0 : 0.0);
		fv.setValue("s1 or s2 contains negation word", s1ContainsNegationWord || s2ContainsNegationWord ? 1.0 : 0.0);
		fv.setValue("s1 #words", 1.0 * sentence1.length);
		fv.setValue("s2 #words", 1.0 * sentence2.length);
		fv.setValue("s1 s2 diff #words unnormalized", diffSentenceLength);
		fv.setValue("s1 s2 diff #words normalized", diffSentenceLengthNormalized);
		fv.setValue("unigram overlap unnormalized", 1.0 * getUnigramOverlap(sentence1, sentence2, false));
		fv.setValue("unigram overlap normalized", 1.0 * getUnigramOverlap(sentence1, sentence2, true));
		fv.setValue("bigram overlap unnormalized", 1.0 * getBigramOverlap(sentence1, sentence2, false));
		fv.setValue("bigram overlap normalized", 1.0 * getBigramOverlap(sentence1, sentence2, true));
		fv.setValue("no numbers", noNumbers ? 1.0 : 0.0);
		fv.setValue("no numbers or same numbers", noNumbersOrSameNumbers ? 1.0 : 0.0);
		fv.setValue("same numbers", sameNumbers ? 1.0 : 0.0);
		fv.setValue("numbers of one sentence properly contained in other sentence", numbersOfOneSentenceProperlyContainedInOtherSentence ? 1.0 : 0.0);
		
		return fv;
	}
    
    @Override
    public FeatureVectorsCollection extract(Dataset dataset){
        FeatureVectorsCollection fvc = new FeatureVectorsCollection();
        
        try{
            for(Integer index : dataset.getIndicesSet()){
                AbstractInstance instance = dataset.getInstance(index);
                int instanceIndex = instance.getIndex();
                FeatureVector fv = new FeatureVector(instanceIndex);

                if(instance instanceof experiment.dep.conll2015.Instance){
                    fv.setIndex(instance.getIndex());
                    DepTree depTree1 = ((experiment.dep.conll2015.Instance) instance).arguments[0];
                    DepTree depTree2 = ((experiment.dep.conll2015.Instance) instance).arguments[1];
                    fv.integrate(getChunkingFeatureVector(instanceIndex, depTree1.getSentenceAsString(), depTree2.getSentenceAsString()));
                    fv.integrate(getSuperficialFeatureVector(instanceIndex, depTree1, depTree2));

                }else if(instance instanceof experiment.dep.msrscc.Instance){
                    //TODO
                }

                fvc.append(instanceIndex, fv);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return fvc;
    }

}

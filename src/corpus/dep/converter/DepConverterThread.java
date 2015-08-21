package corpus.dep.converter;

import cdt.Helper;
import corpus.Corpus;
import experiment.TargetElements;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.count.CountTensor;

public class DepConverterThread implements Runnable {

	protected DepConverter superior;
	protected File corpusFile;
	protected HashSet<String> targetWords;
	protected HashMap<String, CountMatrix> targetWordJointCountsMatrixMap;
	
	public DepConverterThread(DepConverter superior, File corpusFile, HashSet<String> targetWords){
		this.superior = superior;
		this.corpusFile = corpusFile;
		//this.targetWords = targetWords;
		
        //prepare an empty LDOP for every requested word
        targetWordJointCountsMatrixMap = new HashMap<>();
        
        /*for(int i=0; i<TargetElements.getSize(); i++){
            TargetWord targetWord = Vocabulary.getTargetWord(i);
            CountMatrix m = new CountMatrix();
            m.setName(targetWord.getWord());
            targetWord.setRepresentation(m);
        }
        */
        
        this.targetWords = targetWords;
        for(String targetWord : targetWords){
            CountMatrix m = new CountMatrix();
            m.setName(targetWord);
            targetWordJointCountsMatrixMap.put(targetWord, new CountMatrix());
        }

    }
	
	
	protected synchronized void convert(){
		Helper.report("[DepConverter] (" + corpusFile.getName() + ") Processing documents...");
		
		DepDocument doc = new DepDocument();
		
		int amountOfDocuments = Corpus.getAmountOfDocuments();

		int docCounter = 0;
		
		try{
			//prepare to read from corpus
			BufferedReader in = Helper.getFileReader(corpusFile);
			
			//process documents from corpus
			Long lineCounter = 0L;
			while(true){
				
				//get current document
				doc.clear();
				lineCounter = doc.createFromFileReader(in, lineCounter);
								
				//if this document does not contain any interesting branchings then skip it
				if(doc.isEmpty() || lineCounter == null) break;
				
				//get document vectors from document
				HashMap<String, CountTensor> wordDocumentTensorMap = doc.getWordDocumentTensorMap(targetWords);
				
				//update local LDOPs
                for(String word : targetWords){
				//for(int i=0; i<TargetElements.getSize(); i++){
                    //TargetWord targetWord = Vocabulary.getTargetWord(i);
                    //String word = targetWord.getWord();
					CountTensor wordDocumentTensor = wordDocumentTensorMap.get(word);
					CountMatrix jdop = targetWordJointCountsMatrixMap.get(word);
                    //CountMatrix jdop = (CountMatrix) targetWord.getRepresentation();
					//#jdop.add(wordDocumentVector.outerProduct(wordDocumentVector)); //use the (full) normal outer product (contains redundant entries due to symmetry)
					jdop.add(
						wordDocumentTensor.outerProductOnlyUpperRightTriangle(wordDocumentTensor)
					); //only use upper right triangle of outer product to save half the space on disk when joint counts get saved to file
					//jdop.reduceCardinality(space.getIntegerParameter("jdopscard")); //TODO
				}

				
				//only process the first [amountOfDocuments] documents
				docCounter++;
				if(amountOfDocuments > 0 && docCounter >= amountOfDocuments) break;
				if(docCounter % 1000 == 0){
					Helper.report("[DepConverter] (" + corpusFile.getName() + ") " + docCounter + " documents have been processed.");
				}
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Helper.report("[DepConverter] (" + corpusFile.getName() + ") ...Finished after processing " + docCounter + " documents.");

		//report document conversion is finished
		superior.reportConverterThreadDone(this, targetWordJointCountsMatrixMap);
	}


	/*
	private synchronized void convertIgnoringDocumentStructure(){
		DepDocument doc = new DepDocument(space);
		
		int amountOfDocuments = space.getIntegerParameter("amountofdocuments");
		
		HashMap<String, Tensor> wordCorpusVectorMap = new HashMap<String, Tensor>();

		try{
			//prepare to read from corpus
			BufferedReader in =
				corpusFile.getName().endsWith(".gz") ?
				new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(corpusFile)))) :
				new BufferedReader(new FileReader(corpusFile));

			//process documents from corpus
			int counter = 0;
			while(true){
				
				//get current document
				doc.clear();
				doc.createFromFileReader(in);
				if(doc.isEmpty()) break;
				
				//get document vectors from document
				HashMap<String, Tensor> wordDocumentVectorMap = doc.getWordDocumentVectorMap(words);
				for(String word : words){
					Tensor existingWordCorpusVector = wordCorpusVectorMap.get(word);
					Tensor wordDocumentVector = wordDocumentVectorMap.get(word);
					
					if(existingWordCorpusVector == null){
						wordCorpusVectorMap.put(word, wordDocumentVector);
					}else{
						existingWordCorpusVector.add(wordDocumentVector);
					}
				}
			
				//only process the first [documents] documents
				counter++;
				if(amountOfDocuments > 0 && counter >= amountOfDocuments) break;
				if(counter % 1000 == 0){
					System.out.println("[DepConverter] (" + corpusFile.getName() + ") " + counter + " documents have been processed.");
				}
			}
			
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		//report document conversion is finished
		exp.reportDepConverterThreadDoneIgnoringDocumentStructure(this, wordCorpusVectorMap);
	}
	*/
	
		
	@Override
	public void run() {
		convert();
		//convertIgnoringDocumentStructure();
	}
	
}

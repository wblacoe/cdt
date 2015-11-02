package corpus.dep.converter;

import corpus.Corpus;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import linearAlgebra.count.CountBaseTensor;
import linearAlgebra.count.CountTensor;

public class DepDocument {

	protected ArrayList<DepTree> depTreeList;
	
	public DepDocument(){
		depTreeList = new ArrayList<>();
	}
	
	
	public Long createFromFileReader(BufferedReader in, Long lineCounter) throws IOException{
		String line;
		while(true){
			
			line = in.readLine();
			lineCounter++;
			if(line == null) return null;
			
			line = line.trim();

			if(line.isEmpty() || line.startsWith("<text")) continue; //ignore empty lines
			
			if(line.equals("</text>")){
				//System.out.println("end of document reached"); //DEBUG
				break;
			}else if(line.equals("<s>")){
				DepTree newDepTree = new DepTree(true);
				lineCounter = newDepTree.createFromFileReader(in, lineCounter);
				//System.out.println(newDepTree.toStringDetailed()); //DEBUG

				//save dep tree
				depTreeList.add(newDepTree);

				//System.out.println(newDepTree); //DEBUG
				if(lineCounter == null){
					//System.out.println("[DepDocument] new dep tree is null!");
					break;
				}
			}else{
				System.err.println("[DepDocument] Unexpected content \"" + line + "\" on line " + lineCounter + "!");
				break;
			}
			
		}
	
		return lineCounter;
	}

	public ArrayList<DepTree> getDepTreeList(){
		return depTreeList;
	}
	public void clear(){
		depTreeList.clear();
	}
	public boolean isEmpty(){
		return depTreeList.isEmpty();
	}
	
	//creates a document tensor for each word in the given collection
	public HashMap<String, CountTensor> getWordDocumentTensorMap(HashSet<String> words){
		
		//each requested word gets a document tensor, which is the sum of all neighbour tensors for that word
		HashMap<String, CountTensor> wordDocumentTensorMap = new HashMap<>();
		for(String word : words){
        //for(int i=0; i<Vocabulary.getSize(); i++){
            //String word = Vocabulary.getTargetWord(i).getWord();
            wordDocumentTensorMap.put(word, new CountTensor());
		}
		
		//threshold for how many neighbours an interesting neighbourhood must have (increase entanglement this way?)
		int minAmountOfNeighbours = Corpus.getMinAmountOfNeighboursWhenCounting();
		
		//go through all trees in this document
		for(DepTree depTree : depTreeList){
			//go through all nodes in this tree
			for(DepNode depNode : depTree.getDepNodes()){
				
				//this node's word
				String depNodeWord = depNode.getWord();
				
				//this node's neighbour arcs
				HashSet<DepArc> neighbourArcs = depNode.neighbourArcSet;
				
				//only consider requested words, and only if they have a minimum amount of neighbours (which are in the vocabulary)
				//if(Vocabulary.contains(depNodeWord) && depNode.getNeighbourhoodSize() >= minAmountOfNeighbours){
                if(words.contains(depNodeWord) && depNode.getNeighbourhoodSize() >= minAmountOfNeighbours){
					
					//prepare the tensor that will encode this word's neighbourhood
					CountTensor neighbourHoodTensor = new CountTensor();
					
					//collect in here all base tensors to be created from this neighbourhood
					HashSet<CountBaseTensor> baseTensors = new HashSet<>();
					CountBaseTensor firstBaseTensor = new CountBaseTensor(1);
					baseTensors.add(firstBaseTensor);
					
					//get all entries in this dep node's neighbourhood
					//Collection<Entry<Object, SubSpace>> entries = depNode.getNeighbourSubSpaceMap().entrySet(); //keys are neighbour numbers (< dimensionality)
					
					//get the first word number for each subspace (wherever one exists)
					for(DepArc neighbourArc : neighbourArcs){
						//if this subspace already has a word number
                        if(firstBaseTensor.getDimensionAtMode(neighbourArc.drc.getModeIndex()) != 0){
							//ignore this entry
							continue;
						//if this subspace is still empty
						}else{
							//assign this entry's word number to this subspace
                            firstBaseTensor.setDimensionAtMode(neighbourArc.drc.getModeIndex(), neighbourArc.neighbourNumber);
							//mark this entry so as not to use it again
							//neighbourNumberSubSpaceEntry.setValue(null);
							neighbourArc.processed = true; //flag this arc so as to not use it again
						}
					}
					//System.out.println("first: " + firstBaseVector); //DEBUG
					
					
					//go through the neighbourhood entries again
					//for(Entry<Object, SubSpace> neighbourNumberSubSpaceEntry : entries){
					for(DepArc neighbourArc : neighbourArcs){
						
						//this time only consider the unmarked entries (if there are any left)
						//SubSpace subSpace = neighbourNumberSubSpaceEntry.getValue();
						//if(subSpace == null) continue; //ignore marked entries
						//int neighbourNumber = (Integer) neighbourNumberSubSpaceEntry.getKey();
						
						//ignore processed arcs
						if(neighbourArc.processed) continue;

						//create copies of each created base vector, changing the word number of this subspace
						TreeSet<CountBaseTensor> baseTensorsToBeAdded = new TreeSet<>();
						for(CountBaseTensor baseTensor : baseTensors){
							CountBaseTensor baseTensorToBeAdded = baseTensor.getCopy();
                            baseTensorToBeAdded.setDimensionAtMode(neighbourArc.drc.getModeIndex(), neighbourArc.neighbourNumber);
							baseTensorsToBeAdded.add(baseTensorToBeAdded);
							//System.out.println("to be added: " + baseVectorToBeAdded); //DEBUG
						}
						//add these copies to the collection of created base vectors
						//baseVectors.addAll(baseVectorsToBeAdded);
						for(CountBaseTensor baseTensorToBeAdded : baseTensorsToBeAdded){
							//System.out.println("adding " + baseVectorToBeAdded); //DEBUG
							baseTensors.add(baseTensorToBeAdded);
						}
						
					}
					
					//create a tensor from all base tensors
					for(CountBaseTensor baseTensor : baseTensors){
						//System.out.println("creating ne-vector from " + baseVector); //DEBUG
						neighbourHoodTensor.add(baseTensor);
					}

					
					//add this neighbourhood tensor into this word's document tensor
					wordDocumentTensorMap.get(depNodeWord).add(neighbourHoodTensor);
					//System.out.println("final: " + neighbourHoodVector); //DEBUG
				}
			}
		}
		
		return wordDocumentTensorMap;
	}
	
}

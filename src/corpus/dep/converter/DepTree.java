package corpus.dep.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class DepTree {

    protected String name;
	protected HashMap<Integer, DepNode> wordInSentenceNumberDepNodeMap;
	protected DepNode root;
	protected boolean regardOnlyNeighboursInVocabulary; //this is true for trees from corpus, but false for trees from the dataset
	protected ArrayList<String> sentenceAsList, unlemmatizedSentenceAsList, posTagsAsList;
	protected String[] sentenceAsArray;
    //protected TargetWord[] sentenceAsTargetWordArray;
	
	
	public DepTree(String name, boolean regardOnlyNeighboursInVocabulary){
        this.name = name;
		wordInSentenceNumberDepNodeMap = new HashMap<>();
		root = null; //will be assigned when this dependency tree is fully created
		this.regardOnlyNeighboursInVocabulary = regardOnlyNeighboursInVocabulary;
		sentenceAsList = new ArrayList<>();
		unlemmatizedSentenceAsList = new ArrayList<>();
		posTagsAsList = new ArrayList<>();
		sentenceAsArray = null;
        //sentenceAsTargetWordArray = null;
	}
	public DepTree(boolean regardOnlyNeighboursInVocabulary){
		this(null, regardOnlyNeighboursInVocabulary);
	}
	
	
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	
	
	//if creation is successful return the number of lines read, if not return null
	//end-of-sentence marker typically is "</s>" for a corpus and "" for a dataset
	public Long createFromFileReader(BufferedReader in, Long lineCounter, String endOfSentenceMarker) throws IOException{
		
		//import all dependency nodes
		String line;
		while(true){
			
			line = in.readLine();
			lineCounter++;
			if(line == null) return null;
			
			line = line.trim();

			//end-of-sentence marker
			if(line.equals(endOfSentenceMarker)){
				break;
			//ignore empty lines and beginning-of-sentence markers
			}else if(line.isEmpty() || line.startsWith("<s")){
				continue;
			//content
			}else{
				String[] entries = line.split("\t");
				if(entries.length < 8) continue; //skip lines that don't have enough entries

				String unlemmatizedWord = entries[1].toLowerCase();
				unlemmatizedSentenceAsList.add(unlemmatizedWord);
				String word = entries[2].toLowerCase();
				sentenceAsList.add(word);
				Integer wordInSentenceNumber = Integer.parseInt(entries[0]);
				Integer headInSentenceNumber = Integer.parseInt(entries[6]);
				String relationWithHead = entries[7];
				String pos = entries[3].toUpperCase();
				posTagsAsList.add(pos);

				DepNode node = new DepNode(word, pos, wordInSentenceNumber, headInSentenceNumber, relationWithHead);
				wordInSentenceNumberDepNodeMap.put(node.getWordInSentenceNumber(), node);

			}
		}
		
		sentenceAsArray = new String[sentenceAsList.size()];
        //sentenceAsTargetWordArray = new TargetWord[sentenceAsList.size()];
		for(int i=0; i<sentenceAsList.size(); i++){
            String s = sentenceAsList.get(i);
			sentenceAsArray[i] = s;
            //sentenceAsTargetWordArray[i] = Vocabulary.getTargetWord(s);
		}
		
		//connect the nodes to make a tree
		for(DepNode node : wordInSentenceNumberDepNodeMap.values()){
			DepNode headNode = wordInSentenceNumberDepNodeMap.get(node.getHeadInSentenceNumber());
			String relationWithHead = node.getRelationWithHead();
			
			//record the root
			if(node.getHeadInSentenceNumber() == 0){
				//if(root != null){
					//System.err.println("[DepTree] Root assigned more than once!");
					//return null; //uncomment this if you are only interested in trees without multiply roots
				//}
				root = node;
				node.setIsRoot(true);
				
			//create arcs both ways
			}else{
				//save arc (dependent to head)
				node.addDepArc(headNode, relationWithHead + "-1", regardOnlyNeighboursInVocabulary);
				
				//save arc (head to dependent)
				headNode.addDepArc(node, relationWithHead, regardOnlyNeighboursInVocabulary);
				
			}
			
		}
		
		return lineCounter;
	}
	public Long createFromFileReader(BufferedReader in, Long lineCounter) throws IOException{
		return createFromFileReader(in, lineCounter, "</s>");
	}
	public Long createFromFileReader(BufferedReader in, String endOfSentenceMarker) throws IOException{
		return createFromFileReader(in, 0L, endOfSentenceMarker);
	}
	public Long createFromFileReader(BufferedReader in) throws IOException{
		return createFromFileReader(in, 0L, "</s>");
	}
	
	public boolean isEmpty(){
		return wordInSentenceNumberDepNodeMap.isEmpty();
	}
	public int getSize(){
		return wordInSentenceNumberDepNodeMap.size();
	}
	public Collection<DepNode> getDepNodes(){
		return wordInSentenceNumberDepNodeMap.values();
	}
	public DepNode getRootNode(){
		return root;
	}
	public ArrayList<String> getSentenceAsList(){
		return sentenceAsList;
	}
	public String[] getSentenceAsArray(){
		return sentenceAsArray;
	}
    //public TargetWord[] getSentenceAsTargetWordArray(){
        //return sentenceAsTargetWordArray;
    //}
    
	//space-delimited
	public String getSentenceAsString(){
		String s = sentenceAsArray[0];
		for(int i=1; i<sentenceAsArray.length; i++){
			s += " " + sentenceAsArray[i];
		}
		return s;
	}
	/*public Matrix getRepresentation(){
		if(root == null) return null;
		return root.getRepresentation();
	}
    */
	public ArrayList<String> getUnlemmatizedSentenceAsList(){
		return unlemmatizedSentenceAsList;
	}
	public ArrayList<String> getPosTagsAsList(){
		return posTagsAsList;
	}
	
	
	
	public String toStringDetailed(){
		String s = "";
		
		//sentence
		for(int i=1; i<=this.getSize(); i++) s += wordInSentenceNumberDepNodeMap.get(i).getWord() + " ";
		s += "\n";
		
		//dep nodes (lists of their dep arcs)
		//for(int i=1; i<=this.getSize(); i++) s += i + ":" + wordInSentenceNumberDepNodeMap.get(i) + "\n";
        for(Integer i : wordInSentenceNumberDepNodeMap.keySet()){
            s += i + ": " + wordInSentenceNumberDepNodeMap.get(i) + "\n";
        }
		
		return s;
	}
	
	//if root is "say", remove all subtrees except for the deepest
	public void removeDirectSpeech(){
		DepNode rootNode = getRootNode();
		if(rootNode.getWord().equals("say")){

			//identify the highest root's dependent
			int maxSubRootHeight = -1;
			DepNode subRootWithMaxHeight = null;
			for(DepNode subRoot : rootNode.getDependents()){
				int subRootHeight = subRoot.getHeight();
				if(subRootHeight > maxSubRootHeight){
					maxSubRootHeight = subRootHeight;
					subRootWithMaxHeight = subRoot;
				}
			}

			if(subRootWithMaxHeight != null){
				root = subRootWithMaxHeight;
			}
			
		}
	}
	
	
	@Override
	public String toString(){
		return "[DepTree] Created tree #" + name + " (" + wordInSentenceNumberDepNodeMap.size() + ")";
	}
	
	public String toString1(){
		String s = "";
		//for(DepNode depNode : wordInSentenceNumberDepNodeMap.values()){
		for(Integer wordInSentenceNumber : wordInSentenceNumberDepNodeMap.keySet()){
			DepNode depNode = wordInSentenceNumberDepNodeMap.get(wordInSentenceNumber);
			//if(depNode == null) break;
			s += wordInSentenceNumber + ":" + depNode.getWord() + " ";
		}
		return s;
	}

    
    public static DepTree importFromReader(BufferedReader in, String endOfSentenceMarker) throws IOException{

        boolean regardOnlyNeighboursInVocabulary = false;
        DepTree depTree = new DepTree(regardOnlyNeighboursInVocabulary);
        
        //import all dependency nodes
		String line;
		while(true){
			
			line = in.readLine();
			if(line == null) return null;
			
			line = line.trim();

			//end-of-sentence marker
			if(line.equals(endOfSentenceMarker)){
				break;
			//ignore empty lines and beginning-of-sentence markers
			}else if(line.isEmpty() || line.startsWith("<s")){
				continue;
			//content
			}else{
				String[] entries = line.split("\t");
				if(entries.length < 8) continue; //skip lines that don't have enough entries

				String unlemmatizedWord = entries[1].toLowerCase();
				depTree.unlemmatizedSentenceAsList.add(unlemmatizedWord);
				String word = entries[2].toLowerCase();
				depTree.sentenceAsList.add(word);
				Integer wordInSentenceNumber = Integer.parseInt(entries[0]);
				Integer headInSentenceNumber = Integer.parseInt(entries[6]);
				String relationWithHead = entries[7];
				String pos = entries[3].toUpperCase();
				depTree.posTagsAsList.add(pos);

				DepNode node = new DepNode(word, pos, wordInSentenceNumber, headInSentenceNumber, relationWithHead);
				depTree.wordInSentenceNumberDepNodeMap.put(node.getWordInSentenceNumber(), node);

			}
		}
		
		depTree.sentenceAsArray = new String[depTree.sentenceAsList.size()];
		for(int i=0; i<depTree.sentenceAsList.size(); i++){
			depTree.sentenceAsArray[i] = depTree.sentenceAsList.get(i);
		}
		
		//connect the nodes to make a tree
		for(DepNode node : depTree.wordInSentenceNumberDepNodeMap.values()){
			DepNode headNode = depTree.wordInSentenceNumberDepNodeMap.get(node.getHeadInSentenceNumber());
			String relationWithHead = node.getRelationWithHead();
			
			//record the root
			if(node.getHeadInSentenceNumber() == 0){
				//if(root != null){
					//System.err.println("[DepTree] Root assigned more than once!");
					//return null; //uncomment this if you are only interested in trees without multiply roots
				//}
				depTree.root = node;
				node.setIsRoot(true);
				
			//create arcs both ways
			}else{
				//save arc (dependent to head)
				node.addDepArc(headNode, relationWithHead + "-1", regardOnlyNeighboursInVocabulary);
				
				//save arc (head to dependent)
				headNode.addDepArc(node, relationWithHead, regardOnlyNeighboursInVocabulary);
				
			}
			
		}
		
		return depTree;
    }
	
	public void addNode(int wordInSentenceNumber, DepNode node){
		wordInSentenceNumberDepNodeMap.put(wordInSentenceNumber, node);
	}
	
	public void setRootNode(int wordInSentenceNumber){
		root = wordInSentenceNumberDepNodeMap.get(wordInSentenceNumber);
		root.setIsRoot(true);
	}
    
}

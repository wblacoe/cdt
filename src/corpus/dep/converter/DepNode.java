package corpus.dep.converter;

import java.util.ArrayList;
import java.util.HashSet;
import linearAlgebra.value.LinearCombinationMatrix;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

public class DepNode {

	//needed for first pass (line by line independently)
	protected String word, pos;
	private String relationWithHead;
	protected int wordInSentenceNumber;
	private int headInSentenceNumber;
	
	//needed for second pass (joining all DepNodes into a DepTree)
	//private HashMap<Integer, SubSpace> neighbourNumberSubSpaceMap;
	
	//when extracting joint counts from the corpus use neighbourNumbers as keys (we only care about neighbours in the vocabulary)
	//when processing dep trees in datasets use the actual neighbourWord (we care about all neighbours)
	protected HashSet<DepArc> neighbourArcSet;
	protected LinearCombinationMatrix representation;
	protected boolean isRoot;
	
	//save all heads distributions that make up this node's representation. the keys are the dependent nodes which the heads distributions came from
	//private HashMap<DepNode, ArrayList<HeadsDistribution>> dependentHeadsDistributionsMap;
	
	
	public DepNode(){
		neighbourArcSet = new HashSet<DepArc>();
	}
	public DepNode(String word, String pos, int wordInSentenceNumber, int headInSentenceNumber, String relationWithHead){
		this();
		this.word = word;
		this.wordInSentenceNumber = wordInSentenceNumber;
		this.headInSentenceNumber = headInSentenceNumber;
		this.relationWithHead = relationWithHead;
		this.pos = pos;
        representation = null;
		isRoot = false;
	}
	
	
	public String getWord(){
		return word;
	}
	public int getWordInSentenceNumber(){
		return wordInSentenceNumber;
	}
	public int getHeadInSentenceNumber(){
		return headInSentenceNumber;
	}
	public String getRelationWithHead(){
		return relationWithHead;
	}
	/*public HashMap<Object, SubSpace> getNeighbourSubSpaceMap(){
		return neighbourSubSpaceMap;
	}
	*/
	public HashSet<DepArc> getNeighbourArcSet(){
		return neighbourArcSet;
	}
	public int getNeighbourhoodSize(){
		//return neighbourSubSpaceMap.size();
		return neighbourArcSet.size();
	}
	public boolean isLeaf(){
		//for(Entry<Object, SubSpace> entries : neighbourSubSpaceMap.entrySet()) if(entries.getValue().isFromHeadToDependent()) return false;
		for(DepArc neighbourArc : neighbourArcSet) if(neighbourArc.drc.isFromHeadToDependent()) return false;
		return true;
	}
	
	public void setRepresentation(LinearCombinationMatrix r){
        representation = r;
    }
    public LinearCombinationMatrix getRepresentation(){
        return representation;
    }
    
	public boolean isRoot(){
		return isRoot;
	}
	public void setIsRoot(boolean isRoot){
		this.isRoot = isRoot;
	}
	
	public ArrayList<DepNode> getHeads(){
		ArrayList<DepNode> heads = new ArrayList<DepNode>();
		/*for(Entry<Object, SubSpace> entry : neighbourSubSpaceMap.entrySet()){
			Object object = entry.getKey();
			SubSpace drc = entry.getValue();
			if(!drc.isFromHeadToDependent() && object instanceof DepNode){
				DepNode neighbour = (DepNode) object;
				heads.add(neighbour);
			}
		}
		*/
		for(DepArc neighbourArc : neighbourArcSet){
			if(!neighbourArc.drc.isFromHeadToDependent()) heads.add(neighbourArc.neighbourNode);
		}
		
		return heads;
	}
	public ArrayList<DepNode> getDependents(){
		ArrayList<DepNode> dependents = new ArrayList<DepNode>();
		/*for(Entry<Object, SubSpace> entry : neighbourSubSpaceMap.entrySet()){
			Object object = entry.getKey();
			SubSpace drc = entry.getValue();
			if(drc.isFromHeadToDependent() && object instanceof DepNode){
				DepNode neighbour = (DepNode) object;
				dependents.add(neighbour);
			}
		}
		*/
		for(DepArc neighbourArc : neighbourArcSet){
			if(neighbourArc.drc.isFromHeadToDependent()) dependents.add(neighbourArc.neighbourNode);
		}
		
		return dependents;
	}
	
	public int getAmountOfNeighbours(){
		//return neighbourSubSpaceMap.size();
		return neighbourArcSet.size();
	}
	
	//this.height=1 for leaves, this.height=max{d.height+1|d in all dependents}
	public int getHeight(){
		
		//if this is a LEAF
		if(this.isLeaf()) return 1;
		
		
		//is this is an INNER NODE
		int height = -1;
		for(DepNode dependent : getDependents()){
			int tempHeight = dependent.getHeight() + 1;
			if(tempHeight > height) height = tempHeight;
		}
		
		return height;
	}
	
	public int getSizeOfSubTreeRootedHere(){
		
		//count this node
		int size = 1;

		//count dependents recursively
		if(!isLeaf()){
			for(DepNode dep : getDependents()) size += dep.getSizeOfSubTreeRootedHere();
		}
		
		return size;
	}
	
	/*public String getPos(){
		return pos;
	}
    */
	
	/*public void addDepArcIfNeighbourInVocabulary(DepNode neighbourNode, String depRelation){
		//System.out.println("adding arc <" + neighbourNode + ", " + relation + ">"); //DELME
		if(space.isThereASubSpaceFor(depRelation)){
			Integer neighbourNumber = space.getWordNumber(depRelation, neighbourNode.getWord());
			
			//treat non-vocabulary neighbours like non-present neighbours
			if(neighbourNumber != null){
				SubSpace subspace = space.getSubSpaceByDepRelation(depRelation);
				neighbourNumberSubSpaceMap.put(neighbourNumber, subspace);
			}
		}
	}
	*/
	
	public void addDepArc(DepNode neighbourNode, String depRelation, boolean regardOnlyNeighboursInVocabulary){
		//System.out.println("adding arc <" + neighbourNode + ", " + relation + ">"); //DELME
        DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationString(depRelation);
		if(drc != null){ //only recognise dep relation if it is in some DRC
			String neighbour = neighbourNode.getWord();
			//DepRelationCluster drc = DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationString(depRelation);
			
			//(extracting joint counts from corpus)
			//treat non-vocabulary neighbours like non-present neighbours
			if(regardOnlyNeighboursInVocabulary && drc.hasContextWord(neighbour)){
				Integer neighbourNumber = drc.getDimensionIndex(neighbour);
				/*if(neighbourSubSpaceMap.containsKey(neighbourNumber)){
					Helper.report("[DepNode " + hashCode() + "] Overwriting an arc because dimensions are equal! (" + neighbourSubSpaceMap.get(neighbourNumber) + ":" + neighbourNumber + " --> " + subspace + ":" + neighbourNumber + ")");
				}
				neighbourSubSpaceMap.put(neighbourNumber, subspace);
				*/
				DepArc newNeighbourArc = new DepArc();
				newNeighbourArc.neighbourNumber = neighbourNumber;
				newNeighbourArc.drc = drc;
				neighbourArcSet.add(newNeighbourArc);
			}
			
			//(processing trees in datasets)
			//regard all neighbours
			if(!regardOnlyNeighboursInVocabulary){
				//System.out.println("[DEBUG] word=\"" + word + "\", neighbour=\"" + neighbour + "\", subspace=" + subspace); //DEBUG
				//neighbourSubSpaceMap.put(neighbourNode, subspace);
				DepArc newNeighbourArc = new DepArc();
				newNeighbourArc.neighbourNode = neighbourNode;
				newNeighbourArc.drc = drc;
				neighbourArcSet.add(newNeighbourArc);
			}
			
		}
	}
	
	
	/*@Override
	public String toString(){
		String s = word + " <";
		for(Integer neighbourNumber : neighbourNumberSubSpaceMap.keySet()){
			s += neighbourNumberSubSpaceMap.get(neighbourNumber) + ":" + neighbourNumber + ", ";
		}
		return s + ">";
	}
	*/

	@Override
	public String toString(){
		String s = "" + wordInSentenceNumber + ": " + word + " <";
		/*for(Object neighbour : neighbourSubSpaceMap.keySet()){
			DepNode neighbourNode = (DepNode) neighbour;
			SubSpace drc = neighbourSubSpaceMap.get(neighbour);
			if(drc.isFromHeadToDependent()) s += drc + ":" + neighbourNode.getWord() + ", ";
		}
		*/
		for(DepArc neighbourArc : neighbourArcSet){
			//if(neighbourArc.drc.isFromHeadToDependent()) s += neighbourArc.drc + ":" + neighbourArc.neighbourNode.getWord() + ", ";
            boolean a = neighbourArc.drc.isFromHeadToDependent();
            if(a){
                s += neighbourArc.drc + ":";
                s += neighbourArc.neighbourNode.getWord() + ", ";
            }
		}
		return s + ">";
	}
	
}

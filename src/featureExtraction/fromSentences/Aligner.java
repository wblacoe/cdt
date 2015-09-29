package featureExtraction.fromSentences;

public class Aligner {

	private Object[] sequence1, sequence2;
	
	private int gapPenalty; //gap penalty
	private int[][] costMatrix; //cost matrix
	private int[][][] backPointers;
	
	public Aligner(Object[] sequence1, Object[] sequence2){
		this.sequence1 = sequence1;
		this.sequence2 = sequence2;
		
		//DEBUG
		//for(int6 i=0; i<sequence2.length; i++) System.out.println("" + i + ": " + sequence1[i].toString());
		//System.out.println();
		//for(int i=0; i<sequence2.length; i++) System.out.println("" + i + ": " + sequence2[i].toString());
		
		gapPenalty = 0;
	}	

	
	private void initializeCostMatrix(){
		int length1 = sequence1.length;
		int length2 = sequence2.length;
		
		costMatrix = new int[length1+1][length2+1];
		for(int i=0; i<length1; i++) costMatrix[i][0] = i * gapPenalty;
		for(int j=0; j<length2; j++) costMatrix[0][j] = j * gapPenalty;
		
		backPointers = new int[length1+1][length2+1][2];
		for(int i=0; i<length1; i++) backPointers[i][0] = new int[]{ -1, -1 };
		for(int j=0; j<length2; j++) backPointers[0][j] = new int[]{ -1, -1 };
	}

	private int getSimilarity(Object object1, Object object2){
		boolean equal = object1.equals(object2);
		return (equal ? 1 : 0);
	}

	private void needlemanWunsch(){
		for(int i=1; i<=sequence1.length; i++){
			for(int j=1; j<=sequence2.length; j++){
				Object object1 = sequence1[i-1];
				Object object2 = sequence2[j-1];
				int similarity = getSimilarity(object1, object2);
				int match = costMatrix[i-1][j-1] + similarity;
				int delete = costMatrix[i-1][j] + gapPenalty;
				int insert = costMatrix[i][j-1] + gapPenalty;
				//determine the maximum value of these three
				int maximum;
				if(match > delete){
					if(match > insert){
						maximum = match;
						backPointers[i][j] = new int[]{ -1, -1 };
					}else{
						maximum = insert;
						backPointers[i][j] = new int[]{ 0, -1 };
					}
				}else{
					if(delete > insert){
						maximum = delete;
						backPointers[i][j] = new int[]{ -1, 0 };
					}else{
						maximum = insert;
						backPointers[i][j] = new int[]{ 0, -1 };
					}
				}
				costMatrix[i][j] = maximum; //maximum of all three values
				//o[i][j] = operation;
			}
		}
	}
	
	//reconstructs the path taken to the final cell (i,j) in the cost matrix
	//creates an alignment based on this path
	private Alignment getAlignment(){
		Alignment a = new Alignment();
		
		int i = sequence1.length;
		int j = sequence2.length;
		
		int[] backPointer;
		while(i>0 && j>0){
			Object object1 = sequence1[i-1];
			Object object2 = sequence2[j-1];
			backPointer = backPointers[i][j];
			
			int moveBack1 = backPointer[0];
			int moveBack2 = backPointer[1];
			//System.out.println("i=" + i + ", j=" + j + ", word1=" + word1 + ", word2=" + word2 + ", moveBack1=" + moveBack1 + ", moveBack2=" + moveBack2);
			
			//record an alignment item
			if(moveBack1 == -1){
				//match
				if(moveBack2 == -1){
					a.addAlignmentPair(object1, object2);
				//insert
				}else{
					a.addUnalignedItem1(object1);
				}
			//delete
			}else{
				a.addUnalignedItem2(object2);
			}
			
			//get next coordinates
			i += moveBack1;
			j += moveBack2;
		}
		
		return a;
	}
	
	public Alignment needlemanWunschAndGetAlignment(){
		initializeCostMatrix();
		needlemanWunsch();
		
		Alignment alignment = getAlignment();
		//System.out.println(alignment); //DEBUG
		//System.out.println(costMatrixToString()); //DEBUG
		
		return alignment;
	}
	
	public String costMatrixToString(){
		String s = "";
		for(int i=0; i<costMatrix.length; i++){
			int[] row = costMatrix[i];
			for(int j=0; j<row.length; j++){
				s += "\t" + row[j];
			}
			s += "\n";
		}
		
		return s;
	}

	
	public static void main1(String[] args){
		
		String sentence1 = "a brown and white dog is running through the tall grass";
		String sentence2 = "a brown and white dog is moving through the grass";
		String sentence3 = "a brown and white dog is moving through the wild grass";
		
		Aligner a1 = new Aligner(sentence1.split(" "), sentence2.split(" "));
		System.out.println(a1.needlemanWunschAndGetAlignment());
		
		System.out.println();
		
		Aligner a2 = new Aligner(sentence1.split(" "), sentence3.split(" "));
		System.out.println(a2.needlemanWunschAndGetAlignment());
		
	}

	public static void main(String[] args){
		
		//String s1 = "a brown and white dog is running through the tall wild grass";
		//String s2 = "a brown and white dog is moving through the grass";
		
		String s1 = "A man is flooring a sitting guitar player";
		String s2 = "A man is sitting on the floor in a room and strumminig a guitar";
		
		ChunkedSentence cs1 = new ChunkedSentence(s1);
		ChunkedSentence cs2 = new ChunkedSentence(s2);
		
		cs1.createChunks();
		cs2.createChunks();
		//System.out.println(cs1 + "\n" + cs2);
		
		Aligner a;
		
		a = new Aligner(cs1.getWordArray(), cs2.getWordArray());
		System.out.println(a.needlemanWunschAndGetAlignment() + "\n");
		
		a = new Aligner(cs1.getLabeledWordArray(), cs2.getLabeledWordArray());
		System.out.println(a.needlemanWunschAndGetAlignment() + "\n");

		a = new Aligner(cs1.getChunkArray(), cs2.getChunkArray());
		System.out.println(a.needlemanWunschAndGetAlignment() + "\n");

		a = new Aligner(cs1.getChunkTypeArray(), cs2.getChunkTypeArray());
		System.out.println(a.needlemanWunschAndGetAlignment() + "\n");
	}

}

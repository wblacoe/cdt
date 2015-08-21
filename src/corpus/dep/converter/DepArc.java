package corpus.dep.converter;

import space.dep.DepRelationCluster;

//a handy class to store arcs when extracting trees from files and converting them to tensors
public class DepArc {

	public boolean processed = false;
	public int neighbourNumber; //this is used when extracting trees from a corpus (possibly only for neighbour numbers in vocabulary)
	public DepNode neighbourNode; //this is used when extracting trees from a dataset (all neighbours are relevant)
	public DepRelationCluster drc; //this is the relation (cluster) that connects with the neighbour
    //public int modeIndex; //0 is dummy

}

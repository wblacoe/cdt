package corpus.associationFunction;

import cdt.Helper;
import corpus.dep.marginalizer.DepMarginalCounts;
import io.VocabularyMatrixExporter;
import io.VocabularyMatrixImporter;
import java.io.File;
import java.util.HashMap;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.ValueMatrix;
import numberTypes.NNumber;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */
//joint count matrices are already attached to elements in vocabulary
public class AssociationFunction {
    
    public synchronized HashMap<String, ValueMatrix> compute(HashMap<String, CountMatrix> jdops){
        return null;
    }
	
    public void compute(){
        Helper.report("[AssociationFunction] Applying no association function...");
    }

    //import and associaionate lexical count matrices
    public static void main(String[] args){
        
        File projectFolder1 = new File("/local/falken-1");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder1);
        
        File inFile = new File(projectFolder1, "preprocessed/ukwac.depParsed/5up5down/wordsim353/space.test");
        DepNeighbourhoodSpace.importFromFile(inFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

        VocabularyMatrixImporter mi = new VocabularyMatrixImporter(false);
        mi.importMatricesFromFiles(new File(projectFolder1, "experiments/wordsim353/jdops.tiny").listFiles());
        
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(new File(projectFolder1, "preprocessed/ukwac.depParsed/marginalcounts.gz"));
        AssociationFunction af = new SppmiFunction(dmc, 5000, 1000);
        af.compute();
        
        VocabularyMatrixExporter me = new VocabularyMatrixExporter();
        me.exportMatricesToFiles(new File(projectFolder1, "experiments/wordsim353/ldops.tiny"), 1);
    }
    
}

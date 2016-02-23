package experiment.dep.four4cl;

import cdt.Helper;
import experiment.Dataset;
import experiment.dep.DepExperiment;
import experiment.dep.Vocabulary;
import featureExtraction.FeatureVectorsCollection;
import innerProduct.InnerProductsCache;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import linearAlgebra.Matrix;
import linearAlgebra.count.CountMatrix;
import linearAlgebra.value.ValueMatrix;
import numberTypes.NNumber;
import space.dep.DepNeighbourhoodSpace;

/**
 *
 * @author wblacoe
 */

public class Four4CL extends DepExperiment {

	public static final int MSRPC_TASK = 0;
	public static final int ZEICHNER_TASK = 1;
	public static final int SICK_TASK = 2;
	public static final int RW2012_TASK = 3;

    public Four4CL(){
        dataset = new Dataset();
    }
    
    public void importDataset(File datasetFile, int task){
        Helper.report("[Four4CL] Importing dataset from " + datasetFile.getAbsolutePath() + "...");
        
        try{
            BufferedReader in = Helper.getFileReader(datasetFile);
            
            while(true){
                Instance instance = Instance.importFromReader(in, task);
				/*if(task == MSRPC_TASK){
					instance.sentenceTrees[0].removeDirectSpeech();
					instance.sentenceTrees[1].removeDirectSpeech();
				}
				*/
                if(instance == null){
                    break;
                }else{
                    dataset.setInstance(instance.getIndex(), instance);
                }
            }
            
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Helper.report("[Four4CL] ...Finished importing dataset (" + dataset.getSize() + " instances) from " + datasetFile.getAbsolutePath());
    }
	
	public static HashSet<Integer> importIndices(File indicesFile){
		HashSet<Integer> indices = new HashSet<>();
		
		try{
			BufferedReader in = Helper.getFileReader(indicesFile);
			
			String line;
			while((line = in.readLine()) != null){
				indices.add(Integer.parseInt(line));
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return indices;
	}
	
    
    public static void mainMsrpc(String[] args){
        
        //space
		File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/four4cl/space.four4cl");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

		//experiment
        Four4CL exp = new Four4CL();
		
        //gather and save joint counts
        /*File jdopsFolder = new File(projectFolder, "experiments/four4cl/jdops");
		exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        int delta = 5000;
        int ldopCardinality = 2000;
        SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
        File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
        */
        //inner products cache
        File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
        ipc.importFromFile(innerProductsFile);
		
		//composition
		File datasetFile = new File(projectFolder, "datasets/msrpc/trainAndTest.withoutFinalPunctuation");
        //File datasetFile = new File(projectFolder, "datasets/conll2015/trainAndDevAndTest.implicitOnly");
        exp.importDataset(datasetFile, MSRPC_TASK);
		File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importMatrices(ldopsFolder, false);
		File sdopsFolder = new File(projectFolder, "experiments/four4cl/sdops.forRootsAndSubRoots.msrpc");
		/*int saveSdopsEvery = 50;
		int amountOfTreesPerThread = 100;
		exp.composeTrees(ipc, sdopsFolder, innerProductsFile, amountOfTreesPerThread, saveSdopsEvery);
		exp.saveSdopsToFiles(sdopsFolder);
		ipc.saveToFile(innerProductsFile);
		*/
		//feature extraction
		exp.importSdopsFromFiles(sdopsFolder);
		exp.attachSdopsToDataset();
		String[] classStrings = new String[]{ "0.0", "1.0" };
		HashSet<Integer> trainIndices = importIndices(new File(projectFolder, "datasets/msrpc/indices.train"));
		HashSet<Integer> testIndices = importIndices(new File(projectFolder, "datasets/msrpc/indices.test"));
		//without feature selection
		ArrayList<String> kotlermanFeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/kotlerman.list"));
		FeatureVectorsCollection fvcKotlerman = exp.extractFeatureVectorsFromSdops(ipc, kotlermanFeatureList);
		fvcKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.msrpc/fvec/msrpc.kotlerman.fvec"));
		//with ski features
		FeatureVectorsCollection fvcOldAndKotlerman = new FeatureVectorsCollection(new File(projectFolder, "experiments/four4cl/fvec.old/msrpc.nofs.fvec"));
		try{
			fvcOldAndKotlerman.integrate(fvcKotlerman);
		}catch(Exception e){
			e.printStackTrace();
		}
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.msrpc/fvec/msrpc.ski.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.msrpc/arff/msrpc.ski.train.arff"), trainIndices, classStrings);
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.msrpc/arff/msrpc.ski.test.arff"), testIndices, classStrings);
		//with feature selection
		ArrayList<String> fsList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/msrpc.fs58.list"));
		fvcOldAndKotlerman.applyFeatureSelection(fsList);
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.msrpc/fvec/msrpc.fs58.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.msrpc/arff/msrpc.fs58.train.arff"), trainIndices, classStrings);
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.msrpc/arff/msrpc.fs58.test.arff"), testIndices, classStrings);
	}

	public static void mainZeichner(String[] args){
        
        //space
		File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/four4cl/space.four4cl");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

		//experiment
        Four4CL exp = new Four4CL();
		
        //gather and save joint counts
        /*File jdopsFolder = new File(projectFolder, "experiments/four4cl/jdops");
		exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        int delta = 5000;
        int ldopCardinality = 2000;
        SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
        File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
        */
        //inner products cache
        File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
        ipc.importFromFile(innerProductsFile);
		
		//composition
		File datasetFile = new File(projectFolder, "datasets/zeichner.sentences/all");
        exp.importDataset(datasetFile, ZEICHNER_TASK);
		File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importMatrices(ldopsFolder, false);
		File sdopsFolder = new File(projectFolder, "experiments/four4cl/sdops.zeichner");
		/*int saveSdopsEvery = 50;
		int amountOfTreesPerThread = 100;
		exp.composeTrees(ipc, sdopsFolder, innerProductsFile, amountOfTreesPerThread, saveSdopsEvery);
		exp.saveSdopsToFiles(sdopsFolder);
		ipc.saveToFile(innerProductsFile);
		*/
		//feature extraction
		exp.importSdopsFromFiles(sdopsFolder);
		exp.attachSdopsToDataset();
		String[] classStrings = new String[]{ "0.0", "1.0" };
		HashSet<Integer> trainIndices = importIndices(new File(projectFolder, "datasets/zeichner.sentences/omriSplit/indices.train"));
		HashSet<Integer> testIndices = importIndices(new File(projectFolder, "datasets/zeichner.sentences/omriSplit/indices.test"));
		//without feature selection
		ArrayList<String> kotlermanFeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/kotlerman.list"));
		FeatureVectorsCollection fvcKotlerman = exp.extractFeatureVectorsFromSdops(ipc, kotlermanFeatureList);
		fvcKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.zeichner/fvec/zeichner.kotlerman.fvec"));
		//with ski features
		FeatureVectorsCollection fvcOldAndKotlerman = new FeatureVectorsCollection(new File(projectFolder, "experiments/four4cl/fvec.old/zeichner.nofs.fvec"));
		try{
			fvcOldAndKotlerman.integrate(fvcKotlerman);
		}catch(Exception e){
			e.printStackTrace();
		}
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.zeichner/fvec/zeichner.ski.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.zeichner/arff/zeichner.ski.train.arff"), trainIndices, classStrings);
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.zeichner/arff/zeichner.ski.test.arff"), testIndices, classStrings);
		//with feature selection
		ArrayList<String> fsList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/zeichner.sentences.fs30.list"));
		fvcOldAndKotlerman.applyFeatureSelection(fsList);
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.zeichner/fvec/zeichner.fs30.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.zeichner/arff/zeichner.fs30.train.arff"), trainIndices, classStrings);
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.zeichner/arff/zeichner.fs30.test.arff"), testIndices, classStrings);
	}

	public static void mainSick(String[] args){
        
        //space
		File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/four4cl/space.four4cl");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

		//experiment
        Four4CL exp = new Four4CL();
		
        //gather and save joint counts
        /*File jdopsFolder = new File(projectFolder, "experiments/four4cl/jdops");
		exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        int delta = 5000;
        int ldopCardinality = 2000;
        SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
        File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
        */
        //inner products cache
        File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
        ipc.importFromFile(innerProductsFile);
		
		//composition
		File datasetFile = new File(projectFolder, "datasets/sick/trialAndTrainAndTest");
        exp.importDataset(datasetFile, ZEICHNER_TASK);
		File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importMatrices(ldopsFolder, false);
		File sdopsFolder = new File(projectFolder, "experiments/four4cl/sdops.sick");
		int saveSdopsEvery = 50;
		int amountOfTreesPerThread = 100;
		exp.composeTrees(ipc, sdopsFolder, innerProductsFile, amountOfTreesPerThread, saveSdopsEvery);
		exp.saveSdopsToFiles(sdopsFolder);
		ipc.saveToFile(innerProductsFile);
		
		//feature extraction
		exp.importSdopsFromFiles(sdopsFolder);
		exp.attachSdopsToDataset();
		String[] classStrings = new String[]{ "0.0", "1.0", "2.0" };
		HashSet<Integer> trainIndices = importIndices(new File(projectFolder, "datasets/sick/indices.trialAndTrain"));
		HashSet<Integer> testIndices = importIndices(new File(projectFolder, "datasets/sick/indices.test"));
		//without feature selection
		ArrayList<String> kotlermanFeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/kotlerman.list"));
		FeatureVectorsCollection fvcKotlerman = exp.extractFeatureVectorsFromSdops(ipc, kotlermanFeatureList);
		fvcKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.sick/fvec/sick.kotlerman.fvec"));
		//with ski features
		FeatureVectorsCollection fvcOldAndKotlerman = new FeatureVectorsCollection(new File(projectFolder, "experiments/four4cl/fvec.old/sick.nofs.fvec"));
		try{
			fvcOldAndKotlerman.integrate(fvcKotlerman);
		}catch(Exception e){
			e.printStackTrace();
		}
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.sick/fvec/sick.ski.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.sick/arff/sick.ski.train.arff"), trainIndices, classStrings);
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.sick/arff/sick.ski.test.arff"), testIndices, classStrings);
		//with feature selection
		ArrayList<String> fsList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/sick.fs46.list"));
		fvcOldAndKotlerman.applyFeatureSelection(fsList);
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.sick/fvec/sick.fs46.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.sick/arff/sick.fs46.train.arff"), trainIndices, classStrings);
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.sick/arff/sick.fs46.test.arff"), testIndices, classStrings);
	}
	
	public static void mainRW2012(String[] args){
        
        //space
		File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/four4cl/space.four4cl");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

		//experiment
        Four4CL exp = new Four4CL();
		
        //gather and save joint counts
        /*File jdopsFolder = new File(projectFolder, "experiments/four4cl/jdops");
		exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        int delta = 5000;
        int ldopCardinality = 2000;
        SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
        File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
        */
        //inner products cache
        File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
        ipc.importFromFile(innerProductsFile);
		
		//composition
		File datasetFile = new File(projectFolder, "datasets/rw2012/all.fiveway");
        exp.importDataset(datasetFile, ZEICHNER_TASK);
		File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
        exp.importMatrices(ldopsFolder, false);
		File sdopsFolder = new File(projectFolder, "experiments/four4cl/sdops.rw2012");
		int saveSdopsEvery = 50;
		int amountOfTreesPerThread = 100;
		exp.composeTrees(ipc, sdopsFolder, innerProductsFile, amountOfTreesPerThread, saveSdopsEvery);
		exp.saveSdopsToFiles(sdopsFolder);
		ipc.saveToFile(innerProductsFile);
		
		//feature extraction
		exp.importSdopsFromFiles(sdopsFolder);
		exp.attachSdopsToDataset();
		String[] classStrings = new String[]{ "0.0", "1.0", "2.0", "3.0", "4.0" };
		//without feature selection
		ArrayList<String> kotlermanFeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/kotlerman.list"));
		FeatureVectorsCollection fvcKotlerman = exp.extractFeatureVectorsFromSdops(ipc, kotlermanFeatureList);
		fvcKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.rw2012/fvec/rw2012.fvec"));
		//with ski features
		FeatureVectorsCollection fvcOldAndKotlerman = new FeatureVectorsCollection(new File(projectFolder, "experiments/four4cl/fvec.old/rw2012.nofs.fvec"));
		try{
			fvcOldAndKotlerman.integrate(fvcKotlerman);
		}catch(Exception e){
			e.printStackTrace();
		}
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.rw2012/fvec/rw2012.ski.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.rw2012/arff/rw2012.ski.arff"), classStrings);
		//with feature selection
		ArrayList<String> fsList = FeatureVectorsCollection.importFeatureNamesList(new File(projectFolder, "experiments/four4cl/featurelists/rw2012.fs20.list"));
		fvcOldAndKotlerman.applyFeatureSelection(fsList);
		fvcOldAndKotlerman.exportToFvecFile(new File(projectFolder, "experiments/four4cl/output.rw2012/fvec/rw2012.fs20.fvec"));
		fvcOldAndKotlerman.exportToArffFile(new File(projectFolder, "experiments/four4cl/output.rw2012/arff/rw2012.fs20.arff"), classStrings);
	}

	private static void filterAndSaveMatrices(File dopsFolder, String[] dopsOfInterest, File outputFolder){
		try{
			for(File dopsFile : dopsFolder.listFiles()){
				BufferedReader in = Helper.getFileReader(dopsFile);
				Matrix dop = Matrix.importFromReader(in);
				System.out.println("imported matrix for \"" + dop.getName() + "\""); //DEBUG
				for(String dopOfInterest : dopsOfInterest){
					if(dop.getName().equals(dopOfInterest)){
						System.out.println("saving matrix for \"" + dop.getName() + "\""); //DEBUG
						BufferedWriter out = Helper.getFileWriter(new File(outputFolder, dopOfInterest));
						dop.saveToWriter(out);
						out.close();
					}
				}
				in.close();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void forArticle1(){
        //space
		File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/four4cl/space.four4cl");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

		//experiment
        Four4CL exp = new Four4CL();

		String[] dopsOfInterest = new String[]{ "catch", "red", "ball" };
		File jdopsFolder = new File(projectFolder, "experiments/four4cl/jdops");
		File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
		File forArticleFolder = new File(projectFolder, "experiments/four4cl/forArticle");
		Helper.prettyPrint = true;
		filterAndSaveMatrices(jdopsFolder, dopsOfInterest, new File(forArticleFolder, "jdops"));
		filterAndSaveMatrices(ldopsFolder, dopsOfInterest, new File(forArticleFolder, "ldops"));
		
		/*
		//inner products cache
        File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
        ipc.importFromFile(innerProductsFile);
		*/
	}

	
	public static void forArticle(String[] words){
        //space
		File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/four4cl/space.four4cl");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

		//experiment
		File outputFolder = new File(projectFolder, "experiments/four4cl/forArticle");
		Helper.prettyPrint = true;
		
        Four4CL exp1 = new Four4CL();
		File jdopsFolder = new File(projectFolder, "experiments/four4cl/forArticle/jdops");
		exp1.importMatrices(jdopsFolder, false);
		try{
			BufferedWriter outJdops = Helper.getFileWriter(new File(outputFolder, "pretty_jdops"));
			for(String word : words){
				((CountMatrix) Vocabulary.getTargetWord(word).getLexicalRepresentation()).saveToWriter(outJdops);
			}
			outJdops.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		Four4CL exp2 = new Four4CL();
		File ldopsFolder = new File(projectFolder, "experiments/four4cl/ldops");
		exp2.importMatrices(ldopsFolder, false);
		try{
			BufferedWriter outLdops = Helper.getFileWriter(new File(outputFolder, "pretty_ldops"));
			BufferedWriter outTraces = Helper.getFileWriter(new File(outputFolder, "pretty_traces"));
			for(String word : words){
				ValueMatrix m = ((ValueMatrix) Vocabulary.getTargetWord(word).getLexicalRepresentation());
				m.saveToWriter(outLdops);
			}
			outLdops.close();
			outTraces.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args){
		//mainMsrpc(args);
		//mainZeichner(args);
		forArticle(args);
	}
	
}
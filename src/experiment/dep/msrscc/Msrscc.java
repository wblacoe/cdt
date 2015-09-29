package experiment.dep.msrscc;

import cdt.Helper;
import corpus.associationFunction.SppmiFunction;
import corpus.dep.converter.DepTree;
import corpus.dep.marginalizer.DepMarginalCounts;
import experiment.dep.DepExperiment;
import experiment.dep.TargetWord;
import experiment.dep.Vocabulary;
import innerProduct.InnerProductsCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import linearAlgebra.value.LinearCombinationMatrix;
import numberTypes.NNumber;
import numberTypes.NNumberVector;
import space.dep.DepNeighbourhoodSpace;
import space.dep.DepRelationCluster;

/**
 *
 * @author wblacoe
 */
public class Msrscc extends DepExperiment {

    public Msrscc(){

    }
    
    protected HashMap<Integer, DepTree> getIntegerDepTreeMap(){
        HashMap<Integer, DepTree> integerDepTreeMap = new HashMap<>();
        
        for(Integer index : dataset.getIndicesSet()){
            Instance instance = (Instance) dataset.getInstance(index);
            for(int i=1; i<=5; i++){
                DepTree depSentence = instance.getDepSentence(i);
                integerDepTreeMap.put(5 * (index-1) + i, depSentence);
            }
        }
        
        return integerDepTreeMap;
    }
    
    public void importDataset(File datasetFile){
        Helper.report("[Msrscc] Importing dataset from " + datasetFile.getAbsolutePath() + "...");
        
        try{
            BufferedReader in = Helper.getFileReader(datasetFile);
            
            while(true){
                Instance instance = Instance.importFromReader(in);
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
        
        Helper.report("[Msrscc] ...Finished importing dataset (" + dataset.getSize() + " instances) from " + datasetFile.getAbsolutePath());
    }
    
    public void run(){
        File datasetFile = new File("/local/william/datasets/msrscc/all.parsed");
        importDataset(datasetFile);
    }

	
	private static NNumber similarity(InnerProductsCache ipc, int twIndex1, int twIndex2){
        //NNumber ip12 = ipc.getInnerProduct(twIndex1, twIndex2, true);
        NNumber ip12 = ipc.getInnerProduct(Vocabulary.getTargetWord(twIndex1).getWord(), Vocabulary.getTargetWord(twIndex2).getWord(), true);
        if(ip12 == null) return null;
		System.out.println("ip(" + Vocabulary.getTargetWord(twIndex1).getWord() + ", " + Vocabulary.getTargetWord(twIndex2).getWord() + ") = " + ip12.getDoubleValue() + " "); //DEBUG
        //NNumber ip11 = ipc.getInnerProduct(twIndex1, twIndex1, true);
        NNumber ip11 = ipc.getInnerProduct(Vocabulary.getTargetWord(twIndex1).getWord(), Vocabulary.getTargetWord(twIndex1).getWord(), true);
        if(ip11 == null) return null;
		System.out.println("ip(" + Vocabulary.getTargetWord(twIndex1).getWord() + ", " + Vocabulary.getTargetWord(twIndex1).getWord() + ") = " + ip11.getDoubleValue() + " "); //DEBUG
        //NNumber ip22 = ipc.getInnerProduct(twIndex2, twIndex2, true);
        NNumber ip22 = ipc.getInnerProduct(Vocabulary.getTargetWord(twIndex2).getWord(), Vocabulary.getTargetWord(twIndex2).getWord(), true);
        if(ip22 == null) return null;
		System.out.println("ip(" + Vocabulary.getTargetWord(twIndex2).getWord() + ", " + Vocabulary.getTargetWord(twIndex2).getWord() + ") = " + ip22.getDoubleValue() + " "); //DEBUG

        NNumber similarity = ip12.multiply(ip11.multiply(ip22).sqrt().reciprocal());
        return similarity;
    }

	private static LinearCombinationMatrix compose(InnerProductsCache ipc, String headString, DepRelationCluster drc, LinearCombinationMatrix dependentRepresentation){
		System.out.println("composing: head=" + headString + ", drc=" + drc.getName() + ", dep=" + dependentRepresentation.getName()); //DEBUG
		
		TargetWord head = Vocabulary.getTargetWord(headString);
        int headTargetWordIndex = head.getIndex();
		
        NNumberVector partialTraceDiagonalVector = dependentRepresentation.getPartialTraceDiagonalVector(drc.getModeIndex());
		
        //multiply the weights for alternative heads by their similarities with given head
        NNumberVector weightedSimilaritiesVector = new NNumberVector(Vocabulary.getSize());
		
		for(int i=0; i<Vocabulary.getSize(); i++){
			NNumber weight = partialTraceDiagonalVector.getWeight(i);
			if(weight != null && !weight.isZero()){
				NNumber similarity = similarity(ipc, headTargetWordIndex, i);
				if(similarity != null && !similarity.isZero()){
					System.out.println(
						"weight(" + dependentRepresentation.getName() + ", " + drc.getName() + ", " + Vocabulary.getTargetWord(i).getWord() + ") = " + weight.getDoubleValue() + "; " +
						"sim(" + headString + ", " + Vocabulary.getTargetWord(i).getWord() + ") = " + similarity.getDoubleValue() + "; " + 
						"weight * sim = " + weight.multiply(similarity).getDoubleValue()
					); //DEBUG
					weightedSimilaritiesVector.setWeight(i, weight.multiply(similarity));
				}
			}
		}

		LinearCombinationMatrix c = new LinearCombinationMatrix(weightedSimilaritiesVector);
		c.setName(headString + "_" + dependentRepresentation.getName());
		return c;
	}
    
    public static void mainForArticle(String[] args){
        
        //space
        //File projectFolder = new File("/local/falken-1");
        File projectFolder = new File("/local/william");
        //File projectFolder = new File("/disk/scratch/william.banff");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/msrscc/msrscc.space");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        
        //experiment
        Msrscc exp = new Msrscc();

        //gather and save joint counts
        File jdopsFolder = new File(projectFolder, "experiments/msrscc/jdops");
		exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, 8, 1000);
		/*HashSet<String> targetWordsForArticle = new HashSet<>();
		targetWordsForArticle.addAll(Arrays.asList(
			//new String[]{ "catch", "throw", "ball", "party", "blue", "white", "red", "yellow" }
			//new String[]{ "line", "page", "turn", "click", "leave", "bit", "paper", "look", "one", "type", "include", "follow", "appear", "add", "anything", "increase", "come", "return", "see", "article", "show", "set", "make", "effect", "datum", "team", "case", "@card@", "report", "course", "range", "form", "use", "level", "thing", "ninety", "time", "difficult", "people", "require", "apply", "high", "sell", "eight", "live", "bring", "buy", "write", "few", "tell", "close", "stand", "everything", "fall", "provide", "die", "produce", "lead", "present", "more", "thousand", "give", "feel", "know", "launch", "win", "last", "everyone", "meet", "seem", "available", "mean", "open", "try", "four", "say", "hold", "get", "call", "update", "good", "offer", "individual", "play", "help", "patient", "visit", "anyone", "comment", "go", "letter", "money", "age", "post", "music", "message", "risk", "rule", "aspect", "start", "evidence", "law", "matter", "factor", "scheme", "file", "image", "end", "item", "something", "order", "date", "building", "spend", "woman", "house", "sector", "document", "body", "link", "application", "word", "game", "position", "role", "source", "amount", "two", "idea", "meeting", "right", "policy", "price", "world", "standard", "version", "party", "side", "term", "name", "rate", "century", "view", "interest", "business", "product", "book", "study", "period", "question", "condition", "process", "value", "school", "change", "person", "country", "community", "week", "development", "programme", "material", "point", "child", "member", "man", "experience", "life", "event", "project", "number", "take", "detail", "part", "problem", "place", "company", "site", "group", "issue", "day", "system", "work", "area", "way", "service", "information", "year" }
            //new String[]{ "aboard", "about", "above", "accept", "access", "achieve", "across", "add", "address", "affect", "afford", "after", "against", "ago", "allow", "along", "alongside", "although", "amid", "among", "amongst", "apply", "around", "ask", "assign", "assure", "attend", "attract", "avoid", "award", "base", "bear", "beat", "because", "before", "begin", "behind", "below", "beneath", "beside", "besides", "between", "betwixt", "beyond", "book", "break", "bring", "build", "buy", "call", "@card@", "carry", "cause", "change", "charge", "check", "chez", "choose", "circa", "close", "collect", "come", "complete", "concern", "consider", "contact", "contain", "control", "cost", "count", "cover", "create", "cum", "cut", "deliver", "demonstrate", "deny", "describe", "despite", "detail", "determine", "develop", "discuss", "down", "download", "draw", "drive", "drop", "earn", "eat", "email", "e-mail", "en", "encourage", "enhance", "enjoy", "ensure", "enter", "establish", "etc", "etc.", "examine", "except", "exclude", "exist", "experience", "explain", "explore", "express", "face", "feature", "feed", "feel", "find", "follow", "forgive", "form", "frae", "gain", "generate", "get", "give", "go", "grant", "guarantee", "hear", "help", "hit", "hold", "identify", "i.e.", "implement", "improve", "include", "increase", "inform", "inside", "introduce", "involve", "join", "keep", "kill", "know", "land", "last", "launch", "lead", "learn", "leave", "lend", "let", "live", "loan", "look", "lord", "lose", "love", "mail", "maintain", "make", "manage", "mean", "meet", "minus", "miss", "more", "move", "na", "nearest", "need", "next", "notwithstanding", "obtain", "off", "offer", "once", "onto", "open", "opposite", "order", "out", "outside", "outwith", "over", "owe", "par", "pass", "past", "pay", "pend", "perform", "pick", "place", "play", "please", "plus", "post", "present", "prevent", "produce", "promise", "promote", "protect", "prove", "provide", "publish", "push", "put", "quote", "raise", "reach", "read", "receive", "recommend", "record", "reduce", "reflect", "refuse", "regard", "remind", "remove", "render", "replace", "report", "represent", "require", "return", "run", "save", "say", "secure", "see", "seek", "select", "sell", "send", "serve", "set", "share", "shew", "show", "spare", "spend", "start", "stop", "strike", "study", "supply", "support", "take", "teach", "telephone", "tell", "thank", "think", "thoughout", "through", "throw", "toward", "treat", "try", "turn", "under", "underneath", "understand", "undertake", "unlike", "until", "unto", "upon", "use", "versus", "view", "visit", "vs.", "walk", "want", "watch", "wear", "welcome", "whereas", "while", "whilst", "whither", "win", "wish", "with", "work", "worth", "write" }
            new String[]{ "aboard", "about", "above", "accept", "access", "achieve", "across", "add", "address", "affect", "afford", "after", "against", "age", "ago", "allow", "along", "alongside", "although", "amid", "among", "amongst", "amount", "anyone", "anything", "appear", "application", "apply", "area", "around", "article", "ask", "aspect", "assign", "assure", "attend", "attract", "available", "avoid", "award", "ball", "base", "bear", "beat", "because", "before", "begin", "behind", "below", "beneath", "beside", "besides", "between", "betwixt", "beyond", "bit", "blue", "body", "book", "break", "bring", "build", "building", "business", "buy", "call", "@card@", "carry", "case", "catch", "cause", "century", "change", "charge", "check", "chez", "child", "choose", "circa", "click", "close", "collect", "come", "comment", "community", "company", "complete", "concern", "condition", "consider", "contact", "contain", "control", "cost", "count", "country", "course", "cover", "create", "cum", "cut", "date", "datum", "day", "deliver", "demonstrate", "deny", "describe", "despite", "detail", "determine", "develop", "development", "die", "difficult", "discuss", "document", "down", "download", "draw", "drive", "drop", "earn", "eat", "effect", "eight", "email", "e-mail", "en", "encourage", "end", "enhance", "enjoy", "ensure", "enter", "establish", "etc", "etc.", "event", "everyone", "everything", "evidence", "examine", "except", "exclude", "exist", "experience", "explain", "explore", "express", "face", "factor", "fall", "feature", "feed", "feel", "few", "file", "find", "follow", "forgive", "form", "four", "frae", "gain", "game", "generate", "get", "give", "go", "good", "grant", "group", "guarantee", "hear", "help", "high", "hit", "hold", "house", "idea", "identify", "i.e.", "image", "implement", "improve", "include", "increase", "individual", "inform", "information", "inside", "interest", "introduce", "involve", "issue", "item", "join", "keep", "kill", "know", "land", "last", "launch", "law", "lead", "learn", "leave", "lend", "let", "letter", "level", "life", "line", "link", "live", "loan", "look", "lord", "lose", "love", "mail", "maintain", "make", "man", "manage", "material", "matter", "mean", "meet", "meeting", "member", "message", "minus", "miss", "money", "more", "move", "music", "na", "name", "nearest", "need", "next", "ninety", "notwithstanding", "number", "obtain", "off", "offer", "once", "one", "onto", "open", "opposite", "order", "out", "outside", "outwith", "over", "owe", "page", "paper", "par", "part", "party", "pass", "past", "patient", "pay", "pend", "people", "perform", "period", "person", "pick", "place", "play", "please", "plus", "point", "policy", "position", "post", "present", "prevent", "price", "problem", "process", "produce", "product", "programme", "project", "promise", "promote", "protect", "prove", "provide", "publish", "push", "put", "question", "quote", "raise", "range", "rate", "reach", "read", "receive", "recommend", "record", "red", "reduce", "reflect", "refuse", "regard", "remind", "remove", "render", "replace", "report", "represent", "require", "return", "right", "risk", "role", "rule", "run", "save", "say", "scheme", "school", "sector", "secure", "see", "seek", "seem", "select", "sell", "send", "serve", "service", "set", "share", "shew", "show", "side", "site", "something", "source", "spare", "spend", "stand", "standard", "start", "stop", "strike", "study", "supply", "support", "system", "take", "teach", "team", "telephone", "tell", "term", "thank", "thing", "think", "thoughout", "thousand", "through", "throw", "time", "toward", "treat", "try", "turn", "two", "type", "under", "underneath", "understand", "undertake", "unlike", "until", "unto", "update", "upon", "use", "value", "version", "versus", "view", "visit", "vs.", "walk", "want", "watch", "way", "wear", "week", "welcome", "whereas", "while", "whilst", "white", "whither", "win", "wish", "with", "woman", "word", "work", "world", "worth", "write", "year", "yellow" }
		));
		exp.extractAndSaveJointCountsFromCorpus(jdopsFolder, "forArticle", 1, targetWordsForArticle);
		*/
        
		//associationate jdops to ldops
        File marginalCountsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/marginalcounts.gz");
        DepMarginalCounts dmc = DepMarginalCounts.importFromFile(marginalCountsFile);
        int delta = 5000;
        int ldopCardinality = 2000;
        SppmiFunction sf = new SppmiFunction(dmc, delta, ldopCardinality);
		File ldopsFolder = new File(projectFolder, "experiments/msrscc/ldops");
		exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolder);
        //File ldopsFolderUgly = new File(projectFolder, "experiments/msrscc/ldops.ugly");
        //File ldopsFolderPretty = new File(projectFolder, "experiments/msrscc/ldops.pretty");
		//Helper.prettyPrint = false;
        //exp.importAssociationateAndSaveMatrices(jdopsFolder, dmc, sf, ldopsFolderUgly);
        //Helper.prettyPrint = true;
        //exp.saveMatrices(ldopsFolderPretty);
        
        /*
		File ldopsFolderUgly = new File(projectFolder, "experiments/msrscc/ldops.ugly");
        File ldopsFolderPretty = new File(projectFolder, "experiments/msrscc/ldops.pretty");
		exp.importMatrices(ldopsFolderUgly, false);

		File innerProductsFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/msrscc/innerProducts.txt");
        InnerProductsCache ipc = new InnerProductsCache();
		ipc.importFromFile(innerProductsFile);

		LinearCombinationMatrix yellowL = new LinearCombinationMatrix("yellow");
		LinearCombinationMatrix yellowBallL = compose(ipc, "ball", DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationClusterName("mod-1"), yellowL);
        ValueMatrix yellowBallV = yellowBallL.toValueMatrix();
        
        LinearCombinationMatrix catchYellowBallL = compose(ipc, "catch", DepNeighbourhoodSpace.getDepRelationClusterFromDepRelationClusterName("obj-1"), yellowBallL);
        ValueMatrix catchYellowBallV = catchYellowBallL.toValueMatrix();

        Helper.prettyPrint = false;
        yellowBallV.saveToFile(new File(ldopsFolderUgly, "yellow_ball"));
        catchYellowBallV.saveToFile(new File(ldopsFolderUgly, "catch_yellow_ball"));
        Helper.prettyPrint = true;
        yellowBallV.saveToFile(new File(ldopsFolderPretty, "yellow_ball"));
        catchYellowBallV.saveToFile(new File(ldopsFolderPretty, "catch_yellow_ball"));
		*/
    }
    
    public static void main(String[] args){

        //space
        File projectFolder = new File("/local/william");
        DepNeighbourhoodSpace.setProjectFolder(projectFolder);
        File spaceFile = new File(projectFolder, "preprocessed/ukwac.depParsed/5up5down/msrscc/msrscc.space");
        DepNeighbourhoodSpace.importFromFile(spaceFile);
        DepNeighbourhoodSpace.saveToFile(spaceFile);
        DepNeighbourhoodSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);
        
        //experiment
        Msrscc exp = new Msrscc();

        //
        File ldopsFolder = new File(projectFolder, "experiments/msrscc/ldops");
        exp.importMatrices(ldopsFolder, false);
    }
    
}

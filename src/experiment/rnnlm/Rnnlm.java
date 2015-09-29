package experiment.rnnlm;

import cdt.Helper;
import experiment.Dataset;
import featureExtraction.AbstractFeatureVectorExtractor;
import featureExtraction.FeatureVectorsCollection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wblacoe
 */
public class Rnnlm {
    
    protected static Pattern msrpcPattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" paraphrastic=\\\"(.*?)\\\">");
    protected static Pattern rw2012Pattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" label=\\\"(.*?)\\\">");
    protected static Pattern sickPattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" similarity=\\\".*\\\" entailment=\\\"(.*?)\\\">");
    protected static Pattern zeichnerPattern = Pattern.compile("<s sentencepair=\\\"(.*?)\\\" sentence=\\\"(.*?)\\\" entailing=\\\"(.*?)\\\">");

    public Rnnlm(){

	}
    
    
    private Double[] importVectorFromString(String line, String delimiter){
        String[] entries = line.split(delimiter);
        Double[] vector = new Double[entries.length];
        for(int i=0; i<entries.length; i++){
            vector[i] = Double.parseDouble(entries[i]);
        }
        
        return vector;
    }
    
    public Dataset importRessources(String name, Pattern pattern, File datasetFile, File vectorsFile, HashMap<String, Integer> labelToIntegerMap){
        Helper.report("[Rnnlm] Importing " + name + " ressources...");
        
        Dataset dataset = new Dataset();
        
        String vectorStringDelimiter = " ";
        
        try{
            BufferedReader inDataset = Helper.getFileReader(datasetFile);
            BufferedReader inVectors = Helper.getFileReader(vectorsFile);
            
            String line;
            Matcher matcher;
            while((line = inDataset.readLine()) != null){
                if(line.startsWith("<s") && (matcher = pattern.matcher(line)).find()){
                    Integer index = Integer.parseInt(matcher.group(1));
                    Integer sentenceNumber = Integer.parseInt(matcher.group(2));
                    Instance instance = (Instance) dataset.getInstance(index);
                    Double[] vector = importVectorFromString(inVectors.readLine(), vectorStringDelimiter);
                    Integer label = labelToIntegerMap.get(matcher.group(3));
                    
                    if(instance == null) instance = new Instance();
                    instance.setIndex(index);
                    if(sentenceNumber == 1){
                        instance.vector1 = vector;
                    }else if(sentenceNumber == 2){
                        instance.vector2 = vector;
                    }
                    instance.label = label;

                    dataset.setInstance(index, instance);
                }
            }
            
            inDataset.close();
            inVectors.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Helper.report("[Rnnlm] ...Finished importing " + name + " ressources.");
        return dataset;
    }
    
	
	private Collection<Integer> importIndicesFromFile(File file){
		Collection<Integer> indices = new HashSet<>();
		
		try{
			BufferedReader in = Helper.getFileReader(file);
		
			String line;
			while((line = in.readLine()) != null){
				Integer index = Integer.parseInt(line);
				indices.add(index);
			}
			
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return indices;
	}
	
    public void a(File datasetsFolder, File vectorsFolder, File localFolder){

		try{
		
			HashMap<String, Integer> msrpcLabelToIntegerMap = new HashMap<>();
			msrpcLabelToIntegerMap.put("0", 0);
			msrpcLabelToIntegerMap.put("1", 1);
			Dataset msrpcDataset = importRessources("msrpc", msrpcPattern, new File(datasetsFolder, "msrpc/all"), new File(vectorsFolder, "msrpc.e1.gz"), msrpcLabelToIntegerMap);
			ArrayList<String> msrpcFeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(localFolder, "featurenameslist/msrpc.fs58.list"));
			AbstractFeatureVectorExtractor msrpcExtractor = new AbstractFeatureVectorExtractor(new String[]{ "fromVectors" }, null, msrpcFeatureList);
			FeatureVectorsCollection msrpcFvc = new FeatureVectorsCollection(new File(localFolder, "fvec.dops/msrpc.fvec"));
			msrpcFvc.integrate(msrpcExtractor.extract(msrpcDataset));
			msrpcFvc.applyFeatureSelection(msrpcFeatureList);
			msrpcFvc.exportToFvecFile(new File(localFolder, "fvec.rnnlm/msrpc.fvec"));
			msrpcFvc.exportToArffFile(new File(localFolder, "arff.rnnlm/msrpc.train.arff"), importIndicesFromFile(new File(datasetsFolder, "msrpc/indices.train")), new String[]{ "0.0", "1.0" });
			msrpcFvc.exportToArffFile(new File(localFolder, "arff.rnnlm/msrpc.test.arff"), importIndicesFromFile(new File(datasetsFolder, "msrpc/indices.test")), new String[]{ "0.0", "1.0" });

			HashMap<String, Integer> rw2012LabelToIntegerMap = new HashMap<>();
			rw2012LabelToIntegerMap.put("BACKWARDS_CONTAINMENT", 0);
			rw2012LabelToIntegerMap.put("CONTAINMENT", 1);
			rw2012LabelToIntegerMap.put("PARAPHRASE", 2);
			rw2012LabelToIntegerMap.put("RELATED", 3);
			rw2012LabelToIntegerMap.put("UNRELATED", 4);
			Dataset rw2012Dataset = importRessources("rw2012", rw2012Pattern, new File(datasetsFolder, "rw2012/all"), new File(vectorsFolder, "rw2012.e1.gz"), rw2012LabelToIntegerMap);
			ArrayList<String> rw2012FeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(localFolder, "featurenameslist/rw2012.fs20.list"));
			AbstractFeatureVectorExtractor rw2012Extractor = new AbstractFeatureVectorExtractor(new String[]{ "fromVectors" }, null, rw2012FeatureList);
			FeatureVectorsCollection rw2012Fvc = new FeatureVectorsCollection(new File(localFolder, "fvec.dops/rw2012.fvec"));
			rw2012Fvc.integrate(rw2012Extractor.extract(rw2012Dataset));
			rw2012Fvc.applyFeatureSelection(rw2012FeatureList);
			rw2012Fvc.exportToFvecFile(new File(localFolder, "fvec.rnnlm/rw2012.fvec"));
			rw2012Fvc.exportToArffFile(new File(localFolder, "arff.rnnlm/rw2012.arff"), new String[]{ "0.0", "1.0", "2.0", "3.0", "4.0" });

			HashMap<String, Integer> sickLabelToIntegerMap = new HashMap<>();
			sickLabelToIntegerMap.put("ENTAILMENT", 0);
			sickLabelToIntegerMap.put("CONTRADICTION", 1);
			sickLabelToIntegerMap.put("NEUTRAL", 2);
			Dataset sickDataset = importRessources("sick", sickPattern, new File(datasetsFolder, "sick/all"), new File(vectorsFolder, "sick.e1.gz"), sickLabelToIntegerMap);
			ArrayList<String> sickFeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(localFolder, "featurenameslist/sick.fs46.list"));
			AbstractFeatureVectorExtractor sickExtractor = new AbstractFeatureVectorExtractor(new String[]{ "fromVectors" }, null, sickFeatureList);
			FeatureVectorsCollection sickFvc = new FeatureVectorsCollection(new File(localFolder, "fvec.dops/sick.fvec"));
			sickFvc.integrate(sickExtractor.extract(sickDataset));
			sickFvc.applyFeatureSelection(sickFeatureList);
			sickFvc.exportToFvecFile(new File(localFolder, "fvec.rnnlm/sick.fvec"));
			sickFvc.exportToArffFile(new File(localFolder, "arff.rnnlm/sick.trialAndTrain.arff"), importIndicesFromFile(new File(datasetsFolder, "sick/indices.trialAndTrain")), new String[]{ "0.0", "1.0", "2.0" });
			sickFvc.exportToArffFile(new File(localFolder, "arff.rnnlm/sick.test.arff"), importIndicesFromFile(new File(datasetsFolder, "sick/indices.test")), new String[]{ "0.0", "1.0", "2.0" });

			HashMap<String, Integer> zeichnerLabelToIntegerMap = new HashMap<>();
			zeichnerLabelToIntegerMap.put("false", 0);
			zeichnerLabelToIntegerMap.put("true", 1);
			Dataset zeichnerDataset = importRessources("zeichner", zeichnerPattern, new File(datasetsFolder, "zeichner.sentences/all"), new File(vectorsFolder, "zeichner.sentences.e1.gz"), zeichnerLabelToIntegerMap);
			ArrayList<String> zeichnerFeatureList = FeatureVectorsCollection.importFeatureNamesList(new File(localFolder, "featurenameslist/zeichner.sentences.fs30.list"));
			AbstractFeatureVectorExtractor zeichnerExtractor = new AbstractFeatureVectorExtractor(new String[]{ "fromVectors" }, null, zeichnerFeatureList);
			FeatureVectorsCollection zeichnerFvc = new FeatureVectorsCollection(new File(localFolder, "fvec.dops/zeichner.sentences.fvec"));
			zeichnerFvc.integrate(zeichnerExtractor.extract(zeichnerDataset));
			zeichnerFvc.applyFeatureSelection(zeichnerFeatureList);
			zeichnerFvc.exportToArffFile(new File(localFolder, "fvec.rnnlm/zeichner.sentencesfvec"), new String[]{ "0.0", "1.0" });
			zeichnerFvc.exportToArffFile(new File(localFolder, "arff.rnnlm/zeichner.sentences.train.arff"), importIndicesFromFile(new File(datasetsFolder, "zeichner.sentences/omriSplit/indices.train")), new String[]{ "0.0", "1.0" });
			zeichnerFvc.exportToArffFile(new File(localFolder, "arff.rnnlm/zeichner.sentences.test.arff"), importIndicesFromFile(new File(datasetsFolder, "zeichner.sentences/omriSplit/indices.test")), new String[]{ "0.0", "1.0" });

		}catch(Exception e){
			e.printStackTrace();
		}
    }
   
    public static void main(String[] args){
        int epoch = (args != null && args.length >= 1 && args[0] != null && !args[0].isEmpty() ? Integer.parseInt(args[0]) : 1);
        
		File datasetsFolder = new File("/disk/scratch/william/datasets");
		File vectorsFolder = new File("/disk/scratch/s1270921/large_scale_rnnlm/repr/e" + epoch);
		File localFolder = new File("/disk/scratch/william/workWithXingxing");
        Rnnlm rnnlm = new Rnnlm();
		rnnlm.a(datasetsFolder, vectorsFolder, localFolder);
    }

}

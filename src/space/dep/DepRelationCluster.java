package space.dep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import space.Dimension;
import space.Mode;

/*
 * Contains context words, index starts with 1, element 0 is dummy word
 *
 * @author wblacoe
*/
public class DepRelationCluster extends Mode {
    
    protected static final Pattern drcPattern = Pattern.compile("<mode index=\\\"(.*?)\\\" name=\\\"(.*?)\\\" direction=\\\"(.*?)\\\">");
    protected boolean isFromHeadToDependent;
    protected ArrayList<String> depRelationStrings;
    protected HashMap<String, ContextWord> wordContextWordMap;

    public DepRelationCluster(int vocabularySize){
        super(vocabularySize);
        depRelationStrings = new ArrayList<>();
        wordContextWordMap = new HashMap<>();
    }
    public DepRelationCluster(boolean isFromHeadToDependent){
        super();
        this.isFromHeadToDependent = isFromHeadToDependent;
        depRelationStrings = new ArrayList<>();
        wordContextWordMap = new HashMap<>();
    }
    public DepRelationCluster(int vocabularySize, boolean isFromHeadToDependent){
        super(vocabularySize);
        this.isFromHeadToDependent = isFromHeadToDependent;
        depRelationStrings = new ArrayList<>();
        wordContextWordMap = new HashMap<>();
    }
    
    
    
    public Boolean isFromHeadToDependent(){
		return isFromHeadToDependent;
	}
    
    public ArrayList<String> getDepRelationStrings(){
        return depRelationStrings;
    }
    
    public void addDepRelationString(String depRelationString){
        depRelationStrings.add(depRelationString);
    }
	
    //dimensionIndex should be in [1;dimensionality]
    public void setContextWord(int dimensionIndex, ContextWord cw){
        this.setDimensionObject(dimensionIndex, cw);
        wordContextWordMap.put(cw.getWord(), cw);
    }
	
    public ContextWord getContextWord(int dimensionIndex){
        Dimension dimensionObject = getDimensionObject(dimensionIndex);
        if(dimensionObject != null){
            return ((ContextWord) dimensionObject);
        }else{
            return null;
        }
    }
    
    public boolean hasContextWord(String word){
        return wordContextWordMap.containsKey(word);
    }
    
    public ContextWord getContextWord(String word){
        return wordContextWordMap.get(word);
    }
    
    public Integer getDimensionIndex(String word){
        ContextWord cw = wordContextWordMap.get(word);
        if(cw == null){
            return null;
        }else{
            return cw.getDimensionIndex();
        }
    }
    
    public int getAmountOfContextWords(){
        return getDimensionality();
    }
    
    public static void importDepRelationsFromReader(BufferedReader in, DepRelationCluster drc) throws IOException{
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</deprelations>")){
                break;
            }else if(!line.isEmpty() && !line.startsWith("#")){
                drc.addDepRelationString(line);
            }
        }
    }
    
    //returns dimensionality
    private static void importDimensionsFromReader(BufferedReader in, DepRelationCluster drc) throws IOException{
        String line;
        while((line = in.readLine()) != null){
            if(line.equals("</dimensions>")){
                break;
            }else{
                ContextWord cw = ContextWord.importFromString(line);
                drc.setContextWord(cw.getDimensionIndex(), cw);
            }
        }
    }
    
    public static DepRelationCluster importFromReader(BufferedReader in, String line) throws IOException{
    
        DepRelationCluster drc = null;
        
        int modeIndex;
        String modeName;
        boolean isFromHeadToDependent;
        Matcher matcher = drcPattern.matcher(line);
        if(matcher.find()){
            modeIndex = Integer.parseInt(matcher.group(1));
            modeName = matcher.group(2);
            isFromHeadToDependent = matcher.group(3).equals("h2d");
            drc = new DepRelationCluster(DepNeighbourhoodSpace.getDimensionality(), isFromHeadToDependent);
            drc.setModeIndex(modeIndex);
            drc.setName(modeName);
            while((line = in.readLine()) != null){
                if(line.startsWith("<deprelations")){
                   importDepRelationsFromReader(in, drc);
                }else if(line.startsWith("<dimensions")){
                    importDimensionsFromReader(in, drc);
                }else if(line.equals("</mode>")){
                    break;
                }
            }
        }
        
        return drc;
    }
    
    public void saveToWriter(BufferedWriter out) throws IOException{
        out.write("<mode index=\"" + getModeIndex() + "\" name=\"" + getName() + "\" direction=\"" + (isFromHeadToDependent() ? "h2d" : "d2h") + "\">\n");
        out.write("<deprelations>\n");
        for(String depRelationString : depRelationStrings){
            out.write(depRelationString + "\n");
        }
        out.write("</deprelations>\n");
        out.write("<dimensions>\n");
        for(int d=1; d<=getDimensionality(); d++){
            ContextWord cw = getContextWord(d);
            if(cw == null){
                System.out.println("DRC " + name + ", dimension " + d + ", context word cannot be saved to file because NULL!"); //DEBUG
            }else{
                cw.saveToWriter(out);
            }
        }
        out.write("</dimensions>\n");
        out.write("</mode>\n");
    }


    public String toStringDetailed(){
        String s = "mode #" + getModeIndex() + " \"" + getName() + "\"\n";
        
        if(!depRelationStrings.isEmpty()){
            s += "dep relations:\n";
            for(String depRelationString : depRelationStrings){
                s += "\"" + depRelationString + "\"\n";
            }
        }
        
        for(int i=1; i<=Math.min(5,getAmountOfContextWords()); i++){
            ContextWord cw = getContextWord(i);
            if(cw != null){
                s += cw.toString() + "\n";
            }
        }
        return s + "...\n";
    }
    
    @Override
    public String toString(){
        return name;
    }
    
}

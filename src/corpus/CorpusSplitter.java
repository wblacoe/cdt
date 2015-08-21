package corpus;

import cdt.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author wblacoe
 */
public class CorpusSplitter {

    public static void split(File inFile, File outFolder, int amountOfFiles, int minAmountOfLines) {
        try{
            BufferedReader in = Helper.getFileReader(inFile);
            
            for(int i=1; i<=amountOfFiles; i++){
                File outFile = new File(outFolder, i + ".gz");
                BufferedWriter out = Helper.getFileWriter(outFile);
                int lineCounter = 0;
                String line;
                while((line = in.readLine()) != null){
                    out.write(line + "\n");
                    lineCounter++;
                    if(lineCounter >= minAmountOfLines && line.equals("</text>")){
                        break;
                    }
                }
                out.close();
            }
            
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args){
        File inFile = new File(args[0]);
        File outFolder = new File(args[1]);
        int amountOfFiles = Integer.parseInt(args[2]);
        int minAmountOfLines = Integer.parseInt(args[3]);
        
        split(inFile, outFolder, amountOfFiles, minAmountOfLines);
    }
    
}
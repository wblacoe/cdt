package cdt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class Helper {
    
    //how to sort base matrices in a matrix
    public static final int NOT_SORTED = 0;
    public static final int SORTED_BY_VALUE = 1;
    public static final int SORTED_BY_DIMENSION = 2;

    //can be used for anything (e.g. matrix multiplication)
    public static final int QUICK = 0;
    public static final int SLOW = 1;
    
    
    //threading
    public static int amountOfCores = 40;
    
    public static void setAmountOfCores(int n){
        amountOfCores = n;
    }
    public static int getAmountOfCores(){
        return amountOfCores;
    }
    
    
    //marginal counts
    public static File marginalCountsFile;
    public static int minMarginalCount;
    
    public static void setMarginalCountsFile(File f){
        marginalCountsFile = f;
    }
    public static File getMarginalCountsFile(){
        return marginalCountsFile;
    }
    
    public static void setMinMarginalCount(int n){
        minMarginalCount = n;
    }
    public static int getMinMarginalCount(){
        return minMarginalCount;
    }
    

    //log print
	public static String getTimeString(){
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
		return f.format(Calendar.getInstance().getTime());
	}
	
	public synchronized static void report(String s){
		System.out.println(getTimeString() + " " + s);
	}
    
    
    //I/O
    public synchronized static BufferedReader getFileReader(File file) throws IOException{
        return
            file.getName().endsWith(".gz") ?
            new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))) :
            new BufferedReader(new FileReader(file));
    }
    public synchronized static BufferedReader getFileReader(String fileName) throws IOException{
        return getFileReader(new File(fileName));
    }
    
    public synchronized static BufferedWriter getFileWriter(File file) throws IOException{
        ensureContainingFolderExists(file);
        return
            file.getName().endsWith(".gz") ?
            new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8")) :
            new BufferedWriter(new FileWriter(file));
    }
    public synchronized static BufferedWriter getFileWriter(String fileName) throws IOException{
        return getFileWriter(new File(fileName));
    }

    public synchronized static void ensureContainingFolderExists(File file){
        if(file.getParentFile() != null && !file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        } 
    }

 
    public static boolean prettyPrint = false;
    public static boolean prettyRead = false;

}
package ku.syucel17.comp304.proj3;

import ku.syucel17.comp304.proj3.model.FileSystem;
import ku.syucel17.comp304.proj3.model.FileSystemException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Test {

    //Test class is used to create specific test cases. Takes "inputPath" to the input file, and fsType indicating
    //the type of file system to be used for testing. I assume we don't have to comment this class, and regardless,
    //it is pretty self-explanatory.

    private FileSystem fs;
    private int fsType;
    private Scanner read;
    private String inputPath;
    private StringTokenizer tokenizer;

    private int createRejectCounter = 0;
    private int extendRejectCounter = 0;
    private int shrinkRejectCounter = 0;
    private int accessRejectCounter = 0;
    private long runtime = 0;

    public Test(String inputPath, int fsType) {
        this.inputPath = inputPath;
        this.fsType = fsType;

        File file = new File(inputPath);
        try {read = new Scanner(file);}
        catch (FileNotFoundException e) {
            System.err.println("Input file \"" + inputPath + "\" not found.");
            System.exit(0);
        }
        String inputPathSub = inputPath.substring(inputPath.indexOf('_')+1);
        int BLOCK_SIZE = Integer.parseInt(inputPathSub.substring(0, inputPathSub.indexOf('_')));
        fs = new FileSystem(fsType, BLOCK_SIZE);
    }

    public void run(){
        long t_init = System.currentTimeMillis();
        while(read.hasNextLine()){

            tokenizer = new StringTokenizer(read.nextLine(), ":");
            ArrayList<String> args = new ArrayList<String>();
            while(tokenizer.hasMoreTokens())
                args.add(tokenizer.nextToken());

            try {
                switch (args.get(0)) {
                    case "c":
                        fs.createFile(Integer.parseInt(args.get(1)));
                        break;
                    case "a":
                        fs.access(Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)));
                        break;
                    case "e":
                        //if(Integer.parseInt(args.get(1)) == 60 && Integer.parseInt(args.get(2)) == 14)
                        fs.extend(Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)));
                        break;
                    case "sh":
                        fs.shrink(Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)));
                        break;
                }
            } catch (FileSystemException e){
                switch(e.getType()){
                    case FileSystemException.CREATION_REJECTED:
                        createRejectCounter++;
                        break;
                    case FileSystemException.EXTENSION_REJECTED:
                        extendRejectCounter++;
                        break;
                    case FileSystemException.SHRINKING_REJECTED:
                        shrinkRejectCounter++;
                        break;
                    case FileSystemException.ACCESS_REJECTED:
                        accessRejectCounter++;
                        break;
                    default:
                        System.err.println("Error code: " + e.getType());
                        e.printStackTrace();
                }
                if(e.getType()==FileSystemException.FATAL)
                    System.exit(0);
            }
        }
        runtime = System.currentTimeMillis() - t_init;
    }

    public void reset(){
        createRejectCounter = 0;
        extendRejectCounter = 0;
        shrinkRejectCounter = 0;
        accessRejectCounter = 0;
        runtime = 0;
        File file = new File(inputPath);
        try {read = new Scanner(file);}
        catch (FileNotFoundException e) {
            System.err.println("Input file \"" + inputPath + "\" not found.");
            System.exit(0);
        }
        String inputPathSub = inputPath.substring(inputPath.indexOf('_')+1);
        int BLOCK_SIZE = Integer.parseInt(inputPathSub.substring(0, inputPathSub.indexOf('_')));
        fs = new FileSystem(fsType, BLOCK_SIZE);
    }

    public int getCreateRejectCounter() {
        return createRejectCounter;
    }

    public int getExtendRejectCounter() {
        return extendRejectCounter;
    }

    public int getShrinkRejectCounter() {
        return shrinkRejectCounter;
    }

    public int getAccessRejectCounter() {
        return accessRejectCounter;
    }

    public long getRuntime() {
        return runtime;
    }
}

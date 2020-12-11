import java.io.*;
import java.util.ArrayList;

public class Compiler {

    private String srcPath;
    private String dstPath;
    private ArrayList<Instruction> instructions;
    private ArrayList<Instruction> labels;
    private Encoder encoder;

    public Compiler(String srcPath, String dstPath){
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        instructions = new ArrayList<Instruction>();
        labels = new ArrayList<Instruction>();
        encoder = new Encoder();
    }

    public void compile() throws CompilerException {
        readAssemblyCode();
        writeMachineCode();
    }

    private void readAssemblyCode() throws CompilerException {
        int addressCounter = 0;
        File file = new File(srcPath);
        BufferedReader read;
        try {
            read = new BufferedReader(new FileReader(file));
            String newLine;
            while ((newLine = read.readLine()) != null){
                if(newLine.equals(""))
                    continue;
                Instruction instruction = new Instruction(newLine, addressCounter++);
                if(Main.DEBUG)
                    System.out.println("READ " + instruction);
                if(instruction.isLabel()) {
                    labels.add(instruction);
                    addressCounter--;
                }
                else
                    instructions.add(instruction);
            }
            read.close();
        } catch (FileNotFoundException e) {
            System.err.println("Src file not found.");
        } catch (IOException e) {
            System.err.println("Could not read from src.");
        } catch (CompilerException e){
            e.setTypeRead();
            throw e;
        }
    }

    private void writeMachineCode() throws CompilerException {
        File file = new File(dstPath);
        PrintWriter write;
        try{
            if(file.exists()){
                file.delete();
                file.createNewFile();
            }
            write = new PrintWriter(file);
            write.println("v2.0 raw");
            for (Instruction instruction: instructions)
                write.println(encoder.encodeInstruction(instruction, labels));
            write.close();
        } catch (FileNotFoundException e) {
            System.err.println("Dst file not found.");
        } catch (IOException e) {
            System.err.println("Could not write to dst.");
        } catch (CompilerException e){
            e.setTypeCompile();
            throw e;
        }
    }
}

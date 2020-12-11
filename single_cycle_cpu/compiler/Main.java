import java.io.File;

public class Main {

    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        String path = (new File(".")).getAbsolutePath()+"/";

        //Compiler compiler = new Compiler(path+args[0], path+args[1]);
        Compiler compiler = new Compiler(path+"src.txt", path+"code.txt");

        try {
            compiler.compile();
        } catch (CompilerException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}

public class CompilerException extends Exception{

    private String message;
    private String type;
    private Instruction instruction;

    public CompilerException(String message, Instruction instruction){
        this.message = message;
        this.type = "UNKNOWN";
        this.instruction = instruction;
    }

    @Override
    public void printStackTrace() {
        System.err.println(type + " ERROR AT " + instruction.toString() +
                "\nERROR MESSAGE: " + message);
        if(Main.DEBUG)
            super.printStackTrace();
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public void setTypeRead() {
        this.type = "READ";
    }

    public void setTypeCompile() {
        this.type = "COMPILATION";
    }


}

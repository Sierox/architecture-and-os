import java.util.ArrayList;
import java.util.Arrays;

public class Encoder {

    public String encodeInstruction(Instruction instruction, ArrayList<Instruction> labels) throws CompilerException {

        if(Main.DEBUG){
            System.out.println("ENCODING: " + instruction);
        }

        StringBuilder binaryCode = new StringBuilder();
        for (ArgumentType arg: instruction.getDefaultFormat()) {
            switch(arg){
                case opcode:
                    try {
                        binaryCode.append(encodeOpcode(instruction.getArgument(arg)));
                    } catch (CompilerException e) {
                        e.setInstruction(instruction);
                        throw e;
                    }
                    continue;
                case rd, rs, rt:
                    try {
                        binaryCode.append(encodeRegister(instruction.getArgument(arg)));
                    } catch (CompilerException e) {
                        e.setInstruction(instruction);
                        throw e;
                    }
                    continue;
                case shamt:
                    try {
                        binaryCode.append(encodeShamt(instruction.getArgument(arg)));
                    } catch (CompilerException e) {
                        e.setInstruction(instruction);
                        throw e;
                    }
                    continue;
                case funct:
                    binaryCode.append(encodeFunct());
                    continue;
                case immediate:
                    try {
                        binaryCode.append(encodeImmediate(instruction.getArgument(arg)));
                    } catch (CompilerException e) {
                        e.setInstruction(instruction);
                        throw e;
                    }
                    continue;
                case label:
                    boolean foundLabel = false;
                    for (Instruction label: labels) {
                        if(instruction.getArgument(arg).equals(label.getArgument(arg))){
                            String branchAddress = computeBranchAddress(instruction, label);
                            try {
                                binaryCode.append(encodeLabel(branchAddress));
                            } catch (CompilerException e) {
                                e.setInstruction(instruction);
                                throw e;
                            }
                            foundLabel = true;
                            break;
                        }
                    }
                    if(foundLabel)
                        break;
                    throw new CompilerException("Could not find label \"" + instruction.getArgument(arg) + "\" for branch.", instruction);
                case target:
                    boolean foundTarget = false;
                    for (Instruction label: labels) {
                        if(instruction.getArgument(arg).equals(label.getArgument(ArgumentType.label))){
                            String branchAddress = computeBranchAddress(instruction, label);
                            try {
                                binaryCode.append(encodeTarget(branchAddress));
                            } catch (CompilerException e) {
                                e.setInstruction(instruction);
                                throw e;
                            }
                            foundTarget = true;
                            break;
                        }
                    }
                    if(foundTarget)
                        break;
                    throw new CompilerException("Could not find label \"" + instruction.getArgument(arg) + "\" for jump.", instruction);
            }
        }
        return completeDigits(binaryToHexadecimal(binaryCode.toString()), 8);
    }

    private String encodeOpcode(String opcode) throws CompilerException {
        switch(opcode){
            case "add":
                return "000000";
            case "sub":
                return "000001";
            case "mult":
                return "000010";
            case "and":
                return "000011";
            case "or":
                return "000100";
            case "addi":
                return "000101";
            case "sll":
                return "000110";
            case "slt":
                return "000111";
            case "mfhi":
                return "001000";
            case "mflo":
                return "001001";
            case "lw":
                return "001010";
            case "sw":
                return "001011";
            case "beq":
                return "001100";
            case "blez":
                return "001101";
            case "j":
                return "001110";
            case "vak":
                return "001111";
        }
        throw new CompilerException("Could not encode opcode \"" + opcode + "\".", null);
    }

    private String encodeRegister(String register) throws CompilerException {
        if(register == null)
            return "00000";
        if(!register.startsWith("$")){
            throw new CompilerException("Invalid register \"" + register + "\".", null);
        } else {
            int registerNo = 0;
            try{
                registerNo = Integer.parseInt(register.substring(1));
            }
            catch (Exception e){
                throw new CompilerException("Invalid register \"" + register + "\".", null);
            }
            if(registerNo < 0 || registerNo > 15){
                throw new CompilerException("Register \"" + register + "\" is out of bound [$0 - $15].", null);
            } else
                return completeDigits(Integer.toBinaryString(registerNo), 5);
        }
    }

    private String encodeShamt(String shamt) throws CompilerException {
        if(shamt == null){
            return "00000";
        }
        int shamtVal = 0;
        try{
            shamtVal = Integer.parseInt(shamt);
        } catch (Exception e){
            throw new CompilerException("Invalid shamt value: \"" + shamt + "\".", null);
        }
        return completeDigits(Integer.toBinaryString(shamtVal), 5);
    }

    private String encodeFunct(){
        // Funct unused in this instruction set.
        return "000000";
    }

    private String encodeImmediate(String immediate) throws CompilerException {
        int immediateVal = 0;
        try{
            immediateVal = Integer.parseInt(immediate);
        } catch (Exception e){
            throw new CompilerException("Invalid immediate value: \"" + immediate + "\".", null);
        }
        return completeDigits(intToNBitString(immediateVal, 16), 16);
    }

    private String encodeLabel(String label) throws CompilerException {
        int labelVal = 0;
        try{
            labelVal = Integer.parseInt(label);
        } catch (Exception e){
            throw new CompilerException("Invalid label value \"" + label + "\" for branch.", null);
        }
        return completeDigits(intToNBitString(labelVal, 16), 16);
    }

    private String encodeTarget(String target) throws CompilerException {
        int targetVal = 0;
        try{
            targetVal = Integer.parseInt(target);
        } catch (Exception e){
            throw new CompilerException("Invalid label value \"" + target + "\" for jump.", null);
        }
        return completeDigits(intToNBitString(targetVal, 16), 26);
    }

    private String computeBranchAddress(Instruction from, Instruction to) throws CompilerException {
        if (!Arrays.asList(from.getInstructionFormat()).contains(ArgumentType.label) && !Arrays.asList(from.getInstructionFormat()).contains(ArgumentType.target)){
            throw new CompilerException("Instruction does not have label for branch address computation.", from);
        }
        if (!to.isLabel()){
            throw new CompilerException("Branch target is not a label.", to);
        }
        return Integer.toString(to.getAddress() - from.getAddress() - 1);
    }

    private String binaryToHexadecimal(String binary){
        return Integer.toString(Integer.parseInt(binary, 2), 16);
    }

    private String completeDigits(String value, int completeTo){
        StringBuilder sb = new StringBuilder(value);
        for (int i = value.length(); i < completeTo; i++) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    private String intToNBitString(int i, int n){
        String binary = Integer.toBinaryString(i);
        if(binary.length()>n)
            binary = binary.substring(binary.length()-n);
        return binary;
    }
}

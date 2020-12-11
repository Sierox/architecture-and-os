import java.util.Arrays;
import java.util.HashMap;

public class Instruction {

    private int address;
    HashMap<ArgumentType, String> map;

    public Instruction(String instruction, int address) throws CompilerException {
        this.address = address;
        String[] args;
        String[] split = instruction.split(" ");

        if (split[0].startsWith(":")) {
            map = getEmptyLMap();
            map.replace(ArgumentType.opcode, "label");
            map.replace(ArgumentType.label, split[0].substring(1));
            return;
        } else {
            map = getEmptyRMap();
            map.replace(ArgumentType.opcode, split[0]);
            switch (getType()){
                case R:
                    map = getEmptyRMap();
                    break;
                case I:
                    map = getEmptyIMap();
                    break;
                case B:
                    map = getEmptyBMap();
                    break;
                case J:
                    map = getEmptyJMap();
                    break;
            }
            map.replace(ArgumentType.opcode, split[0]);

            args = new String[split.length+1];
            if(split.length >= 3){
                if(!split[2].contains("("))
                    args = new String[split.length];
            }


            for (int i = 1; i < split.length; i++) {
                if (!split[i].contains("("))
                    args[i] = split[i].replace(",", "");
                else {
                    args[i] = split[i].substring(0, split[i].indexOf('('));
                    args[i + 1] = split[i].substring(split[i].indexOf('(')+1, split[i].indexOf(')'));
                }
            }
            int i = 1;
            for (ArgumentType arg: getInstructionFormat()) {
                if(arg == ArgumentType.opcode)
                    continue;
                map.replace(arg, args[i]);
                i++;
            }
            System.out.println(map);
        }
    }

    public int getAddress() {
        return address;
    }

    public String getArgument(ArgumentType type) {
        return map.get(type);
    }

    public HashMap<ArgumentType, String> getMap() {
        return map;
    }

    public boolean isLabel() {
        return getArgument(ArgumentType.opcode).equals("label");
    }


    public InstructionType getType() throws CompilerException {
        switch(getArgument(ArgumentType.opcode)){
            case "add", "sub", "mult", "and", "or", "sll", "slt", "mfhi", "mflo", "div":
                return InstructionType.R;
            case "addi", "lw", "sw":
                return InstructionType.I;
            case "beq", "blez":
                return InstructionType.B;
            case "j":
                return InstructionType.J;
            case "label":
                return InstructionType.L;
        }
        throw new CompilerException("Undefined opcode \"" + getArgument(ArgumentType.opcode) + "\".", this);
    }

    public ArgumentType[] getInstructionFormat() throws CompilerException {
        switch (getArgument(ArgumentType.opcode)) {
            case "add", "sub", "and", "or", "slt", "div":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rd, ArgumentType.rs, ArgumentType.rt};
            case "mult":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rs, ArgumentType.rt};
            case "sll":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rd, ArgumentType.rs, ArgumentType.shamt};
            case "addi":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rt, ArgumentType.rs, ArgumentType.immediate};
            case "mfhi", "mflo":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rd};
            case "lw", "sw":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rt, ArgumentType.immediate, ArgumentType.rs};
            case "beq":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rs, ArgumentType.rt, ArgumentType.label};
            case "blez":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rs, ArgumentType.label};
            case "j":
                return new ArgumentType[]{ArgumentType.opcode, ArgumentType.target};
        }
        throw new CompilerException("Undefined opcode \"" + getArgument(ArgumentType.opcode) + "\".", this);
    }

    public ArgumentType[] getDefaultFormat() throws CompilerException {
        switch (getType()){
            case R:
                return getRFormat();
            case I:
                return getIFormat();
            case B:
                return getBFormat();
            case J:
                return getJFormat();
        }
        return null;
    }

    public static ArgumentType[] getRFormat(){
        return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rs, ArgumentType.rt, ArgumentType.rd, ArgumentType.shamt, ArgumentType.funct};
    }

    public static ArgumentType[] getIFormat(){
        return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rs, ArgumentType.rt, ArgumentType.immediate};
    }

    public static ArgumentType[] getBFormat(){
        return new ArgumentType[]{ArgumentType.opcode, ArgumentType.rs, ArgumentType.rt, ArgumentType.label};
    }

    public static ArgumentType[] getJFormat(){
        return new ArgumentType[]{ArgumentType.opcode, ArgumentType.target};
    }

    public static ArgumentType[] getLFormat(){
        return new ArgumentType[]{ArgumentType.opcode, ArgumentType.label};
    }

    public static HashMap<ArgumentType, String> getEmptyRMap(){
        HashMap<ArgumentType, String> map = new HashMap<ArgumentType, String>();
        for (ArgumentType arg: getRFormat()) {
            map.put(arg, null);
        }
        return map;
    }

    public static HashMap<ArgumentType, String> getEmptyIMap(){
        HashMap<ArgumentType, String> map = new HashMap<ArgumentType, String>();
        for (ArgumentType arg: getIFormat()) {
            map.put(arg, null);
        }
        return map;
    }

    public static HashMap<ArgumentType, String> getEmptyBMap(){
        HashMap<ArgumentType, String> map = new HashMap<ArgumentType, String>();
        for (ArgumentType arg: getBFormat()) {
            map.put(arg, null);
        }
        return map;
    }

    public static HashMap<ArgumentType, String> getEmptyJMap(){
        HashMap<ArgumentType, String> map = new HashMap<ArgumentType, String>();
        for (ArgumentType arg: getJFormat()) {
            map.put(arg, null);
        }
        return map;
    }

    public static HashMap<ArgumentType, String> getEmptyLMap(){
        HashMap<ArgumentType, String> map = new HashMap<ArgumentType, String>();
        for (ArgumentType arg: getLFormat()) {
            map.put(arg, null);
        }
        return map;
    }

    @Override
    public String toString() {
        return address + ": " + map.toString();
    }
}

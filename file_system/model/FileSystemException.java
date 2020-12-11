package ku.syucel17.comp304.proj3.model;

public class FileSystemException extends Exception{

    //Self-explanatory class for handling exceptions in the FileSystem.

    public static final int CREATION_REJECTED = 0;
    public static final int EXTENSION_REJECTED = 1;
    public static final int SHRINKING_REJECTED = 2;
    public static final int ACCESS_REJECTED = 3;
    public static final int DT_ERROR = 4;
    public static final int FAT_ERROR = 5;
    public static final int FATAL = 6;
    public static final int OTHER = 7;

    private int type;
    public FileSystemException(String message, int type){
        super(message);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}

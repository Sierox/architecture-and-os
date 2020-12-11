package ku.syucel17.comp304.proj3.model;

public class FileAllocationTable {

    /* Array holding the FAT entries. Each entry is 4 bytes (1 int).
    * If entry slot i is empty, t[i] = -2
    * If entry slot i is the end of a file, t[i] = -1
    * If entry slot i is pointing to the next block b, t[i] = b
    * */
    private int[] t;

    // Other than the format/conventions of "t", the data structure is self-explanatory.

    public FileAllocationTable(){
        t = new int[FileSystem.NUMBER_BLOCKS];
        for (int i=0; i<t.length; i++)
            t[i] = -2;
    }

    public void setNext(int i, int n) throws FileSystemException {
        checkBounds(i);
        if (!(n >= 0 && n < t.length))
            throw new FileSystemException("Next block: " + n + " is not in bounds of [0 - " + (t.length - 1) + "].", FileSystemException.FAT_ERROR);
        t[i] = n;
    }

    public void setEmpty(int i) throws FileSystemException {
        checkBounds(i);
        t[i] = -2;
    }

    public void setEnd(int i) throws FileSystemException {
        checkBounds(i);
        t[i] = -1;
    }

    public int getNext(int i) throws FileSystemException {
        checkBounds(i);
        if (isEmpty(i) || isEnd(i))
            throw new FileSystemException("Block: " + i + " is empty or an end block.", FileSystemException.FAT_ERROR);
        return t[i];
    }

    public boolean isEmpty(int i) throws FileSystemException {
        checkBounds(i);
        return t[i]==-2;
    }

    public boolean isEnd(int i) throws FileSystemException {
        checkBounds(i);
        return t[i]==-1;
    }

    private void checkBounds(int i) throws FileSystemException {
        if (!(i>=0 && i<t.length))
            throw new FileSystemException("Block:" + i + " is not in bounds of [0 - " + (t.length-1) + "].", FileSystemException.FAT_ERROR);
    }
}

package ku.syucel17.comp304.proj3.model;

import java.util.ArrayList;

public class DirectoryTable {

    /* ArrayList holding the DT entries.
    * Each item is an entry with a 3D vector with values:
    * 0: File identifier
    * 1: Starting block
    * 2: File size
    * */
    private ArrayList<Integer[]> t;

    // Other than the format/conventions of "t", the data structure is self-explanatory.

    public DirectoryTable(){
        t = new ArrayList<>();
    }

    public void addEntry(int id, int startingBlock, int fileSize) throws FileSystemException {
        if(t.size() == FileSystem.NUMBER_BLOCKS)
            throw new FileSystemException("DT already at full size, can't add more entries.", FileSystemException.DT_ERROR);
        Integer[] n = {id, startingBlock, fileSize};
        t.add(n);
    }

    public boolean containsId(int id){
        for (int i=0; i<t.size(); i++){
            if(t.get(i)[0]==id) {
                return true;
            }
        }
        return false;
    }

    public boolean containsStartingBlock(int startingBlock){
        for (int i=0; i<t.size(); i++){
            if(t.get(i)[1]==startingBlock) {
                return true;
            }
        }
        return false;
    }

    public void removeEntry(int id) throws FileSystemException {
        for (int i=0; i<t.size(); i++){
            if(t.get(i)[0]==id) {
                t.remove(t.get(i));
                break;
            }
        }
        throw new FileSystemException("DT entry with id: " + id + " not found.", FileSystemException.DT_ERROR);
    }

    public void modifyEntry(int id, int startingBlock, int fileSize) throws FileSystemException {
        for (int i=0; i<t.size(); i++){
            if(t.get(i)[0]==id) {
                if(startingBlock != -1)
                    t.get(i)[1] = startingBlock;
                if(fileSize != -1)
                    t.get(i)[2] = fileSize;
                return;
            }
        }
        throw new FileSystemException("DT entry with id: " + id + " not found.", FileSystemException.DT_ERROR);
    }

    public Integer[] getEntryFromId(int id) throws FileSystemException {
        for (int i=0; i<t.size(); i++){
            if(t.get(i)[0]==id)
                return t.get(i);
        }
        throw new FileSystemException("DT entry with id: " + id + " not found.", FileSystemException.DT_ERROR);
    }

    public Integer[] getEntryFromStartingBlock(int startingBlock) throws FileSystemException {
        for(int i=0; i<t.size(); i++){
            if(t.get(i)[1]==startingBlock)
                return t.get(i);
        }
        throw new FileSystemException("DT entry with starting block: " + startingBlock + " not found.", FileSystemException.DT_ERROR);
    }

    public int size(){
        return t.size();
    }

    public void clear(){
        t.clear();
    }
}

package ku.syucel17.comp304.proj3.model;

import java.util.Random;

public class FileSystem {

    // Main class for the File System structure.

    public static final int CONTIGUOUS = 0;
    public static final int LINKED = 1;

    protected static final int NUMBER_BLOCKS = 32768;
    protected int BLOCK_SIZE;
    Random r;
    private int type;
    private int ids = 0;

    private FileAllocationTable fat;
    private DirectoryTable dt;
    private int[] blocks;
    private int buffer;

    public FileSystem(int type, int BLOCK_SIZE) {
        this.type = type;
        this.BLOCK_SIZE = BLOCK_SIZE;
        if (type == LINKED) fat = new FileAllocationTable();
        dt = new DirectoryTable();
        blocks = new int[NUMBER_BLOCKS];
        r = new Random();
    }

    /* This function allocates a space for the file
        of size file length bytes on the disk, updates the DT and the FAT. The file blocks
        may be filled with random number greater than zero. A suitable hole need to be found
        in contiguous allocation case. If no whole is found, you need to do compaction/defragmentation of
        the directory contents. If there is not enough space to store the file, the operation will be
        rejected with a warning message.
    */
    public void createFile(int fileSize) throws FileSystemException {
        //Check if there is enough space for the file
        if (byteToBlock(fileSize) > getFreeBlockAmount())
            throw new FileSystemException("Not enough space for create.", FileSystemException.CREATION_REJECTED);
        else {
            //Generate id for file.
            int fileID = generateID();
            switch (type) {
                case CONTIGUOUS:
                    //Try to find a contiguous space for the file (returns -1 if not found).
                    int holeIndex = findHole(byteToBlock(fileSize));
                    //If contiguous space not found, defragment (returns index of first empty block).
                    if (holeIndex == -1)
                        holeIndex = defragment();
                    //Add DT entry for file.
                    dt.addEntry(fileID, holeIndex, fileSize);
                    //Fill the blocks owned by the file.
                    for (int i = holeIndex; i < (holeIndex + byteToBlock(fileSize)); i++)
                        blocks[i] = getRandomBlockData();
                    break;
                case LINKED:
                    //Start by finding an empty block and filling it.
                    int blockIndex = findEmptyBlock(0);
                    blocks[blockIndex] = getRandomBlockData();
                    //Add DT entry for file
                    dt.addEntry(fileID, blockIndex, fileSize);
                    //Iterate once for each block the file will cover (except the first one).
                    for (int i = 1; i < byteToBlock(fileSize); i++) {
                        //Set "previous index" to the index from the last iteration.
                        int prev = blockIndex;
                        //Find a new empty block (starting from index prev, since we know the ones before it are full).
                        blockIndex = findEmptyBlock(prev);
                        //Fill the new block.
                        blocks[blockIndex] = getRandomBlockData();
                        //Modify FAT such that index "prev" points to index "blockIndex".
                        fat.setNext(prev, blockIndex);
                    }
                    //Set the last index in FAT to -1 (symbolizing the end of a file).
                    fat.setEnd(blockIndex);
                    break;
                default:
                    throw new FileSystemException("File System in invalid state: " + type, FileSystemException.FATAL);
            }
        }
    }

    /* Returns the location of the byte having the given
        offset in the directory, where byte offset is the offset of that byte from the beginning
        of the file.
    */
    public int access(int fileID, int fileOffset) throws FileSystemException {
        Integer[] entry;
        //Find the entry from ID.
        try {entry = dt.getEntryFromId(fileID); }
        catch (FileSystemException e) {
            throw new FileSystemException("File with id \"" + fileID + "\" does not exist.", FileSystemException.ACCESS_REJECTED);
        }
        //Check is given offset is not greater than the size of the file.
        if (entry[2] < fileOffset)
            throw new FileSystemException("Given fileOffset greater than fileSize.", FileSystemException.ACCESS_REJECTED);
        switch (type) {
            case CONTIGUOUS:
                //Return the information in the relevant block.
                return blocks[entry[1] + (fileOffset / BLOCK_SIZE)];
            case LINKED:
                //Go through the linked blocks using the FAT and return the information in the relevant block.
                int index = entry[1];
                while (!fat.isEnd(index))
                    index = fat.getNext(index);
                return blocks[index];
            default:
                throw new FileSystemException("File System in invalid state: " + type, FileSystemException.FATAL);
        }
    }

    /* Extends the given file by the given amount, where
        extension is the number of blocks not bytes. For simplicity, the extension will always
        add block after the last block of the file. If there is no sufficient space to extend the
        file, the operation will be rejected with a warning message. In contiguous allocation,
        if there is no contiguous space, you need to do compaction and may reallocate the file
        blocks. Remember that you have a buffer that can accommodate only single block.
     */
    public void extend(int fileID, int extension) throws FileSystemException {
        // Check if there is enough space on the directory to begin with.
        if (extension > getFreeBlockAmount())
            throw new FileSystemException("Not enough space for extend.", FileSystemException.EXTENSION_REJECTED);
        else {
            Integer[] entry;
            //Find DT entry from ID.
            try { entry = dt.getEntryFromId(fileID); }
            catch (FileSystemException e) {
                throw new FileSystemException("File with id \"" + fileID + "\" does not exist.", FileSystemException.EXTENSION_REJECTED);
            }
            switch (type) {
                case CONTIGUOUS:
                    // contiguous has two uses depending on the case that runs:
                    // In "Case 1", contiguous is the amount of contiguous space after the file.
                    // In "Case 2", it is the index of the first contiguous space the extended file can fit in.
                    int contiguous = 0;
                    // Here, contiguous is initialized for Case 1, so this code computes the amount of contiguous space
                    // right after the file ends.
                    for (int i = entry[1] + byteToBlock(entry[2]); i < NUMBER_BLOCKS; i++) {
                        if (blocks[i] != 0)
                            break;
                        contiguous++;
                    }
                    if (contiguous >= extension) {
                        // Case 1: File has enough contiguous space next to it.
                        // Go through each block the file will be extended into and fill them.
                        for (int i = entry[1] + byteToBlock(entry[2]); i < extension + entry[1] + byteToBlock(entry[2]); i++)
                            blocks[i] = getRandomBlockData();
                        // Modify the DT entry and extend the file size accordingly.
                        dt.modifyEntry(entry[0], -1, entry[2] + (extension * BLOCK_SIZE));
                    } else if ((contiguous = findHole(byteToBlock(entry[2]) + extension)) != -1) {
                        // Case 2: Extended file fits in another space.
                        // Start by moving he file such that it starts at index "contiguous" (defined inside the else if
                        // such that it points to the first contiguous space the extended file can fit in.).
                        move(entry[1], byteToBlock(entry[2]), contiguous);
                        // Go through each block the file will be extended into and fill them.
                        for (int i = contiguous + byteToBlock(entry[2]); i < contiguous + byteToBlock(entry[2]) + extension; i++) {
                            blocks[i] = getRandomBlockData();
                        }
                        // Modify the DT entry and extend the file size accordingly.
                        dt.modifyEntry(entry[0], contiguous, entry[2] + (extension * BLOCK_SIZE));
                    } else {
                        // Case 3: File fits after defragmentation.
                        // Start by defragmenting.
                        defragment();
                        // Find the DT entry for the file from ID.
                        entry = dt.getEntryFromId(fileID);
                        // Create space for the extension (how this function works is explained above it).
                        createExtensionSpace(entry[1] + byteToBlock(entry[2]), extension);
                        // Go through each block the file will be extended into and fill them.
                        for (int i = entry[1] + byteToBlock(entry[2]); i < entry[1] + byteToBlock(entry[2]) + extension; i++)
                            blocks[i] = getRandomBlockData();
                        // Modify the DT entry and extend the file size accordingly.
                        dt.modifyEntry(entry[0], -1, entry[2] + (extension * BLOCK_SIZE));
                    }
                    break;
                case LINKED:
                    // Set "index" to starting block of file.
                    int index = entry[1];
                    // Go through the file using FAT until reaching the end.
                    while (!fat.isEnd(index))
                        index = fat.getNext(index);
                    // Iterate once for each extension block.
                    for (int i = 0; i < extension; i++) {
                        // Set "previous index" as the index in the last iteration.
                        int prev = index;

                        //Search for empty block from index 0 for i=0.
                        if (i == 0) index = findEmptyBlock(0);
                        //For the rest of the searches, start from the previous index, since we know all blocks before it are full.
                        else index = findEmptyBlock(prev);

                        //Fill new block.
                        blocks[index] = getRandomBlockData();
                        //Modify FAT such that index "prev" points to index "blockIndex".
                        fat.setNext(prev, index);
                    }
                    //Set the last index in FAT to -1 (symbolizing the end of a file).
                    fat.setEnd(index);
                    //Modify DT entry and increase size of the file by appropriate amount.
                    dt.modifyEntry(entry[0], -1, entry[2] + (extension * BLOCK_SIZE));
                    break;
                default:
                    throw new FileSystemException("File System in invalid state: " + type, FileSystemException.FATAL);
            }
        }
    }

    /* Shrinks the file by the given number of blocks. The
        shrinking deallocates the last blocks of the file. Note that deallocation means just that
        these blocks are no more referred by that file and you can use them to store new data,
        and there is no need to move them or the files adjacent to them at the moment. You
        can indicate that block is freed by storing zero in it, knowing that you store random
        positive values in the filled blocks.
    */
    public void shrink(int fileID, int shrinking) throws FileSystemException {
        Integer[] entry;
        // Find DT entry from ID.
        try {
            entry = dt.getEntryFromId(fileID);
        } catch (FileSystemException e) {
            throw new FileSystemException("File with id \"" + fileID + "\" does not exist.", FileSystemException.SHRINKING_REJECTED);
        }
        // Check if shrinking amount is less than the file size. If not reject.
        if (byteToBlock(entry[2]) <= shrinking) {
            throw new FileSystemException("File is smaller or equal to shrinking size.", FileSystemException.SHRINKING_REJECTED);
        } else {
            switch (type) {
                case CONTIGUOUS:
                    //Go through the blocks to be removed and remove the data in them.
                    for (int i = entry[1] + byteToBlock(entry[2]) - 1; i >= entry[1] + byteToBlock(entry[2]) - shrinking; i--)
                        blocks[i] = 0;
                    dt.modifyEntry(entry[0], -1, entry[2] - (shrinking * BLOCK_SIZE));
                    break;
                case LINKED:
                    //Similar to extend,, instead of moving to the end, moves to the block which will be the end after shrinking.
                    int index = entry[1];
                    for (int i = 1; i < byteToBlock(entry[2]) - shrinking; i++)
                        index = fat.getNext(index);
                    //Temp is set as the index of the block which will be the end after shrinking.
                    int temp = index;
                    //Index is set to the next block (first to be deleted) using the FAT.
                    index = fat.getNext(index);
                    //Sets the "temp" block as the end of the file.
                    fat.setEnd(temp);
                    //Iterates until finding the "old end" and empties all the blocks.
                    while (!fat.isEnd(index)) {
                        blocks[index] = 0;
                        temp = index;
                        index = fat.getNext(index);
                        // Sets FAT entry for the block to -2 (indicating empty).
                        fat.setEmpty(temp);
                    }
                    // Empties the last block aswell.
                    blocks[index] = 0;
                    fat.setEmpty(index);
                    // Modifies the DT entry and shrinks file size by the appropriate amount.
                    dt.modifyEntry(entry[0], -1, entry[2] - (shrinking * BLOCK_SIZE));
                    break;
                default:
                    throw new FileSystemException("File System in invalid state: " + type, FileSystemException.FATAL);
            }
        }
    }

    /* Returns starting index for hole of size s blocks. Returns -1 if no hole with sufficient size is found. */
    private int findHole(int s) throws FileSystemException {
        // Iterates through each block.
        for (int i = 0; i <= blocks.length - s; i++) {
            if (blocks[i] == 0) {
                //If block i is empty, we assume we have a match until proven otherwise.
                boolean match = true;
                for (int j = i + 1; j < i + s; j++)
                    //If all blocks from i to i+s are 0's, we have a match.
                    if (blocks[j] != 0) {
                        //If any of them are not 0, thus full, we don't have a match for i.
                        match = false;
                        //So break out of the inner loop.
                        break;
                    }
                if (match) return i;
            }
        }
        //Return -1 if not found any matches.
        return -1;
    }

    //Returns the first empty block starting from "from". Pretty self-explanatory.
    private int findEmptyBlock(int from) throws FileSystemException {
        for (int i = from; i < NUMBER_BLOCKS; i++) {
            if (blocks[i] == 0)
                return i;
        }
        throw new FileSystemException("No free blocks, this error should never be thrown.", FileSystemException.FATAL);
    }

    //Returns the number of free blocks. Pretty self-explanatory.
    private int getFreeBlockAmount() throws FileSystemException {
        int c = 0;
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] == 0)
                c++;
        }
        return c;
    }

    /* Defragments directory and returns the index of the first free block after defragmentation. */
    private int defragment() throws FileSystemException {
        //Index of the leftmost empty block. Set to -1 (-infinity).
        int to = -1;
        //Set to 0 if block with index 0 is empty
        if (blocks[0] == 0) to = 0;
        //Iterate through every block.
        for (int i = 1; i < blocks.length; i++) {
            if (blocks[i] == 0 && blocks[i - 1] != 0) {
                //If we arrived at an empty block AFTER a full block, it is the leftmost empty block, so set to=i.
                to = i;
            } else if (blocks[i] != 0 && to != -1) {
                //Else if we arrived at a full block, we are guaranteed to be at the beginning of a file. And if
                //there is an empty space to move the file to before it (to != 0), move the file from "i" to "to" and
                //modify the DT entry accordingly.
                Integer[] entry = dt.getEntryFromStartingBlock(i);
                move(i, byteToBlock(entry[2]), to);
                dt.modifyEntry(entry[0], to, -1);
                //Increase i and to by appropriate amounts, so the for loop doesn't waste time looking for a file or
                //empty space at where the file was removed from and where it was moved to.
                i += byteToBlock(entry[2]) - 1;
                to += byteToBlock(entry[2]);
            }
        }
        return to;
    }

    // Used to create contiguous space for a file between files after defragmentation in extend.
    // eg. initially:                  1x2x3x4x5x6x7 (x represents empty block)
    // eg. after defrag:               1234567xxxxxx
    // eg. after createExtensionSpace: 1234xxx567xxx (3 blocks of space created for file 4)
    private void createExtensionSpace(int start, int size) throws FileSystemException {
        //Start by finding the last full block in the directory.
        int last = -1;
        for (int i = blocks.length - 1; i >= start; i--) {
            if (blocks[i] != 0) {
                last = i;
                break;
            }
        }
        if (last == -1)
            throw new FileSystemException("Invalid start index for createExtensionSpace.", FileSystemException.OTHER);
        if (last + size > NUMBER_BLOCKS)
            throw new FileSystemException("Too big size for createExtensionSpace.", FileSystemException.OTHER);
        //Move every block from rear-to-front "size" blocks to the right until reaching start (which is the end of the file
        // we are creating space for). Upon reaching the first block of any file in the way, modify DT entry accordingly.
        for (int i = last; i >= start; i--) {
            move(i, 1, i + size);
            if (dt.containsStartingBlock(i)) {
                Integer[] entry = dt.getEntryFromStartingBlock(i);
                dt.modifyEntry(entry[0], i + size, -1);
            }
        }
    }

    // Moves data in blocks [from..from+size] to blocks [to..to+size] one by one (according to the single buffer rule).
    private void move(int from, int size, int to) {
        for (int i = 0; i < size; i++) {
            buffer = blocks[from + i];
            blocks[to + i] = buffer;
            blocks[from + i] = 0;
        }
    }

    // Generates a random int which is not 0 (0 is reserved for indicating empty blocks).
    private int getRandomBlockData() {
        int n;
        do n = r.nextInt();
        while (n == 0);
        return n;
    }

    //Self-explanatory
    private int generateID() {
        return ids++;
    }

    //Given the size of a file in bytes, computes how many blocks it will take.
    private int byteToBlock(int bytes) {
        return (int) Math.ceil(((double) bytes) / BLOCK_SIZE);
    }
}

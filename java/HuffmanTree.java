import java.io.*;
import java.util.*;

public class HuffmanTree implements Huffman{


    /**
     * Counts the frequency of each character in the tree.
     * @param pathName is the name of the file you want to count
     * Throws exception if file doesn't exist
     * @return a map of <Character, Long> which tells you each character and what the frequency of it is
     */
    @Override
    public Map<Character, Long> countFrequencies(String pathName) throws IOException {
        Map<Character, Long> frequency = new TreeMap<Character, Long>();
        try { //try if file exists
            BufferedReader input = new BufferedReader(new FileReader(pathName));
            int cNum;
            while ((cNum = input.read()) != -1) { //while not EOF
                char c = (char) cNum; //Convert integer value from reading to character
                frequency.put(c, frequency.getOrDefault(c, 0L) + 1); //Either adds the character to the map, or adds 1 to frequency
            }
            input.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }

        return frequency;
    }
    /**
     * Creates the tree of characters and their frequencies
     * @param frequencies is the map from the previous method
     * Uses a priority queue to sort from the least frequencies to most frequencies
     * @return a BinaryTree<CodeTreeElement>
     */
    @Override
    public BinaryTree<CodeTreeElement> makeCodeTree(Map<Character, Long> frequencies) {
        PriorityQueue<BinaryTree<CodeTreeElement>> queue = new PriorityQueue<>((BinaryTree<CodeTreeElement> c1, BinaryTree<CodeTreeElement> c2)->{
            if(c1.getData().getFrequency()>c2.getData().getFrequency()) return 1;
            else if (c1.getData().getFrequency() < c2.getData().getFrequency()) return -1;
            else return 0;
        }); //Uses anonymous function to tell the PriorityQueue how to sort, in this case least to greatest
        if (frequencies.isEmpty()) return null; //boundary check, checks in main
        for (Character K: frequencies.keySet()) {//fills the PriorityQueue with Trees of the Char,Freq
            queue.add(new BinaryTree<CodeTreeElement>(new CodeTreeElement(frequencies.get(K),K)));
        }
        //While there is more than 1 tree, the two smallest elements become the left and right leaves of a new tree,
        // which is then placed into the queue with combined frequencies and a null character, which we will never see.
        while(queue.size()!=1) {
            BinaryTree<CodeTreeElement> left = queue.remove();
            Long f1 = left.getData().getFrequency();
            BinaryTree<CodeTreeElement> right = queue.remove();
            Long f2 = right.getData().getFrequency();
            queue.add(new BinaryTree<CodeTreeElement>(new CodeTreeElement(f1+f2,'\0'),left,right));
        }
        return queue.remove();//Empties the queue, and also returns the finished tree.
    }
    /**
     * Creates the codes for compression/decompression
     * @param codeTree finished tree of characters and their frequencies from the previous method
     * Uses a helper function to traverse the tree and add codes to the map
     * @return a Map<Character, String> of the character, and its codes
     */
    @Override
    public Map<Character, String> computeCodes(BinaryTree<CodeTreeElement> codeTree) {
        Map<Character, String> codes = new HashMap<>();
        if (codeTree.isLeaf()) { //Boundary check if there is one character
            codes.put(codeTree.getData().getChar(), "1");  // Arbitrary single-bit code
            return codes;
        }
        traverse(codeTree,"",codes);
        return codes;
    }
    /**
     * Traverses the tree and adds to the map
     * @param codeTree is the tree from makeCodeTree
     * @param path is the 1s and 0s which make up the character for decompression
     * @param map is the map we need to fill with the characters and paths
     * Uses recursion to search through the tree, and adding to the path while it recurses.
     */
    public void traverse(BinaryTree<CodeTreeElement> codeTree, String path, Map<Character,String> map) {
        if (codeTree.isLeaf()) {//If you hit a character node, add the character and the path you took to get there, and return
            map.put(codeTree.getData().getChar(), path);
            return;
        }
        if (codeTree.hasLeft()) { //If you can go left, go left and add 0 to the path
            traverse(codeTree.getLeft(),path+"0",map);
        }

        if (codeTree.hasRight()) { //If you can go right, go right and add 1 to the path
            traverse(codeTree.getRight(),path+"1",map);
        }
    }
    /**
     * Compresses the file we made with the codeMap
     * @param codeMap is the map from the previous method(s)
     * @param pathName is the name of the file we are compressing
     * @param compressedPathName is the name of the file we compress to.
     * Throws exception if file doesn't exist
     * Compresses the file into a bunch of 0s and 1s
     */
    @Override
    public void compressFile(Map<Character, String> codeMap, String pathName, String compressedPathName) throws IOException {
        int cNum; //Initializing an integer to take the value of the Unicode
        BufferedReader input = null; //Initializing reader and writer as null, in case the file path doesn't exist
        BufferedBitWriter bitOutput = null;
        try { //Try and see if the file path exists
            input = new BufferedReader(new FileReader(pathName));
            bitOutput = new BufferedBitWriter(compressedPathName);

            while ((cNum = input.read()) != -1) { //While not EOF
                char c1 = (char) cNum;
                String code = codeMap.get(c1); //Gets the value of the path from the character key
                for (char c2 : code.toCharArray()) { //Converts the path into a bunch of characters
                    if (c2 == '0') {
                        bitOutput.writeBit(false); //Writes 0 if 0
                    } else {
                        bitOutput.writeBit(true); //Writes 1 if 1
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        } finally { //No matter what, close the reader/writer
            if (input != null) input.close();
            if (bitOutput != null) bitOutput.close();
        }
    }
    /**
     * Decompresses a compressed file
     * @param compressedPathName is the compressed file
     * @param decompressedPathName what you want to call the decompressed file
     * @param codeTree is the tree with the codes and characters
     * Uses 0s and 1s to traverse the tree. If it hits a leaf, type a character and repeat
     */
    @Override
    public void decompressFile(String compressedPathName, String decompressedPathName, BinaryTree<CodeTreeElement> codeTree) throws IOException {
        BufferedBitReader bitInput = null; //Initializing reader/writer
        BufferedWriter output = null;
        try { //try and see if the file path exists
            bitInput = new BufferedBitReader(compressedPathName);
            output = new BufferedWriter(new FileWriter(decompressedPathName));

            while (bitInput.hasNext()) {
                BinaryTree<CodeTreeElement> temp = codeTree; //Creates a copy so we can traverse without changing codeTree
                while (!temp.isLeaf()) {
                    boolean b = bitInput.readBit();
                    if(b) { //If b is true (which is b==1) go right, otherwise go left
                        temp = temp.getRight();
                    }
                    else {
                        temp = temp.getLeft();
                    }
                }
                output.write(temp.getData().getChar()); //When it hits a leaf, write the character of the CodeTreeElement
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        } finally { //Always closes the reader/writer
            if (bitInput != null) bitInput.close();
            if (output != null) output.close();
        }
    }

    public static void main(String[] args) {
        HuffmanTree h = new HuffmanTree();
        Map<Character, Long> test;
        try {
            test = h.countFrequencies("PS3/USConstitution.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BinaryTree<CodeTreeElement> tree = h.makeCodeTree(test);
        if (tree==null) { //Checks to see if the file is empty
            System.out.println("Empty File");
            return;
        }
        Map<Character, String> map = h.computeCodes(tree);
        System.out.println(tree);
        System.out.println(map);
        try {
            h.compressFile(map,"PS3/USConstitution.txt","PS3/consTest.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            h.decompressFile("PS3/consTest.txt","PS3/consDecompTest.txt",tree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

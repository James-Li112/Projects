import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Viterbi{
    /**
     * Counts the number of times each word appears with each tag in the training data
     * @param filename1 the path to the tags training file
     * @param filename2 the path to the sentences training file
     * @return a nested map of tag to word to count
     * @throws IOException if either file cannot be read
     */
    public static Map<String, Map<String,Double>> countWords(String filename1,String filename2) throws IOException {
        BufferedReader reader1 = new BufferedReader(new FileReader(filename1));
        BufferedReader reader2 = new BufferedReader(new FileReader(filename2));
        Map<String,Map<String,Double>> frequencies = new HashMap<>();
        String tagLine;

        while ((tagLine = reader1.readLine())!=null) {
            String wordLine = reader2.readLine();

            String[] words = wordLine.split("\\s+");
            String[] tags = tagLine.split("\\s+");

            for(int i=0;i<words.length;i++) {
                Map<String, Double> wordCount;
                if(!frequencies.containsKey(tags[i])) {
                    wordCount = new HashMap<>();
                    frequencies.put(tags[i],wordCount);
                }
                else {
                    wordCount = frequencies.get(tags[i]);
                }
                wordCount.put(words[i].toLowerCase(), wordCount.getOrDefault(words[i],0.0)+1);
            }
        }
        reader1.close();
        reader2.close();
        return frequencies;
    }
    /**
     * Counts the number of times each tag transitions to another tag in the training data,
     * including transitions from the special start state "#".
     * @param filename the path to the tags training file
     * @return a nested map of tag -> next tag -> count
     * @throws IOException if the file cannot be read
     */
    public static Map<String, Map<String,Double>> pathMap(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Map<String, Map<String,Double>> paths = new HashMap<>();
        String line;



        while ((line = reader.readLine())!=null) {
            String[] tags = line.split("\\s+");
            Map<String, Double> tagCount;
            if (!paths.containsKey("#")) {
                paths.put("#", (tagCount = new HashMap<>()));
            }
            else {
                tagCount = paths.get("#");
            }
            tagCount.put(tags[0], tagCount.getOrDefault(tags[0],0.0)+1);
            for (int i = 0; i< tags.length-1;i++) {
                if(!paths.containsKey(tags[i])) {
                    tagCount = new HashMap<>();
                    paths.put(tags[i],tagCount);
                }
                else {
                    tagCount = paths.get(tags[i]);
                }
                tagCount.put(tags[i+1], tagCount.getOrDefault(tags[i+1],0.0)+1);
            }
        }
        reader.close();
        return paths;
    }
    /**
     * Normalizes raw counts in a nested map to log probabilities.
     * For each outer key, divides each inner value by the total of all inner values,
     * then takes the natural log.
     * @param map a nested map of state -> state/word -> raw count
     * @return the same map with counts replaced by log probabilities
     */
    public static Map<String, Map<String,Double>> computeProb(Map<String, Map<String,Double>> map) {
        for (String key:map.keySet()) {
            Double sum = 0D;
            for(Double num:map.get(key).values()) {
                sum+=num;
            }
            for(String key2:map.get(key).keySet()) {
                map.get(key).put(key2,Math.log(map.get(key).get(key2)/sum));
            }
        }
        return map;
    }
    /**
     * Finds the most likely sequence of part-of-speech tags for a given sentence
     * using the Viterbi algorithm and a trained HMM.
     * @param sentence the input sentence split into individual words
     * @param words the observation log probability map (tag -> word -> log probability)
     * @param tags the transition log probability map (tag -> next tag -> log probability)
     * @return an array of POS tags corresponding to each word in the sentence
     */
    public static String[] viterbi(String[] sentence, Map<String, Map<String,Double>> words, Map<String, Map<String,Double>> tags) {
        ArrayList<Map<String, String>> allPaths = new ArrayList<>();
        Map<String, Double> currentScores = new HashMap<>();
        currentScores.put("#", 0D);
        for (int i = 0; i < sentence.length; i++) {
            Map<String, String> backPointer = new HashMap<>();
            Map<String, Double> nextScores = new HashMap<>();
            for (String tag1 : currentScores.keySet()) {
                for (String tag2 : tags.get(tag1).keySet()) {
                    Double wordScore = -20D;
                    if (words.containsKey(tag2) && words.get(tag2).containsKey(sentence[i].toLowerCase())) {
                        wordScore = words.get(tag2).get(sentence[i].toLowerCase());
                    }
                    Double maxScore = currentScores.get(tag1) + tags.get(tag1).get(tag2)+wordScore;
                    if (!nextScores.containsKey(tag2) || maxScore > nextScores.get(tag2)) {
                        nextScores.put(tag2, maxScore);
                        backPointer.put(tag2, tag1);
                    }
                }
            }
            allPaths.add(backPointer);
            currentScores = nextScores;
        }
        Double bestScore = Double.NEGATIVE_INFINITY;
        String lastWord = "";
        for (String key:currentScores.keySet()) {
            if (currentScores.get(key)>bestScore) {
                bestScore = currentScores.get(key);
                lastWord = key;
            }
        }
        LinkedList<String> sentenceTags = new LinkedList<>();
        sentenceTags.addFirst(lastWord);
        for(int i = allPaths.size()-1; i>=0;i--) {
            lastWord = allPaths.get(i).get(lastWord);
            sentenceTags.addFirst(lastWord);
        }
        if (sentenceTags.get(0).equals("#")) {
            sentenceTags.removeFirst();
        }
        return sentenceTags.toArray(new String[0]);
    }
    /**
     * Console-based tester that reads sentences from user input and prints
     * each word tagged with its predicted part of speech.
     * Trained on the Brown corpus. Press enter on an empty line to quit.
     * @throws IOException if the training files cannot be read
     */
    public static void inputViterbi() throws IOException {
        Scanner scanner = new Scanner(System.in);
        Map<String, Map<String, Double>> wordProb = computeProb(countWords("PS5/texts/brown-train-tags.txt", "PS5/texts/brown-train-sentences.txt"));
        Map<String, Map<String, Double>> tagProb = computeProb(pathMap("PS5/texts/brown-train-tags.txt"));
        System.out.println("Enter a sentence, and press enter to quit.");
        while(true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) break;
            String[] sentence = line.split("\\s+");
            String[] result = viterbi(sentence,wordProb, tagProb);
            for (int i = 0; i < sentence.length; i++) {
                System.out.print(sentence[i] + "/" + result[i] + " ");
            }
            System.out.println();
        }
        scanner.close();
    }
    /**
     * File-based tester that evaluates the performance of the Viterbi tagger
     * on a pair of test files, counting correct and incorrect tag predictions.
     * @param filename1 the path to the test tags file
     * @param filename2 the path to the test sentences file
     * @throws IOException if either file cannot be read
     */
    public static void sentenceTest(String filename1,String filename2) throws IOException {
        Map<String, Map<String, Double>> wordProb = computeProb(countWords("PS5/texts/brown-train-tags.txt", "PS5/texts/brown-train-sentences.txt"));
        Map<String, Map<String, Double>> tagProb = computeProb(pathMap("PS5/texts/brown-train-tags.txt"));
        BufferedReader reader1 = new BufferedReader(new FileReader(filename1));
        BufferedReader reader2 = new BufferedReader(new FileReader(filename2));

        String tagLine;
        int correct = 0;
        int incorrect = 0;

        while ((tagLine = reader1.readLine()) != null) {
            String wordLine = reader2.readLine();

            String[] words = viterbi(wordLine.split("\\s+"),wordProb,tagProb);
            String[] tags = tagLine.split("\\s+");
            for (int i=0;i< words.length;i++) {
                if (words[i].equals(tags[i])) correct++;
                else incorrect++;
            }
        }
        System.out.println("Number of correct tags: "+correct);
        System.out.println("Number of incorrect tags: "+incorrect);
    }

    public static void main(String[] args) {
        try {
            //Hard coded test
            Map<String, Map<String, Double>> transitions = new HashMap<>();
            transitions.put("#", new HashMap<>());
            transitions.get("#").put("NP", Math.log(1.0 / 3));
            transitions.get("#").put("PRO", Math.log(1.0 / 3));
            transitions.get("#").put("MOD", Math.log(1.0 / 3));
            transitions.put("NP", new HashMap<>());
            transitions.get("NP").put("V", Math.log(1.0));
            transitions.put("MOD", new HashMap<>());
            transitions.get("MOD").put("V", Math.log(1.0));
            transitions.put("PRO", new HashMap<>());
            transitions.get("PRO").put("V", Math.log(1.0));
            transitions.put("V", new HashMap<>());
            transitions.get("V").put("DET", Math.log(1.0));
            transitions.put("DET", new HashMap<>());
            transitions.get("DET").put("N", Math.log(1.0));
            Map<String, Map<String, Double>> observations = new HashMap<>();
            observations.put("NP", new HashMap<>());
            observations.get("NP").put("will", Math.log(1.0));
            observations.put("MOD", new HashMap<>());
            observations.get("MOD").put("will", Math.log(1.0));
            observations.put("PRO", new HashMap<>());
            observations.get("PRO").put("i", Math.log(1.0));
            observations.put("V", new HashMap<>());
            observations.get("V").put("eats", Math.log(1.0));
            observations.put("DET", new HashMap<>());
            observations.get("DET").put("the", Math.log(1.0));
            observations.put("N", new HashMap<>());
            observations.get("N").put("fish", Math.log(1.0));
            String[] sentence = {"will", "eats", "the", "fish"};
            String[] expected = {"NP", "V", "DET", "N"};
            String[] result = viterbi(sentence, observations, transitions);
            System.out.print("Sentence: ");
            for (String w : sentence) System.out.print(w + " ");
            System.out.print("\nExpected: ");
            for (String t : expected) System.out.print(t + " ");
            System.out.print("\nGot: ");
            for (String t : result) System.out.print(t + " ");
            System.out.println();
            //Testing other methods
            observations = computeProb(countWords("PS5/texts/brown-train-tags.txt", "PS5/texts/brown-train-sentences.txt"));
            transitions = computeProb(pathMap("PS5/texts/brown-train-tags.txt"));

            sentence = "My favorite band is Oasis , and I love Liam Gallagher . ".split("\\s+");
            result = viterbi(sentence, observations, transitions);
            for (int i = 0; i < sentence.length; i++) {
                System.out.print(sentence[i] + "/" + result[i] + " ");
            }
            System.out.println();
            inputViterbi();
            System.out.println();
            sentenceTest("PS5/texts/brown-test-tags.txt","PS5/texts/brown-test-sentences.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

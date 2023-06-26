import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Sudi Testing class: console, file, and hard-coded tests
 * @authors Annie Tang and Ashley Liang, CS10 W22
 */

public class SudiTesting extends Sudi {

    /**
     * Console-based test method to give the tags from an input file
     * @param in Scanner that scans console input
     * @throws Exception
     */
    public void consoleTest(Scanner in) throws Exception {

        //loop that continues to handle input until "q" is pressed
        while (true) {
            String line;
            System.out.println("Enter sentence:");
            line = in.nextLine();
            if (line.equals("q")) {
                System.out.println("quit");
                break;
            }
            System.out.println(viterbi(line));
        }

    }

    /**
     * File-based test method to evaluate the performance on a pair of test files (comparing the Viterbi generated POS with actual POS)
     * @param testSentences File with the test sentences
     * @param testTags File with the test sentence's actual part of speech
     * @return String with the number of tags rights/wrong and the percentage right/wrong
     * @throws Exception
     */
    public String fileTest(String testSentences, String testTags) throws Exception {
        //reading files for sentences and its tags
        BufferedReader sentenceReader = new BufferedReader(new FileReader(testSentences));
        BufferedReader tagReader = new BufferedReader(new FileReader(testTags));

        String sentenceLine = "";
        String tagLine = "";

        int numCorrect = 0;
        int numIncorrect = 0;

        try {
            //go through training data files and counting transitions and observations
            while ((sentenceLine = sentenceReader.readLine()) != null && (tagLine = tagReader.readLine()) != null) {
                ArrayList<String> runTags = viterbi(sentenceLine);
                //compare generated tags to actual tags
                String[] actualTags = tagLine.split(" ");

                if (runTags.size() != actualTags.length) {
                    return "Number of tags not equal";
                }
                for (int i = 0; i < runTags.size(); i++) {
                    if (runTags.get(i).equals(actualTags[i])) {
                        numCorrect++;
                    }
                    else {
                        numIncorrect++;
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println("Error Reading Files");
        }

        //close files
        sentenceReader.close();
        tagReader.close();

        //calculating percentage correct/incorrect
        double percentCorrect = (double) numCorrect / (numCorrect + numIncorrect) * 100;
        double percentIncorrect = (double) numIncorrect / (numCorrect + numIncorrect) * 100;

        return numCorrect + " tags right " + numIncorrect + " tags wrong\n" + percentCorrect + "% correct, " + percentIncorrect + "% incorrect";

    }

    /**
     * Method to test Viterbi with a hard-coded graph instead of POSTraining
     */
    public void test() {
        //"chase watch dog chase watch" -> "# -> NP -> V -> N -> V -> N"
        String sentence = "chase watch dog chase watch";

        transitions.put("#", new HashMap<String, Double>());
        transitions.put("NP", new HashMap<String, Double>());
        transitions.put("V", new HashMap<String, Double>());
        transitions.put("N", new HashMap<String, Double>());

        transitions.get("#").put("NP", 0.0);
        transitions.get("NP").put("V", 0.0);
        transitions.get("V").put("N", 0.0);
        transitions.get("N").put("V", 0.0);

        observations.put("NP", new HashMap<String, Double>());
        observations.put("V", new HashMap<String, Double>());
        observations.put("N", new HashMap<String, Double>());

        observations.get("NP").put("chase", 0.0);
        observations.get("V").put("watch", -0.693147);
        observations.get("V").put("chase", -0.693147);
        observations.get("N").put("watch", -0.693147);
        observations.get("N").put("dog", -0.693147);

        System.out.println("sentence: " + sentence);
        System.out.println("transitions: " + transitions);
        System.out.println("observations: " + observations);

        System.out.println("pos path: " + viterbi(sentence));

    }

    public static void main(String[] args) throws Exception {
        SudiTesting sudiTest = new SudiTesting();

        //hard-coded hashmaps test
        sudiTest.test();

        //POSTraining with Brown training files
        sudiTest.POSTagging("./texts/brown-train-sentences.txt", "texts/brown-train-tags.txt");

//        //file test with example
//        String exTest = sudiTest.fileTest("texts/example-sentences.txt", "texts/example-tags.txt");
//        System.out.println("Example Test Results:");
//        System.out.println(exTest);

        //file test with simple test
        String simpleTest = sudiTest.fileTest("./texts/simple-test-sentences.txt", "texts/simple-test-tags.txt");
        System.out.println("Simple Test Results:");
        System.out.println(simpleTest);

        //file test with brown test
        String brownTest = sudiTest.fileTest("./texts/brown-test-sentences.txt", "texts/brown-test-tags.txt");
        System.out.println("Brown Test Results:");
        System.out.println(brownTest);

        //console test:
        //declare Scanner to read from console, loop to handle input until enter "q"
        Scanner in = new Scanner(System.in);
        sudiTest.consoleTest(in);

    }
}
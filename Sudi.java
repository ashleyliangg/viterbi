import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Sudi Class for PS5 Digital Assistant Sudi
 * @authors Ashley Liang and Annie Tang, CS10 W22
 */

public class Sudi {
    //HashMap<currentPOS, HashMap<word, probability>>
    HashMap<String, HashMap<String, Double>> observations;
    //HashMap<currentPOS, HashMap<nextPOS, probability>>
    HashMap<String, HashMap<String, Double>> transitions;

    /**
     * Constructor that initializes both transition and observation maps
     */
    public Sudi() {
        transitions = new HashMap<String, HashMap<String, Double>>();
        observations = new HashMap<String, HashMap<String, Double>>();
    }

    /**
     * Trains Sudi by creating 2 maps that hold the probability of each word and transition, HMM approach
     * @param sentenceFile Training file with the sentences
     * @param tagFile Training files with the corresponding POS for each word in the sentences
     * @throws Exception
     */
    public void POSTagging(String sentenceFile, String tagFile) throws Exception{
        //BufferedReaders for sentences and its corresponding tags
        BufferedReader sentenceReader = new BufferedReader(new FileReader(sentenceFile));
        BufferedReader tagReader = new BufferedReader(new FileReader(tagFile));

        String sentenceLine = "";
        String tagLine = "";

        try {
            //go through training data files and counting transitions and observations
            while ((sentenceLine = sentenceReader.readLine()) != null && (tagLine = tagReader.readLine()) != null) {
                String[] words = sentenceLine.toLowerCase().split(" ");
                String[] tags = tagLine.split(" ");

                //looping through each tag and word if the length of the array of POS tags is equal to length of the array of words, add to observation and transition map
                if (words.length == tags.length) {

                    //add POS, word, count of that word being that POS to observations map
                    for (int i = 0; i < tags.length; i++) {
                        //if POS exists in observations
                        if (observations.containsKey(tags[i])) {
                            //if POS and word already exist, increment count
                            if (observations.get(tags[i]).containsKey(words[i])) {
                                observations.get(tags[i]).put(words[i], observations.get(tags[i]).get(words[i]) + 1);
                            }
                            //if contains the POS but doesn't contain the word yet
                            else {
                                observations.get(tags[i]).put(words[i], 1.0);
                            }
                        }
                        //if POS doesn't exist in observations, add POS and HashMap (word and 1 for first occurrence)
                        else {
                            HashMap<String, Double> wordOccurrences = new HashMap<String, Double>();
                            wordOccurrences.put(words[i], 1.0);
                            observations.put(tags[i], wordOccurrences);
                        }


                        //add currentPOS, nextPOS, and count of that transition to transitions map

                        //if first pos of sentence, need to add # before it
                        if (i == 0) {
                            //if doesn't contain "#"
                            if (!transitions.containsKey("#")) {
                                HashMap<String, Double> initialTransition = new HashMap<String, Double>();
                                initialTransition.put(tags[i], 1.0);
                                transitions.put("#", initialTransition);
                            }
                            //"#" in transitions
                            else {
                                //if tag (nextPOS) not in "#"
                                if (!transitions.get("#").containsKey(tags[i])) {
                                    transitions.get("#").put(tags[i], 1.0);
                                }
                                //tag (nextPOS) in transition, increase value by 1
                                else {
                                    transitions.get("#").put(tags[i], transitions.get("#").get(tags[i]) + 1);
                                }
                            }
                        }

                        //transitions from 0 to tags.length - 1 for comparison
                        if (i < tags.length - 1) {
                            //if currPOS tag doesn't exist in transitions
                            if (!transitions.containsKey(tags[i])) {
                                //add currPOS tag and new HashMap with nextPOS and 1 because first occurrence
                                HashMap<String, Double> transitionOccurrences = new HashMap<String, Double>();
                                transitionOccurrences.put(tags[i + 1], 1.0);
                                transitions.put(tags[i], transitionOccurrences);
                            }
                            //currPOS tag does exist in transitions
                            else {
                                //if nextPOS doesn't exist as a key in the map
                                if (!transitions.get(tags[i]).containsKey(tags[i + 1])) {
                                    //add next to the map
                                    transitions.get(tags[i]).put(tags[i + 1], 1.0);
                                }
                                //nextPOS exists, increase value of next tag by 1
                                else {
                                    transitions.get(tags[i]).put(tags[i + 1], transitions.get(tags[i]).get(tags[i + 1]) + 1);
                                }
                            }
                        }
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

        //go over observations to normalize counts to log probabilities
        for (String pos : observations.keySet()) {
            double obsTotal = 0;
            //calculate total num of word occurrences for each pos
            for (String word : observations.get(pos).keySet()) {
                obsTotal += observations.get(pos).get(word);
            }

            //updating to log probabilities
            for (String word: observations.get(pos).keySet()) {
                //double check if total isn't 0
                if (obsTotal != 0) {
                    observations.get(pos).put(word, Math.log(observations.get(pos).get(word) / obsTotal));
                }
            }
        }

        //go over transitions to normalize counts to log probabilities
        for (String pos : transitions.keySet()) {
            double tranTotal = 0;

            //calculate total num of transition for each pos
            for (String prevPos : transitions.get(pos).keySet()) {
                tranTotal += transitions.get(pos).get(prevPos);
            }

            //updating to log probabilities
            for (String prevPos : transitions.get(pos).keySet()) {
                //double check if total isn't 0
                if (tranTotal != 0) {
                    transitions.get(pos).put(prevPos, Math.log(transitions.get(pos).get(prevPos) / tranTotal));
                }
            }
        }
    }

    /**
     * Method to perform Viterbi decoding to find the best sequence of tags for a line of input
     * @param input String input of words
     * @return Arraylist of the best sequence of tags for that input
     */
    public ArrayList<String> viterbi(String input) {
        //backtrace: list, corresponding to the number of words in the string input, of hashmaps with nextPOS (next states) and currPOS (current states)
        ArrayList<HashMap<String, String>> backTrace = new ArrayList<HashMap<String, String>>();

        //keeping track of all possible current states/scores
        HashMap<String, Double> currScores = new HashMap<String, Double>();
        //keeping track of all possible next states/scores
        HashMap<String, Double> nextScores;

        //set the initial tag of the sentence ("#") and score to 0
        currScores.put("#", 0.0);

        //make list of every word in sentence
        String[] words = input.toLowerCase().split(" ");

        for (int i = 0; i < words.length; i++) {

            //create new nextScores map
            nextScores = new HashMap<String, Double>();

            //create back trace map with the next state and current state
            HashMap<String, String> btMap = new HashMap<String, String>();

            //looping over each possible currState
            for (String currState : currScores.keySet()) {
                //looping over each possible next state if the transitions map contains that current state
                if (transitions.containsKey(currState)) {
                    for (String nextPOS : transitions.get(currState).keySet()) {
                        //score of next state (nextScore) = current + transition + observation
                        double currScore = currScores.get(currState);
                        double transitionScore = transitions.get(currState).get(nextPOS);
                        double observationScore;

                        //if word found in the nextPOS
                        if (observations.get(nextPOS).containsKey(words[i])) {
                            observationScore = observations.get(nextPOS).get(words[i]);
                        }
                        else {
                            observationScore = -100.0;
                        }

                        double nextScore = currScore + transitionScore + observationScore;

                        //updating state in nextScores & btMap
                        if (!nextScores.containsKey(nextPOS) || nextScore > nextScores.get(nextPOS)) {
                            nextScores.put(nextPOS, nextScore);
                            btMap.put(nextPOS, currState);
                            backTrace.add(i, btMap);
                        }
                    }
                }
            }
            //update the currScore map
            currScores = nextScores;
        }


        //Creating the backtrack to find the best sequence
        //first find the best score in the last currScore (last word of the input string)
        ArrayList<String> posPath = new ArrayList<String>();
        String mostProbable = "";
        double max = Double.NEGATIVE_INFINITY;
        for (String currPos : currScores.keySet()) {
            if (currScores.get(currPos) > max) {
                mostProbable = currPos;
                max = currScores.get(mostProbable);
            }
        }

        //backtracking along backtrace, grabbing the max score value and putting in the most probable pos path
        for (int i = words.length - 1; i >= 0; i--) {
            posPath.add(0, mostProbable);
            mostProbable = backTrace.get(i).get(mostProbable);
        }
        return posPath;

    }
}

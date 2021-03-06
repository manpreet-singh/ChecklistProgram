/*
   Copyright 2018 Manpreet Singh

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package net.manpreet_singh.checklist;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Manpreet Singh (2854787)
 *         FedEx Smartport SEWA/5983
 */
public class Checklist {

    // Containers, Lists, and Hash Sets needed for use later on.
    private ArrayList<String> mCheckList = new ArrayList<>();
    private Collection<String> mSimilar;
    private ArrayList<String[]> outputBuffer = new ArrayList<>();
    private ArrayList<String> mFinalOutput = new ArrayList<>();
    private Collection<String> mDifferent = new HashSet<>();
    private String mDestinationTag;

    private DateFormat mDateFormat = new SimpleDateFormat("dd-MMM");
    private DateFormat mGenerationFormat = new SimpleDateFormat("HH:mm:ss");

    private String mOutputFileName;

    /**
     * This Class Takes in the name of the file (The Checklist) for which it will be filtering the incoming data from
     * It also takes care of writing the final files to the disk.
     *
     * @param fileName The Name of the Master Checklist which to filter from.
     * @param outputFile The Name of the Output file to output to.
     * @param destinationTag The Destination Tag to print into the Output file.
     */
    public Checklist(String fileName, String outputFile, String destinationTag) throws Exception {
        // Initialize Variables
        this.mOutputFileName = outputFile;
        this.mDestinationTag = destinationTag;

        // Parse through the filename and generate the filters.
        parseChecklist(fileName);

        // Initialize the Similar hash set with all the elements from the Checklist.
        //mSimilar = new HashSet<>(mCheckList);
        mSimilar = new HashSet<>();
    }

    /**
     * Parse the Master Checklist this object will be using as the filter.
     * @param fileName Path to the file to be used as the filter.
     */
    private void parseChecklist(String fileName) throws Exception {
        // Go through the file line by line and copy the current line into the variable line
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        // Split up the line by commas and put into array
        for (String line; (line = br.readLine()) != null;) {
            String[] lineBuffer = line.split(",");

            // Only add zipcode to the checklist if it has anything in the zip code cell and it's a number
            if (lineBuffer.length > 1 && lineBuffer[1] != null && lineBuffer[1].matches(".*\\d+.*")) {
                mCheckList.add(Utility.removeZipcodePrefix(lineBuffer[1]));
                outputBuffer.add(lineBuffer);
            }
        }
    }

    /**
     * Filter the data passed into this Method based on the filters set up prior to calling this method.
     * @param list The Set of date to filter.
     */
    public void generateDifference(Collection<String> list) throws FileNotFoundException{
        mSimilar.addAll(mCheckList);

        // Add all elements from the Checklist to the Difference buffer.
        mDifferent.addAll(mCheckList);

        // If the size of the list passed to this function has anything in it,
        // also add it to the Difference buffer.
        if(list != null && !list.isEmpty()) {
            mDifferent.addAll(list);

            // Retain only the similar items between the Checklist and the list
            // that is passed into this function.
            mSimilar.retainAll(list);

            // Remove all duplicates.
            mDifferent.removeAll(mSimilar);
        }

        generateFinalCutlist();
        outputChecklistToFile();
    }

    /**
     * Generate the final List after narrowing down which elements to add to the final list.
     * This Method should only ever be called from within in this class.
     */
    private void generateFinalCutlist() {
        // Go through the Hash set generated earlier which includes all the zipcodes that need to be activated.
        for (String s : mDifferent) {
            // Go through the output buffer, and find all the information associated with a certain zipcode,
            // I.E. The Location name, the Pallet Location, and such.
            for (String[] anOutputBuffer : outputBuffer) {
                String temp = anOutputBuffer[1];
                if (Objects.equals(s, Utility.removeZipcodePrefix(temp))) {
                    mFinalOutput.add(anOutputBuffer[0] + "," + anOutputBuffer[1] + "," + anOutputBuffer[2] + "\n");
                    //System.out.printf("added to remove : %d\n", j);
                }
            }
        }
        mFinalOutput.sort(this::compare);
    }

    private int compare(String a, String b) {
        return a.split(",")[2].compareTo(b.split(",")[2]);
    }

    /**
     * Output the final list buffer into an Excel file
     * @throws FileNotFoundException Unable to open the Output File to write data to.
     */
    private void outputChecklistToFile() throws FileNotFoundException {
        String header = "DDU,Zipcode,Location," + mDateFormat.format(new Date()) + "," + mDestinationTag + "\n";
        OutputChecklistKt.outputList(mFinalOutput, this.mOutputFileName + ".xls", header);
        prepForNextRun();
    }

    /**
     * Clear all data pertaining to this current run of the Checklist Program
     */
    private void prepForNextRun() {
        this.mFinalOutput.clear();
        this.mSimilar.clear();
        this.mDifferent.clear();
    }
}

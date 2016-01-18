/*
    Copyright 2015 Barcelona Supercomputing Center
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

package es.bsc.autonomic.powermodeller.tools.featureScaling;

import au.com.bytecode.opencsv.CSVReader;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.exceptions.DataSetException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class DataStandardizationTest {

    private String csv1path = getClass().getResource("/scale1test.csv").getPath();
    private String csv2path = getClass().getResource("/scale2test.csv").getPath();

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    @Test
    public void scaleDataset() {
        CSVReader reader = null;
        DataStandardization data = new DataStandardization();

        data.generateStandardization(csv1path, "A");
        try{
            reader = new CSVReader(new FileReader(data.getFileOutputPath()), CoreConfiguration.CSV_DELIMITER);

            //Read all rows at once
            List<String[]> allRows = reader.readAll();


            assertTrue(Arrays.toString(allRows.get(0)).equals("[A, B, C]"));
            assertTrue(Arrays.toString(allRows.get(1)).equals("[2, 299, 301]"));
            assertTrue(Arrays.toString(allRows.get(2)).equals("[1, 300, 300]"));
            assertTrue(Arrays.toString(allRows.get(3)).equals("[3, 301, 299]"));

        } catch (IOException e) {
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new DataSetException("Error while closing CSV file.");
            }
        }
    }


    @Test
    public void applyScaling(){
        CSVReader reader = null;
        DataStandardization data_t = new DataStandardization();
        data_t.generateStandardization(csv1path, "A");

        DataStandardization data = new DataStandardization(data_t.getMu(), data_t.getSigma());
        data.applyStandardization(csv2path, "A");

        try{
            reader = new CSVReader(new FileReader(data.getFileOutputPathValidated()), CoreConfiguration.CSV_DELIMITER);

            //Read all rows at once
            List<String[]> allRows = reader.readAll();

            assertTrue(Arrays.toString(allRows.get(0)).equals("[A, B, C]"));
            assertTrue(Arrays.toString(allRows.get(1)).equals("[4, 299, 301]"));
            assertTrue(Arrays.toString(allRows.get(2)).equals("[2, 300, 300]"));
            assertTrue(Arrays.toString(allRows.get(3)).equals("[6, 301, 299]"));

        } catch (IOException e) {
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new DataSetException("Error while closing CSV file.");
            }
        }
    }
}

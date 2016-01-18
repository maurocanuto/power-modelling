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

package es.bsc.autonomic.powermodeller;

import static org.junit.Assert.*;

import es.bsc.autonomic.powermodeller.DataSet;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.exceptions.DataSetException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DataSetTest {

    private static DataSet csvA, csvB, csvC, csvDecomposedPower;

    @Before
    public void testContextInitialization() {
        csvA = new DataSet(getClass().getResource("/csvA.csv").getPath());
        csvA.setIndependent("power");
        csvB = new DataSet(getClass().getResource("/csvB.csv").getPath());
        csvB.setIndependent("power");
        csvC = new DataSet(getClass().getResource("/csvC.csv").getPath());
        csvC.setIndependent("power");
        csvDecomposedPower = new DataSet(getClass().getResource("/decomposedPower.csv").getPath());
        csvDecomposedPower.setIndependent(CoreConfiguration.PACTUAL_LABEL);
    }

    /**
     * Source: http://www.rgagnon.com/javadetails/java-0483.html
     * @param path Directory to be deleted. It also deletes non-empty directories.
     * @return True if deletion is successful, false otherwise.
     */
    private static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }

    @AfterClass
    public static void testContextCleanUp() {
        /*deleteDirectory(new File(CoreConfiguration.TEMPDIR));*/
    }

    @Test
    public void testMetaDataParsing() throws Exception {
        assertTrue(csvA.getHeaderString().equals("power,cpu,cache"));
        assertEquals(csvA.getSize(), 2);
    }

    @Test
    public void testToString() throws Exception {
        assertTrue(csvB.toString().equals("power,cpu,cache\n" +
                                            "50.6,4.3,0.7\n" +
                                            "70.0,70.0,0.9\n" +
                                            "33.0,30.0,2.9\n"));
    }

    @Test
    public void testCSVJoinCompatibleWorks() throws Exception {
        DataSet csvJoined = DataSet.join(Arrays.asList(csvA, csvB));
        assertTrue(csvJoined.toString().equals("power,cpu,cache\n" +
                                                "100.6,2.3,0.7\n" +
                                                "140.0,100.0,8.9\n" +
                                                "50.6,4.3,0.7\n" +
                                                "70.0,70.0,0.9\n" +
                                                "33.0,30.0,2.9\n"));
    }

    @Test(expected = DataSetException.class)
    public void testCSVJoinIncompatibleFails() throws Exception {
        DataSet.join(Arrays.asList(csvA, csvC));
    }

    @Test
    public void testCSVSwapColumnWorks() throws Exception {
        assertTrue((csvA.replaceCol("power", "newpower", Arrays.asList(2.0, 3.0))).toString().equals("newpower,cpu,cache\n" +
                "2.0,2.3,0.7\n" +
                "3.0,100.0,8.9\n"));
    }

    @Test(expected = DataSetException.class)
    public void testCSVSwapColumnFails() throws Exception {
        csvA.replaceCol("power", "newpower", Arrays.asList(2.0, 3.0, 4.0));
    }

    @Test
    public void testCSVGetColWorks() throws Exception {
        assertTrue(csvA.getCol("cpu").toString().equals("[2.3, 100.0]"));
    }

    @Test
    public void testCSVDataSetFromColumns() throws Exception {
        HashMap<String, List<Double>> columns = new HashMap<String, List<Double>>();
        columns.put("P", Arrays.asList(2.0, 3.0, 4.0));
        columns.put("Pcpu", Arrays.asList(45.0, 50.0, 72.0));
        columns.put("Pmemory", Arrays.asList(22.0, 13.0, 33.0));

        DataSet csvNew = new DataSet(columns);
        assertTrue(csvNew.toString().equals("Pmemory,P,Pcpu\n" +
                "22.0,2.0,45.0\n" +
                "13.0,3.0,50.0\n" +
                "33.0,4.0,72.0\n"));
    }

    @Test(expected = DataSetException.class)
    public void testCSVDataSetFromIncorrectColumnsFails() throws Exception {
        HashMap<String, List<Double>> columns = new HashMap<String, List<Double>>();
        columns.put("power", Arrays.asList(2.0, 3.0, 4.0));
        columns.put("WRONG_SIZE", Arrays.asList(45.0, 50.0, 72.0, 12.5));
        columns.put("memory", Arrays.asList(22.0, 13.0, 33.0));

        DataSet csvNew = new DataSet(columns);
    }

    @Test
    public void testCSVSubstractColumn() throws Exception {
        List<Double> Pcpu = Arrays.asList(60.0, 60.0);
        double Pidle = 30.0;

        DataSet csvNew = csvA.substractFromColumn("power", "power-Pcpu", Pcpu);
        assertTrue(csvNew.toString().equals("power-Pcpu,cpu,cache\n" +
                                            "40.599999999999994,2.3,0.7\n" +
                                            "80.0,100.0,8.9\n"));

        csvNew = csvA.substractFromColumn("power", "power-Pidle", Pidle);
        assertTrue(csvNew.toString().equals("power-Pidle,cpu,cache\n" +
                                            "70.6,2.3,0.7\n" +
                                            "110.0,100.0,8.9\n"));
    }

    @Test
    public void testCSVOrdering() throws Exception {
        assertTrue(csvA.order().toString().equals("power,cache,cpu\n" +
                                            "100.6,0.7,2.3\n" +
                                            "140.0,8.9,100.0\n"));
    }

    @Test
    public void testCSVAddColumns() throws Exception {
        List<String> addingCols = Arrays.asList("cpu", "cache");

        DataSet csvNew = csvC.addColumns("cpu+cache", addingCols);

        assertTrue(csvNew.toString().equals("power,memory,cpu+cache\n" +
                                            "50.6,240.0,5.0\n" +
                                            "70.0,1024.0,70.9\n"));
        assertTrue(csvNew.getIndependent().equals("power"));

        addingCols = Arrays.asList("cpu", "cache", "memory");
        csvNew = csvC.addColumns("cpu+cache+memory", addingCols);

        assertTrue(csvNew.toString().equals("power,cpu+cache+memory\n" +
                                            "50.6,245.0\n" +
                                            "70.0,1094.9\n"));
        assertTrue(csvNew.getIndependent().equals("power"));

        addingCols = Arrays.asList("power", "cache");
        csvNew = csvC.addColumns("power+cache", addingCols);

        assertTrue(csvNew.toString().equals("cpu,memory,power+cache\n" +
                                            "4.3,240.0,51.300000000000004\n" +
                                            "70.0,1024.0,70.9\n"));
        assertTrue(csvNew.getIndependent().equals("power+cache"));
    }

    @Test
    public void testCSVSubSampleRandomDataSet() throws Exception {

        DataSet csvNew = csvA.getSubSampleRandom(1);

        assertTrue(csvNew.toString().equals("power,cpu,cache\n" +
                                            "140.0,100.0,8.9\n")
                || csvNew.toString().equals("power,cpu,cache\n" +
                                            "100.6,2.3,0.7\n"));


    }

    @Test
    public void testCSVSubSampleRangeDataSet() throws Exception {

        DataSet csvNew = csvDecomposedPower.getSubSampleRange(4, 10);

        assertTrue(csvNew.toString().equals("Pactual,Pcpu,Pdisk,Pidle,Pmem,Pnet\n" +
                                            "81.0,83.00180569642602,3.768711699774623,5.0,-4.2695324085272635,-3.0745358441732904\n" +
                                            "88.0,83.00184179927034,3.8677208997306103,5.0,-3.2734168941598174,-0.3404591066093037\n" +
                                            "98.5,83.00176302942818,3.833370769133635,5.0,0.07151260856060215,3.3585858912713826\n" +
                                            "99.5,83.00180569642602,3.8252883854637583,5.0,1.9061694905004538,0.6704600232462878\n" +
                                            "97.5,83.00189431249845,3.833370769133635,5.0,1.0307303142228674,2.255765022338011\n" +
                                            "96.5,83.00177943981195,3.811144214041475,5.0,1.2248578453585068,2.255765022338011\n"));
    }
}
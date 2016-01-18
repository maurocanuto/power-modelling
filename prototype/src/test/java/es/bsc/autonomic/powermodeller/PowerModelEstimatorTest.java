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

import org.junit.AfterClass;
import org.junit.Before;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PowerModelEstimatorTest {

    private static List<String> trainingCpu, trainingMemory, trainingDisk, trainingNetwork;
    private static String validationFiles;
    private static String varCPU, varMEM, varDISK, varNET;
    @Before
    public void testContextInitialization() {

        trainingCpu = new ArrayList<String>();
        trainingMemory = new ArrayList<String>();
        trainingDisk = new ArrayList<String>();
        trainingNetwork = new ArrayList<String>();



        trainingCpu.add(getClass().getResource("/trainingCPU.csv").getPath());
        trainingMemory.add(getClass().getResource("/trainingMemory.csv").getPath());
        trainingDisk.add(getClass().getResource("/trainingDisk.csv").getPath());
        trainingNetwork.add(getClass().getResource("/trainingNetwork.csv").getPath());
        validationFiles = (getClass().getResource("/validationFile.csv").getPath());

        varCPU = getClass().getResource("/variablesCPU.conf").getPath();
        varMEM = getClass().getResource("/variablesMEM.conf").getPath();
        varDISK = getClass().getResource("/variablesDISK.conf").getPath();
        varNET = getClass().getResource("/variablesNET.conf").getPath();
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
/*
    @Test
    public void testEstimateValidationFile() throws Exception {
        CoreConfiguration.CLASSIFIER = "linearregression";
        CoreConfiguration.INDEPENDENT = "powerWatts";
        CoreConfiguration.POWER_IDLE = 5;
        PowerModelGenerator pmg = new PowerModelGenerator(trainingCpu,trainingMemory,trainingDisk,trainingNetwork, varCPU, varMEM, varDISK, varNET);
        PowerModelEstimator estimator = new PowerModelEstimator(validationFiles, pmg);

        DataSet ret = estimator.estimateAllResources();

        //System.out.println("RESULTS FILE: " + d.getFilePath());
        System.out.println(ret);
        assertTrue(ret.toString().equals("Pactual,Pcpu,Pdisk,Pidle,Pmem,Pnet\n" +
                "81.0,83.00180569642602,3.8071030222065363,5.0,-4.305675388414166,23.485066749305435\n" +
                "83.0,83.00175646527467,3.8394325568860426,5.0,-4.302140266532418,-3.809749756795539\n" +
                "82.5,83.00175646527467,3.8273089813812278,5.0,-4.305652565112899,-4.7287671475733495\n" +
                "82.0,83.00177943981195,3.7444645487649932,5.0,-4.264371437657054,-4.7287671475733495\n" +
                "81.0,83.00180569642602,3.768711699774623,5.0,-4.2695324085272635,-3.0745358441732904\n" +
                "88.0,83.00184179927034,3.8677208997306103,5.0,-3.2734168941598174,-0.3404591066093037\n" +
                "98.5,83.00176302942818,3.833370769133635,5.0,0.07151260856060215,3.3585858912713826\n" +
                "99.5,83.00180569642602,3.8252883854637583,5.0,1.9061694905004538,0.6704600232462878\n" +
                "97.5,83.00189431249845,3.833370769133635,5.0,1.0307303142228674,2.255765022338011\n" +
                "96.5,83.00177943981195,3.811144214041475,5.0,1.2248578453585068,2.255765022338011\n"));
    }
*/

}

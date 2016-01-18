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

import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PowerModelGeneratorTest {

    private static List<String> trainingCpu, trainingMemory, trainingDisk, trainingNetwork;
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

    @Test
    public void testGenerateCPUModel() throws Exception {
        CoreConfiguration.CLASSIFIER = "linearregression";
        PowerModelGenerator pmg = new PowerModelGenerator(trainingCpu,null,null,null,null, varCPU, null, null, null, null);
        pmg.generateModelCPU();
        assertTrue((pmg.getCpuModel().getClassifier().toString()).equals("\n" +
                "Linear Regression Model\n" +
                "\n" +
                "powerWatts =\n" +
                "\n" +
                "      3.2821 * cpu_user3 +\n" +
                "     83.0018"));

    }


    @Test
    public void testGenerateMemoryModel() throws Exception {
        CoreConfiguration.CLASSIFIER = "linearregression";
        PowerModelGenerator pmg = new PowerModelGenerator(trainingCpu,trainingMemory,null,null, null, varCPU, varMEM, null, null, null);
        pmg.generateModelMemory();
        assertTrue((pmg.getMemoryModel().getClassifier().toString()).equals("\n" +
                "Linear Regression Model\n" +
                "\n" +
                "P-Pcpu =\n" +
                "\n" +
                "      0      * L1_icache_loads_misses +\n" +
                "     -4.8381"));

    }


    @Test
    public void testGenerateDiskModel() throws Exception {
        CoreConfiguration.CLASSIFIER = "linearregression";
        PowerModelGenerator pmg = new PowerModelGenerator(trainingCpu,trainingMemory,trainingDisk,null, null, varCPU, varMEM, varDISK, null, null);
        pmg.generateModelDisk();
        assertTrue((pmg.getDiskModel().getClassifier().toString()).equals("\n" +
                "Linear Regression Model\n" +
                "\n" +
                "P-Pcpu-Pmem =\n" +
                "\n" +
                "     -0.004  * contexts +\n" +
                "      4.5204"));
    }


    @Test
    public void testGenerateNetworkModel() throws Exception {
        CoreConfiguration.CLASSIFIER = "linearregression";
        PowerModelGenerator pmg = new PowerModelGenerator(trainingCpu,trainingMemory,trainingDisk,trainingNetwork, null, varCPU, varMEM, varDISK, varNET, null);
        pmg.generateModelNetwork();
        assertTrue((pmg.getNetworkModel().getClassifier().toString()).equals("\n" +
                "Linear Regression Model\n" +
                "\n" +
                "P-Pcpu-Pmem-Pdisk =\n" +
                "\n" +
                "      0.0919 * SIMD_FP_256_PACKED_SINGLE +\n" +
                "     -4.7288"));
    }


}
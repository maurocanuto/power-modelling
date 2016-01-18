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

import es.bsc.autonomic.powermodeller.tools.VariableParser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class ResourceModelTest {
    private static DataSet csvA, csvB, csvC;
    private static String variables;

    @Before
    public void testContextInitialization() {
        csvA = new DataSet(getClass().getResource("/csvA.csv").getPath());
        csvA.setIndependent("power");
        csvB = new DataSet(getClass().getResource("/csvB.csv").getPath());
        csvB.setIndependent("power");
        csvC = new DataSet(getClass().getResource("/csvC.csv").getPath());
        csvC.setIndependent("power");
        variables = getClass().getResource("/variables5.conf").getPath();
    }

    /**
     * Source: http://www.rgagnon.com/javadetails/java-0483.html
     * @param path Directory to be deleted. It also deletes non-empty directories.
     * @return True if deletion is successful, false otherwise.
     */
    private static synchronized boolean deleteDirectory(File path) {
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
    public void estimateIndependet(){

        List<String> metrics = Arrays.asList("cpu", "cache");

        VariableParser varParser = new VariableParser(variables,csvA.getHeader());

        ResourceModel res_mod = new ResourceModel(csvA, varParser);
        res_mod.estimateIndependent(csvB, varParser);

    }
}

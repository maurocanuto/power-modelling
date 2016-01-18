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

package es.bsc.autonomic.powermodeller.tools;

import es.bsc.autonomic.powermodeller.DataSet;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.tools.classifiers.LinearRegressionClassifier;
import es.bsc.autonomic.powermodeller.tools.classifiers.RepTreeClassifier;
import es.bsc.autonomic.powermodeller.tools.classifiers.WekaWrapper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.REPTree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class WekaWrapperTest {
    private static DataSet training_ds;
    private static DataSet validation_ds;

    @Before
    public void testContextInitialization() {
        training_ds = new DataSet(getClass().getResource("/trainingLR.csv").getPath());
        validation_ds = new DataSet(getClass().getResource("/validationLR.csv").getPath());
    }

    /**
     * Source: http://www.rgagnon.com/javadetails/java-0483.html
     * @param path Directory to be deleted. It also deletes non-empty directories.
     * @return True if deletion is successful, false otherwise.
     */
    private static synchronized  boolean deleteDirectory(File path) {
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
    public void processDataSet(){

        WekaWrapper weka = new LinearRegressionClassifier();

        HashMap<String,String> expressions = new HashMap<String, String>();
        expressions.put("new_var1", "a2*10");
        expressions.put("new_var2", "(a3+a2)^2");


        List<String> colums = new ArrayList<String>();
        colums.add("cpu");
        colums.add("new_var1");
        colums.add("new_var2");

        VariableParser parser = new VariableParser(training_ds.getFilePath(), training_ds.getHeader());
        parser.setColumns(colums);
        parser.setNewMetrics(expressions);

        DataSet out = weka.processDataSet(training_ds, parser);

        assertTrue(out.toString().equals("powerWatts,cpu,new_var1,new_var2\n" +
                "23,2,20,25\n" +
                "46,4,40,100\n" +
                "69,6,60,225\n" +
                "154,10,100,900\n" +
                "132,23,230,676\n" +
                "187,11,110,1089\n"));
    }

    @Test
    public void applyLinearRegression() throws Exception {

        LinearRegressionClassifier weka = new LinearRegressionClassifier();
        Classifier model = weka.buildClassifier(training_ds);

        double[] coefficients = ((LinearRegression) model).coefficients();
        double[] pred = {0.0, 4.9999999881448, 2.9999999801862316, 2.0000000097219908, 2.0364953456919466E-7};

        for (int i = 0 ; i<4; i++)
            assertTrue(coefficients[i] == pred[i]);

    }

    @Test
    public void validateLinearRegression(){
        List<Double> expected = new ArrayList<Double>();

        LinearRegressionClassifier weka = new LinearRegressionClassifier();
        Classifier model = weka.buildClassifier(training_ds);

        List<Double> estimated = WekaWrapper.validateDataset(model, validation_ds);


        double[] pred = {71.00000037326959, 115.00000058362654, 45.000000171036504, 130.00000018822257, 1126.9999975512417, 191.9999999463099};

        for (int i=0; i< pred.length; i++)
        {
            expected.add(pred[i]);
        }

        assertTrue(estimated.equals(expected));
    }

    @Test
    public void applyRepTree() throws Exception {

        RepTreeClassifier weka = new RepTreeClassifier();

        REPTree model = (REPTree) weka.buildClassifier(training_ds);
        assertTrue(model.toString().equals("\n" +
                "REPTree\n" +
                "============\n" +
                " : 101.83 (4/4826.25) [2/996.25]\n" +
                "\n" +
                "Size of the tree : 1"));
    }

    @Test
    public void validateRepTree(){
        List<Double> expected = new ArrayList<Double>();
        RepTreeClassifier weka = new RepTreeClassifier();
        REPTree model = (REPTree) weka.buildClassifier(training_ds);

        List<Double> estimated = WekaWrapper.validateDataset(model, validation_ds);

        double[] pred = {101.83333333333333, 101.83333333333333, 101.83333333333333, 101.83333333333333, 101.83333333333333, 101.83333333333333};

        for (int i=0; i< pred.length; i++)
        {
            expected.add(pred[i]);
        }

        assertTrue(estimated.equals(expected));
    }

}

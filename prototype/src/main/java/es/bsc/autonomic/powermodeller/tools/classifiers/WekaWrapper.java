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

package es.bsc.autonomic.powermodeller.tools.classifiers;

import es.bsc.autonomic.powermodeller.DataSet;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.exceptions.WekaWrapperException;
import es.bsc.autonomic.powermodeller.tools.VariableParser;
import es.bsc.autonomic.powermodeller.tools.filters.FilterTool;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.unsupervised.attribute.AddExpression;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static weka.filters.Filter.useFilter;

public abstract class WekaWrapper {
    private static final String LINEAR_REGRESSION = "LinearRegression";
    private static final String LINEAR_REGRESSION_CLASS = "LinearRegressionClassifier";
    private static final String REPTREE_CLASSIFIER="RepTree";
    private static final String REPTREE_CLASSIFIER_CLASS="RepTreeClassifier";
    private static final String MULTILAYERPERCEPTRON = "MultilayerPerceptron";
    private static final String MULTILAYERPERCEPTRON_CLASSIFIER_CLASS = "MultilayerPerceptronClassifier";
    private static final String BAGGING = "Bagging";
    private static final String BAGGING_CLASS = "BaggingClassifier";

    final static Logger logger = Logger.getLogger(WekaWrapper.class);


    /*
    processDataSet method:
        1. get Independent Variable
        2. get HashMap from VariableParser
        3. generate new variables
        4. get List of final set of variable to use in the model from VariableParser
        5. remove all the variables not present in the List
        6. Remove invalid instances (with "nan" or "?")
    */

    public static DataSet processDataSet(DataSet ds, VariableParser parser) {

        String independent = ds.getIndependent();

        if (independent == null)
            throw new WekaWrapperException("Independent variable is not set in dataset.");

        HashMap<String, String> expression_list = parser.getNewMetrics();
        Instances data = convertDataSetToInstances(ds);

        try {
            // Apply filters for all the new variables
            for (Map.Entry<String, String> entry : expression_list.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                logger.debug("Generating new variable " + key + " as " + value);

                AddExpression add_filter = new AddExpression();
                add_filter.setName(key);
                add_filter.setExpression(value);
                add_filter.setInputFormat(data);

                data = useFilter(data, add_filter);

            }

        } catch (Exception e) {
            logger.error("Error while processing new variables", e);
            throw new WekaWrapperException("Error while processing new variables");
        }

        // Iterate over all the columns and keep only the ones contained in variables list
        List<String> variables = parser.getColumns();

        // Append independent variable to the list of variables to keep
        variables.add(independent);

        // Remove unneeded attributes
        try {

            // it's important to iterate from last to first, because when we remove
            // an instance, the rest shifts by one position.
            for (int i = data.numAttributes() - 1; i >= 0; i--) {
                String n = data.attribute(i).name();
                if (!variables.contains(data.attribute(i).name())) {
                    logger.trace("Deleting unnecessary attribute " + data.attribute(i).name());
                    data.deleteAttributeAt(i);
                }
            }

            data.toString();
        } catch (Exception e) {
            logger.error("Error while removing unneeded variables", e);
            throw new WekaWrapperException("Error while removing unneeded variables");
        }



        // Convert Instances in csv and return the new DataSet
        String new_path = CoreConfiguration.getNewCSVFileName();
        try {
            CSVSaver saver = new CSVSaver();
            saver.setInstances(data);
            saver.setFile(new File(new_path));
            saver.writeBatch();
        } catch (Exception e) {
            logger.error("Error while removing unneeded variables", e);
            throw new WekaWrapperException("Error while removing unneeded variables");
        }

        DataSet ret = new DataSet(new_path);
        ret.setIndependent(independent);
        return ret;
    }


    public static Classifier generateClassifier(DataSet training_ds) {

        WekaWrapper w = getClassifierConf(CoreConfiguration.CLASSIFIER);
        return w.buildClassifier(training_ds);

    }

    public static Classifier generateClassifier(DataSet training_ds, String classifier) {

        WekaWrapper w = getClassifierConf(classifier);
        return w.buildClassifier(training_ds);

    }

    public static List<Double> validateDataset(Classifier classifier, DataSet validationDS) {

        logger.debug("Validating dataset.");
        List<Double> estimated = new ArrayList<Double>();
        Instances validation_ds = convertDataSetToInstances(validationDS);
        //logger.debug(classifier.toString());
        try {
            // estimate each instance
            for (Instance instance : validation_ds) {
                try {
                    Double k = classifier.classifyInstance(instance);
                    estimated.add(k);
                } catch (Exception e) {
                    logger.error("Error while estimating instances", e);
                    throw new WekaWrapperException("Error while estimating instances");
                }

            }
        } catch (Exception e) {
            logger.error("Error while reading input DataSet", e);
            throw new WekaWrapperException("Error while reading input DataSet");
        }
        return estimated;

    }


    private static WekaWrapper getClassifierConf(String cls){
        String pack = WekaWrapper.class.getPackage().getName();

        if (cls.equalsIgnoreCase(LINEAR_REGRESSION))
            cls = LINEAR_REGRESSION_CLASS;
        else if (cls.equalsIgnoreCase(REPTREE_CLASSIFIER))
            cls = REPTREE_CLASSIFIER_CLASS;
        else if (cls.equalsIgnoreCase(MULTILAYERPERCEPTRON))
            cls = MULTILAYERPERCEPTRON_CLASSIFIER_CLASS;
        else if (cls.equalsIgnoreCase(BAGGING))
            cls = BAGGING_CLASS;

        String cls_use = pack.concat(".").concat(cls);

        WekaWrapper w = null;

        try {

            w = (WekaWrapper) Class.forName(cls_use).newInstance();

        } catch (Exception e){
            logger.error("Error in classifier definition", e);
            throw new WekaWrapperException("Error in classifier definition");
        }

        return w;
    }

    public static String evaluateDataset(Classifier classifier, DataSet trainingDS, DataSet validationDS) {

        Instances training_ds = convertDataSetToInstances(trainingDS);
        Instances validation_ds = convertDataSetToInstances(validationDS);
        String summary;

        try {
            // Evaluete dataset with weka and return a summary
            Evaluation evaluation = new Evaluation(training_ds);
            evaluation.evaluateModel(classifier, validation_ds);
            summary = evaluation.toSummaryString();
        } catch (Exception e) {
            logger.error("Error while evaluating Dataset", e);
            throw new WekaWrapperException("Error while evaluating Dataset", e);
        }

        return summary;

    }

    public static Instances convertDataSetToInstances(DataSet ds){

        Instances instances;
        try {
            // Read all the instances in the file and initialize data

            ConverterUtils.DataSource source = new ConverterUtils.DataSource(ds.getFilePath());
            instances = source.getDataSet();

            instances.setClassIndex(instances.attribute(ds.getIndependent()).index());

        } catch (Exception e) {
            logger.error("Error while reading input DataSet", e);
            throw new WekaWrapperException("Error while reading input DataSet");
        }

        return instances;
    }

    protected abstract Classifier buildClassifier(DataSet training_ds);

}

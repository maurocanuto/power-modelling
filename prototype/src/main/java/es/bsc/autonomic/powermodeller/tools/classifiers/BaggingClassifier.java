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
import es.bsc.autonomic.powermodeller.exceptions.WekaWrapperException;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class BaggingClassifier extends WekaWrapper  {

    @Override
    protected Classifier buildClassifier(DataSet training_ds) {
        logger.debug("Building Bagging classifier.");

        Classifier model = null;

        // Get the independent variable index
        String independent = training_ds.getIndependent();

        if (independent == null)
            throw new WekaWrapperException("Independent variable is not set in dataset.");

        try {

            // Read all the instances in the file (ARFF, CSV, XRFF, ...)
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(training_ds.getFilePath());
            Instances instances = source.getDataSet();

            // Set the independent variable (powerWatts).
            instances.setClassIndex(instances.attribute(independent).index());

            // Builds a regression model for the given data.
            model = new weka.classifiers.meta.Bagging();

            // Build Linear Regression
            model.buildClassifier(instances);

        } catch (WekaWrapperException e) {
            logger.error("Error while creating Bagging classifier.", e);
            throw new WekaWrapperException("Error while creating Bagging classifier.");

        } catch (Exception e) {
            logger.error("Error while applying Bagging to data set instances.", e);
            throw new WekaWrapperException("Error while applying Bagging to data set instances.");
        }

        return model;

    }

}

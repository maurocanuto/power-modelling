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

import es.bsc.autonomic.powermodeller.exceptions.WekaWrapperException;
import es.bsc.autonomic.powermodeller.tools.VariableParser;
import es.bsc.autonomic.powermodeller.tools.classifiers.WekaWrapper;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static es.bsc.autonomic.powermodeller.tools.classifiers.WekaWrapper.evaluateDataset;

public class ResourceModel implements Serializable{
    private static final long serialVersionUID = 1L;

    private Classifier model;
    private VariableParser variableParser;
    private List<String> originalHeader;
    private String independent;

    final static Logger logger = Logger.getLogger(ResourceModel.class);

    public ResourceModel(DataSet training, VariableParser varParser) {
        logger.debug("Processing training dataset.");
        DataSet trainingDSForModel = WekaWrapper.processDataSet(training, varParser);
        logger.debug("Processed training dataset. Ordering...");

        trainingDSForModel = trainingDSForModel.order();

        logger.debug("Ordered dataset. Generating classifier...");
        model = WekaWrapper.generateClassifier(trainingDSForModel);
        logger.debug("Model built.");

        variableParser = varParser;
        originalHeader = trainingDSForModel.getHeader();
        independent = trainingDSForModel.getIndependent();

    }

    public ResourceModel(DataSet training, String classifier){

        training = training.order();
        logger.debug("Ordered dataset. Generating classifier...");
        model = WekaWrapper.generateClassifier(training, classifier);
        originalHeader = training.getHeader();
        logger.debug("Model built.");

    }

    public Classifier getClassifier() {
        return model;
    }

    public List<Double> estimateIndependent(DataSet validationDataset, VariableParser validationVarParser) {

        DataSet validationDS = WekaWrapper.processDataSet(validationDataset, validationVarParser);

        validationDS = validationDS.order();
        if (!validationDS.getHeader().containsAll(originalHeader)){
            logger.error("Incompatible training and validation dataset. Training: " + originalHeader.toString() + " Validation: " + validationDS.getHeaderString());
            throw new WekaWrapperException("Incompatible training and validation dataset. Training: " + originalHeader.toString() + " Validation: " + validationDS.getHeaderString());
        }

        return WekaWrapper.validateDataset(model, validationDS);
    
    }


    public List<Double> estimateIndependent(DataSet validationDataset) {
        DataSet validationDS = WekaWrapper.processDataSet(validationDataset, this.variableParser);

        validationDS = validationDS.order();
        List<String> originalHeaderWithoutIndep = new ArrayList<String>(originalHeader);
        originalHeaderWithoutIndep.remove(independent);
        List<String> validationHeaderWithoutIndep = new ArrayList<String>(validationDS.getHeader());
        validationHeaderWithoutIndep.remove(validationDS.getIndependent());

        if ( ! originalHeaderWithoutIndep.containsAll(validationHeaderWithoutIndep)) {
            logger.error("Incompatible training and validation dataset. Training: " + originalHeader.toString() + " Validation: " + validationDS.getHeaderString());
            throw new WekaWrapperException("Incompatible training and validation dataset. Training: " + originalHeader.toString() + " Validation: " + validationDS.getHeaderString());
        }

        return WekaWrapper.validateDataset(model, validationDS);

    }
/*
    public String evaluateModel(DataSet validationDS){

        logger.debug("Evaluating model...");
        DataSet valid = WekaWrapper.processDataSet(validationDS, variableParser);
        valid = valid.order();
        return evaluateDataset(this.model, this.originalTrainingDS, valid);
    }*/

    public List<Double> estimateIndependent2Step(DataSet validationDS) {
        //DataSet validationDS = WekaWrapper.processDataSet(validationDataset, this.variableParser);

        validationDS = validationDS.order();
        List<String> originalHeaderWithoutIndep = new ArrayList<String>(originalHeader);
        originalHeaderWithoutIndep.remove(independent);
        List<String> validationHeaderWithoutIndep = new ArrayList<String>(validationDS.getHeader());
        validationHeaderWithoutIndep.remove(validationDS.getIndependent());

        if ( ! originalHeaderWithoutIndep.containsAll(validationHeaderWithoutIndep)) {
            logger.error("Incompatible training and validation dataset. Training: " + originalHeader.toString() + " Validation: " + validationDS.getHeaderString());
            throw new WekaWrapperException("Incompatible training and validation dataset. Training: " + originalHeader.toString() + " Validation: " + validationDS.getHeaderString());
        }

        return WekaWrapper.validateDataset(model, validationDS);

    }

}

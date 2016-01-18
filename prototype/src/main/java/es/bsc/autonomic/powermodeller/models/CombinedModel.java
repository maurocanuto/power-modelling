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

package es.bsc.autonomic.powermodeller.models;

import es.bsc.autonomic.powermodeller.DataSet;
import es.bsc.autonomic.powermodeller.PowerModelEstimator;
import es.bsc.autonomic.powermodeller.PowerModelGenerator;
import es.bsc.autonomic.powermodeller.ResourceModel;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.tools.featureScaling.DataStandardization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CombinedModel extends Models implements Serializable{
    private static final long serialVersionUID = 1L;
    private final String type = CoreConfiguration.COMBINED_MODEL;
    private List<String> validationFiles;

    private ResourceModel cpu;
    private ResourceModel memory;
    private ResourceModel disk;
    private ResourceModel network;
    private ResourceModel step2Model;

    private DataStandardization dataStandardization;

    @Override
    public void generateModel() {

        PowerModelGenerator pmg = new PowerModelGenerator();
        this.dataStandardization = pmg.getDataStandardization();
        // Generating model - STEP 1
        if (CoreConfiguration.SCALE_DATA) {

            this.cpu = pmg.generateCustomResourceModel(pmg.getTrainingCPUDataSetPaths(), CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_CPU));
            this.memory = pmg.generateCustomResourceModel(pmg.getTrainingMemoryDataSetPaths(), CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_MEMORY));
            this.disk = pmg.generateCustomResourceModel(pmg.getTrainingDiskDataSetPaths(), CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_DISK));
            this.network = pmg.generateCustomResourceModel(pmg.getTrainingNetworkDataSetPaths(), CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_NETWORK));

            DataSet trainingJoined = new DataSet(pmg.getTrainingGlobalDataSetPaths().get(0));

            DataSet outputPredicted = validateFirstModel(trainingJoined);
            logger.info("Estimation 1 STEP: " + outputPredicted.getFilePath());

            // Generating model step 2
            // Apply each Resource model to the whole training set and get a second model
            // We generate a dataSet with the following columns: Pactual, Pcpu, Pmem, Pdisk, Pnet
            // Now apply the second algorithm to it

            this.step2Model = pmg.generate2StepModel(outputPredicted);


        }else {

            this.cpu = pmg.generateCustomResourceModel(CoreConfiguration.TRAINING_CPU, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_CPU));
            this.memory = pmg.generateCustomResourceModel(CoreConfiguration.TRAINING_MEMORY, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_MEMORY));
            this.disk = pmg.generateCustomResourceModel(CoreConfiguration.TRAINING_DISK, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_DISK));
            this.network = pmg.generateCustomResourceModel(CoreConfiguration.TRAINING_NETWORK, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_NETWORK));

            // join all training set

            List<String> dataToJoin = new ArrayList<String>();
            for (String file : CoreConfiguration.TRAINING_CPU)
                dataToJoin.add(file);
            for (String file : CoreConfiguration.TRAINING_MEMORY)
                dataToJoin.add(file);
            for (String file : CoreConfiguration.TRAINING_DISK)
                dataToJoin.add(file);
            for (String file : CoreConfiguration.TRAINING_NETWORK)
                dataToJoin.add(file);

            DataSet trainingJoined = DataSet.joinDataSetsFromPath(dataToJoin);

            DataSet outputPredicted = validateFirstModel(trainingJoined);
            logger.info("Estimation 1 STEP: " + outputPredicted.getFilePath());

            // Generating model step 2
            // Apply each Resource model to the whole training set and get a second model
            // We generate a dataSet with the following columns: Pactual, Pcpu, Pmem, Pdisk, Pnet
            // Now apply the second algorithm to it

            this.step2Model = pmg.generate2StepModel(outputPredicted);
        }

    }

    private DataSet validateFirstModel(DataSet validationDS){

        HashMap<String, List<Double>> resultsTrained = new HashMap<String, List<Double>>();

        resultsTrained.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));

        DataSet trainingEstimatedCPU = validateSingleModelResource(validationDS, this.cpu);
        resultsTrained.put(CoreConfiguration.PCPU_LABEL,trainingEstimatedCPU.getCol(CoreConfiguration.PPREDICTED_LABEL));

        DataSet trainingEstimatedMemory = validateSingleModelResource(validationDS, this.memory);
        resultsTrained.put(CoreConfiguration.PMEM_LABEL,trainingEstimatedMemory.getCol(CoreConfiguration.PPREDICTED_LABEL));

        DataSet trainingEstimatedDisk = validateSingleModelResource(validationDS, this.disk);
        resultsTrained.put(CoreConfiguration.PDISK_LABEL,trainingEstimatedDisk.getCol(CoreConfiguration.PPREDICTED_LABEL));

        DataSet trainingEstimatedNetwork = validateSingleModelResource(validationDS, this.network);
        resultsTrained.put(CoreConfiguration.PNET_LABEL,trainingEstimatedNetwork.getCol(CoreConfiguration.PPREDICTED_LABEL));

        DataSet outputPredicted = new DataSet(resultsTrained);
        outputPredicted.setIndependent(CoreConfiguration.PACTUAL_LABEL);

        return outputPredicted;
    }




    private DataSet validateSingleModelResource(DataSet validationDS, ResourceModel model){
        PowerModelEstimator pme = new PowerModelEstimator(validationDS);
        return pme.estimateCustom(model);
    }





    @Override
    public DataSet validateModel() {

        DataSet validationDS = null;

        if (CoreConfiguration.SCALE_DATA) {
            String fileToProcess = DataSet.joinDataSetsFromPath(validationFiles).getFilePath();
            DataStandardization validateStandardization = new DataStandardization(this.dataStandardization.getMu(), this.dataStandardization.getSigma());

            validationDS = new DataSet(validateStandardization.applyStandardization(fileToProcess, CoreConfiguration.INDEPENDENT));
        }
        else{
            validationFiles = new ArrayList<String>(CoreConfiguration.VALIDATION);
            validationDS = DataSet.joinDataSetsFromPath(validationFiles);
        }

        logger.info(step2Model.getClassifier());
        // Validate - Step 1
        DataSet outputPredicted = validateFirstModel(validationDS);
        // Validate - Step 2

        logger.info("Model validated with " + outputPredicted.getSize() + " samples.");
        PowerModelEstimator pme = new PowerModelEstimator(outputPredicted);
        return pme.estimate2Step(this.step2Model);

    }

    @Override
    protected String getType() {
        return this.type;
    }
}

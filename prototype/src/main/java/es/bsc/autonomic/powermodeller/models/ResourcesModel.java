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
import java.util.List;

public class ResourcesModel extends Models implements Serializable{
    private static final long serialVersionUID = 1L;
    private List<String> validationFiles;

    private final String type = CoreConfiguration.RESOURCES_MODEL;
    private ResourceModel cpu;
    private ResourceModel memory;
    private ResourceModel disk;
    private ResourceModel network;
    private DataStandardization dataStandardization;

    @Override
    public void generateModel() {

        PowerModelGenerator pmg = new PowerModelGenerator();

        if (CoreConfiguration.MODEL_RESOURCES_LEVEL.equalsIgnoreCase(CoreConfiguration.RESOURCES_MODEL_TYPE_CPU)) {
            pmg.generateModelCPU();
            this.cpu = pmg.getCpuModel();
        } else if(CoreConfiguration.MODEL_RESOURCES_LEVEL.equalsIgnoreCase(CoreConfiguration.RESOURCES_MODEL_TYPE_MEM)) {
            pmg.generateModelMemory();
            this.cpu = pmg.getCpuModel();
            this.memory = pmg.getMemoryModel();
        } else if(CoreConfiguration.MODEL_RESOURCES_LEVEL.equalsIgnoreCase(CoreConfiguration.RESOURCES_MODEL_TYPE_DISK)) {
            pmg.generateModelDisk();
            this.cpu = pmg.getCpuModel();
            this.memory = pmg.getMemoryModel();
            this.disk = pmg.getDiskModel();
        } else {
            pmg.generateResourcesModel();
            this.cpu = pmg.getCpuModel();
            this.memory = pmg.getMemoryModel();
            this.disk = pmg.getDiskModel();
            this.network = pmg.getNetworkModel();
        }

        this.dataStandardization = pmg.getDataStandardization();

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
        PowerModelEstimator pme = new PowerModelEstimator(validationDS);
        //pme.estimateAllResources(this.cpu, this.memory, this.disk, this.network);

        DataSet result;
        if (CoreConfiguration.MODEL_RESOURCES_LEVEL.equalsIgnoreCase(CoreConfiguration.RESOURCES_MODEL_TYPE_CPU)) {
            pme.estimateCPU(this.cpu);
            result = pme.getCPUActualVSPredicted();
        } else if(CoreConfiguration.MODEL_RESOURCES_LEVEL.equalsIgnoreCase(CoreConfiguration.RESOURCES_MODEL_TYPE_MEM)) {
            pme.estimateMemory(this.cpu, this.memory);
            result = pme.getMemoryActualVSPredicted();
        } else if(CoreConfiguration.MODEL_RESOURCES_LEVEL.equalsIgnoreCase(CoreConfiguration.RESOURCES_MODEL_TYPE_DISK)) {
            pme.estimateDisk(this.cpu, this.memory, this.disk);
            result = pme.getDiskActualVSPredicted();
        } else {
            pme.estimateAllResources(this.cpu, this.memory, this.disk, this.network);
            result = pme.getGlobalActualVSPredicted();
        }

        logger.info("Model (LEVEL = " + CoreConfiguration.MODEL_RESOURCES_LEVEL + ") validated with " + result.getSize() + " samples.");
        return result;
    }

    @Override
    protected String getType() {
        return this.type;
    }


}

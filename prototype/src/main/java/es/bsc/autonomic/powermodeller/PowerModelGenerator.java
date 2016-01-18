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
import es.bsc.autonomic.powermodeller.tools.VariableParser;
import es.bsc.autonomic.powermodeller.tools.featureScaling.DataStandardization;
import es.bsc.autonomic.powermodeller.tools.filters.FilterTool;
import org.apache.log4j.Logger;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Controls the general workflow and the models applied.
 * This is where the modification in the training and validation sets will be performed.
 * It calculates the overall server power and generates the "P|Pcpu|Pmem|Pdisk|Pnet" table.
 */
public class PowerModelGenerator {

    public DataSet trainingCPU, trainingMemory, trainingDisk, trainingNetwork;
    private ResourceModel cpu, memory, disk, network;
    private List<String> trainingCPUDataSetPaths;
    private List<String> trainingMemoryDataSetPaths;
    private List<String> trainingDiskDataSetPaths;
    private List<String> trainingNetworkDataSetPaths;
    private List<String> trainingGlobalDataSetPaths;

    private String CONF_MODEL_CPU = CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_CPU);
    private String CONF_MODEL_MEMORY = CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_MEMORY);
    private String CONF_MODEL_DISK = CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_DISK);
    private String CONF_MODEL_NETWORK = CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_NETWORK);
    private String CONF_MODEL_GLOBAL = CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_GLOBAL);

    final static Logger logger = Logger.getLogger(PowerModelGenerator.class);

    private DataStandardization dataStandardization;

    /**
     * Construction to be used in production.
     */
    public PowerModelGenerator() {
        if (CoreConfiguration.SCALE_DATA) {
            String fileToProcess;

            if (CoreConfiguration.MODEL_TYPE.equals(CoreConfiguration.GLOBAL_MODEL)){

                fileToProcess = DataSet.join(loadTrainingDataSets(CoreConfiguration.TRAINING_GLOBAL)).getFilePath();
                logger.info ("Training global: " + fileToProcess);
                dataStandardization = new DataStandardization();
                dataStandardization.generateStandardization(fileToProcess, CoreConfiguration.INDEPENDENT);
                logger.info("Training processed by standardization: " + dataStandardization.getFileOutputPath());

                trainingGlobalDataSetPaths = Arrays.asList(dataStandardization.getFileOutputPath());

            }else{
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

                fileToProcess = DataSet.joinDataSetsFromPath(dataToJoin).getFilePath();

                trainingGlobalDataSetPaths = new ArrayList<String>();
                trainingGlobalDataSetPaths.add(fileToProcess);

                dataStandardization = new DataStandardization();
                dataStandardization.generateStandardization(fileToProcess, CoreConfiguration.INDEPENDENT);
                logger.info("All training dataset processed by standardization: " + dataStandardization.getFileOutputPath());

                trainingCPUDataSetPaths = new ArrayList<String>();
                trainingMemoryDataSetPaths = new ArrayList<String>();
                trainingDiskDataSetPaths = new ArrayList<String>();
                trainingNetworkDataSetPaths = new ArrayList<String>();

                for (String file : CoreConfiguration.TRAINING_CPU){
                    trainingCPUDataSetPaths.add(dataStandardization.applyStandardization(file, CoreConfiguration.INDEPENDENT));
                }
                for (String file : CoreConfiguration.TRAINING_MEMORY){
                    trainingMemoryDataSetPaths.add(dataStandardization.applyStandardization(file, CoreConfiguration.INDEPENDENT));
                }
                for (String file : CoreConfiguration.TRAINING_DISK){
                    trainingDiskDataSetPaths.add(dataStandardization.applyStandardization(file, CoreConfiguration.INDEPENDENT));
                }
                for (String file : CoreConfiguration.TRAINING_NETWORK){
                    trainingNetworkDataSetPaths.add(dataStandardization.applyStandardization(file, CoreConfiguration.INDEPENDENT));
                }


            }



        } else {
            trainingCPUDataSetPaths = CoreConfiguration.TRAINING_CPU;
            trainingMemoryDataSetPaths = CoreConfiguration.TRAINING_MEMORY;
            trainingDiskDataSetPaths = CoreConfiguration.TRAINING_DISK;
            trainingNetworkDataSetPaths = CoreConfiguration.TRAINING_NETWORK;
            trainingGlobalDataSetPaths = CoreConfiguration.TRAINING_GLOBAL;
        }

    }

    /**
     * Constructor only to be used in test mode.
     *
     * @param trainingCPUDataSetPaths
     * @param trainingMemoryDataSetPaths
     * @param trainingDiskDataSetPaths
     * @param trainingNetworkDataSetPaths
     * @param trainingGlobalDataSetPaths
     * @param CONF_MODEL_CPU
     * @param CONF_MODEL_MEMORY
     * @param CONF_MODEL_DISK
     * @param CONF_MODEL_NETWORK
     * @param CONF_MODEL_GLOBAL
     */
    public PowerModelGenerator(List<String> trainingCPUDataSetPaths,
                               List<String> trainingMemoryDataSetPaths,
                               List<String> trainingDiskDataSetPaths,
                               List<String> trainingNetworkDataSetPaths,
                               List<String> trainingGlobalDataSetPaths,
                               String CONF_MODEL_CPU,
                               String CONF_MODEL_MEMORY,
                               String CONF_MODEL_DISK,
                               String CONF_MODEL_NETWORK,
                               String CONF_MODEL_GLOBAL) {

        this.trainingCPUDataSetPaths = trainingCPUDataSetPaths;
        this.trainingMemoryDataSetPaths = trainingMemoryDataSetPaths;
        this.trainingDiskDataSetPaths = trainingDiskDataSetPaths;
        this.trainingNetworkDataSetPaths = trainingNetworkDataSetPaths;
        this.trainingGlobalDataSetPaths = trainingGlobalDataSetPaths;
        this.CONF_MODEL_CPU = CONF_MODEL_CPU;
        this.CONF_MODEL_MEMORY = CONF_MODEL_MEMORY;
        this.CONF_MODEL_DISK = CONF_MODEL_DISK;
        this.CONF_MODEL_NETWORK = CONF_MODEL_NETWORK;
        this.CONF_MODEL_GLOBAL = CONF_MODEL_GLOBAL;

    }

    private List<DataSet> loadTrainingDataSets(List<String> dataSetPaths) {
        List<DataSet> ret = new ArrayList<DataSet>();

        for (String filePath : dataSetPaths) {
            DataSet temp = new DataSet(filePath);
            if (CoreConfiguration.PREPROCESS_DATASET) {
                temp = FilterTool.applyPreprocessingFilter(CoreConfiguration.FILTER_TYPE, temp);
            }
            ret.add(temp);
        }
        return ret;

    }

    private void buildModelCPU() {
        logger.debug("Generating CPU training DataSet");

        trainingCPU = DataSet.join(loadTrainingDataSets(trainingCPUDataSetPaths));

        logger.info("Generated CPU TRAINING DataSet with " + trainingCPU.getSize() + " samples. File: " + trainingCPU.getFilePath());

        VariableParser varParserCPU_CPU = new VariableParser(CONF_MODEL_CPU, trainingCPU.getHeader());
        varParserCPU_CPU.getColumns();

        cpu = new ResourceModel(trainingCPU, varParserCPU_CPU);

        logger.info("Generated CPU Model: ");
        logger.info(cpu.getClassifier());

    }

    public ResourceModel generate2StepModel(DataSet trainingDS) {

        logger.debug("Generating 'Step 2 model' with algorithm: " + CoreConfiguration.STEP_2_CLASSIFIER);
        ResourceModel res = new ResourceModel(trainingDS, CoreConfiguration.STEP_2_CLASSIFIER);
        return res;

    }

    private void buildModelMemory() {
        logger.debug("Generating Memory training DataSet");

        trainingMemory = DataSet.join(loadTrainingDataSets(trainingMemoryDataSetPaths));
        logger.info("Generated MEMORY TRAINING DataSet with " + trainingMemory.getSize() + " samples. File: " + trainingMemory.getFilePath());

        List<Double> predictedCpu = cpu.estimateIndependent(trainingMemory);

        DataSet trainingMemWithoutCpu = trainingMemory.substractFromColumn(trainingMemory.getIndependent(), CoreConfiguration.P_PCPU, predictedCpu);
        logger.info("P-Pcpu path: " + trainingMemWithoutCpu.getFilePath());

        VariableParser varParserMemory = new VariableParser(CONF_MODEL_MEMORY, trainingMemWithoutCpu.getHeader());

        memory = new ResourceModel(trainingMemWithoutCpu, varParserMemory);

        logger.info("Generated Memory Model: ");
        logger.info(memory.getClassifier());

    }


    private void buildModelDisk() {

        logger.debug("Generating Disk training DataSet");

        trainingDisk = DataSet.join(loadTrainingDataSets(trainingDiskDataSetPaths));
        logger.info("Generated DISK TRAINING DataSet with " + trainingDisk.getSize() + " samples. File: " + trainingDisk.getFilePath());

        List<Double> predictedCpu = cpu.estimateIndependent(trainingDisk);
        DataSet trainingDiskWithoutCpu = trainingDisk.substractFromColumn(trainingDisk.getIndependent(), CoreConfiguration.P_PCPU, predictedCpu);

        List<Double> predictedMemory = memory.estimateIndependent(trainingDiskWithoutCpu);
        DataSet trainingDiskWithoutCpuMemory = trainingDiskWithoutCpu.substractFromColumn(trainingDiskWithoutCpu.getIndependent(), CoreConfiguration.P_PCPU_PMEM, predictedMemory);

        VariableParser varParserDisk = new VariableParser(CONF_MODEL_DISK, trainingDiskWithoutCpuMemory.getHeader());

        disk = new ResourceModel(trainingDiskWithoutCpuMemory, varParserDisk);
        logger.info("Generated Disk Model: ");
        logger.info(disk.getClassifier());

    }

    private void buildModelNetwork() {

        logger.debug("Generating Network training DataSet");

        trainingNetwork = DataSet.join(loadTrainingDataSets(trainingNetworkDataSetPaths));
        logger.info("Generated NETWORK TRAINING DataSet with " + trainingNetwork.getSize() + " samples. File: " + trainingNetwork.getFilePath());

        List<Double> predictedCpu = cpu.estimateIndependent(trainingNetwork);
        DataSet trainingNetworkWithoutCpu = trainingNetwork.substractFromColumn(trainingNetwork.getIndependent(), CoreConfiguration.P_PCPU, predictedCpu);

        List<Double> predictedMemory = memory.estimateIndependent(trainingNetworkWithoutCpu);
        DataSet trainingNetworkWithoutCpuMemory = trainingNetworkWithoutCpu.substractFromColumn(trainingNetworkWithoutCpu.getIndependent(), CoreConfiguration.P_PCPU_PMEM, predictedMemory);

        List<Double> predictedDisk = disk.estimateIndependent(trainingNetworkWithoutCpuMemory);
        DataSet trainingNetworkWithoutCpuMemoryDisk = trainingNetworkWithoutCpuMemory.substractFromColumn(trainingNetworkWithoutCpuMemory.getIndependent(), CoreConfiguration.P_PCPU_PMEM_PDISK, predictedDisk);

        VariableParser varParserNetwork = new VariableParser(CONF_MODEL_NETWORK, trainingNetworkWithoutCpuMemoryDisk.getHeader());
        network = new ResourceModel(trainingNetworkWithoutCpuMemoryDisk, varParserNetwork);
        logger.info("Generated Network Model: ");
        logger.info(network.getClassifier());
    }

    private ResourceModel buildModelResource(List<String> trainingFile, String varParConf) {

        logger.debug("Generating training DataSet");
        DataSet training = DataSet.join(loadTrainingDataSets(trainingFile));

        logger.info("Generated DataSet with " + training.getSize() + " samples: " + training.getFilePath());

        VariableParser varParserCustom = new VariableParser(varParConf, training.getHeader());
        varParserCustom.getColumns();

        ResourceModel custom = new ResourceModel(training, varParserCustom);

        logger.info("Generated Model: ");
        logger.info(custom.getClassifier());
        return custom;
    }

    public void generateModelCPU() {
        this.buildModelCPU();
    }

    public void generateModelMemory() {
        this.buildModelCPU();
        this.buildModelMemory();
    }

    public void generateModelDisk() {
        this.buildModelCPU();
        this.buildModelMemory();
        this.buildModelDisk();
    }

    public void generateModelNetwork() {
        this.buildModelCPU();
        this.buildModelMemory();
        this.buildModelDisk();
        this.buildModelNetwork();
    }

    public void generateResourcesModel() {
        this.generateModelNetwork();
    }

    public ResourceModel generateCustomResourceModel(List<String> training, String varPars) {
        return this.buildModelResource(training, varPars);
    }

    public ResourceModel generateGlobalModel() {
        return this.buildModelResource(trainingGlobalDataSetPaths, CONF_MODEL_GLOBAL);
    }



    public ResourceModel getCpuModel() {
        return cpu;
    }

    public ResourceModel getMemoryModel() {
        return memory;
    }

    public ResourceModel getDiskModel() {
        return disk;
    }

    public ResourceModel getNetworkModel() {
        return network;
    }

    public DataStandardization getDataStandardization() {
        return dataStandardization;
    }

    public List<String> getTrainingCPUDataSetPaths() {
        return trainingCPUDataSetPaths;
    }

    public List<String> getTrainingMemoryDataSetPaths() {
        return trainingMemoryDataSetPaths;
    }

    public List<String> getTrainingDiskDataSetPaths() {
        return trainingDiskDataSetPaths;
    }

    public List<String> getTrainingNetworkDataSetPaths() {
        return trainingNetworkDataSetPaths;
    }

    public List<String> getTrainingGlobalDataSetPaths() {
        return trainingGlobalDataSetPaths;
    }
}

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
import es.bsc.autonomic.powermodeller.exceptions.ModelException;
import es.bsc.autonomic.powermodeller.graphics.TotalPowerAndPredictionDifference;
import es.bsc.autonomic.powermodeller.models.CombinedModel;
import es.bsc.autonomic.powermodeller.models.GlobalModel;
import es.bsc.autonomic.powermodeller.models.Models;
import es.bsc.autonomic.powermodeller.models.ResourcesModel;
import org.apache.log4j.Logger;

import java.io.BufferedReader;

import java.io.InputStreamReader;

public class Main {

    final static Logger logger = Logger.getLogger(Main.class);
    PowerModelGenerator pmg = new PowerModelGenerator();

    public static void main(String[] args) {
        Models resMod = null;
        long start = System.currentTimeMillis();

        // generate new model
        if (CoreConfiguration.MODEL_TYPE.equals(CoreConfiguration.RESOURCES_MODEL)) {
            resMod = new ResourcesModel();
        } else if (CoreConfiguration.MODEL_TYPE.equals(CoreConfiguration.GLOBAL_MODEL)) {
            resMod = new GlobalModel();
        } else if (CoreConfiguration.MODEL_TYPE.equals(CoreConfiguration.COMBINED_MODEL)) {
            resMod = new CombinedModel();
        } else {
            throw new ModelException(CoreConfiguration.MODEL_TYPE + " is not a valid type of model!");
        }


        //generateStandardization(CoreConfiguration.TRAINING_CPU.get(0), "ss", CoreConfiguration.INDEPENDENT);


        // Build model or deserialize one
        resMod.buildModel();

        // Validation
        DataSet result = resMod.runValidation();

        // Show graph
        TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(result);
        graph.display();
        printEstimationInfo(result.getFilePath());

        long end = System.currentTimeMillis();
        long time = end - start;

        System.out.println("Execution time: " + time / 1000 + " s");
    }


    public static void printEstimationInfo(String file) {

        logger.info("Estimating errors");
        System.out.println("################################");

        String RscriptPath = CoreConfiguration.getFilePath(CoreConfiguration.R_SCRIPT_ESTIMATION);
        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String[]{RscriptPath, file});
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("################################");
    }





/*
    public void validateResources(String file){
        PowerModelEstimator estimator = new PowerModelEstimator(file, pmg);
        DataSet estimatedValues = estimator.estimateAllResources();

        DataSet validationDS = new DataSet(file);
        logger.info("####### GLOBAL MODEL ##########");

        logger.info("CPU model evaluation: ");
        logger.info(estimator.evaluateResource(validationDS, estimator.cpu));

        logger.info("Memory model evaluation: ");
        List<Double> predictedCpu = estimatedValues.getCol(CoreConfiguration.PCPU_LABEL);
        DataSet validationWithoutCpu = validationDS.substractFromColumn(validationDS.getIndependent(), validationDS.getIndependent(), predictedCpu);
        logger.info(estimator.evaluateResource(validationWithoutCpu, estimator.memory));

        logger.info("Disk model evaluation: ");
        List<Double> predictedMemory = estimatedValues.getCol(CoreConfiguration.PMEM_LABEL);
        DataSet validationWithoutCpuMem = validationDS.substractFromColumn(validationDS.getIndependent(), validationDS.getIndependent(), predictedMemory);
        logger.info(estimator.evaluateResource(validationWithoutCpuMem, estimator.disk));

        logger.info("Network model evaluation: ");
        List<Double> predictedDisk= estimatedValues.getCol(CoreConfiguration.PDISK_LABEL);
        DataSet validationWithoutCpuMemDisk = validationDS.substractFromColumn(validationDS.getIndependent(), validationDS.getIndependent(), predictedDisk);
        logger.info(estimator.evaluateResource(validationWithoutCpuMemDisk, estimator.network));

        logger.info("#################");

        logger.info("Displaying Graphs");

        DataSet result = estimator.getGlobalActualVSPredicted();
        TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(result);
        graph.display();
    }


    public void validateCPU(String file){

        PowerModelEstimator estimator = new PowerModelEstimator(file, pmg);
        DataSet validationDS = new DataSet(file);
        logger.info("####### MODEL CPU ##########");
        estimator.estimateCPU();

        logger.info("CPU Auto-evaluation: ");
        logger.info(estimator.evaluateResource(pmg.getTrainingCPU(), estimator.cpu));

        logger.info("CPU model evaluation: ");
        logger.info(estimator.evaluateResource(validationDS, estimator.cpu));

        logger.info("Displaying Graphs");
        DataSet result = estimator.getCPUActualVSPredicted();

        TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(result);
        //TotalPowerAndPredictionDifference graph2 = new TotalPowerAndPredictionDifference(result.getSubSampleRandom(150));
        //TotalPowerVsTotalPrediction graph2 = new TotalPowerVsTotalPrediction(result);

        graph.display();
        //graph2.display();
    }

    public void validateMemory(String file){

        PowerModelEstimator estimator = new PowerModelEstimator(file, pmg);
        DataSet validationDS = new DataSet(file);

        logger.info("####### MODEL MEMORY ##########");
        DataSet estimatedValues = estimator.estimateMemory();

        logger.info("CPU model evaluation: ");
        logger.info(estimator.evaluateResource(validationDS, estimator.cpu));

        logger.info("Memory model evaluation: ");
        List<Double> predictedCpu = estimatedValues.getCol(CoreConfiguration.PCPU_LABEL);
        DataSet validationWithoutCpu = validationDS.substractFromColumn(validationDS.getIndependent(), validationDS.getIndependent(), predictedCpu);
        logger.info(estimator.evaluateResource(validationWithoutCpu, estimator.memory));

        logger.info("Displaying Graphs");
        DataSet result = estimator.getMemoryActualVSPredicted();

        //TotalPowerVsTotalPrediction graph = new TotalPowerVsTotalPrediction(result);
        TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(result);
        graph.display();

    }


    public void validateDisk(String file){

        PowerModelEstimator estimator = new PowerModelEstimator(file, pmg);
        DataSet validationDS = new DataSet(file);

        logger.info("####### MODEL DISK ##########");
        DataSet estimatedValues =  estimator.estimateDisk();

        logger.info("CPU model evaluation: ");
        logger.info(estimator.evaluateResource(validationDS, estimator.cpu));

        logger.info("Memory model evaluation: ");
        List<Double> predictedCpu = estimatedValues.getCol(CoreConfiguration.PCPU_LABEL);
        DataSet validationWithoutCpu = validationDS.substractFromColumn(validationDS.getIndependent(), validationDS.getIndependent(), predictedCpu);
        logger.info(estimator.evaluateResource(validationWithoutCpu, estimator.memory));

        logger.info("Disk model evaluation: ");
        List<Double> predictedMemory = estimatedValues.getCol(CoreConfiguration.PMEM_LABEL);
        DataSet validationWithoutCpuMem = validationDS.substractFromColumn(validationDS.getIndependent(), validationDS.getIndependent(), predictedMemory);
        logger.info(estimator.evaluateResource(validationWithoutCpuMem, estimator.disk));

        logger.info("Displaying Graphs");
        DataSet result = estimator.getDiskActualVSPredicted();

        TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(result);
        graph.display();

    }

    public void validateNetwork(String file){
        this.validateResources(file);
    }

    public void validateCustom(List<String> training, String validation, String varP){

        PowerModelEstimator estimator = new PowerModelEstimator(validation);
        DataSet validationDS = new DataSet(validation);
        DataSet results = estimator.estimateCustom(training, varP);

        logger.info("####### MODEL CUSTOM ##########");
        logger.info(estimator.evaluateResource(validationDS, estimator.custom));

        TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(results);
        graph.display();
    }


    public void validateComposedModel(String file){

        logger.info("####### Starting composed model ##########");
        logger.info("####### Generating model using training set as test set ##########");

        List union1 = ListUtils.union(CoreConfiguration.TRAINING_CPU, CoreConfiguration.TRAINING_MEMORY);
        List union2 = ListUtils.union(union1, CoreConfiguration.TRAINING_DISK);
        List trainingUnion = ListUtils.union(union2, CoreConfiguration.TRAINING_NETWORK);

        DataSet trainingSet = DataSet.joinDataSetsFromPath(trainingUnion);
        DataSet trainingDS = computeStepOne(trainingSet.getFilePath());
        logger.info("Step 1 finished - training file: " + trainingDS.getFilePath());

        List<String> validationFiles = new ArrayList<String>(CoreConfiguration.VALIDATION);
        DataSet validationSet = DataSet.joinDataSetsFromPath(validationFiles);
        DataSet validationDS = computeStepOne(validationSet.getFilePath());
        logger.info("Step 1 finished - validation file: " + validationDS.getFilePath());

        ArrayList<String> trainPath = new ArrayList<String>();
        trainPath.add(trainingDS.getFilePath());



        //TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(result_cpu);
        //graph.display();


    }

    private DataSet computeStepOne(String file){

        PowerModelEstimator estimator = new PowerModelEstimator(file, pmg);
        DataSet trainingDS = new DataSet(file);

        logger.info("####### MODEL CPU ##########");
        DataSet result_cpu = estimator.estimateCustom(CoreConfiguration.TRAINING_CPU, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_CPU));
        logger.info(estimator.evaluateResource(trainingDS, estimator.custom));

        logger.info("####### MODEL MEMORY ##########");
        DataSet result_memory = estimator.estimateCustom(CoreConfiguration.TRAINING_MEMORY, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_MEMORY));
        logger.info(estimator.evaluateResource(trainingDS, estimator.custom));

        logger.info("####### MODEL DISK ##########");
        DataSet result_disk = estimator.estimateCustom(CoreConfiguration.TRAINING_DISK, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_DISK));
        logger.info(estimator.evaluateResource(trainingDS, estimator.custom));

        logger.info("####### MODEL NETWORK ##########");
        DataSet result_net = estimator.estimateCustom(CoreConfiguration.TRAINING_NETWORK, CoreConfiguration.getFilePath(CoreConfiguration.CONF_MODEL_NETWORK));
        logger.info(estimator.evaluateResource(trainingDS, estimator.custom));

        HashMap<String, List<Double>> training_step1 = new HashMap<String, List<Double>>();
        training_step1.put(CoreConfiguration.PACTUAL_LABEL, result_cpu.getCol(CoreConfiguration.PACTUAL_LABEL));
        training_step1.put(CoreConfiguration.PCPU_LABEL, result_cpu.getCol(CoreConfiguration.PPREDICTED_LABEL));
        training_step1.put(CoreConfiguration.PMEM_LABEL, result_memory.getCol(CoreConfiguration.PPREDICTED_LABEL));
        training_step1.put(CoreConfiguration.PDISK_LABEL, result_disk.getCol(CoreConfiguration.PPREDICTED_LABEL));
        training_step1.put(CoreConfiguration.PNET_LABEL, result_net.getCol(CoreConfiguration.PPREDICTED_LABEL));

        DataSet DsAfterStep1 = new DataSet(training_step1);
        DsAfterStep1.setIndependent(CoreConfiguration.PACTUAL_LABEL);
        DsAfterStep1.order();

        return DsAfterStep1;
    }*/

}

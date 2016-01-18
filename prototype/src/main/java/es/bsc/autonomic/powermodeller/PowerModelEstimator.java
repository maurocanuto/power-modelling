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
import es.bsc.autonomic.powermodeller.exceptions.PowerModelEstimatorException;
import es.bsc.autonomic.powermodeller.tools.filters.FilterTool;
import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Evaluate one or more validation files using generated models.
 */
public class PowerModelEstimator {

        private DataSet validationDS;
        private DataSet estimatedDS;

        final static Logger logger = Logger.getLogger(PowerModelEstimator.class);

        public PowerModelEstimator(DataSet validationDS) {
                this.validationDS = validationDS;
        }

        public DataSet estimateAllResources(ResourceModel cpu, ResourceModel memory, ResourceModel disk, ResourceModel network){
                return this.estimateNetwork(cpu, memory, disk, network);
        }
/*
        public String evaluateResource(DataSet validationDS, ResourceModel rm){
                return rm.evaluateModel(validationDS);
        }*/

        public DataSet getGlobalActualVSPredicted(){

                DataSet actVsPred;
                HashMap<String, List<Double>> predicted = new HashMap<String, List<Double>>();

                if (this.estimatedDS == null) {
                        logger.error("Estimation not done yet");
                        throw new PowerModelEstimatorException("Estimation not done yet");
                }

                List<String> variableToSum = new ArrayList<String>();
                if (CoreConfiguration.PREPROCESS_DATASET && CoreConfiguration.FILTER_TYPE.equalsIgnoreCase(FilterTool.REMOVE_IDLE))
                        variableToSum.add(CoreConfiguration.PIDLE_LABEL);

                variableToSum.add(CoreConfiguration.PCPU_LABEL);
                variableToSum.add(CoreConfiguration.PMEM_LABEL);
                variableToSum.add(CoreConfiguration.PDISK_LABEL);
                variableToSum.add(CoreConfiguration.PNET_LABEL);

                DataSet newDs = this.estimatedDS.addColumns(CoreConfiguration.PPREDICTED_LABEL, variableToSum);
                predicted.put(CoreConfiguration.PACTUAL_LABEL, this.estimatedDS.getCol(CoreConfiguration.PACTUAL_LABEL));
                predicted.put(CoreConfiguration.PPREDICTED_LABEL, newDs.getCol(CoreConfiguration.PPREDICTED_LABEL));

                actVsPred = new DataSet(predicted);
                logger.info("Actual Power vs Predicted: " + actVsPred.getFilePath());
                return actVsPred;
        }

        public DataSet getCPUActualVSPredicted(){

                DataSet actVsPred;
                HashMap<String, List<Double>> predicted = new HashMap<String, List<Double>>();

                if (this.estimatedDS == null) {
                        logger.error("Estimation not done yet");
                        throw new PowerModelEstimatorException("Estimation not done yet");
                }

                List<String> variableToSum = new ArrayList<String>();
                if (CoreConfiguration.PREPROCESS_DATASET && CoreConfiguration.FILTER_TYPE.equalsIgnoreCase(FilterTool.REMOVE_IDLE))
                        variableToSum.add(CoreConfiguration.PIDLE_LABEL);

                variableToSum.add(CoreConfiguration.PCPU_LABEL);

                DataSet newDs = this.estimatedDS.addColumns(CoreConfiguration.PPREDICTED_LABEL, variableToSum);
                predicted.put(CoreConfiguration.PACTUAL_LABEL, this.estimatedDS.getCol(CoreConfiguration.PACTUAL_LABEL));
                predicted.put(CoreConfiguration.PPREDICTED_LABEL, newDs.getCol(CoreConfiguration.PPREDICTED_LABEL));


                actVsPred = new DataSet(predicted);
                return actVsPred;
        }

        public DataSet getMemoryActualVSPredicted(){

                DataSet actVsPred;
                HashMap<String, List<Double>> predicted = new HashMap<String, List<Double>>();

                if (this.estimatedDS == null) {
                        logger.error("Estimation not done yet");
                        throw new PowerModelEstimatorException("Estimation not done yet");
                }

                List<String> variableToSum = new ArrayList<String>();

                if (CoreConfiguration.PREPROCESS_DATASET && CoreConfiguration.FILTER_TYPE.equalsIgnoreCase(FilterTool.REMOVE_IDLE))
                        variableToSum.add(CoreConfiguration.PIDLE_LABEL);

                variableToSum.add(CoreConfiguration.PCPU_LABEL);
                variableToSum.add(CoreConfiguration.PMEM_LABEL);

                DataSet newDs = this.estimatedDS.addColumns(CoreConfiguration.PPREDICTED_LABEL, variableToSum);
                predicted.put(CoreConfiguration.PACTUAL_LABEL, this.estimatedDS.getCol(CoreConfiguration.PACTUAL_LABEL));
                predicted.put(CoreConfiguration.PPREDICTED_LABEL, newDs.getCol(CoreConfiguration.PPREDICTED_LABEL));

                actVsPred = new DataSet(predicted);
                return actVsPred;
        }

        public DataSet getDiskActualVSPredicted(){

                DataSet actVsPred;
                HashMap<String, List<Double>> predicted = new HashMap<String, List<Double>>();

                if (this.estimatedDS == null) {
                        logger.error("Estimation not done yet");
                        throw new PowerModelEstimatorException("Estimation not done yet");
                }

                List<String> variableToSum = new ArrayList<String>();

                if (CoreConfiguration.PREPROCESS_DATASET && CoreConfiguration.FILTER_TYPE.equalsIgnoreCase(FilterTool.REMOVE_IDLE))
                        variableToSum.add(CoreConfiguration.PIDLE_LABEL);

                variableToSum.add(CoreConfiguration.PCPU_LABEL);
                variableToSum.add(CoreConfiguration.PMEM_LABEL);
                variableToSum.add(CoreConfiguration.PDISK_LABEL);

                DataSet newDs = this.estimatedDS.addColumns(CoreConfiguration.PPREDICTED_LABEL, variableToSum);
                predicted.put(CoreConfiguration.PACTUAL_LABEL, this.estimatedDS.getCol(CoreConfiguration.PACTUAL_LABEL));
                predicted.put(CoreConfiguration.PPREDICTED_LABEL, newDs.getCol(CoreConfiguration.PPREDICTED_LABEL));

                actVsPred = new DataSet(predicted);
                return actVsPred;
        }

        public DataSet estimateCPU(ResourceModel cpu){
                HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();

                results.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));


                // Add idle
                List<Double> idle = new ArrayList<Double>();
                while(idle.size() < validationDS.getSize()) idle.add(CoreConfiguration.POWER_IDLE);
                results.put(CoreConfiguration.PIDLE_LABEL, idle);

                List<Double>  predicted_cpu = cpu.estimateIndependent(validationDS);

                results.put(CoreConfiguration.PCPU_LABEL, predicted_cpu);

                DataSet resultsDS = new DataSet(results);
                resultsDS.setIndependent(CoreConfiguration.PACTUAL_LABEL);

                this.estimatedDS = resultsDS.order();
                logger.info("Estimation file with " + this.estimatedDS.getSize() + " samples: " + this.estimatedDS.getFilePath());
                return this.estimatedDS;
        }

        public DataSet estimateMemory(ResourceModel cpu, ResourceModel memory){
                // generate models
                HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();

                results.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));

                // Add idle
                List<Double> idle = new ArrayList<Double>();
                while(idle.size() < validationDS.getSize()) idle.add(CoreConfiguration.POWER_IDLE);
                results.put(CoreConfiguration.PIDLE_LABEL, idle);

                List<Double>  predicted_cpu = cpu.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PCPU_LABEL, predicted_cpu);

                List<Double>  predicted_memory = memory.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PMEM_LABEL, predicted_memory);

                DataSet resultsDS = new DataSet(results);
                resultsDS.setIndependent(CoreConfiguration.PACTUAL_LABEL);

                this.estimatedDS = resultsDS.order();
                logger.info("Esimation file: " + this.estimatedDS.getFilePath());
                return this.estimatedDS;
        }


        public DataSet estimateDisk(ResourceModel cpu, ResourceModel memory, ResourceModel disk){

                HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();
                results.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));

                // Add idle
                List<Double> idle = new ArrayList<Double>();
                while(idle.size() < validationDS.getSize()) idle.add(CoreConfiguration.POWER_IDLE);
                results.put(CoreConfiguration.PIDLE_LABEL, idle);

                List<Double>  predicted_cpu = cpu.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PCPU_LABEL, predicted_cpu);

                List<Double>  predicted_memory = memory.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PMEM_LABEL, predicted_memory);

                List<Double>  predicted_disk = disk.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PDISK_LABEL, predicted_disk);

                DataSet resultsDS = new DataSet(results);
                resultsDS.setIndependent(CoreConfiguration.PACTUAL_LABEL);

                this.estimatedDS = resultsDS.order();
                logger.info("Esimation file: " + this.estimatedDS.getFilePath());
                return this.estimatedDS;
        }


        public DataSet estimateNetwork(ResourceModel cpu, ResourceModel memory, ResourceModel disk, ResourceModel network){

                HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();

                results.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));

                // Add idle
                List<Double> idle = new ArrayList<Double>();
                while(idle.size() < validationDS.getSize()) idle.add(CoreConfiguration.POWER_IDLE);
                results.put(CoreConfiguration.PIDLE_LABEL, idle);

                List<Double>  predicted_cpu = cpu.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PCPU_LABEL, predicted_cpu);

                List<Double>  predicted_memory = memory.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PMEM_LABEL, predicted_memory);

                List<Double>  predicted_disk = disk.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PDISK_LABEL, predicted_disk);

                List<Double>  predicted_network = network.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PNET_LABEL, predicted_network);

                DataSet resultsDS = new DataSet(results);
                resultsDS.setIndependent(CoreConfiguration.PACTUAL_LABEL);

                this.estimatedDS = resultsDS.order();
                logger.info("Esimation file: " + this.estimatedDS.getFilePath());
                return this.estimatedDS;
        }
        public DataSet estimateCustom(ResourceModel custom){

                HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();

                results.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));

                // Add idle
                List<Double> idle = new ArrayList<Double>();
                while(idle.size() < validationDS.getSize()) idle.add(CoreConfiguration.POWER_IDLE);
                results.put(CoreConfiguration.PIDLE_LABEL, idle);


                List<Double>  predicted_custom = custom.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PPREDICTED_LABEL, predicted_custom);

                DataSet resultsDS = new DataSet(results);
                resultsDS.setIndependent(CoreConfiguration.PACTUAL_LABEL);

                this.estimatedDS = resultsDS.order();
                logger.info("Esimation file: " + this.estimatedDS.getFilePath());
                return this.estimatedDS;
        }

        public DataSet estimateGlobal(ResourceModel custom){

                HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();
                results.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));

                // Add idle
                //List<Double> idle = new ArrayList<Double>();
                //while(idle.size() < validationDS.getSize()) idle.add(CoreConfiguration.POWER_IDLE);
                //results.put(CoreConfiguration.PIDLE_LABEL, idle);

                List<Double>  predicted_network = custom.estimateIndependent(validationDS);
                results.put(CoreConfiguration.PPREDICTED_LABEL, predicted_network);

                DataSet resultsDS = new DataSet(results);
                resultsDS.setIndependent(CoreConfiguration.PACTUAL_LABEL);

                this.estimatedDS = resultsDS.order();
                logger.info("Esimation file: " + this.estimatedDS.getFilePath());
                return this.estimatedDS;
        }

        public DataSet estimate2Step(ResourceModel custom){
                HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();

                results.put(CoreConfiguration.PACTUAL_LABEL, validationDS.getCol(validationDS.getIndependent()));

                // Add idle
                List<Double> idle = new ArrayList<Double>();
                while(idle.size() < validationDS.getSize()) idle.add(CoreConfiguration.POWER_IDLE);
                results.put(CoreConfiguration.PIDLE_LABEL, idle);


                List<Double>  predicted_custom = custom.estimateIndependent2Step(validationDS);
                results.put(CoreConfiguration.PPREDICTED_LABEL, predicted_custom);

                DataSet resultsDS = new DataSet(results);
                resultsDS.setIndependent(CoreConfiguration.PACTUAL_LABEL);

                this.estimatedDS = resultsDS.order();
                logger.info("Esimation file: " + this.estimatedDS.getFilePath());
                return this.estimatedDS;


        }

}

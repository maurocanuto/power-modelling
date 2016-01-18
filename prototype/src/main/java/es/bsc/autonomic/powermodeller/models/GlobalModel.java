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

public class GlobalModel extends Models implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String type = CoreConfiguration.GLOBAL_MODEL;
    private List<String> validationFiles;
    private ResourceModel global;
    private DataStandardization dataStandardization;

    @Override
    public void generateModel() {
        PowerModelGenerator pmg = new PowerModelGenerator();

        this.global = pmg.generateGlobalModel();
        this.dataStandardization = pmg.getDataStandardization();

    }

    @Override
    public DataSet validateModel() {

        DataSet validationDS = null;

        if (CoreConfiguration.SCALE_DATA) {
            String fileToProcess = DataSet.joinDataSetsFromPath(CoreConfiguration.VALIDATION).getFilePath();
            DataStandardization validateStandardization = new DataStandardization(this.dataStandardization.getMu(), this.dataStandardization.getSigma());

            validationDS = new DataSet(validateStandardization.applyStandardization(fileToProcess, CoreConfiguration.INDEPENDENT));
        }
        else{
            validationFiles = new ArrayList<String>(CoreConfiguration.VALIDATION);
            validationDS = DataSet.joinDataSetsFromPath(validationFiles);
        }


        PowerModelEstimator pme = new PowerModelEstimator(validationDS);
        DataSet result = pme.estimateGlobal(this.global);

        logger.info("Model validated with " + result.getSize() + " samples.");
        return result;
    }

    @Override
    protected String getType() {
        return this.type;
    }
}

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
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.exceptions.ModelException;
import es.bsc.autonomic.powermodeller.tools.featureScaling.DataStandardization;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Models {
    final static Logger logger = Logger.getLogger(Models.class);
    private final String SerializedModelFilePath = CoreConfiguration.TEMPDIR + "/" + "serialized" + this.getClass().getSimpleName() + ".model";

    /**
     * Build the model according to the type configured in the configuration file and serialize the model.
     * Types available: resourcesModel, globalModel, composedModel
     */

    public void buildModel(){

        if (CoreConfiguration.NEW_MODEL) {
            this.generateModel();


            serializeModel();
            logger.info("Model serialized: " + SerializedModelFilePath);
        }

    }

    /**
     * Validate the model using a validation dataSet
     */

    public DataSet runValidation(){

        DataSet ret;
        if (CoreConfiguration.NEW_MODEL) {
            ret = this.validateModel();
        }else{
            logger.info("Using deserialized model: " + SerializedModelFilePath);
            Models mod = unserializeModel();

            if (!mod.getType().equalsIgnoreCase(CoreConfiguration.MODEL_TYPE))
                throw new ModelException("The serialized model does not correspond to the type defined in the configuration file: " + mod.getType() + " vs " + CoreConfiguration.MODEL_TYPE);

            ret = mod.validateModel();
        }

        return ret;

    }

    private void serializeModel(){

        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(SerializedModelFilePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(this);
            out.close();
            fileOut.close();

        } catch (IOException e) {
            throw new ModelException("Error while serializing model: " + e.getMessage());
        }

    }

    private Models unserializeModel() {

        Models model;

        try {
            FileInputStream fileIn = new FileInputStream(CoreConfiguration.MODEL_PATH);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            model = (Models) in.readObject();
            in.close();
            fileIn.close();

        } catch (IOException i) {
            throw new ModelException("Error while unserializing model: " + i.getMessage());
        } catch (ClassNotFoundException c) {
            throw new ModelException("Class not found while unserializing model: " + c.getMessage());
        }

        return model;
    }

    protected abstract void generateModel();
    protected abstract DataSet validateModel();
    protected abstract String getType();
}

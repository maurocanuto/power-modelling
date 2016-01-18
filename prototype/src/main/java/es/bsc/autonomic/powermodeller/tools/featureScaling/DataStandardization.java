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

package es.bsc.autonomic.powermodeller.tools.featureScaling;

import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;

public class DataStandardization implements Serializable {
    private static final long serialVersionUID = 1L;

    final static Logger logger = Logger.getLogger(DataStandardization.class);

    private String mu;
    private String sigma;
    private String fileOutputPath;
    private String fileOutputPathValidated;

    public DataStandardization(String mu, String sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    public DataStandardization() {
    }

    public void generateStandardization(String fileToScale, String independentVar) {

        logger.info("Scaling training data....");
        this.fileOutputPath = CoreConfiguration.getNewCSVFileName();
        this.mu = CoreConfiguration.getNewSerializedFileName();
        this.sigma = CoreConfiguration.getNewSerializedFileName();

        String RscriptPath = CoreConfiguration.getFilePath(CoreConfiguration.R_SCRIPT_GENERATE_SCALING);
        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String[]{RscriptPath, fileToScale, fileOutputPath, mu, sigma, independentVar});
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            System.out.println("################################");
            logger.info("Scaled file: " + fileOutputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String applyStandardization(String fileToScale, String independentVar){

        logger.info("Scaling validation data....");
        fileOutputPathValidated = CoreConfiguration.getNewCSVFileName();

        String RscriptPath = CoreConfiguration.getFilePath(CoreConfiguration.R_SCRIPT_APPLY_SCALING);
        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String[]{RscriptPath, fileToScale, mu, sigma, fileOutputPathValidated, independentVar});
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            System.out.println("################################");
            logger.info("Scaled file: " + fileOutputPathValidated);
            return  fileOutputPathValidated;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public String getMu() {
        return mu;
    }

    public String getSigma() {
        return sigma;
    }

    public String getFileOutputPathValidated() {
        return fileOutputPathValidated;
    }

    public String getFileOutputPath() {
        return fileOutputPath;
    }
}

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

package es.bsc.autonomic.powermodeller.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

import static java.nio.file.Files.setPosixFilePermissions;


public class CoreConfiguration {

    public static char CSV_DELIMITER;
    public static String INDEPENDENT;
    public static String TEMPDIR ="temp-" + Calendar.getInstance().getTimeInMillis();
    public static List<String> TRAINING_CPU;
    public static List<String> TRAINING_MEMORY;
    public static List<String> TRAINING_DISK;
    public static List<String> TRAINING_NETWORK;
    public static List<String> TRAINING_GLOBAL;
    public static List<String> VALIDATION;
    public static boolean PREPROCESS_DATASET;
    public static boolean SCALE_DATA;

    public static int MOVING_AVG_WINDOW;
    public static String FILTER_TYPE;
    public static double POWER_IDLE;
    public static String CLASSIFIER;

    public static String MODEL_PATH;
    public static boolean NEW_MODEL;
    //public static boolean STEP2_NEW_MODEL;
    public static String STEP_2_CLASSIFIER;
    //public static String STEP_2_MODEL_PATH;
    public static String POWER_IDLE_VALIDATION;
    public static String MODEL_TYPE;
    public static String MODEL_RESOURCES_LEVEL;

    public static final String P_PCPU = "P-Pcpu";
    public static final String P_PCPU_PMEM = "P-Pcpu-Pmem";
    public static final String P_PCPU_PMEM_PDISK = "P-Pcpu-Pmem-Pdisk";

    public static final String PIDLE_LABEL = "Pidle";
    public static final String PCPU_LABEL = "Pcpu";
    public static final String PMEM_LABEL = "Pmem";
    public static final String PDISK_LABEL = "Pdisk";
    public static final String PNET_LABEL = "Pnet";
    public static final String PACTUAL_LABEL = "Pactual";
    public static final String PPREDICTED_LABEL = "Ppredicted";

    // Configuration files
    private static String PMG_HOME = System.getenv("PMG_HOME");
    private final static String PMG_HOME_DEFAULT = "/opt/PowerModelGenerator";
    private static final String CONF_GLOBAL = "/conf/configuration.properties";
    private static final String CONF_DATASETS = "/conf/datasets.properties";
    public static final String CONF_MODEL_CPU = "/conf/model-cpu.properties";
    public static final String CONF_MODEL_MEMORY = "/conf/model-memory.properties";
    public static final String CONF_MODEL_DISK = "/conf/model-disk.properties";
    public static final String CONF_MODEL_NETWORK = "/conf/model-network.properties";
    public static final String CONF_MODEL_GLOBAL = "/conf/model-global.properties";
    public static final String R_SCRIPT_ESTIMATION = "/conf/estimation.r";
    public static final String R_SCRIPT_GENERATE_SCALING = "/conf/scaling_data_training.r";
    public static final String R_SCRIPT_APPLY_SCALING = "/conf/scaling_data_validation.r";
    public static final String R_SCRIPT_MOVING_AVERAGE = "/conf/movingAverage.r";
    // Model types:
    public final static String RESOURCES_MODEL = "resources";
    public final static String GLOBAL_MODEL = "global";
    public final static String COMBINED_MODEL = "combined";
    // Subtypes of resources model:
    public final static String RESOURCES_MODEL_TYPE_CPU = "cpu";
    public final static String RESOURCES_MODEL_TYPE_MEM = "cpu+mem";
    public final static String RESOURCES_MODEL_TYPE_DISK = "cpu+mem+disk";
    public final static String RESOURCES_MODEL_TYPE_NET = "cpu+mem+disk+net";
    public final static String RESOURCES_MODEL_TYPE_ALL = "all";

    private final static Logger logger = Logger.getLogger(CoreConfiguration.class);

    // WARNING: this attribute must be the last to be declared in this class. NEVER MODIFY IT.
    private static final CoreConfiguration CONF_INSTANCE = new CoreConfiguration();

    /**
     * Private constructor
     */
    private CoreConfiguration() {
        //General configuration
        PropertiesConfiguration config = getPropertiesConfiguration(CoreConfiguration.CONF_GLOBAL);

        CSV_DELIMITER = config.getString("csv-delimiter").charAt(0);
        INDEPENDENT = config.getString("independent");
        PREPROCESS_DATASET = config.getBoolean("preprocess-dataset");
        SCALE_DATA = config.getBoolean("scale-data");

        FILTER_TYPE = config.getString("filter-type");
        MOVING_AVG_WINDOW = config.getInt("moving-average-window");
        POWER_IDLE = config.getDouble("power-idle-training");
        CLASSIFIER = config.getString("classifier");

        NEW_MODEL = config.getBoolean("new-model");
        MODEL_TYPE = config.getString("model-type");
        MODEL_RESOURCES_LEVEL = config.getString("model-resources-level");
        MODEL_PATH = config.getString("model-path");
        //STEP2_NEW_MODEL = config.getBoolean("step2-new-model");
        STEP_2_CLASSIFIER = config.getString("step2-classifier");
        //STEP_2_MODEL_PATH = config.getString("step2-model-path");
        POWER_IDLE_VALIDATION = config.getString("power-idle-validation");


        //Training dataset configuration
        PropertiesConfiguration datasets = getPropertiesConfiguration(CoreConfiguration.CONF_DATASETS);

        TRAINING_CPU = Arrays.asList(datasets.getStringArray("trainingCPU"));
        TRAINING_MEMORY = Arrays.asList(datasets.getStringArray("trainingMemory"));
        TRAINING_DISK = Arrays.asList(datasets.getStringArray("trainingDisk"));
        TRAINING_NETWORK = Arrays.asList(datasets.getStringArray("trainingNetwork"));
        VALIDATION = Arrays.asList(datasets.getStringArray("validation"));
        TRAINING_GLOBAL = Arrays.asList(datasets.getStringArray("trainingGlobal"));

        //Temporal working directory creation
        File fileDir = new File(TEMPDIR);
        if (!fileDir.exists()) {
            logger.debug("Creating directory: " + fileDir);
            boolean result = false;

            try{
                fileDir.mkdir();
                result = true;
            } catch(SecurityException se){
                //handle it
            }
            if(result) {
                TEMPDIR = fileDir.getAbsolutePath();
                logger.info("Temporary directory " + TEMPDIR + " created");
            }
        }
    }


    public static PropertiesConfiguration getPropertiesConfiguration(String configFile) {
        String filePath = null;
        PropertiesConfiguration config = null;

        try {
            filePath = getFilePath(configFile);
            config = new PropertiesConfiguration(filePath);
        } catch (ConfigurationException ex) {
            logger.error("Error reading " + filePath + " configuration file: " + ex.getMessage());
            logger.error(ex.getMessage());
        }

        return config;
    }

    public static String getFilePath(String configFile) {
        String pmgHome = PMG_HOME;
        if (pmgHome == null) {
            pmgHome = PMG_HOME_DEFAULT;
            PMG_HOME = pmgHome;
            logger.warn("Please set environment variable PMG_HOME. Using default " + pmgHome);
        }

        File fileObject = new File(pmgHome.concat(configFile));
        if (!fileObject.exists()) {
            try {
                createDefaultConfigFile(fileObject);
            } catch (Exception ex) {
                logger.error("Error reading " + pmgHome.concat(configFile) + " configuration file: ", ex);
            }
        }

        return pmgHome.concat(configFile);
    }

    private static void createDefaultConfigFile(File fileObject) throws Exception {
        logger.debug("File " + fileObject.getAbsolutePath() + " didn't exist. Creating one with default values...");

        //Create parent directories.
        logger.debug("Creating parent directories.");
        new File(fileObject.getParent()).mkdirs();

        //Create an empty file to copy the contents of the default file.
        logger.debug("Creating empty file.");
        new File(fileObject.getAbsolutePath()).createNewFile();

        //Copy file.
        logger.debug("Copying file " + fileObject.getName());
        InputStream streamIn = CoreConfiguration.class.getResourceAsStream("/" + fileObject.getName());
        FileOutputStream streamOut = new FileOutputStream(fileObject.getAbsolutePath());
        byte[] buf = new byte[8192];
        while (true) {
            int length = streamIn.read(buf);
            if (length < 0) {
                break;
            }
            streamOut.write(buf, 0, length);
        }

        //Close streams after copying.
        try {
            streamIn.close();
        } catch (IOException ex) {
            logger.error("Couldn't close input stream");
            logger.error(ex.getMessage());
        }
        try {
            streamOut.close();
        } catch (IOException ex) {
            logger.error("Couldn't close file output stream");
            logger.error(ex.getMessage());
        }

        setPosixFilePermissions(fileObject.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
    }

    public static String getNewCSVFileName() {
        return TEMPDIR + "/" + UUID.randomUUID().toString() + ".csv";
    }

    public static String getNewSerializedFileName() {
        return TEMPDIR + "/" + UUID.randomUUID().toString() + ".ser";
    }
}

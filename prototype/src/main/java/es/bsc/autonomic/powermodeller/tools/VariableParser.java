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

package es.bsc.autonomic.powermodeller.tools;

import es.bsc.autonomic.powermodeller.exceptions.VariableParserException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes the files containing the Model Variables of a Resource.
 */
public class VariableParser implements Serializable{
    private static final long serialVersionUID = 1L;

    private List<String> dataSetHeader;
    private HashMap<String, String> newMetrics;
    private List<String> columns;
    final static Logger logger = Logger.getLogger(VariableParser.class);


    public VariableParser(String filePath, List<String> dataSetHeader) {
        this.dataSetHeader = dataSetHeader;

        PropertiesConfiguration config = new PropertiesConfiguration();
        try {
            config.load(filePath);

            columns = new LinkedList(Arrays.asList(config.getStringArray("metrics")));
            for(String column : columns) {
                if( !dataSetHeader.contains(column) ) {
                    throw new VariableParserException("Selected metric '" + column + "' does not exist in the provided dataset");
                }
            }

            HashMap<String, String> varDictionary = processRawHM(populateHMFromKeyPrefix("var", config), null);
            newMetrics = processRawHM(populateHMFromKeyPrefix("newmetric", config), varDictionary);

        } catch (ConfigurationException e) {
            logger.error("Error while loading configuration file", e);
            throw new VariableParserException("Error while loading configuration file");
        }
    }


    private List<String> mierda(List<String> dataSetHeader) {
        List<String> ret = new ArrayList<String>();

        for(String metric : dataSetHeader) {
            if( !metric.equalsIgnoreCase("powerWatts") &&  !metric.equalsIgnoreCase("Pcpu") &&  !metric.equalsIgnoreCase("P-Pcpu") &&  !metric.equalsIgnoreCase("P-Pcpu-Pmem") &&  !metric.equalsIgnoreCase("P-Pcpu-Pmem-Pdisk")) {
                ret.add(metric);
            }
        }
        return ret;
    }

    private HashMap<String, String> populateHMFromKeyPrefix(String prefix, PropertiesConfiguration config) {

        HashMap<String, String> ret = new HashMap<String, String>();

        Iterator it = config.getKeys(prefix);
        while(it.hasNext()) {
            String prefixAndName = (String)it.next();
            String name = prefixAndName.split("\\.")[1];
            ret.put(name, config.getString(prefixAndName));
        }

        return ret;
    }

    /**
     * Translates the value expressions of a HashMap into argument numbers and returns a new translated HashMap.
     * @param rawHM HashMap to be translated: its values will be translated into argument numbers. i.e. cpu-cycles -> a31
     * @param varDictionary If not null, the function will treat rawHM as new metrics to be included in the columns list
     * @return
     */
    private HashMap<String, String> processRawHM(HashMap<String, String> rawHM, HashMap<String, String> varDictionary) {

        HashMap<String, String> ret = new HashMap<String, String>();

        Pattern regex = Pattern.compile("\\{([^}]*)\\}");
        for (Map.Entry<String, String> entry : rawHM.entrySet()) {
            String name = entry.getKey();
            String expression = entry.getValue();
            Matcher regexMatcher = regex.matcher(expression);

            while (regexMatcher.find())
            {
                String wrappedMetric = regexMatcher.group();
                String metricName = wrappedMetric.substring(1, wrappedMetric.length() - 1);
                if(dataSetHeader.indexOf(metricName) >= 0) { //Operating on an existing metric
                    int j = dataSetHeader.indexOf(metricName) + 1;
                    expression = expression.replace(wrappedMetric, "a" + j);
                } /*else if(ret.containsKey(metricName)) { //Operating on a new var/metric that uses previously defined vars/metrics, respectively
                    expression = expression.replace(wrappedMetric, ret.get(metricName));
                }*/ else if(varDictionary != null && varDictionary.containsKey(metricName)) { //Operating on a new metric using variables previously defined
                    expression = expression.replace(wrappedMetric, varDictionary.get(metricName));
                } else {
                    logger.error("Specified metric name " + metricName + " was not found neither in the available metrics neither on the variable dictionary");
                    logger.debug(columns);
                    throw new VariableParserException("Specified metric name " + metricName + " was not found neither in the available metrics neither on the variable dictionary");
                }
            }
            ret.put(name, expression);
            if(varDictionary != null) {
                columns.add(name);
                logger.debug("Added new metric '" + name + "', with expression: " + expression);
            }
        }

        return ret;
    }

    //TODO Remove?
    public void setNewMetrics(HashMap<String, String> newMetrics) {
        this.newMetrics = newMetrics;
    }

    //TODO Remove?
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }


    public HashMap<String, String> getNewMetrics() {
        return newMetrics;
    }

    public List<String> getColumns() {
        return columns;
    }
}

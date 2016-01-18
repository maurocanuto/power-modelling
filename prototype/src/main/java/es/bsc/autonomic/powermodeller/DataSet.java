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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.exceptions.DataSetException;
import es.bsc.autonomic.powermodeller.exceptions.RemoveInvalidFilterException;
import es.bsc.autonomic.powermodeller.tools.filters.FilterTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Reads a CSV
 * Captures the metrics
 */
public class DataSet {
    private List<String> header;
    private int size;
    private String filePath;
    private boolean delete = true;
    private String independent = CoreConfiguration.INDEPENDENT;
    final static Logger logger = Logger.getLogger(DataSet.class);


    public DataSet(String filePath) {
        this.filePath=filePath;
        //removeInvalidInstances(filePath);
        CSVReader reader = null;
        try{
            reader = new CSVReader(new FileReader(filePath), CoreConfiguration.CSV_DELIMITER);
            String[] headerArray = reader.readNext();
            header = new ArrayList<String>(Arrays.asList(headerArray));
            size = reader.readAll().size();
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }
    }

    private void removeInvalidInstances(String filePath) {
        File inputFile = new File(filePath);
        File tempFile = new File(CoreConfiguration.getNewCSVFileName());
        boolean successful = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.contains("?") || trimmedLine.toLowerCase().contains("nan")) {
                    logger.debug("Removing invalid instance: " + trimmedLine);
                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
            successful = tempFile.renameTo(inputFile);
        } catch(IOException e) {
            successful = false;
        }

        if(!successful) {
            throw new RemoveInvalidFilterException("Error while filtering DataSet in " + filePath);
        }
    }

    public DataSet(String filePath, boolean delete) {
        this(filePath);
        this.delete = delete;
    }



    public DataSet(HashMap<String, List<Double>> columns) {

        List<String> header = new LinkedList(columns.keySet());

        //Transform HashMap to an ordered List of Lists.
        List<List<Double>> valueLists= new LinkedList<List<Double>>();
        for( String metric : header ) {
            valueLists.add(columns.get(metric));
        }

        initializeDataSetFromLists(valueLists, header);
    }

    public DataSet(List<List<Double>> valueLists, List<String> header) {
        initializeDataSetFromLists(valueLists, header);
    }

    private void initializeDataSetFromLists(List<List<Double>> valueLists, List<String> header) {
        //Check that all columns have the same size
        int sizeTemp = valueLists.get(0).size();
        for( List<Double> list : valueLists ) {
            if( sizeTemp != list.size()) {
                logger.error("Columns must have the same size to create a new DataSet. Expected: " + sizeTemp + " Found: " + list.size());
                throw new DataSetException("Columns must have the same size to create a new DataSet. Expected: " + sizeTemp + " Found: " + list.size());
            }
        }

        CSVWriter writer = null;
        String filePath = null;
        try{
            filePath = CoreConfiguration.getNewCSVFileName();
            writer = new CSVWriter(new FileWriter(filePath), CoreConfiguration.CSV_DELIMITER, CSVWriter.NO_QUOTE_CHARACTER);
            writer.writeNext(header.toArray(new String[0]));

            //Implementation 1
            for(int i = 0; i < sizeTemp; i++) {
                List<String> recordTemp = new ArrayList<String>();
                for( List<Double> list : valueLists ) {
                    recordTemp.add(Double.toString(list.get(i)));
                }
                writer.writeNext(recordTemp.toArray(new String[0]));
            }
            //

            /*//Implementation 2
            int headerSize = header.size();
            List<String[]> records = new ArrayList<String[]>();
            for(int i = 0; i < sizeTemp; i++) {
                String[] recordTemp = new String[headerSize];
                for( int j = 0; j < headerSize; j++ ) {
                    recordTemp[j] = Double.toString(valueLists.get(j).get(i));
                }
                records.add(recordTemp);
            }
            writer.writeAll(records);*/


            //Implementation 3
            /*int headerSize = header.size();
            String records[][] = new String[sizeTemp][headerSize];
            for( int j = 0; j < headerSize; j++ ) {
                for(int i = 0; i < sizeTemp; i++) {
                    records[i][j] = Double.toString(valueLists.get(j).get(i));
                }
            }
            for(int i = 0; i < sizeTemp; i++) {
                writer.writeNext(records[i]);
            }*/


        } catch (IOException e) {
            logger.error("Error while writing CSV file.", e);
            throw new DataSetException("Error while writing CSV file.");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }

        this.filePath=filePath;
        this.size = sizeTemp;
        this.header = header;
    }

    public void setIndependent(String independent) {
        if(this.getHeader().contains(independent)) {
            this.independent = independent;
        } else {
            throw new DataSetException("Independent variable " + independent + " does not exist in this dataset.");
        }
    }

    public String getIndependent() {
        if(this.getHeader().contains(independent)) {
            return independent;
        } else {
            throw new DataSetException("Independent variable " + independent + " does not exist in this dataset.");
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public int getMetricIndex(String metricName) {
        return header.indexOf(metricName);
    }

    @Override
    public String toString() {
        String ret = "";
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(filePath), CoreConfiguration.CSV_DELIMITER);
            for(String record[] : reader.readAll()) {
                ret += StringUtils.join(record, ',') + "\n";
            }
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }
        return ret;
    }

    public String getHeaderString() {
        return StringUtils.join(header, ',');
    }

    public List<String> getHeader() { return header; }

    public int getSize() {
        return size;
    }

    private List<String[]> getContents() {
        List<String[]> ret = null;
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(filePath), CoreConfiguration.CSV_DELIMITER, CSVReader.DEFAULT_ESCAPE_CHARACTER, 1);
            ret = reader.readAll();
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }
        return ret;
    }


    public static DataSet join(List<DataSet> datasets) {

        //Check headers are equal before merging. Merge file contents in memory
        String headerStr = datasets.get(0).getHeaderString();
        List<String[]> contents = new LinkedList<String[]>();
        for(DataSet ds : datasets) {
            if(headerStr.equalsIgnoreCase(ds.getHeaderString())) {
                contents.addAll(ds.getContents());
            } else {
                logger.error("Could not add DataSets with different attribute names/size:");
                logger.error("DataSet 1: " + headerStr);
                logger.error("DataSet 2: " + ds.getHeaderString());
                throw new DataSetException("Could not add DataSets with different attribute names/size.");
            }
        }

        CSVWriter writer = null;
        String filePath = null;
        try{
            filePath = CoreConfiguration.getNewCSVFileName();
            writer = new CSVWriter(new FileWriter(filePath), CoreConfiguration.CSV_DELIMITER, CSVWriter.NO_QUOTE_CHARACTER);
            writer.writeNext(datasets.get(0).getHeader().toArray(new String[0]));
            writer.writeAll(contents);
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }

        return new DataSet(filePath);
    }


    public DataSet replaceCol(String colName, String newColName, List<Double> newValues) {

        if( size != newValues.size() ) {
            throw new DataSetException("Can not swap columns between DataSet and array of different number of samples.");
        }

        if( !header.contains(colName)) {
            throw new DataSetException("Specified column '" + colName + "' does not exist in the DataSet.");
        }

        //Read current contents of file
        CSVReader reader = null;
        List<String[]> contents = null;
        try {
            reader = new CSVReader(new FileReader(filePath), CoreConfiguration.CSV_DELIMITER, CSVReader.DEFAULT_ESCAPE_CHARACTER, 1);
            contents = reader.readAll();
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }

        //Modify array with new column name
        int colIndex = header.indexOf(colName);

        //Write new contents to file while modifying specified column
        CSVWriter writer = null;
        String filePath = null;
        try{
            filePath = CoreConfiguration.getNewCSVFileName();
            writer = new CSVWriter(new FileWriter(filePath), CoreConfiguration.CSV_DELIMITER, CSVWriter.NO_QUOTE_CHARACTER);

            List<String> newHeader = new ArrayList<String>(header);
            newHeader.set(colIndex, newColName);

            //Write new header
            writer.writeNext(newHeader.toArray(new String[0]));

            //Write new contents
            for(int i = 0; i < size; i++) {
                List<String> modifiedRecord = new ArrayList<String>(Arrays.asList(contents.get(i)));
                modifiedRecord.set(colIndex, newValues.get(i).toString());
                writer.writeNext(modifiedRecord.toArray(new String[0]));
            }
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }

        DataSet ret = new DataSet(filePath);
        if(colName.equalsIgnoreCase(this.getIndependent())) {
            ret.setIndependent(newColName);
            logger.debug("Changing independent variable column from " + colName + " to " + newColName);
        }
        return ret;
    }

    public List<Double> getCol(String colName) {

        if( !header.contains(colName)) {
            throw new DataSetException("Specified column '" + colName + "' does not exist in the DataSet.");
        }
        int colIndex = header.indexOf(colName);
        //Read current contents of file
        CSVReader reader = null;
        List<Double> ret = new ArrayList<Double>();
        String [] nextLine = null;
        try {
            reader = new CSVReader(new FileReader(filePath), CoreConfiguration.CSV_DELIMITER, CSVReader.DEFAULT_ESCAPE_CHARACTER, 1);

            while ((nextLine = reader.readNext()) != null) {
                ret.add(Double.parseDouble(nextLine[colIndex]));
            }
        } catch (java.lang.NumberFormatException ex){
            logger.error("Incompatible value for metric: " + colName + ". Record value was: " + Arrays.toString(nextLine));
            throw new DataSetException("Incompatible value for metric: " + colName + ". Record value was: " + Arrays.toString(nextLine));
        }
        catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }

        return ret;
    }


    public DataSet substractFromColumn(String colName, String newColName, double value) {
        List<Double> newCol = new LinkedList<Double>();
        for(int i= 0; i < size; i++) {
            newCol.add(value);
        }

        return this.substractFromColumn(colName, newColName, newCol);
    }

    public DataSet substractFromColumn(String colName, String newColName, List<Double> substractingColumn) {

        List<Double> originalColumn = this.getCol(colName);

        if(substractingColumn.size() != size) {
            logger.error("Columns must be of the same size to be substracted one from another.");
            throw new DataSetException("Columns must be of the same size to be substracted one from another.");
        }

        List<Double> newCol = new LinkedList<Double>();
        for(int i= 0; i < size; i++) {
            newCol.add(originalColumn.get(i) - substractingColumn.get(i));
        }

        return this.replaceCol(colName, newColName, newCol);
    }

    public DataSet addColumns(String newColName, List<String> colNames) {

        if( ! header.containsAll(colNames)) {
            logger.error("Not all the columns to be added belonged to this DataSet.");
            throw new DataSetException("Not all the columns to be added belonged to this DataSet.");
        }

        List<String> newColumns = new ArrayList<String>(header);
        newColumns.removeAll(colNames);

        List<Double> sumatoryColumn = new ArrayList<Double>();
        if(colNames.size() > 0) {
            sumatoryColumn = getCol(colNames.get(0));
            if(colNames.size() > 1) {
                for(int i = 1; i < colNames.size(); i++) {
                    List<Double> addingColumn = getCol(colNames.get(i));
                    for(int j = 0; j < sumatoryColumn.size(); j++) {
                        sumatoryColumn.set(j, sumatoryColumn.get(j) + addingColumn.get(j));
                    }
                }
            }
        }

        List<List<Double>> newDataSetColumns = new ArrayList<List<Double>>();
        for(String column : newColumns) {
            newDataSetColumns.add(getCol(column));
        }
        newDataSetColumns.add(sumatoryColumn);
        newColumns.add(newColName);

        DataSet ret = new DataSet(newDataSetColumns, newColumns);
        if(colNames.contains(this.getIndependent())) {
            ret.setIndependent(newColName);
            logger.debug("Changing independent variable column from " + this.getIndependent() + " to " + newColName);
        } else {
            ret.setIndependent(this.getIndependent());
        }
        return ret;
    }

    public DataSet order() {

        List<String> orderedHeader =  new ArrayList<String>(header);
        logger.debug("Ordering - Sorting header");
        orderedHeader.remove(this.getIndependent());

        java.util.Collections.sort(orderedHeader);

        logger.debug("Ordering - Retrieving columns");
        List<List<Double>> columns = new ArrayList<List<Double>>();

        columns.add(getCol(this.getIndependent()));

        for(String metric : orderedHeader) {
            columns.add(getCol(metric));
        }
        orderedHeader.add(0, this.getIndependent());

        logger.debug("Ordering - Generating new DataSet");
        DataSet ret = new DataSet(columns, orderedHeader);

        ret.setIndependent(this.getIndependent());

        return ret;
    }


    public DataSet getSubSampleRandom(int N) {
        //Calculate indexes of samples to be retrieved
        List<Integer> indexList = new ArrayList<Integer>();
        for (int i = 0; i < this.getSize(); i++) {
            indexList.add(i);
        }
        Collections.shuffle(indexList);

        return getSubSample(indexList.subList(0, N));
    }

    public DataSet getSubSampleRange(int fromInclusive, int toExclusive) {
        //Calculate indexes of samples to be retrieved
        List<Integer> indexList = new ArrayList<Integer>();
        for (int i = 0; i < this.getSize(); i++) {
            indexList.add(i);
        }

        return getSubSample(indexList.subList(fromInclusive, toExclusive));
    }


    public DataSet getSubSample(List<Integer> indexList) {

        //Read current contents of file
        CSVReader reader = null;
        List<String[]> contents = null;
        try {
            reader = new CSVReader(new FileReader(filePath), CoreConfiguration.CSV_DELIMITER, CSVReader.DEFAULT_ESCAPE_CHARACTER, 1);
            contents = reader.readAll();
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }

        //Retrieve samples from the original record list
        List<String[]> selectedRecords = new ArrayList<String[]>();
        for (int i = 0; i < indexList.size(); i++) {
            selectedRecords.add(contents.get(indexList.get(i)));
        }

        //Write new contents to file while modifying original one
        CSVWriter writer = null;
        String filePath = null;
        try{
            filePath = CoreConfiguration.getNewCSVFileName();
            writer = new CSVWriter(new FileWriter(filePath), CoreConfiguration.CSV_DELIMITER, CSVWriter.NO_QUOTE_CHARACTER);

            //Write header
            writer.writeNext(header.toArray(new String[0]));

            //Write new contents
            writer.writeAll(selectedRecords);
        } catch (IOException e) {
            logger.error("Error while reading CSV file.", e);
            throw new DataSetException("Error while reading CSV file.");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error while closing CSV file.", e);
                throw new DataSetException("Error while closing CSV file.");
            }
        }

        DataSet ret = new DataSet(filePath);

        return ret;
    }

    public static DataSet joinDataSetsFromPath(List<String> datasetPaths) {
        logger.info("Processing files...");
        List<DataSet> ret = new ArrayList<DataSet>();
        for(String filePath : datasetPaths) {
            DataSet temp = new DataSet(filePath);
            if(CoreConfiguration.PREPROCESS_DATASET) {
                temp = FilterTool.applyPreprocessingFilter(CoreConfiguration.FILTER_TYPE, temp);
            }
            ret.add(temp);
        }
        return DataSet.join(ret);
    }
}

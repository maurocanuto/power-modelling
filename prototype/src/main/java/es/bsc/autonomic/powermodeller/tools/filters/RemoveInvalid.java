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

package es.bsc.autonomic.powermodeller.tools.filters;

import es.bsc.autonomic.powermodeller.DataSet;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import es.bsc.autonomic.powermodeller.exceptions.RemoveInvalidFilterException;

import java.io.*;

public class RemoveInvalid extends FilterTool {

    @Override
    protected DataSet runFilter(DataSet ds) {

        logger.debug("Applying filter " + this.getClass().getSimpleName());

        File inputFile = new File(ds.getFilePath());
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
            throw new RemoveInvalidFilterException("Error while filtering DataSet in " + ds.getFilePath());
        }

        DataSet ret = new DataSet(inputFile.getAbsolutePath());
        ret.setIndependent(ds.getIndependent());
        return ret;
    }
}

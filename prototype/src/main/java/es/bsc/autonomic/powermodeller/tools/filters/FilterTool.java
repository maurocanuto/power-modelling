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
import org.apache.log4j.Logger;

public abstract class FilterTool {
    public static final String MOVING_AVG = "MovingAverage";
    public static final String REMOVE_IDLE = "RemoveIdle";
    public static final String REMOVE_INVALID = "RemoveInvalid";
    final static Logger logger = Logger.getLogger(FilterTool.class);

    public static DataSet applyPreprocessingFilter(String fname, DataSet ds) {
        try {
            String pack = FilterTool.class.getPackage().getName();

            if (fname.equalsIgnoreCase(MOVING_AVG))
                fname = MOVING_AVG;
            else if (fname.equalsIgnoreCase(REMOVE_IDLE))
                fname = REMOVE_IDLE;
            else if (fname.equalsIgnoreCase(REMOVE_INVALID))
                fname = REMOVE_INVALID;

            String filter = pack.concat(".").concat(fname);

            FilterTool f = (FilterTool) Class.forName(filter).newInstance();
            return f.runFilter(ds);

        } catch (ClassNotFoundException e) {
            logger.error("No preprocessing filter '" + fname + "' defined: this filter will be applied");
            return ds;
        } catch (Exception e) {
            logger.error("Error in filter definition '" + fname +"': No filter will be applied");
            logger.error(e);
            e.printStackTrace();
            return ds;
        }
    }

    protected abstract DataSet runFilter(DataSet ds);

}

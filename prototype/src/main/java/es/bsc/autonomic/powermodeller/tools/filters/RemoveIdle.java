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

public class RemoveIdle extends FilterTool{
    private double idle = CoreConfiguration.POWER_IDLE;
    private String independent = CoreConfiguration.INDEPENDENT;

    public RemoveIdle(double idle, String independent) {
        this.idle = idle;
        this.independent = independent;
    }
    public RemoveIdle(String independent) {
        this.independent = independent;
    }
    public RemoveIdle() {
    }

    @Override
    protected DataSet runFilter(DataSet ds) {

        logger.debug("Applying filter " + this.getClass().getSimpleName());
        return ds.substractFromColumn(independent, independent, idle);
    }


}

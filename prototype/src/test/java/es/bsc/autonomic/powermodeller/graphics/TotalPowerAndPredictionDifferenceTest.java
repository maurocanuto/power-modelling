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

package es.bsc.autonomic.powermodeller.graphics;

import es.bsc.autonomic.powermodeller.DataSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TotalPowerAndPredictionDifferenceTest {
    private static DataSet actualVsPredicted;

    @Before
    public void testContextInitialization() {
        actualVsPredicted = new DataSet(getClass().getResource("/actualVsPredicted.csv").getPath());
        actualVsPredicted.setIndependent("Pactual");
    }

    //@Test
    public void testDisplaySimpleGraph() throws Exception {
        TotalPowerAndPredictionDifference graph = new TotalPowerAndPredictionDifference(actualVsPredicted);
        graph.display();

        Thread.sleep(20000);
    }
}
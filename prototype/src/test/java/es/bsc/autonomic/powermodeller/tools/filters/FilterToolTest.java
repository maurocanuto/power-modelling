package es.bsc.autonomic.powermodeller.tools.filters;

import es.bsc.autonomic.powermodeller.DataSet;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static es.bsc.autonomic.powermodeller.tools.filters.FilterTool.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
public class FilterToolTest {

    private static DataSet dataSet;

    @Before
    public void testContextInitialization() {
        dataSet = new DataSet(getClass().getResource("/trainingLR.csv").getPath());
    }

    /**
     * Source: http://www.rgagnon.com/javadetails/java-0483.html
     * @param path Directory to be deleted. It also deletes non-empty directories.
     * @return True if deletion is successful, false otherwise.
     */
    private static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }

    @AfterClass
    public static void testContextCleanUp() {
        /*deleteDirectory(new File(CoreConfiguration.TEMPDIR));*/
    }


    @Test
    public void applyMovingAverage(){

        MovingAverage filter = new MovingAverage(2);
        DataSet newDs = filter.runFilter(dataSet);

        assertTrue(newDs.toString().equals("powerWatts,cpu,mem,disk\n" +
                "46,4,6,4\n" +
                "73,5.5,9.5,8.5\n" +
                "84.8,9,8.2,7.6\n" +
                "117.6,10.8,12,13.8\n" +
                "135.5,12.5,13.5,16.25\n" +
                "157.666666666667,14.6666666666667,15,19.6666666666667\n"
        ));
    }

    @Test
    public void removeIdle(){

        RemoveIdle filter = new RemoveIdle(2.0,"powerWatts");
        DataSet newDs = filter.runFilter(dataSet);

        assertTrue(newDs.toString().equals("powerWatts,cpu,mem,disk\n" +
                "21.0,2,3,2\n" +
                "44.0,4,6,4\n" +
                "67.0,6,9,6\n" +
                "152.0,10,20,22\n" +
                "130.0,23,3,4\n" +
                "185.0,11,22,33\n"));
    }


}

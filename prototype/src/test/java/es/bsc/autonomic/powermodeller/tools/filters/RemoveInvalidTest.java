package es.bsc.autonomic.powermodeller.tools.filters;

import es.bsc.autonomic.powermodeller.DataSet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class RemoveInvalidTest {
    private static DataSet dataSet;

    @Before
    public void testContextInitialization() {
        dataSet = new DataSet(getClass().getResource("/invalid.csv").getPath());
        dataSet.setIndependent("powerWatts");
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
    public void applyRemoveInvalid(){

        FilterTool filter = new RemoveInvalid();
        DataSet newDs = filter.runFilter(dataSet);

        assertTrue(newDs.toString().equals("powerWatts,cpu,mem,disk\n" +
                                            "23,2,3,2\n" +
                                            "46,4,6,4\n" +
                                            "69,6,9,6\n" +
                                            "187,11,22,33\n"));
    }
}
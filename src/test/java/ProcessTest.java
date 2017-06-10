import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.junit.Test;

/**
 * Created by APXEOLOG on 07.06.2017.
 */
public class ProcessTest {

    @Test
    public void testProcessBuilder() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("gradlew.bat", "--no-daemon", "build");
        processBuilder.directory(new File("./gradle-project-for-test/"));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        assertNotEquals(-1, result.indexOf("BUILD SUCCESSFUL"));
    }
}

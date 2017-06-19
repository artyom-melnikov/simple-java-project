import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Created by APXEOLOG on 07.06.2017.
 */
public class ProcessTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        File projectArchive = new File(getClass().getClassLoader().getResource("gradle-project-for-test.zip").getFile());
        assertNotNull(projectArchive);
        ZipUtils.unzip(projectArchive, folder.getRoot());
    }

    @Test
    public void testProcessBuilder() throws Exception {
        String execName = SystemUtils.IS_OS_WINDOWS ? "gradlew.bat" : "gradlew";
        ProcessBuilder processBuilder = new ProcessBuilder(execName, "--no-daemon", "build");
        processBuilder.directory(new File(folder.getRoot(),"gradle-project-for-test"));
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
        System.out.println(result);
        assertNotEquals(-1, result.indexOf("BUILD SUCCESSFUL"));
    }
}

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Created by APXEOLOG on 05.06.2017.
 */

@RunWith(Parameterized.class)
public class ParameterizedTest {
    @Parameterized.Parameters(/*name = "testEscapingMechanism with parameter \"{0}\""*/)
    public static List<String> data() {
        return Arrays.asList("*a\\Qb*", "a\\Qb");
    }

    private String input;

    public ParameterizedTest(String input) {
        this.input = input;
    }

    @Test
    public void test() {
        assertEquals(input, input);
    }
}

package nbcheck.std;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author moroz
 */
public class UtilsTest {

    Properties testData;

    public UtilsTest() {
    }

    @Before
    public void setUp() {
        testData = new Properties();

        testData.put("foo.bar", "val");
        testData.put("foo.bar..", "val1");
        testData.put("foo.bar.aaa", "va2");

        testData.put("one.two", "val");
        testData.put("one.two.1.2", "val1");
        testData.put("one.two.www.eee", "va2");
    }

    @After
    public void tearDown() {
        testData.clear();
        testData = null;
    }

    /**
     * Test of getHierProperty method, of class Utils.
     */
    @Test
    public void testGetHierProperty() {
        System.out.println("getHierProperty");
        String name = "foo.bar.other.too";
        String expResult = "val";
        String result = Utils.getHierProperty(name, testData);
        assertEquals(expResult, result);
    }

    /**
     * Test of tryPropertyNotEmpty method, of class Utils.
     */
    @Test
    public void testTryPropertyNotEmpty() throws Exception {
        System.out.println("tryPropertyNotEmpty");
        String name = "foo.bar";
        String expResult = "val";
        String result = Utils.tryPropertyNotEmpty(name, testData);
        assertEquals(expResult, result);
    }

    @Test(expected = IOException.class)
    public void testTryPropertyNotEmptyWithException() throws Exception {
        System.out.println("tryPropertyNotEmpty");
        String name = "foo.bar.dddd";
        String result = Utils.tryPropertyNotEmpty(name, testData);
        assertEquals("", result); // here exception
    }

    /**
     * Test of getHierPropKeySet method, of class Utils.
     */
    @Test
    public void testGetHierPropKeySet() {
        System.out.println("getHierPropKeySet");
        Set<String> expResult = new HashSet(Arrays.asList("foo.bar", "foo.bar..", "foo.bar.aaa"));
        Set<String> result = Utils.getHierPropKeySet("foo", testData);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNextPropKeyFragment method, of class Utils.
     */
    @Test
    public void testGetNextPropKeyFragment() {
        System.out.println("getNextPropKeyFragment");
        String name = "one.two";
        Set<String> expResult = new HashSet(Arrays.asList("1", "www"));
        Set<String> result = Utils.getNextPropKeyFragment(name, testData);
        assertEquals(expResult, result);

        name = "foo.bar";
        expResult = new HashSet(Arrays.asList("", "aaa"));
        result = Utils.getNextPropKeyFragment(name, testData);
        assertEquals(expResult, result);
    }
}

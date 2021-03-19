package ws.baseline.paradrone.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Ensure that we are mathing correctly
 */
public class NumbersTest {

    @Test
    public void isReal() {
        assertTrue(Numbers.isReal(-1));
        assertTrue(Numbers.isReal(-0));
        assertTrue(Numbers.isReal(0));
        assertTrue(Numbers.isReal(1));
        assertTrue(Numbers.isReal(3.14));
        assertFalse(Numbers.isReal(Double.NaN));
        assertFalse(Numbers.isReal(Double.POSITIVE_INFINITY));
        assertFalse(Numbers.isReal(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void interpolate() {
        assertEquals(0, Numbers.interpolate(0, 100, 0), 0.001);
        assertEquals(40, Numbers.interpolate(0, 100, 0.4), 0.001);
        assertEquals(100, Numbers.interpolate(0, 100, 1), 0.001);
    }

    @Test
    public void parseDistance() {
        assertEquals(Double.NaN, Numbers.parseDistance(""), 0.001);
        assertEquals(Double.NaN, Numbers.parseDistance("X"), 0.001);
        assertEquals(0, Numbers.parseDistance("0"), 0.001);
        assertEquals(0.1, Numbers.parseDistance("0.1"), 0.001);
        assertEquals(100, Numbers.parseDistance("100"), 0.001);
    }

    public void parseInt() {
        assertEquals(-2, Numbers.parseInt("-2", -1));
        assertEquals(-1, Numbers.parseInt("-1", -1));
        assertEquals(0, Numbers.parseInt("0", -1));
        assertEquals(1, Numbers.parseInt("1", -1));
        assertEquals(2, Numbers.parseInt("2", -1));
        assertEquals(-1, Numbers.parseInt("", -1));
//        assertEquals(-1, Numbers.parseInt("0.0", -1));
//        assertEquals(-1, Numbers.parseInt("0.1", -1));
        assertEquals(-1, Numbers.parseInt("X", -1));
        assertEquals(-1, Numbers.parseInt(null, -1));
    }

}

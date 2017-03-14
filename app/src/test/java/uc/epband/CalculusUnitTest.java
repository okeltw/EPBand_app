package uc.epband;

import org.junit.Test;
import uc.epband.Calculus;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by taylor on 3/14/17.
 */

public class CalculusUnitTest {
    // assertEquals(double, double) is depreciated due to precision errors on double.
    // A delta overload is suggested instead; the following is the allowable difference
    static final double delta = 0.0d;

    @Test
    public void integrate_isCorrect() throws Exception {
        double val1 = Calculus.integrate(2, 2, 1, Calculus.INT_MODE.LEFT);  // 2
        double val2 = Calculus.integrate(2, 2, 1, Calculus.INT_MODE.RIGHT); // 2
        double val3 = Calculus.integrate(2, 2, 1, Calculus.INT_MODE.MID);   // 2
        double val4 = Calculus.integrate(2, 2, 1, Calculus.INT_MODE.TRAP);  // 2

        assertEquals(val1, 2, delta);
        assertEquals(val2, 2, delta);
        assertEquals(val3, 2, delta);
        assertEquals(val4, 2, delta);

        double val5 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.LEFT);  // 1
        double val6 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.RIGHT); // 5
        double val7 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.MID);   // 3
        double val8 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.TRAP);  // 3

        assertEquals(val5, 1, delta);
        assertEquals(val6, 5, delta);
        assertEquals(val7, 3, delta);
        assertEquals(val8, 3, delta);
    }

    @Test(expected = IllegalArgumentException.class)
    public void integrateRange_exceptionThrown(){
        double[] emptyArray = {};
        Calculus.integrateRange(emptyArray, 0, Calculus.INT_MODE.RIGHT);
    }

    @Test
    public void integrateRange_isCorrect(){
        double[] range = {0,1,2,3,4,5};
        double width = 1;

        double val1 = Calculus.integrateRange(range, width, Calculus.INT_MODE.LEFT);
        // 0 + 1 + 2 + 3 + 4 = 10
        double val2 = Calculus.integrateRange(range, width, Calculus.INT_MODE.RIGHT);
        // 1 + 2 + 3 + 4 + 5 = 15
        double val3 = Calculus.integrateRange(range, width, Calculus.INT_MODE.MID);
        // 0.5 + 1.5 + 2.5 + 3.5 + 4.5 = 12.5
        double val4 = Calculus.integrateRange(range, width, Calculus.INT_MODE.TRAP);
        // 0.5 + 1.5 + 2.5 + 3.5 + 4.5 = 12.5

        assertEquals(val1, 10, delta);
        assertEquals(val2, 15, delta);
        assertEquals(val3, 12.5, delta);
        assertEquals(val4, 12.5, delta);
    }

    @Test(expected = IllegalArgumentException.class)
    public void integrateRangeArray_exceptionThrown() {
        double[] emptyArray = {};
        Calculus.integrateRangeArray(emptyArray, 0, Calculus.INT_MODE.RIGHT);
    }

    @Test
    public void integrateRangeArray_isCorrect(){
        double[] range = {0,1,2,3,4,5};
        double width = 1;

        double[] val1 = Calculus.integrateRangeArray(range, width, Calculus.INT_MODE.LEFT);
        double[] expect1 = {0,1,2,3,4};

        double[] val2 = Calculus.integrateRangeArray(range, width, Calculus.INT_MODE.RIGHT);
        double[] expect2 = {1,2,3,4,5};

        double[] val3 = Calculus.integrateRangeArray(range, width, Calculus.INT_MODE.MID);
        double[] expect3 = {0.5,1.5,2.5,3.5,4.5};

        double[] val4 = Calculus.integrateRangeArray(range, width, Calculus.INT_MODE.TRAP);
        double[] expect4 = {0.5,1.5,2.5,3.5,4.5};

        assertArrayEquals(val1, expect1, delta);
        assertArrayEquals(val2, expect2, delta);
        assertArrayEquals(val3, expect3, delta);
        assertArrayEquals(val4, expect4, delta);
    }
}

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

        assertEquals(2, val1, delta);
        assertEquals(2, val2, delta);
        assertEquals(2, val3, delta);
        assertEquals(2, val4, delta);

        double val5 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.LEFT);  // 1
        double val6 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.RIGHT); // 5
        double val7 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.MID);   // 3
        double val8 = Calculus.integrate(1, 5, 1, Calculus.INT_MODE.TRAP);  // 3

        assertEquals(1, val5, delta);
        assertEquals(5, val6, delta);
        assertEquals(3, val7, delta);
        assertEquals(3, val8, delta);
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

        assertEquals(10, val1, delta);
        assertEquals(15, val2, delta);
        assertEquals(12.5, val3, delta);
        assertEquals(12.5, val4, delta);
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

        assertArrayEquals(expect1, val1, delta);
        assertArrayEquals(expect2, val2, delta);
        assertArrayEquals(expect3, val3, delta);
        assertArrayEquals(expect4, val4, delta);
    }

    @Test(expected = IllegalArgumentException.class)
    public void linearInterpolatePoint_ExceptionThrown() {
        double[] emptyA = {},
                 emptyB = {};
        Calculus.linearInterpolatePoint(emptyA, emptyB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void linearInterpolateNextPoint_ExceptionThrown() {
        double[] emptyA = {},
                emptyB = {};
        Calculus.linearInterpolateNextPoint(emptyA, emptyB);
    }

    @Test
    public void linearInterpolatePoint_isCorrect(){
        double[] a = {0,0},
                 b = {2,2}; //result = 1,1

        double[] val = Calculus.linearInterpolatePoint(a, b);
        double[] expected = {1,1};

        assertEquals(val.length, 2);
        assertArrayEquals(expected, val, delta );
    }

    @Test
    public void linearInterpolateNextPoint_isCorrect(){
        double[] a = {0,0},
                 b = {2,2}; //result = 4,4

        double[] val = Calculus.linearInterpolateNextPoint(a, b);
        double[] expected = {4,4};

        assertEquals(val.length, 2);
        assertArrayEquals(expected, val, delta );
    }

    @Test
    public void linearInterpolateValue_isCorrect(){
        double a = 0,
                b = 2,
                expected = 1,
                val = Calculus.linearInterpolateValue(a,b);
        assertEquals(expected, val, delta);
    }

    @Test
    public void linearInterpolateNextValue_isCorrect(){
        double a = 0,
                b = 2,
                expected = 4,
                val = Calculus.linearInterpolateNextValue(a,b);
        assertEquals(expected,val,delta);
    }
}

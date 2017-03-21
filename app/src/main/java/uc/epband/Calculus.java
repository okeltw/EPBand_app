package uc.epband;

/**
 * Created by taylor on 3/14/17.
 *
 * Class to handle Calculus calculations
 */

public class Calculus {
    /**
     * Mode of integration, used in integration calculations
     */
    enum INT_MODE{
        LEFT,
        RIGHT,
        MID,
        TRAP
    }

    public static double integrate(double leftHeight, double rightHeight, double width, INT_MODE mode){
        double area = 0;
        switch (mode){
            case LEFT:
                area = leftHeight*width;
                break;
            case RIGHT:
                area = rightHeight*width;
                break;
            case MID:
                area = (rightHeight+leftHeight)/2 * width;
                break;
            case TRAP:
                double minHeight = Math.min(leftHeight, rightHeight);
                double maxHeight = Math.max(leftHeight, rightHeight);
                area = (minHeight * width) + ((maxHeight-minHeight) * width / 2);
        }
        return area;
    }

    private static final String ArgExMsg = "Range must have values.";
    public static double integrateRange(double[] range, double width, INT_MODE mode) throws IllegalArgumentException{
        if (range.length == 0){
            throw new IllegalArgumentException(ArgExMsg);
        } else if (range.length == 1){
            return range[0];
        }

        double left = range[0];
        double right;
        double sum = 0;
        for(int index = 1; index < range.length; index++){
            right = range[index];
            sum += integrate(left, right, width, mode);
            left = right;
        }

        return sum;
    }

    public static double[] integrateRangeArray(double[] range, double width, INT_MODE mode) throws IllegalArgumentException{
        if (range.length == 0){
            throw new IllegalArgumentException(ArgExMsg);
        } else if (range.length == 1){
            return range;
        }

        double[] retArray = new double[range.length-1];
        double left = range[0], right;
        for(int index = 1; index < range.length; index++){
            right = range[index];
            retArray[index-1] = integrate(left, right, width, mode);
            left = right;
        }

        return retArray;
    }

    /**
     * Interpolates a midpoint using a linear algorithm.
     * @param a Cartesian point to interpolate, form [x_val, y_val]
     * @param b Cartesian point to interpolate, form [x_val, y_val]
     * @return The result of interpolation
     * @throws IllegalArgumentException
     */
    public static double[] linearInterpolatePoint(double[] a, double[] b) throws IllegalArgumentException{
        if(a.length != 2 || b.length != 2){
            throw new IllegalArgumentException("Parameters must be of size two.");
        }

        double[] result = new double[2];
        result[0] = a[0] + (b[0] - a[0])/2;
        result[1] = a[1] + (b[1] - a[1])/2;
        return result;
    }

    /**
     * Interpolates the next point using a linear algorithm
     * based on two provided cartesian points. Direction is pointed from a -> b.
     * @param a Cartesian point to interpolate, form [x_val, y_val]
     * @param b Cartesian point to interpolate, form [x_val, y_val]
     * @return The result of interpolation
     * @throws IllegalArgumentException
     */
    public static double[] linearInterpolateNextPoint(double[] a, double[] b) throws IllegalArgumentException{
        if(a.length != 2 || b.length != 2){
            throw new IllegalArgumentException("Parameters must be of size two.");
        }

        double[] result = new double[2];
        result[0] = b[0] + (b[0]-a[0]);
        result[1] = b[1] + (b[1] - a[1]);
        return result;
    }

    /**
     * Interpolates the value (y-value) of the next point.
     * @param a
     * @param b
     * @return
     */
    public static double linearInterpolateNextValue(double a, double b){
        return b + (b-a);
    }

    /**
     * Interpolates the value (y-value) of the midpoint.
     * @param a
     * @param b
     * @return
     */
    public static double linearInterpolateValue(double a, double b){
        return a + (b-a)/2;
    }
}

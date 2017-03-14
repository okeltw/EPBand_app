package uc.epband;

/**
 * Created by taylor on 3/14/17.
 *
 * Class to handle Calculus calculations
 */

public class Calculus {
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
}

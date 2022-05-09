package ch.epfl.gameboj;

/**
 * This interface provide static methods in order to simplify the tests used
 * in other classes.
 * 
 * @author Corentin Junod (283214)
 */
public interface Preconditions {
    
    /**
     * If the boolean "b" is false, throws an IllegalArgumentException,
     * otherwise does nothing.
     * 
     * @param b
     *            the boolean to test
     * @throws IllegalArgumentException
     *             if b is false
     */
    public static void checkArgument(boolean b) {
        if (!b) throw new IllegalArgumentException();
    }

    /**
     * Check if the parameter "v" is an 8 bits value (positive value smaller than 0xFF).
     * 
     * @param v
     *            the number to test
     * @return the same value as "v"
     * @throws IllegalArgumentException
     *             if "v" is not an 8 bits value
     */
    public static int checkBits8(int v) {
        checkArgument(v >= 0x0 && v <= 0xFF);
        return v;
    }

    /**
     * Check if the parameter "v" is a 16 bits value (positive value smaller than 0xFFFF).
     * 
     * @param v
     *            the number to test
     * @return the same value as "v"
     * @throws IllegalArgumentException
     *             if "v" is not a 16 bits value
     */
    public static int checkBits16(int v) {
        checkArgument(v >= 0x0 && v <= 0xFFFF);
        return v;
    }
}

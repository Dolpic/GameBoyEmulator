package ch.epfl.gameboj.bits;

/**
 * This interface must be implemented by an enumeration that represent a sequence of bits.
 * It allow the enumeration to generate masks and return a bit's index.
 * 
 * @author Corentin Junod (283214)
 */

public interface Bit {

    abstract int ordinal();

    /**
     * Return the index of the element in the enumeration, starting from 0.
     * 
     * @return the index of the element
     */
    default int index() {
        return ordinal();
    }

    /**
     * Create a mask filled with 0, and 1 at the element's index.
     * 
     * @return a value where the index of the element is 1 and all others are 0
     */
    default int mask() {
        return Bits.mask(ordinal());
    }
}

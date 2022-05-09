package ch.epfl.gameboj;

/**
 * This interface must be implemented by an enumeration that represent a register.
 * It gives to the enumeration a function index() that does the same as ordinal().
 * 
 * @author Corentin Junod (283214)
 */

public interface Register {
    
    abstract int ordinal();
    
    /**
     * Returns the index of the element in the enumeration, starting from 0
     * @return the index of the element in the enumeration
     */
    default int index() {
        return ordinal();
    }
}

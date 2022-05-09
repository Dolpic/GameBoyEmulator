package ch.epfl.gameboj.component;

/**
 * Interface implemented by all clocked objects.
 * 
 * @author Corentin Junod (283214)
 */
public interface Clocked {

    /**
     * Exectue the given cycle on the object.
     * 
     * @param cycle
     *            the cycle to execute
     */
    public abstract void cycle(long cycle);
}

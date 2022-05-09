package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * Describe a component that can be attached to a bus.
 * 
 * @author Corentin Junod (283214)
 */
public interface Component {

    /** The value returned by a component when there is no data to be read**/
    public static final int NO_DATA = 0x100;

    
    /**
     * Read the value at a given address on the component.
     * 
     * @param address
     *            the address of the value
     * @return the value at a "address", or NO_DATA if there is no data
     * @throws IllegalArgumentException
     *             if "address" is not a 16 bits value
     */
    public abstract int read(int address);

    /**
     * Write an 8 bits value at a given address.
     * 
     * @param address
     *            the address where to write the value
     * @param data
     *            the value to write
     * @throws IllegalArgumentException
     *             if address is not a 16 bits value or if data is not an 8 bits value
     */
    public abstract void write(int address, int data);

    /**
     * Attach the component to a given Bus.
     * 
     * @param bus
     *            the bus, not null
     * @throws NullPointerException
     *             if the given bus is null
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }
}

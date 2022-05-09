package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * This class simulate a bus. His purpose is to connect all GameBoy's component
 * together and let them read and write informations into the memory.
 * 
 * @author Corentin Junod (283214)
 */
public final class Bus {
    
    private final static int DEFAULT_READ_VALUE = 0xFF;
    
    private final ArrayList<Component> list = new ArrayList<Component>();

    /**
     * Add the component to the list of current attached component.
     * 
     * @param component
     *            the component to be attached, not null
     * @throws NullPointerException
     *             if the given component is null
     */
    public void attach(Component component) {
        this.list.add(Objects.requireNonNull(component));
    }

    /**
     * Call the function read(address) on all attached components.
     * 
     * @param address
     *            the 16 bits address that must be read
     * @return the value at address "address" on the first component that
     *         doesn't return Component.NO_DATA
     * @throws IllegalArgumentException
     *             if the given address is not a 16 bits value
     */
    public int read(int address) {
        Preconditions.checkBits16(address);

        for (int i = 0; i < list.size(); i++) {
            int val = list.get(i).read(address);
            if (val != Component.NO_DATA) return val;
        }
        return DEFAULT_READ_VALUE;
    }

    /**
     * Call the function write(address,data) on all attached components.
     * 
     * @param address
     *            the 16 bits address where the data must be written
     * @param data
     *            the 8 bits value that must be written
     * @throws IllegalArgumentException
     *             if "address" is not a 16 bits value or if "data" is not an 8
     *             bits value
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        for (int i = 0; i < this.list.size(); i++) {
            list.get(i).write(address, data);
        }
    }
}

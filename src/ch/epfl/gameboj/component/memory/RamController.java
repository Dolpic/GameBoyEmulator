package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Simulate a controller for a Gameboy RAM. 
 * Used to map a RAM into the bus.
 * 
 * @author Corentin Junod (283214)
 */
public final class RamController implements Component {

    private final Ram ram;
    private final int start, end;

    /**
     * Create a new RamController on a given memory range from a RAM.
     * 
     * @param ram
     *            the RAM to control, not null
     * @param startAddress
     *            the first address that will be accessible on the RAM
     * @param endAddress
     *            the last address that will be accessible on the RAM
     * @throws NullPointerException
     *             if the given RAM is null
     * @throws IllegalArgumentException
     *             if "startAdress" and "endAddress" doesn't describe a valid
     *             range or are not 16 bits values
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Objects.requireNonNull(ram);
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        Preconditions.checkArgument(endAddress - startAddress >= 0 && endAddress - startAddress <= ram.size());
        this.ram = ram;
        this.start = startAddress;
        this.end = endAddress;
    }

    /**
     * Create a new RamController from a given address to the end of a RAM.
     * 
     * @param ram
     *            the RAM to control, not null
     * @param startAddress
     *            the first address that will be accessible on the RAM
     * @throws NullPointerException
     *             if the given RAM is null
     * @throws IllegalArgumentException
     *             if "startAdress" is negative or greater than the size of the RAM
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size());
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address >= start && address < end)
            return ram.read(address - start);
        else
            return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= start && address < end) ram.write(address - start, data);
    }
}

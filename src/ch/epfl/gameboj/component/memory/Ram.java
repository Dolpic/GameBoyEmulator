package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Simulate a Gameboy RAM, mainly a table of bytes.
 * 
 * @author Corentin Junod (283214)
 */
public final class Ram {

    private final byte[] data;

    /**
     * Create a new RAM of a given size (in bytes).
     * 
     * @param size
     *            the size of the new RAM
     * @throws IllegalArgumentException
     *             if the size is negative
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        this.data = new byte[size];
    }

    /**
     * Returns the size (in bytes) of the RAM.
     * @return the size of the RAM
     */
    public int size() {
        return data.length;
    }

    /**
     * Return the value stored at a given position.
     * 
     * @param index
     *            the position of the value to read
     * @return the value stored at "index"
     * @throws IndexOutOfBoundsException
     *             if "index" is negative or greater than the size of the RAM
     */
    public int read(int index) {
        Objects.checkIndex(index, size());
        return Byte.toUnsignedInt(data[index]);
    }

    /**
     * Write a given 8 bits value at a given position.
     * 
     * @param index
     *            the position where to write the value
     * @param value
     *            the 8 bits value to write
     * @throws IndexOutOfBoundsException
     *             if "index" is negative or greater than the size of the RAM
     * @throws IllegalArgumentException
     *             if "value" is not an 8 bits value
     */
    public void write(int index, int value) {
        Objects.checkIndex(index, size());
        byte b = Integer.valueOf(Preconditions.checkBits8(value)).byteValue();
        data[index] = b;
    }
}

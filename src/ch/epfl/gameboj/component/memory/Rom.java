package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Describe a read-only-memory (ROM) (mainly a table of bytes).
 * 
 * @author Corentin Junod (283214)
 */
public final class Rom {

    private final byte[] data;

    /**
     * Create a new ROM from given datas.
     * 
     * @param data
     *            a table that contains the datas to store in the ROM, not null
     * @throws NullPointerException
     *             if the table is null
     */
    public Rom(byte[] data) {
        Objects.requireNonNull(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the ROM size (in bytes).
     * @return the ROM size (in bytes)
     */
    public int size() {
        return this.data.length;
    }

    /**
     * Read the value at a given index.
     * 
     * @param index
     *            the index where to read the value
     * @return the value at "index"
     * @throws IndexOutOfBoundsException
     *             if the given index is negative or greater than the ROM size
     */
    public int read(int index) {
        Objects.checkIndex(index, size());
        return Byte.toUnsignedInt(this.data[index]);
    }

}

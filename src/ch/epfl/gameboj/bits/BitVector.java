package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * This class represents a vector of bits with a specified length
 * 
 * @author Corentin Junod (283214)
 */
 public final class BitVector {

    private final int[] dataTable;
    
    private enum extractType{
        ZERO_EXTENDED, WRAPPED;
    }

    /**
     * Create a new BitVector of a given size and filled with the startValue
     * 
     * @param size
     *            the size of the new BitVector
     * @param startValue
     *            the value used to initialize the vector
     * @throws IllegalArgumentException
     *             if the given size is negative or is not a multiple of 32
     */
    public BitVector(int size, boolean startValue) {
        requireIndexMultipleOf32(size);
        dataTable = new int[size / Integer.SIZE];
        Arrays.fill(dataTable, 0, dataTable.length, startValue ? -1 : 0);
    }

    /**
     * Create a new BitVector of a given size filled with zeros
     * 
     * @param size
     *            the size of the new BitVector
     * @throws IllegalArgumentException
     *            if the given size is negative or is not a multiple of 32
     */
    public BitVector(int size) {
        this(size, false);
    }
    
    private BitVector(int[] values) {
        dataTable = values;
    }

    /**
     * Return the number of bit contained in the vector
     * 
     * @return The vector's length in bits
     */
    public int size() {
        return sizeInInts() * Integer.SIZE;
    }

    /**
     * Test if the bit at a given index is set to 0 or 1
     * 
     * @param index
     *            the index of the bit to test
     * @return true if the tested bit is 1, false otherwise
     * @throws IndexOutOfBoundsException
     *             if the given index is negative or greater than the calling vector length
     */
    public boolean testBit(int index) {
        Objects.checkIndex(index, size());
        int extractedInt = dataTable[index / Integer.SIZE];
        return Bits.test(extractedInt, index % Integer.SIZE);
    }

    /**
     * Return a new BitVector which is the complement of the calling BitVector
     * 
     * @return the complement BitVector
     */
    public BitVector not() {
        int[] result = new int[sizeInInts()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ~dataTable[i];
        }
        return new BitVector(result);
    }
    
    /**
     * Return a new BitVector where every bit is the bitwise AND with the given vector
     * 
     * @param vector
     *            the BitVector with the one the AND must be calculated
     * @return a new BitVector containing the AND operation with the calling
     *         vector and the given vector
     * @throws NullPointerException
     *             if the given BitVector is null
     * @throws IllegalArgumentException
     *             if the given BitVector has not the same size as the calling BitVector
     */
    public BitVector and(BitVector vector) {
        Objects.requireNonNull(vector);
        Preconditions.checkArgument(vector.size() == size());

        int[] result = new int[sizeInInts()];
        for (int i = 0; i < result.length; i++) {
            result[i] = vector.dataTable[i] & dataTable[i];
        }
        return new BitVector(result);
    }
    
    /**
     * Return a new BitVector where every bit is the bitwise OR with the given vector
     * 
     * @param vector
     *            the BitVector with the one the OR must be calculated
     * @return a new BitVector containing the OR operation with the calling
     *         vector and the given vector
     * @throws NullPointerException
     *             if the given BitVector is null
     * @throws IllegalArgumentException
     *             if the given BitVector has not the same size as the calling BitVector
     */
    public BitVector or(BitVector vector) {
        Objects.requireNonNull(vector);
        Preconditions.checkArgument(vector.size() == size());

        int[] result = new int[sizeInInts()];
        for (int i = 0; i < result.length; i++) {
            result[i] = vector.dataTable[i] | dataTable[i];
        }
        return new BitVector(result);
    }
    
    /**
     * Extract the partial vector of a given length starting at a given position with a zero-extension
     * 
     * @param startIndex
     *            the starting index of the sub-vector
     * @param size
     *            the size of the new vector
     * @return a new BitVector which is a part of the original vector,
     *         zero-extended
     * @throws IllegalArgumentException
     *             if size is negative or not a multiple of 32
     */
    public BitVector extractZeroExtended(int startIndex, int size) {
        return new BitVector(extract(extractType.ZERO_EXTENDED, startIndex, size));
    }
    
    /**
     * Extract the partial vector of a given length starting at a given position with a wrapped-extension
     * 
     * @param startIndex
     *            the starting index of the sub-vector
     * @param size
     *            the size of the new vector
     * @return a new BitVector which is a part of the original vector with a
     *         wrapped-extension
     * @throws IllegalArgumentException
     *             if size is negative or not a multiple of 32
     */
    public BitVector extractWrapped(int startIndex, int size) {
        return new BitVector(extract(extractType.WRAPPED, startIndex, size));
    }
    
    /**
     * Shift a vector over a given distance
     * 
     * @param distance
     *            the distance of the shift, positive means left, negative means right
     * @return a new BitVector which is the calling vector shifted over the given distance
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-distance, size());
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return (that instanceof BitVector && dataTable.equals(((BitVector)that).dataTable));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override 
    public int hashCode() {
        return Objects.hash(dataTable);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < size(); i++) {
            result = (testBit(i) ? "1" : "0") + result;
        }
        return result;
    }
    
    
    /** Private functions **/
    
    private int sizeInInts() {
        return dataTable.length;
    }

    private int[] extract(extractType type, int startIndex, int size) {
        requireIndexMultipleOf32(size);

        int[] result = new int[size / Integer.SIZE];
        for (int i = 0; i < result.length; i++) {
            result[i] = getIntAt(type, startIndex + i * Integer.SIZE);
        }
        return result;
    }

    private int getIntAt(extractType type, int index) {
        int quotien = Math.floorDiv(index, Integer.SIZE);
        int rest    = Math.floorMod(index, Integer.SIZE);

        if (rest == 0) {
            switch (type) {
            case ZERO_EXTENDED:
                if (index < 0 || quotien >= sizeInInts())
                    return 0;
                else
                    return dataTable[quotien];
            case WRAPPED:
                return dataTable[Math.floorMod(quotien, sizeInInts())];
            default:
                throw new NullPointerException();
            }
        } else {
            int lowInt  = getIntAt(type, quotien * Integer.SIZE);
            int highInt = getIntAt(type, (quotien + 1) * Integer.SIZE);
            int breakIndex = Integer.SIZE - rest;
            return highInt << breakIndex | Bits.clip(breakIndex, lowInt >> rest);
        }
    }
    
    private static void requireIndexMultipleOf32(int index) {
        Preconditions.checkArgument(index > 0 && index % Integer.SIZE == 0);
    }
    
    
    /** Builder **/
    
    /** Represents a Builder to build a BitVector */
    public static final class Builder {
        
        private int[] dataTable;
        
        /**
         * Create a new Builder of BitVector
         * 
         * @param size
         *            the size of the new BitVector
         * @throws IllegalArgumentException
         *             if the given size is negative or is not a multiple of 32
         */
        public Builder(int size) {
            requireIndexMultipleOf32(size);
            dataTable = new int[size / Integer.SIZE];
            Arrays.fill(dataTable, 0, size / Integer.SIZE, 0);
        }
        
        /**
         * Set a given byte of the builder to a given value
         * 
         * @param index
         *            the byte's index
         * @param value
         *            the new byte's value
         * @return the modified bitVector
         * @throws IllegalStateException
         *             if the BitVector is already built
         * @throws IndexOutOfBoundsException
         *             if the given index is negative or greater than the number
         *             of bytes in the vector
         * @thorws IllegalArgumentException if the given value is not an 8 bits
         *         value
         */
        public Builder setByte(int index, int value) {
            if (dataTable == null) throw new IllegalStateException();
            
            Objects.checkIndex(index, dataTable.length * (Integer.SIZE / Byte.SIZE));
            Preconditions.checkBits8(value);

            int realIndex = index * Byte.SIZE;
            int rest = realIndex % Integer.SIZE;
            int quotien = realIndex / Integer.SIZE;

            int mask = ~(0b1111_1111 << rest);

            dataTable[quotien] = (dataTable[quotien] & mask) | (value << rest);

            return this;
        }
        
        /**
         * Build a new BitVector based on the calling builder
         * 
         * @return a new BitVector based on the calling builder
         * @throws IllegalStateException
         *             if the BitVector is already built
         */
        public BitVector build() {
            if (dataTable == null) throw new IllegalStateException();
            
            BitVector result = new BitVector(dataTable);
            dataTable = null;
            return result;
        }
    }

}

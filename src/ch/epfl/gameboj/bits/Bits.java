package ch.epfl.gameboj.bits;

import java.util.Objects;
import ch.epfl.gameboj.Preconditions;

/**
 * This class provides static methods to manipulate sequences of bits. 
 * All functions always return the values as integers.
 * 
 * @author Corentin Junod (283214)
 */

public final class Bits {

    private final static int[] reverse = { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0,
            0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0, 0x08,
            0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58, 0xD8,
            0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4, 0x64,
            0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4, 0x0C, 0x8C,
            0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C, 0x9C, 0x5C, 0xDC, 0x3C,
            0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2,
            0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2, 0x0A, 0x8A, 0x4A,
            0xCA, 0x2A, 0xAA, 0x6A, 0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA,
            0x7A, 0xFA, 0x06, 0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16,
            0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE,
            0x2E, 0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E,
            0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11, 0x91,
            0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49, 0xC9, 0x29,
            0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79, 0xF9,
            0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5, 0x15, 0x95, 0x55,
            0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D, 0x8D, 0x4D, 0xCD, 0x2D, 0xAD,
            0x6D, 0xED, 0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD, 0x03,
            0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3, 0x13, 0x93, 0x53, 0xD3,
            0x33, 0xB3, 0x73, 0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B,
            0xEB, 0x1B, 0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87,
            0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7, 0x37,
            0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF,
            0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };

    // This class is not instanciable
    private Bits() {}

    /**
     * Generate a mask filled with 0 and an only 1 at the given index.
     * 
     * @param index
     *            position of the 1 in the value
     * @return the mask as an int number
     * @throws IndexOutOfBoundsException
     *             if the index is negative or greater than Integer.SIZE
     */
    public static int mask(int index) {
        return (int) 1 << Objects.checkIndex(index, Integer.SIZE);
    }

    /**
     * Test if a particular bit in a bit sequence is set to 1.
     *
     * @param bits
     *            the sequence of bits
     * @param index
     *            index of the tested bit
     * @return true if the tested bit is 1, false otherwise
     * @throws IndexOutOfBoundsException
     *             if the index is negative or greater than Integer.SIZE
     */
    public static boolean test(int bits, int index) {
        Objects.checkIndex(index, Integer.SIZE);
        return (bits & mask(index)) != 0;
    }

    /**
     * Test if a particular bit in a bit sequence is set to 1.
     *
     * @param bits
     *            the sequence of bits
     * @param bit
     *            Bit that indicate the index of the bit to test
     * @return true if the tested bit is 1, false otherwise
     * @throws IndexOutOfBoundsException
     *             if the index is negative or greater than Integer.SIZE
     */
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.ordinal());
    }

    /**
     * Set a particular bit in a sequence of bits.
     *
     * @param bits
     *            the sequence of bits
     * @param index
     *            position of the bit that must be set
     * @param newValue
     *            new value of the bit (true = 1, false = 0)
     * @return the original sequence of bits in which the bit at position
     *         "index" is set to "newValue"
     * @throws IndexOutOfBoundsException
     *             if the index is negative or greater than Integer.SIZE
     */
    public static int set(int bits, int index, boolean newValue) {
        Objects.checkIndex(index, Integer.SIZE);
        int mask = mask(index);
        
        if (newValue)
            return bits | mask;
        else
            return bits & ~mask;
    }

    /**
     * Clip a sequence of bits from the right to a fixed size.
     *
     * @param size
     *            the new size (in bits) of the bits sequence
     * @param bits
     *            the bits sequence
     * @return the "size" first bits from the right of the original sequence,
     *         the others are set to 0
     * @throws IllegalArgumentException
     *             if "size" is negative or greater than Integer.SIZE
     */
    public static int clip(int size, int bits) {
        Preconditions.checkArgument(size >= 0 && size <= Integer.SIZE);
        if (size == 0) return 0;
        
        return (bits << (Integer.SIZE - size)) >>> (Integer.SIZE - size);
    }

    /**
     * Extract a sub-sequence of bits from an original sequence.
     *
     * @param bits
     *            the bits sequence
     * @param start
     *            the starting bit from the sub-sequence, from the right
     * @param size
     *            the size int bits of the sub-sequence
     * @return the extracted sequence with leading zeros
     * @throws IndexOutOfBoundsException
     *             if the parameters don't describe a valid sub-sequence
     */
    public static int extract(int bits, int start, int size) {
        Objects.checkFromIndexSize(start, size, Integer.SIZE);
        return clip(size, bits >>> start);
    }

    /**
     * Rotate the "size" first bits from a sequence over a fixed distance.
     * 
     * @param size
     *            the number of bits to extract and rotate, from the right
     * @param bits
     *            the bits sequence
     * @param distance
     *            the number of bits to shift the extracted sequence, a positive
     *            value shifts in the left direction, a negative value shifts in
     *            the right direction
     * @return the extracted and shifted sequence with leading zeros
     * @throws IllegalArgumentException
     *             if size is negative or greater than Integer.SIZE, or if the
     *             given sequence doesn't have "size" bits
     */
    public static int rotate(int size, int bits, int distance) {
        Preconditions.checkArgument(size > 0 && size <= Integer.SIZE);
        Preconditions.checkArgument(clip(size, bits) == bits);

        distance = Math.floorMod(distance, size);
        return clip(size, (bits << distance) | (bits >>> (size - distance)));
    }

    /**
     * Extend the sign of an 8 bit value encoded in two's complement. If the
     * value is positive, fill the leading bits with 0, is the value is
     * negative, fill the leading bits with 1.
     * 
     * @param b
     *            the 8 bit value to extend
     * @return the given value with it's sign extended
     * @throws IllegalArgumentException
     *             if the given value is not an 8 bit value
     */
    public static int signExtend8(int b) {
        return (int) ((byte) Preconditions.checkBits8(b));
    }

    /**
     * Reverse the given value by swapping the bits 0 and 7, 1 and 6, 2 and 5, 3 and 4.
     * 
     * @param b
     *            the 8 bit value
     * @return the given value, but reversed
     * @throws IllegalArgumentException
     *             if the given value is not an 8 bit value
     */
    public static int reverse8(int b) {
        return Bits.reverse[Preconditions.checkBits8(b)];
    }

    /**
     * Swap the value of each bit of the given 8 bit value.
     * 
     * @param b
     *            the 8 bit value from which the complement must be taken
     * @return the complement of the 8 bit value
     * @throws IllegalArgumentException
     *             if the given value is not an 8 bit value
     */
    public static int complement8(int b) {
        return Preconditions.checkBits8(b) ^ 0b1111_1111;
    }

    /**
     * Make a 16 bit value from two 8 bit values.
     * 
     * @param highB
     *            the 8 bit value that will be the 8 most significant bits
     * @param lowB
     *            the 8 bit value that will be the 8 least significant bits
     * @return a 16 bits value made from highB and lowB
     * @throws IllegalArgumentException
     *             if the given values are not 8 bit values
     */
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(highB);
        Preconditions.checkBits8(lowB);
        return (int) lowB | (highB << 8);
    }
}

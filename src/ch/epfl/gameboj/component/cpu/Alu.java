package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * This class provides static functions used in Cpu to simulate Gameboy's 16
 * bits operations and raised flags.
 * Note : All functions in this class return an integer corresponding to the 
 * result value and the ZNHC flags packed together.
 * 
 * @author Corentin Junod (283214)
 */
public final class Alu {

    // As this function provides only static methods, it is not instanciable
    private Alu(){}

    /**
     *  Represents the possible raised flags after an operation
     */
    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, // Unused bits
        C, // True iff there is a carry in the operation
        H, // True iff there is a carry in the first 4-bits operation
        N, // True iff the operation is a subtraction
        Z  // True iff the result = 0
    }

    /**
     *  Represents the possible directions for a rotation
     */
    public enum RotDir {
        LEFT, RIGHT
    }

    /**
     * Generate a mask with the given ZNHC parameters.
     * 
     * @param z
     *            if true, the corresponding bit in the return value (8th) will be 1, 0 otherwise
     * @param n
     *            if true, the corresponding bit in the return value (7th) will be 1, 0 otherwise
     * @param h
     *            if true, the corresponding bit in the return value (6th) will be 1, 0 otherwise
     * @param c
     *            if true, the corresponding bit in the return value (5th) will be 1, 0 otherwise
     * @return an integer where the bits at indexes 4 to 7 are set according to
     *         the parameters z,n,h and c
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        return (z ? Flag.Z.mask() : 0) + (n ? Flag.N.mask() : 0)
              +(h ? Flag.H.mask() : 0) + (c ? Flag.C.mask() : 0);
    }

    /**
     * Unpack the value of a pack value+ZNHC.
     * 
     * @param valueFlags
     *            the packed value+ZNHC
     * @return the value that was contained in "valueFlags"
     * @throws IllegalArgumentException
     *             if "valueFlags" is not a valid pack value+ZNHC
     */
    public static int unpackValue(int valueFlags) {
        Preconditions.checkBits16(valueFlags >>> 8);
        Preconditions.checkArgument(Bits.clip(4, valueFlags) == 0);
        return Bits.clip(16, valueFlags >>> 8);
    }

    /**
     * Unpack the flags of a pack value+ZNHC.
     * 
     * @param valueFlags
     *            the packed value+ZNHC
     * @return the flags that were contained in "valueFlags"
     * @throws IllegalArgumentException
     *             if "valueFlags" is not a valid pack value+ZNHC
     */
    public static int unpackFlags(int valueFlags) {
        Preconditions.checkBits16(valueFlags >>> 8);
        Preconditions.checkArgument(Bits.clip(4, valueFlags) == 0);
        return Bits.clip(8, valueFlags);
    }

    /**
     * Perform an 8 bits addition on two numbers and a carry.
     * 
     * @param l
     *            the first 8 bits number
     * @param r
     *            the second 8 bits number
     * @param c0
     *            the carry, if true add one to the addition, if false does nothing
     * @return the packed value of the flags ZNHC and the 8 bits addition of
     *         "l", "r" and "c0"
     * @throws IllegalArgumentException
     *             if "l" or "r" are not 8 bits values
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        boolean h = (Bits.clip(4, l) + Bits.clip(4, r) + (c0 ? 1 : 0)) > 0xF;
        boolean c = (l + r + (c0 ? 1 : 0)) > 0xFF;
        boolean z = Bits.clip(8, (l + r + (c0 ? 1 : 0))) == 0;

        return packValueZNHC(Bits.clip(8, l + r + (c0 ? 1 : 0)), z, false, h, c);
    }
    
    /**
     * Perform an 8 bits subtraction on two numbers and a carry
     * 
     * @param l
     *            the first 8 bits number
     * @param r
     *            the second 8 bits number
     * @param c0
     *            the carry, if true subtract one to the subtraction, if false does nothing
     * @return the packed value of the flags ZNHC and the 8 bits subtraction of
     *         "l", "r" and "c0"
     * @throws IllegalArgumentException
     *             if "l" or "r" are not 8 bits values
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);

        boolean z = Bits.clip(8, l - r - (b0 ? 1 : 0)) == 0;
        boolean c = r + (b0 ? 1 : 0) > l;
        boolean h = Bits.clip(4, r) + (b0 ? 1 : 0) > Bits.clip(4, l);

        return packValueZNHC(Bits.clip(8, l - r - (b0 ? 1 : 0)), z, true, h, c);
    }

    /**
     * Perform an 8 bits addition on two numbers.
     * 
     * @param l
     *            the first 8 bits number
     * @param r
     *            the second 8 bits number
     * @return the packed value of the flags ZNHC and the 8 bits addition of "l" and "r"
     * @throws IllegalArgumentException
     *             if "l" or "r" are not 8 bits values
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }

    /**
     * Perform an 8 bits subtraction on two numbers
     * 
     * @param l
     *            the first 8 bits number
     * @param r
     *            the second 8 bits number
     * @return the packed value of the flags ZNHC and the 8 bits subtraction of
     *         "l" and "r"
     * @throws IllegalArgumentException
     *             if "l" or "r" are not 8 bits values
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * Add two 16 bits numbers, the flags come from the addition of the 8 least
     * significant bits.
     * 
     * @param l
     *            the first 16 bits number
     * @param r
     *            the second 16 bits number
     * @return the packed value of the flags ZNHC and the 16 bits addition of "l" and "r"
     * @throws IllegalArgumentException
     *             if "l" or "r" are not 16 bits values
     */
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);

        boolean h = (Bits.clip(4, l) + Bits.clip(4, r)) > 0xF;
        boolean c = (Bits.clip(8, l) + Bits.clip(8, r)) > 0xFF;
        return packValueZNHC(Bits.clip(16, l + r), false, false, h, c);
    }

    /**
     * Add two 16 bits numbers, the flags come from the addition of the 8 most
     * significant bits.
     * 
     * @param l
     *            the first 16 bits number
     * @param r
     *            the second 16 bits number
     * @return the packed value of the flags ZNHC and the 16 bits addition of "l" and "r"
     * @throws IllegalArgumentException
     *             if "l" or "r" are not 16 bits values
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);

        boolean h = (Bits.clip(12, l) + Bits.clip(12, r)) > 0xFFF;
        boolean c = (Bits.clip(16, l) + Bits.clip(16, r)) > 0xFFFF;
        return packValueZNHC(Bits.clip(16, l + r), false, false, h, c);
    }

    /**
     * Return the binary coded decimal (BCD) of a given positive value smaller than 256.
     * 
     * @param v
     *            the value from which the BCD must be calculated
     * @param n
     *            the N flag from the previous operation
     * @param h
     *            the H flag from the previous operation
     * @param c
     *            the C flag from the previous operation
     * @return the packed value of the flags ZNHC and the value "v" but coded in
     *         binary coded decimal
     * @throws IllegalArgumentException
     *             if v is negative or greater than 0xFF
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkArgument(v >= 0 && v <= 0xFF);

        boolean fixL = h || (!n && Bits.clip(4, v) > 9);
        boolean fixH = c || (!n && v > 0x99);

        int fix = 0x60 * (fixH ? 1 : 0) + 0x06 * (fixL ? 1 : 0);

        if (n) v = v - fix;
        else   v = v + fix;

        return packValueZNHC(Bits.clip(8, v), Bits.clip(8, v) == 0, n, false, fixH);
    }

    /**
     * Calculate the bitwise AND of two 8 bits values.
     * 
     * @param l
     *            the first 8 bits value
     * @param r
     *            the second 8 bits value
     * @return the packed value of the flags ZNHC and the bitwise operation AND
     *         of the two given values
     * @throws IllegalArgumentException
     *             if the two parameters are not 8 bits values
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        return packValueZNHC(l & r, (l & r) == 0, false, true, false);
    }

    /**
     * Calculate the bitwise OR of two 8 bits values.
     * 
     * @param l
     *            the first 8 bits value
     * @param r
     *            the second 8 bits value
     * @return the packed value of the flags ZNHC and the bitwise operation OR
     *         of the two given values
     * @throws IllegalArgumentException
     *             if the two parameters are not 8 bits values
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        return packValueZNHC(l | r, (l | r) == 0, false, false, false);
    }

    /**
     * Calculate the bitwise XOR of two 8 bits values.
     * 
     * @param l
     *            the first 8 bits value
     * @param r
     *            the second 8 bits value
     * @return the packed value of the flags ZNHC and the bitwise operation XOR
     *         of the two given values
     * @throws IllegalArgumentException
     *             if the two parameters are not 8 bits values
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        return packValueZNHC(l ^ r, (l ^ r) == 0, false, false, false);
    }

    /**
     * Shift all bits of a value to the left. The new least significant bit is
     * set to 0.
     * 
     * @param v
     *            the value
     * @return the packed value of the flags ZNHC and the value "v" but with all
     *         bits shifted one bit to the left
     * @throws IllegalArgumentException
     *             if the given value is not an 8 bits value
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        int result = Bits.clip(8, v << 1);
        return packValueZNHC(result, result == 0, false, false, Bits.test(v, 7));
    }

    /**
     * Shift all bits of a value to the right, in an arithmetic way. The new
     * most significant bit is the same as the previous one.
     * 
     * @param v
     *            the value
     * @return the packed value of the flags ZNHC and the value "v" but with all
     *         bits shifted one bit to the right
     * @throws IllegalArgumentException
     *             if the given value is not an 8 bits value
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        boolean c = Bits.test(v, 0);
        v = ((v << 24) >> 1) >>> 24;
        return packValueZNHC(v, v == 0, false, false, c);
    }

    /**
     * Shift all bits of a value to the right, in a logic way. The new least
     * significant is always 0.
     * 
     * @param v
     *            the value
     * @return the packed value of the flags ZNHC and the value "v" but with all
     *         bits shifted one bit to the right
     * @throws IllegalArgumentException
     *             if the given value is not an 8 bits value
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        boolean c = Bits.test(v, 0);
        v = v >>> 1;
        return packValueZNHC(v, v == 0, false, false, c);
    }

    /**
     * Rotate all bits in a given value by one bit.
     * 
     * @param d
     *            a value of a enumeration that indicates the direction of the rotation
     * @param v
     *            the value from which the bits must be rotated
     * @return the packed value of the flags ZNHC and the given value "v" but
     *         with all bits rotated in the direction "d"
     * @throws IllegalArgumentException
     *             if the given value "v" is not an 8 bits value
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);

        boolean c = false;
        if (d == RotDir.LEFT) {
            c = Bits.test(v, 7);
            v = Bits.rotate(8, v, 1);
        } else {
            c = Bits.test(v, 0);
            v = Bits.rotate(8, v, -1);
        }
        return packValueZNHC(v, v == 0, false, false, c);
    }

    /**
     * Rotate through a carry all bits in a given value by one bit.
     * 
     * @param d
     *            a value of a enumeration that indicates the direction of the rotation
     * @param v
     *            the value from which the bits must be rotated
     * @param c
     *            the carry used in the rotation
     * @return the packed value of the flags ZNHC and the given value "v" but
     *         with all bits rotated through a carry in the direction "d"
     * @throws IllegalArgumentException
     *             if the given value "v" is not an 8 bits value
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);

        if (c) v += Bits.mask(8);
        
        if (d == RotDir.LEFT) 
            v = Bits.rotate(9, v, 1);
        else                  
            v = Bits.rotate(9, v, -1);

        c = Bits.test(v, 8);
        if (c) v -= Bits.mask(8);
        
        return packValueZNHC(Bits.clip(8, v), v == 0, false, false, c);
    }

    /**
     * Swap the 4 least significant bits with the 4 most significant bits of a 8
     * bits value.
     * 
     * @param v
     *            the value
     * @return the packed value of the flags ZNHC and the value "v" but with
     *         it's 4 LSB and it's 4 MSB swapped
     * @throws IllegalArgumentException
     *             if the given value is not a 8 bits value
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        v = Bits.rotate(8, v, 4);
        return packValueZNHC(v, v == 0, false, false, false);
    }

    /**
     * Test the value of a given bit in a 8 bits given value.
     * 
     * @param v
     *            the value containing the bit to test
     * @param bitIndex
     *            the index of the bit that must be tested (starting from 0)
     * @return the packed value of the flags ZNHC and 0. The flag Z is true if
     *         the tested bit is 0, false otherwise
     * @throws IllegalArgumentException
     *             if the given value "v" is not a 8 bits value
     * @throws IndexOutOfBoundsException
     *             if "bitIndex" is smaller than 0 or greater than 7
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, 8);
        return packValueZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }

    // Pack a value and ZNHC parameters into one integer
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h, boolean c) {
        Preconditions.checkBits16(v);
        return (Bits.clip(16, v) << 8) + maskZNHC(z, n, h, c);
    }
    
    
    // This function was used for JUnit tests
    public static int _testPackValueZNHC(int v, boolean z, boolean n, boolean h, boolean c) {
        return packValueZNHC(v, z, n, h, c);
    }
}

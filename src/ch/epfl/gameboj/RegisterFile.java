package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * This class stores values of a Register.
 * 
 * @author Corentin Junod (283214)
 */
public final class RegisterFile<E extends Register> {

    private final byte registersData[];

    /**
     * Assign values to a given enum of registers. Must be called with the
     * function values() of an enum of registers.
     * 
     * @param allRegs
     *            the keys for which a value must be assigned
     */
    public RegisterFile(E[] allRegs) {
        registersData = new byte[allRegs.length];
    }

    /**
     * Return the value assigned to a register.
     * 
     * @param reg
     *            the register from which the value must be returned
     * @return the value of register "reg"
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(registersData[reg.index()]);
    }

    /**
     * Define a new 8 bits value for a given register.
     * 
     * @param reg
     *            the register to which a new value must be assigned
     * @param newValue
     *            the new value to assign
     * @throws IllegalArgumentException
     *             if "newValue" is not an 8 bits value
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        registersData[reg.index()] = Integer.valueOf(newValue).byteValue();
    }

    /**
     * Test a given bit of the value contained on a given register.
     * 
     * @param reg
     *            the register that contains the value to test
     * @param b
     *            a Bit that describe the bit to test
     * @return true if the tested bit is 1, false otherwise
     */
    public boolean testBit(E reg, Bit b) {
        return (get(reg) & b.mask()) != 0;
    }

    /**
     * Set a given bit to a given value on a given register.
     * 
     * @param reg
     *            the register from which the value must be modified
     * @param bit
     *            a Bit the describe the bit to modify
     * @param newValue
     *            the new value (true = 1, false = 0) of the bit
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        int newByte = Bits.set(get(reg), bit.index(), newValue);
        set(reg, Integer.valueOf(newByte).byteValue());
    }
}

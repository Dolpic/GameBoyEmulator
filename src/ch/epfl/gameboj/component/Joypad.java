package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * This class emulates a Gameboy Joypad
 * 
 * @author Corentin Junod (283214)
 */
public final class Joypad implements Component{
    
    private static final int KEY_TABLE_WIDTH  = 4;
    private static final int KEY_TABLE_HEIGHT = 2;
    
    private static final int P1_WRITE_MASK = 0b0011_0000;
    private static final int P1_UPDATE_MASK = 0b0000_1111;
    
    private int[] keysPressedLine;
    
    /** All the keys in a Gameboy Joypad */
    public enum Key implements Bit{
        RIGHT, LEFT, UP, DOWN, 
        A, B, SELECT, START
    }
    
    private int p1;
    private enum P1 implements Bit{
        COL0, COL1, COL2, COL3, LINE0, LINE1, UNUSED0, UNUSED1
    }
    
    private final Cpu cpu;
    
    /**
     * Creates a new Joypad based on a given CPU
     * 
     * @param cpu
     *            the cpu of the gameboy, used to raise interruptions
     * @throws IllegalArgumentException
     *             if the given cpu is null
     */
    public Joypad(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        p1 = 0;
        keysPressedLine = new int[KEY_TABLE_HEIGHT];
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(address == AddressMap.REG_P1) 
            return Bits.complement8(p1);
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
        if(address == AddressMap.REG_P1) {
            p1 = (p1 & ~P1_WRITE_MASK) | (Bits.complement8(data) & P1_WRITE_MASK);
            updateState();
        }
    }
    
    /**
     * Set a key to a given position (pressed, unpressed)
     * 
     * @param key
     *            The key to set
     * @param isPressed
     *            true if the key must be pressed, false otherwise
     * @throws NullPointerException
     *             if the given key is null
     */
    public void setKey(Key key, boolean isPressed) {
        Objects.requireNonNull(key);
        int line  = Math.floorDiv(key.index(), KEY_TABLE_WIDTH);
        int index = Math.floorMod(key.index(), KEY_TABLE_WIDTH);
        keysPressedLine[line] = Bits.set(keysPressedLine[line], index, isPressed);
        updateState();
    }
    
    private void updateState() {
        int previousState = Bits.extract(p1, 0, KEY_TABLE_WIDTH);
        int line1 = Bits.test(p1, P1.LINE0)?keysPressedLine[0]:0;
        int line2 = Bits.test(p1, P1.LINE1)?keysPressedLine[1]:0;
        int newState = line1 | line2;
        
        p1 = (p1 & ~P1_UPDATE_MASK) | (newState & P1_UPDATE_MASK);
        
        if(newState != previousState) 
            cpu.requestInterrupt(Interrupt.JOYPAD);
    }

}

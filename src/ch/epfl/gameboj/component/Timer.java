package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Simulate a Gameboy timer.
 * A timer raise at parametrable time interruptions on the CPU.
 * 
 * @author Corentin Junod (283214)
 */
public final class Timer implements Component, Clocked {

    /** The number of ticks per cycle **/
    private static final int TICKS_PER_CYCLE = 4;

    private final Cpu cpu;
    private int mainTimer;
    private int tima; // secondary timer
    private int tma;  // reset value for secondary timer
    private int tac;  // options register

    /**
     * Create a new Timer associated with a Cpu in order the raise interruptions.
     * 
     * @param cpu
     *            the cpu to be associated with, not null
     * @throws NullPointerException
     *             if "cpu" is null
     */
    public Timer(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        mainTimer = 0;
        tima = 0;
        tma = 0;
        tac = 0;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        switch (address) {
            case AddressMap.REG_DIV:  return Bits.extract(mainTimer, 8, 8);
            case AddressMap.REG_TIMA: return tima;
            case AddressMap.REG_TMA:  return tma;
            case AddressMap.REG_TAC:  return tac;
            default: return NO_DATA;
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        boolean previousState = state();

        switch (address) {
        case AddressMap.REG_DIV:
            mainTimer = 0;
            incIfChange(previousState);
            break;
        case AddressMap.REG_TIMA:
            tima = data;
            break;
        case AddressMap.REG_TMA:
            tma = data;
            break;
        case AddressMap.REG_TAC:
            tac = data;
            incIfChange(previousState);
            break;
        }
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        boolean previousState = state();
        mainTimer = Bits.clip(16, mainTimer + TICKS_PER_CYCLE);
        incIfChange(previousState);
    }

    private boolean state() {
        int mainTimerBit = 0;

        switch (Bits.clip(2, tac)) {
        case 0b00:
            mainTimerBit = 9;
            break;
        case 0b01:
            mainTimerBit = 3;
            break;
        case 0b10:
            mainTimerBit = 5;
            break;
        case 0b11:
            mainTimerBit = 7;
            break;
        }
        return Bits.test(tac, 2) && Bits.test(mainTimer, mainTimerBit);
    }

    private void incIfChange(boolean previousState) {
        if (previousState && !state()) {
            if (tima == 0xFF) {
                cpu.requestInterrupt(Interrupt.TIMER);
                tima = tma;
            } else {
                tima++;
            }
        }
    }
}

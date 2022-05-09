package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * This class represents a memory bank controller for a Cartridge ROM of type 0.
 * Note : a Cartridge ROM of type 0 is 32768 bytes long and contains 0 at address 0x147.
 * 
 * @author Corentin Junod (283214)
 */
public final class MBC0 implements Component {

    private final Rom rom;

    /**
     * Create a new Memory Bank Controller from a given ROM.
     * 
     * @param rom
     *            the ROM from which a MBC must be constructed, not null
     * @throws NullPointerException
     *             if the parameter is null
     * @throws IllegalArgumentException
     *             if the given ROM is not 32768 bytes long
     */
    public MBC0(Rom rom) {
        Preconditions.checkArgument(rom.size() == Cartridge.ROM_0_SIZE);
        this.rom = Objects.requireNonNull(rom);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < Cartridge.ROM_0_SIZE)
            return rom.read(address);
        else
            return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        // Nothing to do, a ROM can't be written
    }
}

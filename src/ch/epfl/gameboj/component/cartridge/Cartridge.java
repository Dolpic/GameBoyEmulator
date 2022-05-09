package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * This class represents a Gameboy Cartridge created from a File that contains
 * the bytes from the original Cartridge.
 * 
 * @author Corentin Junod (283214)
 */
public final class Cartridge implements Component {
    
    /** The size of a MBC0 cartridge **/
    public static final int ROM_0_SIZE = 32768;
    
    private static final int ADDRESS_TYPE_CARTRIDGE = 0x147;
    private static final int RAM_SIZE_ADDRESS = 0x149;
    private static final int[] RAM_SIZE = {0, 2048, 8192, 32768};
    
    private final Component MBC;
    
    /**
     * This function create a Cartridge based on a given File.
     * 
     * @param romFile
     *            the File that represents the ROM file, not null
     * @return a Cartridge that contains "romFile"
     * @throws IOException
     *             if "romFile" is null or invalid
     * @throws IllegalArgumentException
     *             if the Cartridge's type is not valid
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        if (romFile == null) throw new IOException();

        try (InputStream stream = new FileInputStream(romFile)) {
            byte[] data = stream.readAllBytes();
            
            switch(data[ADDRESS_TYPE_CARTRIDGE]) {
            case 0:
                return new Cartridge(new MBC0(new Rom(data)));
            case 1:
            case 2:
            case 3:
                return new Cartridge(new MBC1(new Rom(data),RAM_SIZE[data[RAM_SIZE_ADDRESS]]));
            default:
                throw new IllegalArgumentException();
            }
        }
    }
    
    private Cartridge(Component MBC) {
        this.MBC = MBC;
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return MBC.read(address);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        MBC.write(address, data);
    }
}

package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * This class is used to simulate the Gameboy behaviour. 
 * When the gameboy starts, the bootRom is visible but as soon as 
 * something is written at REG_BOOT_ROM_DISABLE, the gameboy masks the bootRom.
 * 
 * @author Corentin Junod (283214)
 */
public final class BootRomController implements Component {

    private final Cartridge cartridge;
    private boolean isBootRomVisible;

    /**
     * Create a new BootRomController based on a cartridge.
     * 
     * @param cartridge
     *            the cartridge to use, not null
     * @throws NullPointerException
     *             if the cartridge is null
     */
    public BootRomController(Cartridge cartridge) {
        this.cartridge = Objects.requireNonNull(cartridge);
        isBootRomVisible = true;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (isBootRomVisible && address >= 0 && address <= 0xFF) 
            return Byte.toUnsignedInt(BootRom.DATA[address]);
        else 
            return cartridge.read(address);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        cartridge.write(address, data);
        if (address == AddressMap.REG_BOOT_ROM_DISABLE) 
            isBootRomVisible = false;
    }
}

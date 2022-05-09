package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
import ch.epfl.gameboj.gui.ImageConverter;
import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;

/**
 * Simulate a whole Gameboy running a game. 
 * Note : At this point, this emulator only support cartridge 
 * from type 0 (32768 bit length), see Cartridge/Cartridge.java
 * 
 * @author Corentin Junod (283214)
 */
/**
 * @author Corentin
 *
 */
public final class GameBoy {
    
    /** Returns the number of cycles per second in a Gameboy */
    public static final long   CYCLES_PER_SECOND = (long) Math.pow(2, 20); 
    /** Returns the number of cycles per nanosecond in a Gameboy */
    public static final double CYCLES_PER_NANOSECOND = CYCLES_PER_SECOND / 1e9;

    private long currentCycle = 0;

    private Bus bus;
    private BootRomController bootRomController;
    private Cpu cpu;
    private LcdController lcdController;
    private final  Timer timer;
    private final Joypad joypad;
    private final RamController workRamController;
    private final RamController echoRamController;
    
    private AnimationTimer animTimer;
    private double currentSpeed;
    private long baseTime;
    private long startTime;
    private int[] currentColorMap;
    
    /**
     * The default color map for the Gameboy, black and white
     */
    public final static int[] DEFAULT_COLOR_MAP = new int[] { 
        0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 
    };

    /**
     * Create a new Gameboy without any cartridge.
     */
    public GameBoy() {
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        workRamController = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        echoRamController = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        cpu               = new Cpu();
        timer             = new Timer(cpu);
        joypad            = new Joypad(cpu);
        lcdController     = new LcdController(cpu);
        bus               = new Bus();
        currentSpeed = 1;
        baseTime = 0;
        currentColorMap = DEFAULT_COLOR_MAP;
    }
    
    /**
     * Start the GameBoy with a given cartridge on a given screen
     * 
     * @param cartridge
     *            The cartridge to start
     * @param screen
     *            The ImageView that must be updated as the Gameboy run
     */
    public void start(Cartridge cartridge, ImageView screen) {
        if (animTimer != null)
            stop();

        startTime = System.nanoTime();
        baseTime = 0;
        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedTime = baseTime + (now - startTime) * currentSpeed;
                runUntil( (long) (GameBoy.CYCLES_PER_NANOSECOND * elapsedTime));
                screen.setImage(ImageConverter.convert( lcdController.currentImage(), currentColorMap));
            }
        };

        insertCartridge(cartridge);
        animTimer.start();
    }
    
    
    /**
     * Stop the gameboy
     */
    public void stop() {
        animTimer.stop();
    }
    
    /**
     * Set a new speed factor for the gameboy
     * 
     * @param newSpeed
     *            The new speed factor
     */
    public void setSpeed(double newSpeed) {
        double previousSpeed = currentSpeed;
        currentSpeed = newSpeed;
        baseTime += (System.nanoTime()-startTime)*previousSpeed;
        startTime = System.nanoTime();
    }
    
    /**
     * Set a new color map for the gameboy
     * 
     * @param newColorMap
     *            The new color map for the gameboy
     */
    public void setColorMap(int[] newColorMap) {
        Preconditions.checkArgument(newColorMap.length == 4);
        currentColorMap = newColorMap.clone();
    }

    /**
     * Return the Bus created by the Gameboy.
     * @return the Gameboy Bus
     */
    public Bus bus() {
        return bus;
    }

    /**
     * Return the CPU created by the Gameboy.
     * @return the Gameboy CPU
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * Return the Timer created by the Gameboy.
     * @return the Gameboy Timer
     */
    public Timer timer() {
        return timer;
    }
    
    /**
     * Return the Joypad created by the Gameboy.
     * @return the Gameboy Joypad
     */
    public Joypad joypad() {
        return joypad;
    }
    
    /**
     * Return the LcdController created by the Gameboy.
     * @return the Gameboy LcdController
     */
    public LcdController lcdController() {
        return lcdController;
    }

    /**
     * Returns the number of already executed cycles on the Gameboy.
     * @return the number of already executed cycles on the Gameboy
     */
    public long cycles() {
        return currentCycle;
    }
    
    /**
     * Returns the current speed factor of the Gameboy
     * @return the current speed factor of the Gameboy
     */
    public double currentSpeed() {
        return currentSpeed;
    }
    
    /**
     * Returns the current color map of the Gameboy
     * @return the current color map of the Gameboy
     */
    public int[] currentColorMap() {
        return currentColorMap.clone();
    }
    
    /**
     * Run the Gameboy until a given clock cycle.
     * 
     * @param cycle
     *            the cycle until the Gameboy must run
     * @throws IllegalArgumentException
     *             if the given cycle is smaller than the last executed cycle
     */
    private void runUntil(long cycle) {
        Preconditions.checkArgument(currentCycle <= cycle);
        if(bootRomController == null) throw new IllegalStateException();
        
        while (currentCycle < cycle) {
            timer.cycle(currentCycle);
            lcdController.cycle(currentCycle);
            cpu.cycle(currentCycle);
            currentCycle++;
        }
    }
    
    /**
     * Insert a new Gameboy without any cartridge.
     * 
     * @param cartridge
     *            the cartridge containing the game to be run by the gameboy, not null
     * @throws NullPointerException
     *             if "cartridge" is null
     */
    private void insertCartridge(Cartridge cartridge) {
        currentCycle = 0;
        bootRomController = new BootRomController(Objects.requireNonNull(cartridge));
        cpu = new Cpu();
        bus = new Bus();
        lcdController = new LcdController(cpu);
        
        bootRomController.attachTo(bus);
        workRamController.attachTo(bus);
        echoRamController.attachTo(bus);
        cpu.attachTo(bus);
        timer.attachTo(bus);
        joypad.attachTo(bus);
        lcdController.attachTo(bus);
    }
}

package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * This class represents a LCD Controller of a Gameboy 
 * 
 * @author Corentin Junod (283214)
 */
public final class LcdController implements Component, Clocked{
    
    /** The width of the Gameboy screen, in pixels */
    public static final int LCD_WIDTH  = 160;
    /** The height of the Gameboy screen, in pixels */
    public static final int LCD_HEIGHT = 144;
    
    private static final int MODE0_DURATION = 51;  //Horizontal blank
    private static final int MODE1_DURATION = 114; //Vertical blank
    private static final int MODE2_DURATION = 20;
    private static final int MODE3_DURATION = 43;
    
    private static final int ADDITIONNAL_BLANK_LINES = 10;
    
    private static final int IMAGE_SIZE  = 256;
    private static final int TILE_SIZE = 8;
    private static final int TILE_PER_LINE = IMAGE_SIZE/TILE_SIZE;
    private static final int TILE_ADDRESS_ALWAYS_ACCESSIBLE = 0x80;
    
    private static final int NUMBER_OF_SPRITE = 40;
    private static final int MAX_SPRITES_ON_SCREEN = 10;
    private static final int BG_SPRITES = 0;
    private static final int FG_SPRITES = 1;
    
    private static final int LY_START_VALUE = 0b11111111;
    
    private static final int STAT_WRITE_MASK = 0b1111_1000;
    
    private static final int DMA_DISABLED_VALUE = AddressMap.OAM_RAM_SIZE;

    private final Cpu cpu;
    private final Ram videoRam;
    private final Ram OAM;
    private Bus bus;
    private LcdImage currentImage;
    private LcdImage.Builder imageBuilder;
    
    private enum MODE{
        MODE0, MODE1, MODE2, MODE3
    }
    
    private final boolean[][] MODES_TABLE = {
        {false,false},{false,true},{true,false},{true,true}
    };
    
    private final RegisterFile<Reg> regFile;
    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }
    
    private enum LCDC implements Bit{
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }
    
    private enum STAT implements Bit{
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED
    }
    
    private enum SPRITE{
        Y, X, TILE, PARAMS
    }
    
    private enum SPRITE_PARAM implements Bit{
        UNUSED0, UNUSED1, UNUSED2, UNUSED3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }
    
    private long nextNonIdleCycle;
    private MODE nextMode;
    
    private int skippedWindowLines, currentDMACycle;
    
    /**
     * Create a new LcdController for a Gameboy
     * 
     * @param cpu
     *            The cpu of the gameboy
     * @throws NullPointerException
     *             if the given cpu is null
     */
    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        regFile  = new RegisterFile<>(Reg.values());
        videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        OAM      = new Ram(AddressMap.OAM_RAM_SIZE);

        imageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
        currentImage = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();

        nextNonIdleCycle = Long.MAX_VALUE;
        nextMode = MODE.MODE2;
        currentDMACycle = DMA_DISABLED_VALUE;
        skippedWindowLines = 0;

        setLyOrLyc(Reg.LY, LY_START_VALUE);
    }
    
    /**
     * Returns the current image that appears on the Gameboy screen
     * @return The current image that appears on the Gameboy screen
     */
    public LcdImage currentImage() {
        return currentImage;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (nextNonIdleCycle == Long.MAX_VALUE && regFile.testBit(Reg.LCDC, LCDC.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
            nextMode = MODE.MODE2;
        }

        if (currentDMACycle < DMA_DISABLED_VALUE) {
            int data = bus.read( Bits.make16(regFile.get(Reg.DMA), 0) + currentDMACycle);
            write(AddressMap.OAM_START + currentDMACycle, data);
            currentDMACycle++;
        }

        if (cycle == nextNonIdleCycle)
            reallyCycle(cycle);
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (isBetween(address, AddressMap.REGS_LCDC_START, AddressMap.REGS_LCDC_END))
            return regFile.get(Reg.values()[address - AddressMap.REGS_LCDC_START]);
        else if (isBetween(address, AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END))
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        else if (isBetween(address, AddressMap.OAM_START, AddressMap.OAM_END))
            return OAM.read(address - AddressMap.OAM_START);
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
        if (isBetween(address, AddressMap.REGS_LCDC_START, AddressMap.REGS_LCDC_END))
            writeInRegsLCDC(Reg.values()[address - AddressMap.REGS_LCDC_START], data);
        else if (isBetween(address, AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END))
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        else if (isBetween(address, AddressMap.OAM_START, AddressMap.OAM_END))
            OAM.write(address - AddressMap.OAM_START, data);
    }
     
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = Objects.requireNonNull(bus);
        bus.attach(this);
    }
    
    
    /*** Private functions ****/
    
    private void reallyCycle(long cycle) {
        switch (nextMode) {
        case MODE2:
            setLyOrLyc(Reg.LY, Bits.clip(Byte.SIZE, currentLine() + 1));
            setMode(MODE.MODE2);
            nextMode = MODE.MODE3;
            nextNonIdleCycle += MODE2_DURATION;
            break;
        case MODE3:
            setMode(MODE.MODE3);
            computeLine();
            nextMode = MODE.MODE0;
            nextNonIdleCycle += MODE3_DURATION;
            break;
        case MODE0: // Horizontal blank
            setMode(MODE.MODE0);
            if (currentLine()+1 == LCD_HEIGHT) {
                prepareNewImage();
                nextMode = MODE.MODE1;
                setMode(MODE.MODE1);
            } else {
                nextMode = MODE.MODE2;
            }
            nextNonIdleCycle += MODE0_DURATION;
            break;
        case MODE1: // Vertical blank
            if (currentLine() == LCD_HEIGHT-1 + ADDITIONNAL_BLANK_LINES) {
                setLyOrLyc(Reg.LY, LY_START_VALUE);
                nextMode = MODE.MODE2;
                nextNonIdleCycle++;
            } else {
                setLyOrLyc(Reg.LY, currentLine() + 1);
                nextNonIdleCycle += MODE1_DURATION;
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    private void writeInRegsLCDC(Reg reg, int data) {
        switch (reg) {
        case DMA:
            currentDMACycle = 0;
            break;
        case LCDC:
            if (!Bits.test(data, LCDC.LCD_STATUS)) {
                setMode(MODE.MODE0);
                setLyOrLyc(Reg.LY, 0);
                nextNonIdleCycle = Long.MAX_VALUE;
            }
            break;
        case LY: // LY is read-only
            return;
        case LYC:
            setLyOrLyc(Reg.LYC, data);
            return;
        case STAT: // 3 LSB of STAT are read-only
            data = (regFile.get(Reg.STAT) & ~STAT_WRITE_MASK) | (data & STAT_WRITE_MASK);
            break;
        default:
            break;
        }
        
        regFile.set(reg, data);
    }
   
    private void setLyOrLyc(Reg reg, int value) {
        Preconditions.checkArgument(reg == Reg.LY || reg == Reg.LYC);
        regFile.set(reg, value);

        boolean areEquals = currentLine() == regFile.get(Reg.LYC);
        regFile.setBit(Reg.STAT, STAT.LYC_EQ_LY, areEquals);

        if (regFile.testBit(Reg.STAT, STAT.INT_LYC) && areEquals)
            cpu.requestInterrupt(Interrupt.LCD_STAT);
    }
    
    private void setMode(MODE mode) {
        switch (mode) {
        case MODE1: // Vertical blank
            cpu.requestInterrupt(Interrupt.VBLANK);
        case MODE0:
        case MODE2: // Horizontal blank
            if (regFile.testBit(Reg.STAT, STAT.values()[STAT.INT_MODE0.index() + mode.ordinal()]))
                cpu.requestInterrupt(Interrupt.LCD_STAT);
        default:
            regFile.setBit(Reg.STAT, STAT.MODE0, MODES_TABLE[mode.ordinal()][1]);
            regFile.setBit(Reg.STAT, STAT.MODE1, MODES_TABLE[mode.ordinal()][0]);
        }
    }
    
    private void prepareNewImage() {
        currentImage = imageBuilder.build();
        imageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
        skippedWindowLines = 0;
    }
    
    
    /*** Lines computation part ***/
    
    private void computeLine() {
        LcdImageLine.Builder bgBuilder = new LcdImageLine.Builder(IMAGE_SIZE);
        LcdImageLine.Builder winBuilder = new LcdImageLine.Builder(IMAGE_SIZE);
        LcdImageLine[] sprites = generateSprites();

        int bgLineInMemory = Math.floorMod(currentLine() + regFile.get(Reg.SCY), IMAGE_SIZE);
        int winLineInMemory = currentLine() - regFile.get(Reg.WY) - skippedWindowLines;

        for (int i = 0; i < TILE_PER_LINE; i++) {
            if (regFile.testBit(Reg.LCDC, LCDC.BG))
                addTileToLine(bgBuilder, regFile.testBit(Reg.LCDC, LCDC.BG_AREA), i, bgLineInMemory);

            if (isWindowActivated() && winLineInMemory >= 0)
                addTileToLine(winBuilder, regFile.testBit(Reg.LCDC, LCDC.WIN_AREA), i, winLineInMemory);
        }

        LcdImageLine bg = buildLine(bgBuilder, regFile.get(Reg.SCX));
        LcdImageLine win = buildLine(winBuilder, 0);

        if (isWindowActivated() && winLineInMemory >= 0)
            bg = bg.join(realWX(), win);
        else if (!isWindowActivated())
            skippedWindowLines++;

        BitVector bgOpacity = bg.opacity() .or(sprites[BG_SPRITES].opacity().not());
        LcdImageLine result = sprites[BG_SPRITES].below(bg.below(sprites[FG_SPRITES]), bgOpacity);
        
        imageBuilder.setLine(currentLine(), result);
    }
    
    private void addTileToLine(LcdImageLine.Builder line, boolean tileArea, int xPosition, int yPosition) {
        int startAddress = AddressMap.BG_DISPLAY_DATA[tileArea ? 1 : 0];
        int offset = Math.floorDiv(yPosition, TILE_SIZE) * TILE_PER_LINE + xPosition;
        int tileId = read(startAddress + offset);

        boolean source = regFile.testBit(Reg.LCDC, LCDC.TILE_SOURCE);
        int lsbTileAddress = getTileAddress(tileId, source) + Math.floorMod(yPosition, TILE_SIZE) * 2;

        line.setBytes(xPosition, 
                Bits.reverse8(read(lsbTileAddress + 1)), 
                Bits.reverse8(read(lsbTileAddress)));
    }
    
    private int getTileAddress(int tileId, boolean tileSource) {
        if (tileSource || tileId >= TILE_ADDRESS_ALWAYS_ACCESSIBLE)
            return AddressMap.TILE_SOURCE[1] + tileId * TILE_SIZE * 2;
        else
            return 2*(AddressMap.TILE_SOURCE[0]+ tileId * TILE_SIZE) - AddressMap.TILE_SOURCE[1];
    }
        
    private LcdImageLine[] generateSprites() {
        LcdImageLine fgSprites = new LcdImageLine.Builder(LCD_WIDTH).build();
        LcdImageLine bgSprites = new LcdImageLine.Builder(LCD_WIDTH).build();

        if (regFile.testBit(Reg.LCDC, LCDC.OBJ)) {
            int[] sprites = spritesIntersectingLine();

            for (int i = sprites.length - 1; i >= 0; i--) {
                if (testSpriteParam(sprites[i], SPRITE_PARAM.BEHIND_BG))
                    bgSprites = bgSprites.below(getSpriteLine(sprites[i]));
                else
                    fgSprites = fgSprites.below(getSpriteLine(sprites[i]));
            }
        }

        LcdImageLine[] result = new LcdImageLine[2];
        result[BG_SPRITES] = bgSprites;
        result[FG_SPRITES] = fgSprites;
        return result;
    }
    
    private LcdImageLine getSpriteLine(int spriteId) {
        LcdImageLine.Builder spriteLine = new LcdImageLine.Builder(LCD_WIDTH);

        int offset = Math.floorMod(currentLine() - realSpriteY(spriteId), currentSpriteSize()) * 2;

        if (testSpriteParam(spriteId, SPRITE_PARAM.FLIP_V))
            offset = (currentSpriteSize() - 1) * 2 - offset;

        int lsbAddress = getTileAddress(getSprite(spriteId, SPRITE.TILE), true) + offset;

        if (testSpriteParam(spriteId, SPRITE_PARAM.FLIP_H))
            spriteLine.setBytes(0, read(lsbAddress + 1), read(lsbAddress));
        else
            spriteLine.setBytes(0, Bits.reverse8(read(lsbAddress + 1)), Bits.reverse8(read(lsbAddress)));

        int palette = testSpriteParam(spriteId, SPRITE_PARAM.PALETTE)
                ? regFile.get(Reg.OBP1)
                : regFile.get(Reg.OBP0);

        return spriteLine.build().shift(realSpriteX(spriteId)).mapColor(palette);
    }
    
    private int[] spritesIntersectingLine() {
        int[] xAndIndex = new int[MAX_SPRITES_ON_SCREEN];
        int foundedSprites = 0;

        for (int i = 0; i < NUMBER_OF_SPRITE && foundedSprites < MAX_SPRITES_ON_SCREEN; i++) {
            if (isBetween(currentLine(), realSpriteY(i), realSpriteY(i) + currentSpriteSize())) {
                xAndIndex[foundedSprites] = realSpriteX(i) << Integer.SIZE / 2 | i;
                foundedSprites++;
            }
        }

        int[] result = new int[foundedSprites];
        Arrays.sort(xAndIndex, 0, foundedSprites);

        for (int i = 0; i < foundedSprites; i++) {
            result[i] = Bits.clip(16, xAndIndex[i]);
        }
        return result;
    }
    
    
    /*** Utilitary functions ***/
    
    private int currentSpriteSize() {
        return regFile.testBit(Reg.LCDC, LCDC.OBJ_SIZE) ? TILE_SIZE * 2 : TILE_SIZE;
    }

    private boolean testSpriteParam(int spriteId, SPRITE_PARAM param) {
        return Bits.test(getSprite(spriteId, SPRITE.PARAMS), param);
    }

    private int realSpriteX(int spriteId) {
        return getSprite(spriteId, SPRITE.X) - TILE_SIZE;
    }

    private int realSpriteY(int spriteId) {
        return getSprite(spriteId, SPRITE.Y) - TILE_SIZE * 2;
    }

    private int getSprite(int spriteId, SPRITE part) {
        return OAM.read((spriteId * 4) + part.ordinal());
    }

    private LcdImageLine buildLine(LcdImageLine.Builder line, int extractOffset) {
        return line.build().extractWrapped(extractOffset, LCD_WIDTH).mapColor(regFile.get(Reg.BGP));
    }

    private boolean isWindowActivated() {
        return regFile.testBit(Reg.LCDC, LCDC.WIN) && isBetween(realWX(), 0, LCD_WIDTH);
    }

    private int realWX() {
        return Math.max(0, regFile.get(Reg.WX) - 7);
    }

    private int currentLine() {
        return regFile.get(Reg.LY);
    }

    private boolean isBetween(int toTest, int min, int max) {
        return (toTest >= min && toTest < max);
    }
}

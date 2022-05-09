package ch.epfl.gameboj.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;

public final class DebugMain3 {
    
    public static void main(String[] args) throws IOException{
        blarggTest(1, "roms/Tests/01-special.gb" );
        blarggTest(2, "roms/Tests/02-interrupts.gb");
        blarggTest(3, "roms/Tests/03-op sp,hl.gb");
        blarggTest(4, "roms/Tests/04-op r,imm.gb");
        blarggTest(5, "roms/Tests/05-op rp.gb");
        blarggTest(6, "roms/Tests/06-ld r,r.gb");
        blarggTest(7, "roms/Tests/07-jr,jp,call,ret,rst.gb");
        blarggTest(8, "roms/Tests/08-misc instrs.gb");
        blarggTest(9, "roms/Tests/09-op r,r.gb");
        blarggTest(10,"roms/Tests/10-bit ops.gb");
        blarggTest(11,"roms/Tests/11-op a,(hl).gb");
        blarggTest(12,"roms/Tests/instr_timing.gb");
        blarggTest(13, "roms/flappyboy.gb");
        blarggTest(14, "roms/flappyboy2.gb");
        blarggTest(15, "roms/Tetris.gb");
        blarggTest(16, "roms/tasmaniaStory.gb");
        blarggTest(17, "roms/Tests/sprite_priority.gb");
    }
    
    
    private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00 };

    public static void blarggTest(int id, String file) throws IOException {
        File romFile = new File(file);
        long cycles = 30_000_000;

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        gb.runUntil(cycles);
        gb.joypad().setKey(Key.A,true);
        gb.runUntil(cycles + (1L << 20));
        gb.joypad().setKey(Key.A,true);
        gb.runUntil(cycles + 2 * (1L << 20));

        LcdImage li = gb.lcdController().currentImage();
        BufferedImage i = new BufferedImage(li.width(), li.height(), BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < li.height(); ++y)
            for (int x = 0; x < li.width(); ++x)
                i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
        
        ImageIO.write(i, "png", new File("output/gb" + id + ".png"));
        System.out.println(id + ".png");
    }
  }
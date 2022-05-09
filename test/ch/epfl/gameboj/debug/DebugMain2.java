package ch.epfl.gameboj.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;

public final class DebugMain2 {
    
    public static void main(String[] args) throws IOException{
        blarggTest(1, "roms/Tests/01-special.gb"    , 30_000_000);
        blarggTest(2, "roms/Tests/02-interrupts.gb" , 30_000_000);
        blarggTest(3, "roms/Tests/03-op sp,hl.gb"   , 30_000_000);
        blarggTest(4, "roms/Tests/04-op r,imm.gb"   , 30_000_000);
        blarggTest(5, "roms/Tests/05-op rp.gb"      , 30_000_000);
        blarggTest(6, "roms/Tests/06-ld r,r.gb"     , 30_000_000);
        blarggTest(7, "roms/Tests/07-jr,jp,call,ret,rst.gb", 30_000_000);
        blarggTest(8, "roms/Tests/08-misc instrs.gb", 30_000_000);
        blarggTest(9, "roms/Tests/09-op r,r.gb"     , 30_000_000);
        blarggTest(10,"roms/Tests/10-bit ops.gb"    , 30_000_000);
        blarggTest(11,"roms/Tests/11-op a,(hl).gb"  , 30_000_000);
        blarggTest(12,"roms/Tests/instr_timing.gb"  , 30_000_000);
        blarggTest(13,"roms/flappyboy.gb"    , 30_000_000);
        blarggTest(14,"roms/flappyboy2.gb"   , 30_000_000);
        blarggTest(15,"roms/Tetris.gb"       , 30_000_000);
        blarggTest(16,"roms/tasmaniaStory.gb", 30_000_000);
        blarggTest(17,"roms/Tests/sprite_priority.gb", 30_000_000);
        blarggTest(18,"roms/SML.gb"             , 30_000_000);
        blarggTest(19,"roms/LegendOfZelda.gb"   , 30_000_000);
        blarggTest(20,"roms/snake.gb"           , 30_000_000);
        blarggTest(21,"roms/2048.gb"            , 30_000_000);
        blarggTest(22,"roms/Bomberman.gb"       , 30_000_000);
    }

    private static final int[] COLOR_MAP = new int[] {
      0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    public static void blarggTest(int idImg, String path, long cycles)
            throws IOException {
        File romFile = new File(path);
        // long cycles = Long.parseLong(args[1]);

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        gb.runUntil(cycles);
        System.out.println(idImg);
        System.out.println("+--------------------+");
        for (int y = 0; y < 18; ++y) {
            System.out.print("|");
            for (int x = 0; x < 20; ++x) {
                char c = (char) gb.bus().read(0x9800 + 32 * y + x);
                System.out.print(Character.isISOControl(c) ? " " : c);
            }
            System.out.println("|");
        }
        System.out.println("+--------------------+");

        LcdImage li = gb.lcdController().currentImage();
        BufferedImage i = new BufferedImage(li.width(), li.height(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < li.height(); ++y)
            for (int x = 0; x < li.width(); ++x)
                i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
        ImageIO.write(i, "png", new File("output/gb" + idImg + ".png"));
    }
  }
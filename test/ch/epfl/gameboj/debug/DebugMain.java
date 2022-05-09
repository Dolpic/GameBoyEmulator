package ch.epfl.gameboj.debug;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class DebugMain {
    public static void main(String[] args) throws IOException {
        blarggTest("roms/Tests/01-special.gb", 30_000_000);
        blarggTest("roms/Tests/02-interrupts.gb", 30_000_000);
        blarggTest("roms/Tests/03-op sp,hl.gb", 30_000_000);
        blarggTest("roms/Tests/04-op r,imm.gb", 30_000_000);
        blarggTest("roms/Tests/05-op rp.gb", 30_000_000);
        blarggTest("roms/Tests/06-ld r,r.gb", 30_000_000);
        blarggTest("roms/Tests/07-jr,jp,call,ret,rst.gb", 30_000_000);
        blarggTest("roms/Tests/08-misc instrs.gb", 30_000_000);
        blarggTest("roms/Tests/09-op r,r.gb", 30_000_000);
        blarggTest("roms/Tests/10-bit ops.gb", 30_000_000);
        blarggTest("roms/Tests/11-op a,(hl).gb", 30_000_000);
        blarggTest("roms/Tests/instr_timing.gb", 30_000_000);
    }
    
    private static void blarggTest(String filePath, long cycles) throws IOException{
        File romFile = new File(filePath);
        //long cycles = Long.parseLong(cycles);

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        Component printer = new DebugPrintComponent();
        printer.attachTo(gb.bus());
        while (gb.cycles() < cycles) {
          long nextCycles = Math.min(gb.cycles() + 17556, cycles);
          gb.runUntil(nextCycles);
          gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
        }
        System.out.println("----------");
    }
  }
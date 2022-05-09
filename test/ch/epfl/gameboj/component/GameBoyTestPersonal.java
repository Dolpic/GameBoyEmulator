package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Opcode;


public class GameBoyTestPersonal {
    
    private enum RegList implements Register {
        PC,SP,A,F,B,C,D,E,H,L
     }
    
    @Disabled
    @Test
    void Test_Fibo(){
        GameBoy g = new GameBoy(null);
        g.cpu()._testSetCP(AddressMap.WORK_RAM_START);
        
        byte[] fib = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0xC0,
                (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
                (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
                (byte)0xCD, (byte)0x0A, (byte)0xC0, (byte)0x4F,
                (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
                (byte)0xC0, (byte)0x81, (byte)0xC1, (byte)0xC9,
              };
        
        for(int i=0; i<fib.length; i++) {
            g.bus().write(AddressMap.WORK_RAM_START+i, Bits.clip(8,fib[i]));
        }
        
        g.runUntil(6000);
        
        assertEquals(89, g.cpu()._testGetPcSpAFBCDEHL()[RegList.A.index()]);
    }
    
    @Disabled
    @Test
    void test_Jumps() {
        GameBoy g = new GameBoy(null);
        g.cpu()._testSetCP(AddressMap.WORK_RAM_START);
        
        byte[] prog = new byte[] {
                (byte)Opcode.LD_HL_N16.encoding, (byte)0x00, (byte)0xD0,
                (byte)Opcode.JP_HL.encoding,
                (byte)Opcode.LD_A_N8.encoding, (byte)0x05,
                (byte)Opcode.CP_A_N8.encoding, (byte)0x05,
                (byte)Opcode.JP_NZ_N16.encoding, (byte)0x10, (byte)0x10,
                (byte)Opcode.JP_C_N16.encoding, (byte)0x20, (byte)0x20,
                (byte)Opcode.JP_Z_N16.encoding, (byte)0x10, (byte)0xD0,
                (byte)Opcode.JR_E8.encoding, (byte)5,
                (byte)Opcode.HALT.encoding,(byte)Opcode.HALT.encoding,(byte)Opcode.HALT.encoding,(byte)Opcode.HALT.encoding,(byte)Opcode.HALT.encoding,
                (byte)Opcode.JR_C_E8.encoding, (byte)0x1111_1110,
                (byte)Opcode.LD_A_N8.encoding, (byte)204,
                (byte)Opcode.HALT.encoding
              };
        
        g.bus().write(0xD000, Opcode.JP_N16.encoding);
        g.bus().write(0xD001, 0x04);
        g.bus().write(0xD002, 0xC0);
        
        g.bus().write(0xD010, Opcode.JP_N16.encoding);
        g.bus().write(0xD011, 0x11);
        g.bus().write(0xD012, 0xC0);
        
        for(int i=0; i<prog.length; i++) {
            g.bus().write(AddressMap.WORK_RAM_START+i, Bits.clip(8,prog[i]));
        }
        
        
        g.runUntil(10000);
        
        assertEquals(204, g.cpu()._testGetPcSpAFBCDEHL()[RegList.A.index()]);
    }
    
    @Disabled
    @Test
    void test_CALL_AND_RET() {
        GameBoy g = new GameBoy(null);
        g.cpu()._testSetCP(AddressMap.WORK_RAM_START);
        
        byte[] prog = new byte[] {
                (byte)Opcode.CALL_N16.encoding, (byte)0x00, (byte)0xD0,
                (byte)Opcode.CP_A_N8.encoding, (byte)85,
                (byte)Opcode.CALL_NC_N16.encoding, (byte)0x00, (byte)0x00,
                (byte)Opcode.CALL_Z_N16.encoding, (byte)0x00, (byte)0x00,
                (byte)Opcode.CALL_C_N16.encoding, (byte)0x10, (byte)0xD0,
                (byte)Opcode.LD_B_N8.encoding, (byte)12,
                (byte)Opcode.HALT.encoding
              };
      
        g.bus().write(0xD000, Opcode.LD_A_N8.encoding);
        g.bus().write(0xD001, 72);
        g.bus().write(0xD002, Opcode.RET.encoding);
        
        g.bus().write(0xD010, Opcode.LD_A_N8.encoding);
        g.bus().write(0xD011, 50);
        g.bus().write(0xD012, Opcode.CP_A_N8.encoding);
        g.bus().write(0xD013, 50);
        g.bus().write(0xD014, Opcode.RET_Z.encoding);
        
        for(int i=0; i<prog.length; i++) {
            g.bus().write(AddressMap.WORK_RAM_START+i, Bits.clip(8,prog[i]));
        }
        
        
        g.runUntil(10000);
        
        assertEquals(12, g.cpu()._testGetPcSpAFBCDEHL()[RegList.B.index()]);
    }
    
}

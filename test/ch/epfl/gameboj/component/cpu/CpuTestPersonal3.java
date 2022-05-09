package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.bits.Bits;

import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTestPersonal3 {
    
    private enum RegList implements Register {
       PC,SP,A,F,B,C,D,E,H,L
    }
    
    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }
   
    private void assertRegisterValue(RegList reg, Cpu c, int v) {
        assertEquals(v, c._testGetPcSpAFBCDEHL()[reg.index()]);
    }
    
    @Test
    void Fib_Test() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        byte[] fib = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
                (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
                (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
                (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
                (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
                (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
              };
        
        for(int i=0; i<fib.length; i++) {
            b.write(i, Bits.clip(8,fib[i]));
        }
        
        int i=0;
        while(c._testGetPcSpAFBCDEHL()[RegList.PC.index()] != 8) {
            c.cycle(i);
            i++;
        }
        
        assertRegisterValue(RegList.A, c, 89);

    }
    
    @Test
    void Fib_Test2() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        byte[] fib = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0D, (byte)0xCD, (byte)0x0A, (byte)0x00,
                (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
                (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
                (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
                (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
                (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
              };
        
        for(int i=0; i<fib.length; i++) {
            b.write(i, Bits.clip(8,fib[i]));
        }
        
        int i=0;
        while(c._testGetPcSpAFBCDEHL()[RegList.PC.index()] != 8) {
            c.cycle(i);
            i++;
        }
        
        assertRegisterValue(RegList.A, c, 233);

    }
    
    @Test
    void test_INTERRUPTS() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        c.write(0xFFFF, 0b0000_1111);
        
        byte[] prog = new byte[] {
                (byte)Opcode.DI.encoding,
                (byte)Opcode.LD_A_N8.encoding, (byte)85,
                (byte)Opcode.LD_C_N8.encoding, (byte)90,
                (byte)Opcode.EI.encoding,
                (byte)Opcode.LD_B_N8.encoding, (byte)123,
                (byte)Opcode.HALT.encoding
              };
      
        b.write(0x40, Opcode.LD_C_N8.encoding);
        b.write(0x41, 72);
        b.write(0x42, Opcode.RETI.encoding);
        
        for(int i=0; i<prog.length; i++) {
            b.write(i, Bits.clip(8,prog[i]));
        }
        

        c.cycle(0);
        c.requestInterrupt(Interrupt.VBLANK);
        c.cycle(1);
        c.cycle(2);
        c.cycle(3);
        c.cycle(4);
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        c.cycle(8);
        c.cycle(9);
        c.cycle(10);
        c.cycle(11);
        c.cycle(12);
        c.cycle(13);
        c.cycle(14);
        c.cycle(15);
        c.cycle(16);
        c.cycle(17);
        c.cycle(18);
        c.cycle(19);
        c.cycle(20);
        c.cycle(21);
        
        assertEquals(85, c._testGetPcSpAFBCDEHL()[RegList.A.index()]);
        assertEquals(123, c._testGetPcSpAFBCDEHL()[RegList.B.index()]);
        assertEquals(72, c._testGetPcSpAFBCDEHL()[RegList.C.index()]);
    }
    
    @Test
    void test_WAKEUP() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        c.write(0xFFFF, 0b0001_1100);
        
        byte[] prog = new byte[] {
                (byte)Opcode.HALT.encoding,
                (byte)Opcode.LD_B_N8.encoding, (byte)85,
                (byte)Opcode.HALT.encoding
              };
      
        b.write(AddressMap.INTERRUPTS[0], Opcode.STOP.encoding);
        b.write(AddressMap.INTERRUPTS[1], Opcode.STOP.encoding);
        
        b.write(AddressMap.INTERRUPTS[2], Opcode.LD_A_N8.encoding);
        b.write(AddressMap.INTERRUPTS[2]+1, 42);
        b.write(AddressMap.INTERRUPTS[2]+2, Opcode.RETI.encoding);

        
        for(int i=0; i<prog.length; i++) {
            b.write(i, Bits.clip(8,prog[i]));
        }
        
        c.cycle(0);
        c.cycle(1);
        c.cycle(2);
        c.requestInterrupt(Interrupt.VBLANK);
        c.cycle(3);
        c.cycle(4);
        c.requestInterrupt(Interrupt.LCD_STAT);
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        c.requestInterrupt(Interrupt.TIMER);
        c.cycle(8);
        c.cycle(9);
        c.cycle(10);
        c.cycle(11);
        c.cycle(12);
        c.cycle(13);
        c.cycle(14);
        c.cycle(15);
        c.cycle(16);
        c.cycle(17);
        c.cycle(18);
        c.cycle(19);
        c.cycle(20);
        c.cycle(21);
        
        assertEquals(0, c._testGetPcSpAFBCDEHL()[RegList.A.index()]);
        assertEquals(85, c._testGetPcSpAFBCDEHL()[RegList.B.index()]);
    }
    
    @Test
    void test_ANOTHER_INTERRUPT_TEST() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        c.write(0xFFFF, 0b0000_1011);
        
       c._testSetSP(0xE000);
        
        byte[] prog = new byte[] {
                (byte)Opcode.EI.encoding,
                (byte)Opcode.LD_A_N8.encoding, (byte)85,
                (byte)Opcode.LD_B_N8.encoding, (byte)10,
                (byte)Opcode.LD_C_N8.encoding, (byte)5,
                (byte)Opcode.LD_D_N8.encoding, (byte)162,
                (byte)Opcode.LD_E_N8.encoding, (byte)72,
                (byte)Opcode.HALT.encoding,
                (byte)Opcode.CALL_Z_N16.encoding, (byte)0x00,(byte)0xB0,
                (byte)Opcode.CALL_C_N16.encoding, (byte)0x00,(byte)0xB0,
                (byte)Opcode.CALL_NC_N16.encoding,(byte)0x00,(byte)0xC0,
                (byte)Opcode.STOP.encoding
              };
      
        b.write(AddressMap.INTERRUPTS[0],   Opcode.ADD_A_B.encoding);
        b.write(AddressMap.INTERRUPTS[0]+1, Opcode.INC_C.encoding);
        b.write(AddressMap.INTERRUPTS[0]+2, Opcode.RETI.encoding);
        
        
        b.write(AddressMap.INTERRUPTS[1],   Opcode.CALL_N16.encoding);
        b.write(AddressMap.INTERRUPTS[1]+1, 0x00);
        b.write(AddressMap.INTERRUPTS[1]+2, 0xA0);
        b.write(AddressMap.INTERRUPTS[1]+3, Opcode.RETI.encoding);
        
        b.write(AddressMap.INTERRUPTS[2], Opcode.STOP.encoding);
        
        b.write(AddressMap.INTERRUPTS[3],   Opcode.CP_A_B.encoding);
        b.write(AddressMap.INTERRUPTS[3]+1, Opcode.RETI.encoding);
        
        b.write(AddressMap.INTERRUPTS[4], Opcode.STOP.encoding);
        
        b.write(0xA000,  Opcode.INC_A.encoding);
        b.write(0xA001,  Opcode.RET.encoding);
        
        b.write(0xB000,  Opcode.STOP.encoding);
        
        b.write(0xC000,  Opcode.RET_Z.encoding);
        b.write(0xC001,  Opcode.RET_C.encoding);
        b.write(0xC002,  Opcode.JR_NZ_E8.encoding); 
        b.write(0xC003,  5); 
        b.write(0xC004,  Opcode.STOP.encoding); 
        b.write(0xC009,  Opcode.LD_C_N8.encoding); 
        b.write(0xC00A,  42); 
        b.write(0xC00B,  Opcode.HALT.encoding); 

        
        for(int i=0; i<prog.length; i++) {
            b.write(i, Bits.clip(8,prog[i]));
        }
        
        c.cycle(0);
        c.cycle(1);
        c.requestInterrupt(Interrupt.VBLANK);
        c.cycle(2);
        c.cycle(3);
        c.cycle(4);
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        c.cycle(8);
        c.cycle(9);
        c.cycle(10);
        c.cycle(11);
        c.cycle(12);
        c.cycle(13);
        assertEquals(85, c._testGetPcSpAFBCDEHL()[RegList.A.index()]);
        assertEquals(1, c._testGetPcSpAFBCDEHL()[RegList.C.index()]);
        
        c.cycle(14);
        
        c.requestInterrupt(Interrupt.LCD_STAT);
        c.cycle(15);
        c.cycle(16);
        c.cycle(17);
        c.cycle(18);
        c.cycle(19);
        c.cycle(20);
        c.cycle(21);
        c.cycle(22);
        c.cycle(23);
        c.cycle(24);
        c.cycle(25);
        c.cycle(26);
        c.cycle(27);
        c.cycle(28);
        c.cycle(29);
        c.cycle(30);
        c.cycle(31);
        c.cycle(32);
        c.cycle(33);
        c.cycle(34);
        
        assertEquals(10, c._testGetPcSpAFBCDEHL()[RegList.B.index()]);
        assertEquals(86, c._testGetPcSpAFBCDEHL()[RegList.A.index()]);
        
        c.cycle(35);
        c.cycle(36);
        c.requestInterrupt(Interrupt.TIMER);
        c.cycle(37);
        c.cycle(38);
        
        assertEquals(5, c._testGetPcSpAFBCDEHL()[RegList.C.index()]);
        assertEquals(162, c._testGetPcSpAFBCDEHL()[RegList.D.index()]);

        c.requestInterrupt(Interrupt.JOYPAD);
        c.cycle(39);
        c.cycle(40);
        
        c.cycle(41);
        c.cycle(42);
        c.cycle(43);
        c.cycle(44);
        
        c.requestInterrupt(Interrupt.SERIAL);
        c.cycle(45);
        c.cycle(46);
        c.cycle(47);
        c.cycle(48);
        c.cycle(49);
        c.cycle(50);
        
        c.cycle(51);
        c.cycle(52);
        c.cycle(53);
        c.cycle(54);
        c.cycle(55);
        
        assertEquals(10, c._testGetPcSpAFBCDEHL()[RegList.B.index()]);
        assertEquals(86, c._testGetPcSpAFBCDEHL()[RegList.A.index()]);
        
        c.cycle(56);
        c.cycle(57);
        c.cycle(58);
        
        c.cycle(59);
        c.cycle(60);
        c.cycle(61);
        
        c.cycle(62);
        c.cycle(63);
        c.cycle(64);
        c.cycle(65);
        c.cycle(66);
        c.cycle(67);
        
        c.cycle(68);
        c.cycle(69);
        
        c.cycle(70);
        c.cycle(71);
        
        c.cycle(72);
        c.cycle(73);
        c.cycle(74);
        
        c.cycle(75);
        assertEquals(42, c._testGetPcSpAFBCDEHL()[RegList.C.index()]);
    }
    
    @Test
    void test_JUMP_WITH_SIGNED_VALUES() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        
        c.write(0xFFFF, 0b0001_1100);
        
        byte[] prog = new byte[] {
                (byte)Opcode.LD_A_N8.encoding,  (byte) 0,
                (byte)Opcode.JR_E8.encoding,    (byte) 5,
                (byte)Opcode.STOP.encoding,(byte)Opcode.STOP.encoding,(byte)Opcode.STOP.encoding,(byte)Opcode.STOP.encoding,(byte)Opcode.STOP.encoding,
                (byte)Opcode.INC_A.encoding,
                (byte)Opcode.CP_A_N8.encoding,  (byte) 5,
                (byte)Opcode.JR_NZ_E8.encoding, (byte) 0b1111_1011,
                (byte)Opcode.HALT.encoding
              };
      
        for(int i=0; i<prog.length; i++) {
            b.write(i, Bits.clip(8,prog[i]));
        }
        
        for(int i=0; i<400; i++) {
            c.cycle(i);
        }
       
        assertEquals(5, c._testGetPcSpAFBCDEHL()[RegList.A.index()]);
    }
}

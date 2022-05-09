package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Opcode;
import ch.epfl.gameboj.component.cpu.Opcode.Family;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class CpuTestPersonal2 {
    
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
    void ADD_A_WithValues() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0, 10, 11, 12, 13, 0b0001_0000, 0xFF, 0xFF);
        
        b.write(0,Opcode.ADD_A_A.encoding);
        c.cycle(0);
        assertRegisterValue(RegList.A, c, 0);
        
        b.write(1,Opcode.ADD_A_B.encoding);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 10);
        
        b.write(2,Opcode.ADD_A_C.encoding);
        c.cycle(2);
        assertRegisterValue(RegList.A, c, 21);
        
        b.write(3,Opcode.ADC_A_D.encoding);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 33);
        
        b.write(4,Opcode.ADD_A_E.encoding);
        c.cycle(4);
        assertRegisterValue(RegList.A, c, 46);
        
        b.write(5,Opcode.ADD_A_H.encoding);
        c.cycle(5);
        assertRegisterValue(RegList.A, c, 45);
        
        b.write(6,Opcode.ADC_A_L.encoding);
        c.cycle(6);
        assertRegisterValue(RegList.A, c, 45);
        
        b.write(7,Opcode.ADD_A_N8.encoding);
        b.write(8,42);
        c.cycle(7);
        c.cycle(8);
        assertRegisterValue(RegList.A, c, 87);
        
        b.write(9,Opcode.ADC_A_N8.encoding);
        b.write(10,1);
        c.cycle(9);
        c.cycle(10);
        assertRegisterValue(RegList.A, c, 88);
        
        c._testSetRegisters(88, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 123);
        
        b.write(11,Opcode.ADD_A_HLR.encoding);
        c.cycle(11);
        c.cycle(12);
        assertRegisterValue(RegList.A, c, 211);
        
        b.write(12,Opcode.ADD_A_HLR.encoding);
        c.cycle(13);
        c.cycle(14);
        assertRegisterValue(RegList.A, c, 78);
        
        b.write(13,Opcode.ADC_A_HLR.encoding);
        c.cycle(15);
        c.cycle(16);
        assertRegisterValue(RegList.A, c, 202);
        
        b.write(14,Opcode.INC_HLR.encoding);
        c.cycle(17);
        c.cycle(18);
        c.cycle(19);
        assertEquals(124, c._testGetValueAtAddressInBus(0x33AA));
    }
    
    @Test
    void LD_HLSP_S8_test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0, 10, 11, 3, 0xFF, 0b0001_0000, 0xFF, 0xFF);
        b.write(0,Opcode.ADD_SP_N.encoding);
        b.write(1, 20);
        c.cycle(0);
        c.cycle(1);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.SP, c, 20);
        
        b.write(2,Opcode.ADD_SP_N.encoding);
        b.write(3, 0b1111_0110);
        c.cycle(4);
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        assertRegisterValue(RegList.SP, c, 10);
    }
    
    @Test
    void INC_AND_OTHERS_WithValues() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0, 10, 11, 3, 0xFF, 0b0001_0000, 0xFF, 0xFF);
        
        b.write(0,Opcode.INC_A.encoding);
        c.cycle(0);
        assertRegisterValue(RegList.A, c, 1);
        
        b.write(1,Opcode.INC_B.encoding);
        c.cycle(1);
        assertRegisterValue(RegList.B, c, 11);
        
        b.write(2,Opcode.INC_H.encoding);
        c.cycle(2);
        assertRegisterValue(RegList.H, c, 0);
        
        b.write(3,Opcode.INC_L.encoding);
        c.cycle(3);
        assertRegisterValue(RegList.L, c, 0);   
        
        b.write(4,Opcode.INC_BC.encoding);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.B, c, 11);   
        assertRegisterValue(RegList.C, c, 12); 
        
        b.write(5,Opcode.INC_DE.encoding);
        c.cycle(6);
        c.cycle(7);
        assertRegisterValue(RegList.D, c, 4);   
        assertRegisterValue(RegList.E, c, 0); 
        
        b.write(6,Opcode.INC_SP.encoding);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.SP, c, 1);   
        
        b.write(7,Opcode.ADD_HL_BC.encoding);
        c.cycle(10);
        c.cycle(11);
        assertRegisterValue(RegList.H, c, 11);   
        assertRegisterValue(RegList.L, c, 12);   
        
        b.write(8,Opcode.ADD_HL_SP.encoding);
        c.cycle(12);
        c.cycle(13);
        assertRegisterValue(RegList.H, c, 11);   
        assertRegisterValue(RegList.L, c, 13);    
    }
    
    @Test
    void SUB_A_WithValues() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(10, 8, 5, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(0,Opcode.SUB_A_A.encoding);
        c.cycle(0);
        assertRegisterValue(RegList.A, c, 0);
        
        b.write(1,Opcode.SUB_A_B.encoding);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 248);
        
        b.write(2,Opcode.SBC_A_C.encoding);
        c.cycle(2);
        assertRegisterValue(RegList.A, c, 242);
        
        b.write(3,Opcode.SUB_A_D.encoding);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 122);
        
        b.write(4,Opcode.SBC_A_E.encoding);
        c.cycle(4);
        assertRegisterValue(RegList.A, c, 109);
        
        b.write(5,Opcode.SUB_A_H.encoding);
        c.cycle(5);
        assertRegisterValue(RegList.A, c, 109);
        
        b.write(6,Opcode.SUB_A_L.encoding);
        c.cycle(6);
        assertRegisterValue(RegList.A, c, 110);
        
        b.write(7,Opcode.SBC_A_N8.encoding);
        b.write(8,42);
        c.cycle(7);
        c.cycle(8);
        assertRegisterValue(RegList.A, c, 67);
        
        b.write(9,Opcode.SUB_A_N8.encoding);
        b.write(10,1);
        c.cycle(9);
        c.cycle(10);
        assertRegisterValue(RegList.A, c, 66);
        
        c._testSetRegisters(66, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 123);
        
        b.write(11,Opcode.SUB_A_HLR.encoding);
        c.cycle(11);
        c.cycle(12);
        assertRegisterValue(RegList.A, c, 199);
        
        b.write(12,Opcode.SBC_A_HLR.encoding);
        c.cycle(13);
        c.cycle(14);
        assertRegisterValue(RegList.A, c, 75);
    }
    
    @Test
    void CP_WithValues() {

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(10, 8, 5, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(0,Opcode.CP_A_A.encoding);
        c.cycle(0);
        assertRegisterValue(RegList.F, c, 0b1100_0000);
        
        b.write(1,Opcode.CP_A_B.encoding);
        c.cycle(1);
        assertRegisterValue(RegList.F, c, 0b0100_0000);
        
        b.write(2,Opcode.CP_A_C.encoding);
        c.cycle(2);
        assertRegisterValue(RegList.F, c, 0b0100_0000);
        
        b.write(3,Opcode.CP_A_D.encoding);
        c.cycle(3);
        assertRegisterValue(RegList.F, c, 0b0101_0000);
        
        b.write(4,Opcode.CP_A_E.encoding);
        c.cycle(4);
        assertRegisterValue(RegList.F, c, 0b0111_0000);
        
        b.write(5,Opcode.CP_A_H.encoding);
        c.cycle(5);
        assertRegisterValue(RegList.F, c, 0b0100_0000);
        
        b.write(6,Opcode.CP_A_L.encoding);
        c.cycle(6);
        assertRegisterValue(RegList.F, c, 0b0111_0000);
        
        b.write(7,Opcode.CP_A_N8.encoding);
        b.write(8,42);
        c.cycle(7);
        c.cycle(8);
        assertRegisterValue(RegList.F, c, 0b0101_0000);
        
        b.write(9,Opcode.CP_A_N8.encoding);
        b.write(10,1);
        c.cycle(9);
        c.cycle(10);
        assertRegisterValue(RegList.F, c, 0b0100_0000);
        
        c._testSetRegisters(66, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 123);
        
        b.write(11,Opcode.CP_A_HLR.encoding);
        c.cycle(11);
        c.cycle(12);
        assertRegisterValue(RegList.F, c, 0b0111_0000);
        
        c._testWriteAtAddressInBus(0x33AA, 1);
        b.write(12,Opcode.CP_A_HLR.encoding);
        c.cycle(13);
        c.cycle(14);
        assertRegisterValue(RegList.F, c, 0b0100_0000);
        
    }
    
    @Test
    void DEC_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(10, 8, 5, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(0,Opcode.DEC_A.encoding);
        c.cycle(0);
        assertRegisterValue(RegList.A, c, 9);
        
        b.write(1,Opcode.DEC_H.encoding);
        c.cycle(1);
        assertRegisterValue(RegList.H, c, 0xFF);
        
        b.write(2,Opcode.DEC_L.encoding);
        c.cycle(2);
        assertRegisterValue(RegList.L, c, 0xFE);
        
        c._testSetRegisters(66, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 123);
        
        b.write(3,Opcode.DEC_HLR.encoding);
        c.cycle(3);
        c.cycle(4);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 122);
        
        b.write(4,Opcode.DEC_BC.encoding);
        c.cycle(5);
        c.cycle(6);
        assertRegisterValue(RegList.B, c, 10);
        assertRegisterValue(RegList.C, c, 10);
        
        b.write(5,Opcode.DEC_SP.encoding);
        c.cycle(7);
        c.cycle(8);
        assertRegisterValue(RegList.SP, c, 0xFFFF);
    }
    
    @Test
    void AND_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0101_1010, 8, 5, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(0,Opcode.AND_A_N8.encoding);
        b.write(1,0b1010_0101);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0);
        
        c._testSetRegisters(0b0101_1010, 16, 0b1111_1101, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(2,Opcode.AND_A_N8.encoding);
        b.write(3,0b0101_1010);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 0b0101_1010);
        
        b.write(4,Opcode.AND_A_N8.encoding);
        b.write(5,0b0000_1111);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.A, c, 0b0000_1010);
        
        b.write(6,Opcode.AND_A_A.encoding);
        c.cycle(6);
        assertRegisterValue(RegList.A, c, 0b0000_1010);
        
        b.write(7,Opcode.AND_A_C.encoding);
        c.cycle(7);
        assertRegisterValue(RegList.A, c, 0b0000_1000);
        
        c._testSetRegisters(61, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 123);

        b.write(8,Opcode.AND_A_HLR.encoding);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.A, c, 0b0011_1001);
    }
    
    @Test
    void OR_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0101_1010, 8, 5, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(0,Opcode.OR_A_N8.encoding);
        b.write(1,0b1010_0101);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1111_1111);
        
        c._testSetRegisters(0b0101_1010, 16, 0b0101_1101, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(2,Opcode.OR_A_N8.encoding);
        b.write(3,0b0101_1010);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 0b0101_1010);
        
        b.write(4,Opcode.OR_A_N8.encoding);
        b.write(5,0b0000_1111);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.A, c, 0b0101_1111);
        
        b.write(6,Opcode.OR_A_A.encoding);
        c.cycle(6);
        assertRegisterValue(RegList.A, c, 0b0101_1111);
        
        c._testSetRegisters(0b0010_0100, 16, 0b0101_1101, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(7,Opcode.OR_A_C.encoding);
        c.cycle(7);
        assertRegisterValue(RegList.A, c, 0b0111_1101);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b0111_1011);

        b.write(8,Opcode.OR_A_HLR.encoding);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.A, c, 0b0111_1111);
    }
    
    
    @Test
    void XOR_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0101_1010, 8, 5, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(0,Opcode.XOR_A_N8.encoding);
        b.write(1,0b1010_0101);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1111_1111);
        
        c._testSetRegisters(0b0101_1010, 16, 0b0101_1101, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(2,Opcode.XOR_A_N8.encoding);
        b.write(3,0b0101_1010);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 0b0000_0000);
        
        b.write(4,Opcode.XOR_A_N8.encoding);
        b.write(5,0b0000_1111);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.A, c, 0b0000_1111);
        
        b.write(6,Opcode.XOR_A_A.encoding);
        c.cycle(6);
        assertRegisterValue(RegList.A, c, 0b0000_0000);
        
        c._testSetRegisters(0b0010_0100, 16, 0b0101_1101, 120, 13, 0b0001_0000, 0, 0xFF);
        
        b.write(7,Opcode.XOR_A_C.encoding);
        c.cycle(7);
        assertRegisterValue(RegList.A, c, 0b0111_1001);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b0111_1011);

        b.write(8,Opcode.XOR_A_HLR.encoding);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.A, c, 0b0100_0110);
        
        b.write(9,Opcode.CPL.encoding);
        c.cycle(10);
        assertRegisterValue(RegList.A, c, 0b1011_1001);
    }
    
    @Test
    void SLA_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0101_1010, 8, 5, 120, 0b0001_0000, 0, 0, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.SLA_A.encoding);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1011_0100);
        
        b.write(2,0xCB);
        b.write(3,Opcode.SLA_E.encoding);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.E, c, 0b0010_0000);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b0111_1011);

        b.write(4,0xCB);
        b.write(5,Opcode.SLA_HLR.encoding);
        c.cycle(4);
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_0110);
    }
    
    @Test
    void SRA_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.SRA_B.encoding);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.B, c, 0b0001_0110);
        
        b.write(2,0xCB);
        b.write(3,Opcode.SRA_H.encoding);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.H, c, 0b1101_0001);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b0111_1011);

        b.write(4,0xCB);
        b.write(5,Opcode.SRA_HLR.encoding);
        c.cycle(4);
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b0011_1101);
    }
    
    @Test
    void SRL_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.SRL_A.encoding);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b0010_1101);
        
        b.write(2,0xCB);
        b.write(3,Opcode.SRL_H.encoding);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.H, c, 0b0101_0001);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b0111_1011);

        b.write(4,0xCB);
        b.write(5,Opcode.SRL_HLR.encoding);
        c.cycle(4);
        c.cycle(5);
        c.cycle(6);
        c.cycle(7);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b0011_1101);
    }
    
    @Test
    void RLA_RLCA_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b1101_1010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,Opcode.RLCA.encoding);
        c.cycle(0);
        assertRegisterValue(RegList.A, c, 0b1011_0101);
        
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0, 0b0001_0000, 0b1010_0010, 0xFF);
        b.write(1,Opcode.RLA.encoding);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1011_0101);
        
        c._testSetRegisters(0b1101_1011, 0b0010_1101, 5, 120, 0, 0b0001_0000, 0b1010_0010, 0xFF);
        b.write(2,Opcode.RRCA.encoding);
        c.cycle(2);
        assertRegisterValue(RegList.A, c, 0b1110_1101);
        
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0, 0b0000_0000, 0b1010_0010, 0xFF);
        b.write(3,Opcode.RRA.encoding);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 0b0010_1101);
    }
    
    @Test
    void Rotation_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b1101_1010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.RLC_A.encoding);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1011_0101);
        
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0, 0b0001_0000, 0b1010_0010, 0xFF);
        b.write(2,0xCB);
        b.write(3,Opcode.RL_A.encoding);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 0b1011_0101);
        
        c._testSetRegisters(0b1101_1010, 0b0010_1101, 0b1111_0000, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(4,0xCB);
        b.write(5,Opcode.RLC_C.encoding);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.C, c, 0b1110_0001);
        
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0b0101_1100, 0b0001_0000, 0b1010_0010, 0xFF);
        b.write(6,0xCB);
        b.write(7,Opcode.RL_E.encoding);
        c.cycle(6);
        c.cycle(7);
        assertRegisterValue(RegList.E, c, 0b1011_1001);
        
        
        c._testSetRegisters(0b1101_1010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(8,0xCB);
        b.write(9,Opcode.RRC_E.encoding);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.E, c, 0b0000_1000);
        
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0, 0b0001_0000, 0b1010_0010, 0xFF);
        b.write(10,0xCB);
        b.write(11,Opcode.RR_A.encoding);
        c.cycle(10);
        c.cycle(11);
        assertRegisterValue(RegList.A, c, 0b1010_1101);
        
        c._testSetRegisters(0b1101_1010, 0b0010_1101, 0b1111_0000, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(12,0xCB);
        b.write(13,Opcode.RRC_C.encoding);
        c.cycle(12);
        c.cycle(13);
        assertRegisterValue(RegList.C, c, 0b0111_1000);
        
        c._testSetRegisters(0b0101_1010, 0b0010_1101, 5, 120, 0b0101_1100, 0b0001_0000, 0b1010_0010, 0xFF);
        b.write(14,0xCB);
        b.write(15,Opcode.RR_E.encoding);
        c.cycle(14);
        c.cycle(15);
        assertRegisterValue(RegList.E, c, 0b1010_1110);
        
        
        
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1011);
        b.write(16,0xCB);
        b.write(17,Opcode.RLC_HLR.encoding);
        c.cycle(16);
        c.cycle(17);
        c.cycle(18);
        c.cycle(19);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_0111);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1011);
        b.write(18,0xCB);
        b.write(19,Opcode.RL_HLR.encoding);
        c.cycle(20);
        c.cycle(21);
        c.cycle(22);
        c.cycle(23);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_0111);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1010);
        b.write(20,0xCB);
        b.write(21,Opcode.RRC_HLR.encoding);
        c.cycle(24);
        c.cycle(25);
        c.cycle(26);
        c.cycle(27);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b0111_1101);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1010);
        b.write(22,0xCB);
        b.write(23,Opcode.RR_HLR.encoding);
        c.cycle(28);
        c.cycle(29);
        c.cycle(30);
        c.cycle(31);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_1101);
    }
    
    @Test
    void SWAP_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b1101_1010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.SWAP_A.encoding);
        b.write(2,0xCB);
        b.write(3,Opcode.SWAP_E.encoding);
        b.write(4,0xCB);
        b.write(5,Opcode.SWAP_H.encoding);
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1010_1101);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.E, c, 0b0000_0001);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.H, c, 0b0010_1010);

        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0001_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1010);
        b.write(6,0xCB);
        b.write(7,Opcode.SWAP_HLR.encoding);
        c.cycle(6);
        c.cycle(7);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1010_1111);
    }
    
    @Test
    void Bit_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b1101_1010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.BIT_0_A.encoding);
        b.write(2,0xCB);
        b.write(3,Opcode.BIT_1_A.encoding);
        b.write(4,0xCB);
        b.write(5,Opcode.BIT_2_A.encoding);
        b.write(6,0xCB);
        b.write(7,Opcode.BIT_3_A.encoding);
        b.write(8,0xCB);
        b.write(9,Opcode.BIT_4_A.encoding);
        b.write(10,0xCB);
        b.write(11,Opcode.BIT_5_A.encoding);
        b.write(12,0xCB);
        b.write(13,Opcode.BIT_7_A.encoding);
        b.write(14,0xCB);
        b.write(15,Opcode.BIT_0_C.encoding);
        b.write(16,0xCB);
        b.write(17,Opcode.BIT_3_E.encoding);
        b.write(18,0xCB);
        b.write(19,Opcode.BIT_5_L.encoding);
        
        b.write(20,0xCB);
        b.write(21,Opcode.BIT_0_HLR.encoding);
        b.write(22,0xCB);
        b.write(23,Opcode.BIT_2_HLR.encoding);
        b.write(24,0xCB);
        b.write(25,Opcode.BIT_5_HLR.encoding);
        b.write(26,0xCB);
        b.write(27,Opcode.BIT_7_HLR.encoding);
        
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.F, c, 0b1010_0000);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.F, c, 0b1010_0000);
        c.cycle(6);
        c.cycle(7);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
        c.cycle(10);
        c.cycle(11);
        assertRegisterValue(RegList.F, c, 0b1010_0000);
        c.cycle(12);
        c.cycle(13);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
        
        
        c.cycle(14);
        c.cycle(15);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
        c.cycle(16);
        c.cycle(17);
        assertRegisterValue(RegList.F, c, 0b1010_0000);
        c.cycle(18);
        c.cycle(19);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0000_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1010);
        
        c.cycle(20);
        c.cycle(21);
        c.cycle(22);
        assertRegisterValue(RegList.F, c, 0b1010_0000);
        c.cycle(23);
        c.cycle(24);
        c.cycle(25);
        assertRegisterValue(RegList.F, c, 0b1010_0000);
        c.cycle(26);
        c.cycle(27);
        c.cycle(28);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
        c.cycle(29);
        c.cycle(30);
        c.cycle(31);
        assertRegisterValue(RegList.F, c, 0b0010_0000);
    }
    
    @Test
    void SET_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0010_0000, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.SET_0_A.encoding);
        b.write(2,0xCB);
        b.write(3,Opcode.SET_1_A.encoding);
        b.write(4,0xCB);
        b.write(5,Opcode.SET_2_A.encoding);
        b.write(6,0xCB);
        b.write(7,Opcode.SET_3_A.encoding);
        b.write(8,0xCB);
        b.write(9,Opcode.SET_4_A.encoding);
        b.write(10,0xCB);
        b.write(11,Opcode.SET_5_A.encoding);
        b.write(12,0xCB);
        b.write(13,Opcode.SET_6_A.encoding);
        b.write(14,0xCB);
        b.write(15,Opcode.SET_7_A.encoding);
        
        b.write(16,0xCB);
        b.write(17,Opcode.SET_0_C.encoding);
        b.write(18,0xCB);
        b.write(19,Opcode.SET_3_E.encoding);
        b.write(20,0xCB);
        b.write(21,Opcode.SET_5_L.encoding);
        
        b.write(22,0xCB);
        b.write(23,Opcode.SET_0_HLR.encoding);
        b.write(24,0xCB);
        b.write(25,Opcode.SET_2_HLR.encoding);
        b.write(26,0xCB);
        b.write(27,Opcode.SET_5_HLR.encoding);
        b.write(28,0xCB);
        b.write(29,Opcode.SET_7_HLR.encoding);
        
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b0010_0001);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 0b0010_0011);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.A, c, 0b0010_0111);
        c.cycle(6);
        c.cycle(7);
        assertRegisterValue(RegList.A, c, 0b0010_1111);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.A, c, 0b0011_1111);
        c.cycle(10);
        c.cycle(11);
        assertRegisterValue(RegList.A, c, 0b0011_1111);
        c.cycle(12);
        c.cycle(13);
        assertRegisterValue(RegList.A, c, 0b0111_1111);
        c.cycle(14);
        c.cycle(15);
        assertRegisterValue(RegList.A, c, 0b1111_1111);
        
        
        c.cycle(16);
        c.cycle(17);
        assertRegisterValue(RegList.C, c, 5);
        c.cycle(18);
        c.cycle(19);
        assertRegisterValue(RegList.E, c, 0b0001_1000);
        c.cycle(20);
        c.cycle(21);
        assertRegisterValue(RegList.L, c, 0xFF);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0000_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1010);
        
        c.cycle(22);
        c.cycle(23);
        c.cycle(24);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_1011);
        c.cycle(25);
        c.cycle(26);
        c.cycle(27);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_1111);
        c.cycle(28);
        c.cycle(29);
        c.cycle(30);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_1111);
        c.cycle(31);
        c.cycle(32);
        c.cycle(33);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_1111);
    }
    
    @Test
    void RES_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b1101_1111, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,0xCB);
        b.write(1,Opcode.RES_0_A.encoding);
        b.write(2,0xCB);
        b.write(3,Opcode.RES_1_A.encoding);
        b.write(4,0xCB);
        b.write(5,Opcode.RES_2_A.encoding);
        b.write(6,0xCB);
        b.write(7,Opcode.RES_3_A.encoding);
        b.write(8,0xCB);
        b.write(9,Opcode.RES_4_A.encoding);
        b.write(10,0xCB);
        b.write(11,Opcode.RES_5_A.encoding);
        b.write(12,0xCB);
        b.write(13,Opcode.RES_6_A.encoding);
        b.write(14,0xCB);
        b.write(15,Opcode.RES_7_A.encoding);
        
        b.write(16,0xCB);
        b.write(17,Opcode.RES_0_C.encoding);
        b.write(18,0xCB);
        b.write(19,Opcode.RES_3_E.encoding);
        b.write(20,0xCB);
        b.write(21,Opcode.RES_5_L.encoding);
        
        b.write(22,0xCB);
        b.write(23,Opcode.RES_0_HLR.encoding);
        b.write(24,0xCB);
        b.write(25,Opcode.RES_2_HLR.encoding);
        b.write(26,0xCB);
        b.write(27,Opcode.RES_5_HLR.encoding);
        b.write(28,0xCB);
        b.write(29,Opcode.RES_7_HLR.encoding);
        
        c.cycle(0);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1101_1110);
        c.cycle(2);
        c.cycle(3);
        assertRegisterValue(RegList.A, c, 0b1101_1100);
        c.cycle(4);
        c.cycle(5);
        assertRegisterValue(RegList.A, c, 0b1101_1000);
        c.cycle(6);
        c.cycle(7);
        assertRegisterValue(RegList.A, c, 0b1101_0000);
        c.cycle(8);
        c.cycle(9);
        assertRegisterValue(RegList.A, c, 0b1100_0000);
        c.cycle(10);
        c.cycle(11);
        assertRegisterValue(RegList.A, c, 0b1100_0000);
        c.cycle(12);
        c.cycle(13);
        assertRegisterValue(RegList.A, c, 0b1000_0000);
        c.cycle(14);
        c.cycle(15);
        assertRegisterValue(RegList.A, c, 0b0000_0000);
        
        
        c.cycle(16);
        c.cycle(17);
        assertRegisterValue(RegList.C, c, 0b0000_0100);
        c.cycle(18);
        c.cycle(19);
        assertRegisterValue(RegList.E, c, 0b0001_0000);
        c.cycle(20);
        c.cycle(21);
        assertRegisterValue(RegList.L, c, 0b1101_1111);
        
        c._testSetRegisters(0b0011_1101, 10, 11, 12, 13, 0b0000_0000, 0x33, 0xAA);
        c._testWriteAtAddressInBus(0x33AA, 0b1111_1010);
        
        c.cycle(22);
        c.cycle(23);
        c.cycle(24);
        c.cycle(25);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_1010);
        c.cycle(26);
        c.cycle(27);
        c.cycle(28);
        c.cycle(29);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1111_1010);
        c.cycle(30);
        c.cycle(31);
        c.cycle(32);
        c.cycle(33);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b1101_1010);
        c.cycle(34);
        c.cycle(35);
        c.cycle(36);
        c.cycle(37);
        assertEquals(c._testGetValueAtAddressInBus(0x33AA), 0b0101_1010);
    }
    
    @Test
    void OTHER_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0100_0010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        b.write(0,Opcode.ADD_A_A.encoding);
        c.cycle(0);
        b.write(1,Opcode.DAA.encoding);
        c.cycle(1);
        assertRegisterValue(RegList.A, c, 0b1000_0100);
        
        c._testSetRegisters(0b0100_0010, 0b0010_1101, 5, 120, 0b0000_0000, 0b0000_0000, 0b1010_0010, 0xFF);
        
        b.write(2,Opcode.SCF.encoding);
        c.cycle(2);
        assertRegisterValue(RegList.F, c, 0b0001_0000);
        
        b.write(3,Opcode.CCF.encoding);
        c.cycle(3);
        assertRegisterValue(RegList.F, c, 0b0000_0000);
        
        b.write(4,Opcode.CCF.encoding);
        c.cycle(4);
        assertRegisterValue(RegList.F, c, 0b0001_0000);
    }
    
    @Test
    void FINAL_Test() {
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
 
        c._testSetRegisters(0b0100_0010, 0b0010_1101, 5, 120, 0b0001_0000, 0, 0b1010_0010, 0xFF);
        
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0xC2);
        b.write(2, Opcode.RLCA.encoding);
        c.cycle(0);
        c.cycle(1);
        c.cycle(2);
        
        assertRegisterValue(RegList.F, c, 0b0001_0000);
    }

}
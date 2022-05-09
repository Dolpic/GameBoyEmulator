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

import java.util.Random;

import org.junit.jupiter.api.Test;

public class CpuTestPersonal1 {
    
    private enum RegBCDEAHL implements Register {
       B,C,D,E,A,H,L
    }
    
    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }
    
    private byte getRegBin(RegBCDEAHL reg) {
        switch(reg) {
        case B : return 0b000;
        case C : return 0b001; 
        case D : return 0b010; 
        case E : return 0b011;
        case H : return 0b100;
        case L : return 0b101; 
        case A : return 0b111;
        default : throw new IllegalArgumentException();
        }
    }
    
    private byte getRegNumber(RegBCDEAHL reg) {
        switch(reg) {
        case B : return 4;
        case C : return 5; 
        case D : return 6; 
        case E : return 7;
        case A : return 2;
        case H : return 8;
        case L : return 9; 
        default : throw new IllegalArgumentException();
        }
    }
    
    private void printTable(int[] t) {
        System.out.println(t[0]+","+t[1]+","+t[2]+","+t[3]+","+t[4]+","+t[5]+","+t[6]+","+t[7]+","+t[8]+","+t[9]);
    }
    
    
    @Test
    void nopDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] {1,0,0,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void nopsDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(100);
        Bus b = connect(c, r);
        
        int curCycle = 0;
        for(int i = 0; i < Opcode.values().length; i++) {
            if(Opcode.values()[i].family == Family.NOP) {
                b.write(i*8, Opcode.NOP.encoding);
                c.cycle(curCycle);
                curCycle++;
                assertArrayEquals(new int[] {curCycle,0,0,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            }
        }
    }
    
    @Test
    void TestHL() {
        Random rng = newRandom();
        
        Cpu c = new Cpu();
        Ram r = new Ram(100);
        Bus b = connect(c, r);
        
        int[] res = new int[] {0,0,0,0,0,0,0,0,0,0};
        
        for(int i = 0; i < RegBCDEAHL.values().length-2; i++) {
         
            int curRng = rng.nextInt(0xFF);
            
            b.write(i*3, Opcode.LD_HLR_N8.encoding);
            b.write(i*3+1, curRng);
            b.write(i*3+2, 0b01_000_000 | (getRegBin(RegBCDEAHL.values()[i]) << 3) | 0b00_000_110); // LD_R8_HLR
            
            for (long cycle = i*5; cycle < i*5+5; cycle++) c.cycle(cycle);
            
            res[0] = i*3+3;
            res[getRegNumber(RegBCDEAHL.values()[i])] = curRng;
            
            //printTable(c._testGetPcSpAFBCDEHL());
            assertArrayEquals(res, c._testGetPcSpAFBCDEHL());
        }
    }
    
    @Test
    void Test_LD_A_HLRU() {
        Random rng = newRandom();
        
        Cpu c = new Cpu();
        Ram r = new Ram(100);
        Bus b = connect(c, r);
        
        int[] res = new int[] {0,0,0,0,0,0,0,0,0,0};
        
        for(int i = 0; i < 10; i++) {
            
            int curRng = rng.nextInt(0xFF);
            
            b.write(i*8, Opcode.LD_A_N8.encoding);
            b.write(i*8+1, curRng);
            
            b.write(i*8+2, Opcode.LD_HLRI_A.encoding);
            
            b.write(i*8+3, Opcode.LD_A_N8.encoding);
            b.write(i*8+4, rng.nextInt(0xFF));
            
            b.write(i*8+5, Opcode.LD_A_HLRD.encoding);
            b.write(i*8+6, Opcode.LD_A_HLRD.encoding);
            
            b.write(i*8+7, Opcode.LD_HLRI_A.encoding);
            
            int nbOfCycles = Opcode.LD_A_N8.cycles+2*Opcode.LD_HLRI_A.cycles+Opcode.LD_A_N8.cycles+2*Opcode.LD_A_HLRD.cycles;
            for (long cycle = i*nbOfCycles; cycle < (i+1)*nbOfCycles; cycle++) c.cycle(cycle);
            
            res[0] = (i+1)*8;
            res[2] = curRng;
            
            //printTable(c._testGetPcSpAFBCDEHL());
            assertArrayEquals(res, c._testGetPcSpAFBCDEHL());
        }
    }
    
    @Test
    void Test_R8() {
        Random rng = newRandom();
        
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        
        int[] res = new int[] {0,0,0,0,0,0,0,0,0,0};
        
        for(int i = 0; i < 50; i++) {
            
            int curRng = rng.nextInt(0xED)+18;
            int curRng2 = rng.nextInt(0xED)+18;
            
            b.write(i*14, Opcode.LD_A_N8.encoding); //Met A à curRng
            b.write(i*14+1, curRng);
            
            b.write(i*14+2, Opcode.LD_N8R_A.encoding); //Met la valeur de A à l'adresse 100
            b.write(i*14+3, 100);
            
            b.write(i*14+4, Opcode.LD_A_N8.encoding); //Assigne une nouvelle valeur à A
            b.write(i*14+5, curRng2);
            
            b.write(i*14+6, Opcode.LD_C_N8.encoding); //Assigne 200 à C
            b.write(i*14+7, 200);
            
            b.write(i*14+8, Opcode.LD_CR_A.encoding); //Met A à l'adresse pointée par C
            
            b.write(i*14+9, Opcode.LD_A_N8R.encoding); //Reprend la première valeur donnée à A
            b.write(i*14+10, 100);
            
            b.write(i*14+11, Opcode.LD_B_A.encoding); //La fait passer dans B
           
            b.write(i*14+12, Opcode.LD_A_CR.encoding); //Reprend la deuxième valeur passée à A
            
            b.write(i*14+13, Opcode.LD_D_A.encoding); //La fait passer dans D
            
            
            int nbOfCycles = Opcode.LD_A_N8.cycles+Opcode.LD_N8R_A.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_C_N8.cycles+Opcode.LD_CR_A.cycles+Opcode.LD_A_N8R.cycles+Opcode.LD_B_A.cycles+Opcode.LD_A_CR.cycles+Opcode.LD_D_A.cycles;
            for (long cycle = i*nbOfCycles; cycle < (i+1)*nbOfCycles; cycle++) c.cycle(cycle);
            
            res[0] = (i+1)*14;
            res[2] = curRng2;
            res[4] = curRng;
            res[5] = 200;
            res[6] = curRng2;
            
            //printTable(c._testGetPcSpAFBCDEHL());
            assertArrayEquals(res, c._testGetPcSpAFBCDEHL());
        }
    }
    
    @Test
    void Test_PushPop() {
        Random rng = newRandom();
        
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        
        int[] res = new int[] {0,0,0,0,0,0,0,0,0,0};
        
        for(int i = 0; i < 50; i++) {
            
            int curRng = rng.nextInt(0xFFEB)+20;
            int curRng2 = rng.nextInt(0xFFEB)+20;
            int curRng3 = rng.nextInt(0xFF);
            
            int ramOffset = i*24; 
            b.write(ramOffset, Opcode.LD_SP_N16.encoding); //Initialise le stack
            b.write(ramOffset+1, 0x45);
            b.write(ramOffset+2, 0x23);
            
            b.write(ramOffset+3, Opcode.LD_BC_N16.encoding); //Assigne une valeur n°1 à BC
            b.write(ramOffset+4, Bits.clip(8, curRng));
            b.write(ramOffset+5, Bits.clip(8, curRng>>8));
            
            b.write(ramOffset+6, Opcode.LD_DE_N16.encoding); //Assigne une valeur n°2 à DE
            b.write(ramOffset+7, Bits.clip(8, curRng2));
            b.write(ramOffset+8, Bits.clip(8, curRng2>>8));
            
            b.write(ramOffset+9, Opcode.PUSH_BC.encoding);
            
            b.write(ramOffset+10, Opcode.PUSH_DE.encoding);
            
            b.write(ramOffset+11, Opcode.LD_BC_N16.encoding); //Assigne 0x5678 à BC
            b.write(ramOffset+12, 0x78);
            b.write(ramOffset+13, 0x56);
            
            b.write(ramOffset+14, Opcode.LD_A_N8.encoding); //Assigne une nouvelle valeur à A
            b.write(ramOffset+15, curRng3);
            
            b.write(ramOffset+16, Opcode.LD_BCR_A.encoding); // Met la valeur de A à l'adresse contenue dans BC
            
            b.write(ramOffset+17, Opcode.POP_BC.encoding);
            
            b.write(ramOffset+18, Opcode.POP_DE.encoding);
            
            b.write(ramOffset+19, Opcode.LD_A_N8.encoding); //Assigne 0 à A
            b.write(ramOffset+20, 0);
            
            b.write(ramOffset+21, Opcode.LD_A_N16R.encoding); //On reprend la valeur placée en 0x5678
            b.write(ramOffset+22, 0x78);
            b.write(ramOffset+23, 0x56);

            int nbOfCycles = Opcode.LD_SP_N16.cycles+Opcode.LD_BC_N16.cycles+Opcode.LD_DE_N16.cycles+Opcode.PUSH_BC.cycles+Opcode.PUSH_DE.cycles+Opcode.LD_BC_N16.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_BCR_A.cycles+Opcode.POP_BC.cycles+Opcode.POP_DE.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_A_N16R.cycles;
            for (long cycle = i*nbOfCycles; cycle < (i+1)*nbOfCycles; cycle++) c.cycle(cycle);
            
            res[0] = (i+1)*24;
            res[1] = 0x2345;
            res[2] = curRng3;
            res[4] = Bits.clip(8, curRng2 >> 8);
            res[5] = Bits.clip(8, curRng2);
            res[6] = Bits.clip(8, curRng >> 8);
            res[7] = Bits.clip(8, curRng);
            
            //printTable(c._testGetPcSpAFBCDEHL());
            assertArrayEquals(res, c._testGetPcSpAFBCDEHL());
        }
    }
    
    
    @Test
    void Test_Others() {
        Random rng = newRandom();
        
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        
        int[] res = new int[] {0,0,0,0,0,0,0,0,0,0};
        
        for(int i = 0; i < 50; i++) {
            
            int curRng = rng.nextInt(0xFF);
            int curRng2 = rng.nextInt(0xFF);
            
            int ramOffset = i*17; 
            
            b.write(ramOffset, Opcode.LD_BC_N16.encoding);  //Met 0x2233 dans BC
            b.write(ramOffset+1, 0x33);
            b.write(ramOffset+2, 0x22);
            
            b.write(ramOffset+3, Opcode.LD_A_N8.encoding); //Met un nombre n°1 dans A
            b.write(ramOffset+4, curRng);
            
            b.write(ramOffset+5, Opcode.LD_BCR_A.encoding);  //Met le nombre n°1 à l'addresse pointée par BC
            
            
            b.write(ramOffset+6, Opcode.LD_DE_N16.encoding); //Met 0x4455 dans DE
            b.write(ramOffset+7, 0x55);
            b.write(ramOffset+8, 0x44);
            
            b.write(ramOffset+9, Opcode.LD_A_N8.encoding); //Met le nombre n°2 dans A
            b.write(ramOffset+10, curRng2);
            
            b.write(ramOffset+11, Opcode.LD_DER_A.encoding); //Met le nombre n°2 à l'adresse pointée par DE
            
            b.write(ramOffset+12, Opcode.LD_A_BCR.encoding);  // Met la valeur de l'adresse pointée par BC dans A
            b.write(ramOffset+13, Opcode.LD_L_A.encoding); //Met A dans L
            
            b.write(ramOffset+14, Opcode.LD_A_DER.encoding); //Met la valeur de l'adresse pointée par DE dans A
            b.write(ramOffset+15, Opcode.LD_H_A.encoding); //Met A dans H
            
            b.write(ramOffset+16, Opcode.LD_SP_HL.encoding); //Met HL dans SP
            

            int nbOfCycles = Opcode.LD_BC_N16.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_BCR_A.cycles+Opcode.LD_DE_N16.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_DER_A.cycles+Opcode.LD_A_BCR.cycles+Opcode.LD_L_A.cycles+Opcode.LD_A_DER.cycles+Opcode.LD_H_A.cycles+Opcode.LD_SP_HL.cycles;
            for (long cycle = i*nbOfCycles; cycle < (i+1)*nbOfCycles; cycle++) c.cycle(cycle);
            
            res[0] = (i+1)*17;
            res[1] = (curRng2 << 8) | curRng;
            res[2] = curRng2;
            res[4] = 0x22;
            res[5] = 0x33;
            res[6] = 0x44;
            res[7] = 0x55;
            res[8] = curRng2;
            res[9] = curRng;
            
            //printTable(c._testGetPcSpAFBCDEHL());
            assertArrayEquals(res, c._testGetPcSpAFBCDEHL());
        }
    }
    
    @Test
    void Test_Others2() {
        Random rng = newRandom();
        
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        
        int[] res = new int[] {0,0,0,0,0,0,0,0,0,0};
        
        for(int i = 0; i < 50; i++) {
            
            int curRng = rng.nextInt(0xFF);
            int curRng2 = rng.nextInt(0xFF);
            
            int ramOffset = i*21; 
            
            b.write(ramOffset, Opcode.LD_A_N8.encoding); //Met un nombre n°1 dans A
            b.write(ramOffset+1, curRng);
            
            b.write(ramOffset+2, Opcode.LD_C_A.encoding); //Copie A dans C

            b.write(ramOffset+3, Opcode.LD_N16R_A.encoding);  //Ecrit ce nombre en 0x2233
            b.write(ramOffset+4, 0x33);
            b.write(ramOffset+5, 0x22);
            
            b.write(ramOffset+6, Opcode.LD_HL_N16.encoding); //Met 0x4455 dans HL
            b.write(ramOffset+7, 0x55);
            b.write(ramOffset+8, 0x44);
            
            b.write(ramOffset+9, Opcode.LD_HLR_N8.encoding);//Ecrit un nombre n°2 en HL
            b.write(ramOffset+10, curRng2);
            
            b.write(ramOffset+11, Opcode.LD_SP_N16.encoding); ////Met 0x6677 dans SP
            b.write(ramOffset+12, 0x77);
            b.write(ramOffset+13, 0x66);
           
            b.write(ramOffset+14, Opcode.LD_N16R_SP.encoding);  //Ecrit SP en 0x8899
            b.write(ramOffset+15, 0x99);
            b.write(ramOffset+16, 0x88);
            
            b.write(ramOffset+17, Opcode.LD_B_HLR.encoding); //Récupère la valeur à l'addresse HL
            b.write(ramOffset+18, Opcode.LD_A_N16R.encoding);//Récupère la valeur à l'adresse SP (0x77 car écrit en little-endian)
            b.write(ramOffset+19, 0x99);
            b.write(ramOffset+20, 0x88);
            
            
            int nbOfCycles = Opcode.LD_A_N8.cycles+Opcode.LD_C_A.cycles+Opcode.LD_N16R_A.cycles+Opcode.LD_HL_N16.cycles+Opcode.LD_HLR_N8.cycles+Opcode.LD_SP_N16.cycles+Opcode.LD_N16R_SP.cycles+Opcode.LD_B_HLR.cycles+Opcode.LD_A_N16R.cycles;
            for (long cycle = i*nbOfCycles; cycle < (i+1)*nbOfCycles; cycle++) c.cycle(cycle);
            
            res[0] = (i+1)*21;
            res[1] = 0x6677;
            res[2] = 0x77;
            res[3] = 0;
            res[4] = curRng2;
            res[5] = curRng;
            res[8] = 0x44;
            res[9] = 0x55;
            
            //printTable(c._testGetPcSpAFBCDEHL());
            assertArrayEquals(res, c._testGetPcSpAFBCDEHL());
        }
    }
}
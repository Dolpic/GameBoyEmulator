package ch.epfl.gameboj.component.cpu;

import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;

//NOTE : This class need the Alu.packValueZNHC to be public in order to work
public class AluTestPersonal {
    
    private boolean T = true;
    private boolean F = false;
    
    private boolean[][] allZNHC = {
                             {T,T,T,T},
                             {F,T,T,T},
                             {T,F,T,T},
                             {T,T,F,T},
                             {T,T,T,F},
                             {F,F,T,T},
                             {F,T,F,T},
                             {F,T,T,F},
                             {T,F,F,T},
                             {T,F,T,F},
                             {T,T,F,F},
                             {F,F,F,T},
                             {F,F,T,F},
                             {F,T,F,F},
                             {T,F,F,F},
                             {F,F,F,F}
    };
    
    @Test
    void maskZNHCWithValues() {
        for(int i=0; i<allZNHC.length; i++) {
            int res = ((allZNHC[i][0]?1:0)<<3) + ((allZNHC[i][1]?1:0)<<2) + ((allZNHC[i][2]?1:0)<<1) + ((allZNHC[i][3]?1:0)<<0);
            assertEquals(res<<4, Alu.maskZNHC(allZNHC[i][0],  allZNHC[i][1],  allZNHC[i][2],  allZNHC[i][3]));
        }
    }
    

    @Test
    void unpackValueIsLogicWithPackValue() {
        Random rng = newRandom();
        for(int i=0; i<allZNHC.length; i++) {
            int val = rng.nextInt(0xFFFF);
            assertEquals(val, Alu.unpackValue(Alu._testPackValueZNHC(val, allZNHC[i][0],  allZNHC[i][1],  allZNHC[i][2],  allZNHC[i][3])));
        }
    }
    
    @Test
    void unpackFlagsIsLogicWithPackValue() {
        Random rng = newRandom();
        for(int i=0; i<allZNHC.length; i++) {
            int res = ((allZNHC[i][0]?1:0)<<3) + ((allZNHC[i][1]?1:0)<<2) + ((allZNHC[i][2]?1:0)<<1) + ((allZNHC[i][3]?1:0)<<0);
            assertEquals(res<<4, Alu.unpackFlags(Alu._testPackValueZNHC(rng.nextInt(0xFFFF), allZNHC[i][0],  allZNHC[i][1],  allZNHC[i][2],  allZNHC[i][3])));
        }
    }
    
    @Test
    void addWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.add(0,0,false));
        assertEquals(0b0000_0000_0000_0000___0000_0001___0000___0000, Alu.add(0,0,true));
        
        assertEquals(0b0000_0000_0000_0000___0000_0000___1011___0000, Alu.add(0b1111_1111, 0b0000_0001, false));
        assertEquals(0b0000_0000_0000_0000___1111_1111___0000___0000, Alu.add(0b1111_1111, 0b0000_0000, false));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1011___0000, Alu.add(0b1111_1111, 0b0000_0000, true));
        
        assertEquals(0b0000_0000_0000_0000___0011_0000___0010___0000, Alu.add(0b0000_1101, 0b0010_0011));
        assertEquals(0b0000_0000_0000_0000___0000_0101___0001___0000, Alu.add(0b1111_0000, 0b0001_0101));
    }
    
    @Test
    void add16LWithValues() {
        assertEquals(0b0000_0000___0000_0000_0000_0000___0000___0000, Alu.add16L(0,0));
        assertEquals(0b0000_0000___0000_0000_0000_0000___0011___0000, Alu.add16L(0b1111_1111_1111_1111, 0b0000_0000_0000_0001));
        
        assertEquals(0b0000_0000___0000_0001_0000_0000___0011___0000, Alu.add16L(0b0000_0000_1111_1111, 0b0000_0000_0000_0001));
        assertEquals(0b0000_0000___1000_0000_0000_0000___0001___0000, Alu.add16L(0b0111_1111_1000_0000, 0b0000_0000_1000_0000));
        assertEquals(0b0000_0000___0000_0000_0001_0000___0010___0000, Alu.add16L(0b1111_0000_0000_1111, 0b0001_0000_0000_0001));
        
        assertEquals(0b0000_0000___1111_1111_1111_1111___0000___0000, Alu.add16L(0b0101_0101_0101_0101, 0b1010_1010_1010_1010));
        assertEquals(0b0000_0000___0101_0101_0101_0100___0011___0000, Alu.add16L(0b1111_0101_1111_0101, 0b0101_1111_0101_1111));
        
        assertEquals(0b0000_0000___0011_0010_1011_1100___0000___0000, Alu.add16L(0b0000_0110_0100_1100, 0b0010_1100_0111_0000));
    }
    
    @Test
    void add16HWithValues() {
        assertEquals(0b0000_0000___0000_0000_0000_0000___0000___0000, Alu.add16H(0,0));
        assertEquals(0b0000_0000___0000_0000_0000_0000___0011___0000, Alu.add16H(0b1111_1111_1111_1111, 0b0000_0000_0000_0001));
        
        assertEquals(0b0000_0000___0000_0001_0000_0000___0000___0000, Alu.add16H(0b0000_0000_1111_1111, 0b0000_0000_0000_0001));
        assertEquals(0b0000_0000___1000_0000_0000_0000___0010___0000, Alu.add16H(0b0111_1111_1000_0000, 0b0000_0000_1000_0000));
        assertEquals(0b0000_0000___0000_0000_0001_0000___0001___0000, Alu.add16H(0b1111_0000_0000_1111, 0b0001_0000_0000_0001));
        
        assertEquals(0b0000_0000___1111_1111_1111_1111___0000___0000, Alu.add16H(0b0101_0101_0101_0101, 0b1010_1010_1010_1010));
        assertEquals(0b0000_0000___0101_0101_0101_0100___0011___0000, Alu.add16H(0b1111_0101_1111_0101, 0b0101_1111_0101_1111));
        
        assertEquals(0b0000_0000___0011_0010_1011_1100___0010___0000, Alu.add16H(0b0000_0110_0100_1100, 0b0010_1100_0111_0000));
    }
    
    @Test
    void subWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1100___0000, Alu.sub(0,0,false));
        assertEquals(0b0000_0000_0000_0000___1111_1111___0111___0000, Alu.sub(0,0,true));
        
        assertEquals(0b0000_0000_0000_0000___1110_1111___0110___0000, Alu.sub(0b1111_0000, 0b0000_0001, false));
        assertEquals(0b0000_0000_0000_0000___1111_1111___0101___0000, Alu.sub(0b0000_1111, 0b0001_0000, false));
        assertEquals(0b0000_0000_0000_0000___0001_0000___0100___0000, Alu.sub(0b0101_0110, 0b0100_0101, true));
        
        assertEquals(0b0000_0000_0000_0000___1111_1110___0100___0000, Alu.sub(0b1111_1111, 0b0000_0001));
        assertEquals(0b0000_0000_0000_0000___0010_0001___0100___0000, Alu.sub(0b1001_0111, 0b0111_0110));
    }
    
    @Test
    void andWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1010___0000, Alu.and(0b0000_0000,0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1010___0000, Alu.and(0b0101_0101,0b1010_1010));
        
        assertEquals(0b0000_0000_0000_0000___1111_1111___0010___0000, Alu.and(0b1111_1111,0b1111_1111));
        assertEquals(0b0000_0000_0000_0000___0101_1010___0010___0000, Alu.and(0b0101_1010,0b0101_1010));
        
        assertEquals(0b0000_0000_0000_0000___0000_1010___0010___0000, Alu.and(0b0000_1111,0b1010_1010));
    }
    
    @Test
    void orWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.or(0b0000_0000,0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___1111_1111___0000___0000, Alu.or(0b0101_0101,0b1010_1010));
        
        assertEquals(0b0000_0000_0000_0000___1111_1111___0000___0000, Alu.or(0b1111_1111,0b1111_1111));
        assertEquals(0b0000_0000_0000_0000___0101_1010___0000___0000, Alu.or(0b0101_1010,0b0101_1010));
        
        assertEquals(0b0000_0000_0000_0000___1010_1111___0000___0000, Alu.or(0b0000_1111,0b1010_1010));
    }
    
    @Test
    void xorWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.xor(0b0000_0000,0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___1111_1111___0000___0000, Alu.xor(0b0101_0101,0b1010_1010));
        
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.xor(0b1111_1111,0b1111_1111));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.xor(0b0101_1010,0b0101_1010));
        
        assertEquals(0b0000_0000_0000_0000___1010_0101___0000___0000, Alu.xor(0b0000_1111,0b1010_1010));
    }
    
    @Test
    void shiftLeftWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.shiftLeft(0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1001___0000, Alu.shiftLeft(0b1000_0000));
        assertEquals(0b0000_0000_0000_0000___0001_0000___0000___0000, Alu.shiftLeft(0b0000_1000));
        
        assertEquals(0b0000_0000_0000_0000___1111_1110___0000___0000, Alu.shiftLeft(0b0111_1111));
        assertEquals(0b0000_0000_0000_0000___1000_0110___0001___0000, Alu.shiftLeft(0b1100_0011));
    }
    
    @Test
    void shiftRightAWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.shiftRightA(0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1001___0000, Alu.shiftRightA(0b0000_0001));
        assertEquals(0b0000_0000_0000_0000___0000_1000___0000___0000, Alu.shiftRightA(0b0001_0000));
        
        assertEquals(0b0000_0000_0000_0000___1111_1111___0000___0000, Alu.shiftRightA(0b1111_1110));
        assertEquals(0b0000_0000_0000_0000___1110_0001___0001___0000, Alu.shiftRightA(0b1100_0011));
    }
    
    @Test
    void shiftRightLWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.shiftRightL(0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1001___0000, Alu.shiftRightL(0b0000_0001));
        assertEquals(0b0000_0000_0000_0000___0000_1000___0000___0000, Alu.shiftRightL(0b0001_0000));
        
        assertEquals(0b0000_0000_0000_0000___0111_1111___0000___0000, Alu.shiftRightL(0b1111_1110));
        assertEquals(0b0000_0000_0000_0000___0110_0001___0001___0000, Alu.shiftRightL(0b1100_0011));
    }
    
    @Test
    void rotateTest() {
        Random rng = newRandom();
        for(int i=0; i<100; i++) {
            
            int val = rng.nextInt(255);
            
            assertEquals(val, Alu.unpackValue(Alu.rotate(RotDir.RIGHT, Alu.unpackValue(Alu.rotate(RotDir.LEFT,val) ) ) ) );
            
            int valCopy = val;
            for(int j=0; j<8; j++) {
                valCopy = Alu.unpackValue(Alu.rotate(RotDir.LEFT,valCopy));
            }
            assertEquals(val,valCopy); 
        }
        
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.rotate(RotDir.LEFT,  0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.rotate(RotDir.RIGHT, 0b0000_0000));
        
        assertEquals(0b0000_0000_0000_0000___0000_0001___0001___0000, Alu.rotate(RotDir.LEFT,  0b1000_0000));
        assertEquals(0b0000_0000_0000_0000___0100_0000___0000___0000, Alu.rotate(RotDir.RIGHT, 0b1000_0000));
        
        assertEquals(0b0000_0000_0000_0000___0000_0010___0000___0000, Alu.rotate(RotDir.LEFT,  0b0000_0001));
        assertEquals(0b0000_0000_0000_0000___1000_0000___0001___0000, Alu.rotate(RotDir.RIGHT, 0b0000_0001));
        
        assertEquals(0b0000_0000_0000_0000___0101_1001___0001___0000, Alu.rotate(RotDir.LEFT,  0b1010_1100));
        assertEquals(0b0000_0000_0000_0000___0101_0110___0000___0000, Alu.rotate(RotDir.RIGHT, 0b1010_1100));
    }
    
    void rotateCWithValues() {
        Random rng = newRandom();
        for(int i=0; i<100; i++) {
            
            int val = rng.nextInt(255);
            
            int res = Alu.rotate(RotDir.LEFT,val,true);
            assertEquals(val, Alu.rotate(RotDir.RIGHT, Alu.unpackValue(res), Bits.test(Alu.unpackFlags(res),4) ) );
            
            int valCopy = val;
            boolean curCarry = true;
            
            for(int j=0; j<9; j++) {
                int tmp = Alu.rotate(RotDir.LEFT,valCopy,curCarry);
                curCarry = Bits.test(Alu.unpackFlags(tmp),4);
                valCopy = Alu.unpackValue(tmp);
            }
            assertEquals(val,valCopy); 
        }
        
        assertEquals(0b0000_0000_0000_0000___0000_0001___1000___0000, Alu.rotate(RotDir.LEFT,  0b0000_0000, true));
        assertEquals(0b0000_0000_0000_0000___1000_0000___1000___0000, Alu.rotate(RotDir.RIGHT, 0b0000_0000, true));
        
        assertEquals(0b0000_0000_0000_0000___0000_0000___1001___0000, Alu.rotate(RotDir.LEFT,  0b1000_0000, false));
        assertEquals(0b0000_0000_0000_0000___0100_0000___1000___0000, Alu.rotate(RotDir.RIGHT, 0b1000_0000, false));
        
        assertEquals(0b0000_0000_0000_0000___0000_0011___1000___0000, Alu.rotate(RotDir.LEFT,  0b0000_0001, true));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1001___0000, Alu.rotate(RotDir.RIGHT, 0b0000_0001, false));
        
        assertEquals(0b0000_0000_0000_0000___0101_1000___1001___0000, Alu.rotate(RotDir.LEFT,  0b1010_1100, false));
        assertEquals(0b0000_0000_0000_0000___1101_0110___1000___0000, Alu.rotate(RotDir.RIGHT, 0b1010_1100, true));
    }
    
    @Test
    void swapWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1000___0000, Alu.swap(0b0000_0000));
        assertEquals(0b0000_0000_0000_0000___1000_0000___0000___0000, Alu.swap(0b0000_1000));
        
        assertEquals(0b0000_0000_0000_0000___0101_0101___0000___0000, Alu.swap(0b0101_0101));
        assertEquals(0b0000_0000_0000_0000___0011_1100___0000___0000, Alu.swap(0b1100_0011));
        assertEquals(0b0000_0000_0000_0000___0101_1010___0000___0000, Alu.swap(0b1010_0101));
        
        assertEquals(0b0000_0000_0000_0000___0000_1111___0000___0000, Alu.swap(0b1111_0000));
        assertEquals(0b0000_0000_0000_0000___1111_1111___0000___0000, Alu.swap(0b1111_1111));
    }
    
    @Test
    void testBitWithValues() {
        assertEquals(0b0000_0000_0000_0000___0000_0000___1010___0000, Alu.testBit(0b1111_0000,0));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1010___0000, Alu.testBit(0b1111_0000,1));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1010___0000, Alu.testBit(0b1111_0000,2));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1010___0000, Alu.testBit(0b1111_0000,3));
        assertEquals(0b0000_0000_0000_0000___0000_0000___0010___0000, Alu.testBit(0b1111_0000,4));
        assertEquals(0b0000_0000_0000_0000___0000_0000___0010___0000, Alu.testBit(0b1111_0000,5));
        assertEquals(0b0000_0000_0000_0000___0000_0000___0010___0000, Alu.testBit(0b1111_0000,6));
        assertEquals(0b0000_0000_0000_0000___0000_0000___0010___0000, Alu.testBit(0b1111_0000,7));
    }
    
    @Test
    void givenValuesOnWeb() {
        assertEquals(0xFF, Alu.unpackValue(0xFF70));
        assertEquals(0X70, Alu.unpackFlags(0xFF70));
        assertEquals(0x70, Alu.maskZNHC(false, true, true, true));
        assertEquals(0b0000_0000_0000_0000___0010_0101___0000___0000, Alu.add(0x10, 0x15));
        assertEquals(0b0000_0000_0000_0000___0001_0000___0010___0000, Alu.add(0x08, 0x08));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1011___0000, Alu.add(0x80, 0x7F, true));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1100___0000, Alu.sub(0x10, 0x10));
        assertEquals(0b0000_0000_0000_0000___1001_0000___0101___0000, Alu.sub(0x10, 0x80));
        assertEquals(0b0000_0000_0000_0000___1111_1111___0111___0000, Alu.sub(0x01, 0x01, true));
        assertEquals(0b0000_0000_0000_0000___0111_0011___0000___0000, Alu.bcdAdjust(0x6D, false, false, false));
        assertEquals(0b0000_0000_0000_0000___0000_1001___0100___0000, Alu.bcdAdjust(0x0F, true, true, false));
        assertEquals(0b0000_0000_0000_0000___0000_0011___0010___0000, Alu.and(0x53, 0xA7));
        assertEquals(0b0000_0000_0000_0000___1111_0111___0000___0000, Alu.or(0x53, 0xA7));
        assertEquals(0b0000_0000_0000_0000___1111_0100___0000___0000, Alu.xor(0x53, 0xA7));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1001___0000, Alu.shiftLeft(0x80));
        assertEquals(0b0000_0000_0000_0000___0100_0000___0000___0000, Alu.shiftRightL(0x80));
        assertEquals(0b0000_0000_0000_0000___1100_0000___0000___0000, Alu.shiftRightA(0x80));
        assertEquals(0b0000_0000_0000_0000___0000_0001___0001___0000, Alu.rotate(RotDir.LEFT, 0x80));
        assertEquals(0b0000_0000_0000_0000___0000_0000___1001___0000, Alu.rotate(RotDir.LEFT, 0x80, false));
        assertEquals(0b0000_0000_0000_0000___0000_0001___0000___0000, Alu.rotate(RotDir.LEFT, 0x00, true));
        assertEquals(0b0000_0000___0001_0010_0000_0000___0011___0000, Alu.add16L(0x11FF, 0x0001));
        assertEquals(0b0000_0000___0001_0010_0000_0000___0000___0000, Alu.add16H(0x11FF, 0x0001));
    }
    
}

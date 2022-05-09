package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

public class BitVectorTest {
    @Test
    void GivenTests() {
        
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = v1.extractZeroExtended(-17, 32).not();
        BitVector v3 = v2.extractWrapped(11, 64);
        
        assertEquals("11111111111111111111111111111111", v1.toString());
        assertEquals("00000000000000011111111111111111", v2.toString());
        assertEquals("1111111111100000000000000011111111111111111000000000000000111111", v3.toString());
        
        BitVector v = new BitVector.Builder(32)
            .setByte(0, 0b1111_0000)
            .setByte(1, 0b1010_1010)
            .setByte(3, 0b1100_1100)
            .build();
        
        assertEquals("11001100000000001010101011110000", v.toString());
        
        BitVector.Builder nintendo = new BitVector.Builder(128);
        nintendo.setByte(0,  0b00001111);
        nintendo.setByte(1,  0b00111111);
        nintendo.setByte(2,  0b11001111);
        nintendo.setByte(3,  0b11000011);
        nintendo.setByte(4,  0b11110011);
        nintendo.setByte(5,  0b11111100);
        nintendo.setByte(6,  0b00111111);
        nintendo.setByte(7,  0b00001111);
        nintendo.setByte(8,  0b11001111);
        nintendo.setByte(9,  0b11000011);
        nintendo.setByte(10, 0b11110011);
        nintendo.setByte(11, 0b11110000);
        nintendo.setByte(12, 0b00000000);
        nintendo.setByte(13, 0b00000000);
        nintendo.setByte(14, 0b00000000);
        nintendo.setByte(15, 0b00000000);
        
        assertEquals(nintendo.build().shift(3).toString(), "00000000000000000000000000000111100001111001111000011110011110000111100111111111111001111001111000011110011110011111100001111000");
        

        nintendo = new BitVector.Builder(160);
        nintendo.setByte(0,  0b10101010);
        nintendo.setByte(1,  0b10101010);
        nintendo.setByte(2,  0b10101010);
        nintendo.setByte(3,  0b10101010);
        nintendo.setByte(4,  0b11111111);
        nintendo.setByte(5,  0b11111111);
        nintendo.setByte(6,  0b11111111);
        nintendo.setByte(7,  0b10111111);
        nintendo.setByte(8,  0b00000000);
        nintendo.setByte(9,  0b00000000);
        nintendo.setByte(10,  0b11111110);
        nintendo.setByte(11,  0b11111111);
        nintendo.setByte(12,  0b01101010);
        nintendo.setByte(13,  0b00000000);
        nintendo.setByte(14,  0b00000000);
        nintendo.setByte(15,  0b00000000);
        
        nintendo.setByte(16,  0b10101010);
        nintendo.setByte(17,  0b10101010);
        nintendo.setByte(18,  0b10101010);
        nintendo.setByte(19,  0b10101010);
        
        assertEquals(nintendo.build().extractWrapped(-65,160).toString(), "1111111111111100000000000000000101111111111111111111111111111111010101010101010101010101010101010101010101010101010101010101010000000000000000000000000011010101");
        
        
        //assertEquals(nintendo.build().extractWrapped(43,160).toString(), "");
        
        
        nintendo = new BitVector.Builder(128);
        
        nintendo.setByte(0,  0b11111111);
        nintendo.setByte(1,  0b11111111);
        nintendo.setByte(2,  0b11111111);
        nintendo.setByte(3,  0b11111111);
        
        assertEquals(nintendo.build().extractZeroExtended(-32,64).toString(), "1111111111111111111111111111111100000000000000000000000000000000");
        
        
        
        
    }
}

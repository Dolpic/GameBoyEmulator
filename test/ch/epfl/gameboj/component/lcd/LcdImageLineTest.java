package ch.epfl.gameboj.component.lcd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.epfl.gameboj.bits.BitVector;

public class LcdImageLineTest {
    
    @Test
    public void test1() {
        BitVector msb     = new BitVector.Builder(32).setByte(0, 0b1111_0000 ).setByte(1, 0b1010_0101).build();
        BitVector lsb     = new BitVector.Builder(32).setByte(0, 0b0101_1010 ).setByte(1, 0b1111_0000).build();
        BitVector opacity = new BitVector.Builder(32).setByte(0, 0b0000_1010 ).setByte(1, 0b0101_1111).build();
        
        LcdImageLine line = new LcdImageLine(msb,lsb,opacity);
       
        assertEquals(32, line.size());
        
        LcdImageLine line2 = line.shift(0);
        
        assertEquals(line2.msb().toString(),     "00000000000000001010010111110000");
        assertEquals(line2.lsb().toString(),     "00000000000000001111000001011010");
        assertEquals(line2.opacity().toString(), "00000000000000000101111100001010");
        
        line2 = line.shift(5);
        
        assertEquals(line2.msb().toString(),     "00000000000101001011111000000000");
        assertEquals(line2.lsb().toString(),     "00000000000111100000101101000000");
        assertEquals(line2.opacity().toString(), "00000000000010111110000101000000");
        
        line2 = line2.shift(-5);
        
        assertEquals(line2.msb().toString(),     "00000000000000001010010111110000");
        assertEquals(line2.lsb().toString(),     "00000000000000001111000001011010");
        assertEquals(line2.opacity().toString(), "00000000000000000101111100001010");
        
        line2 = line.extractWrapped(0, 32);
        
        assertEquals(line2.msb().toString(),     "00000000000000001010010111110000");
        assertEquals(line2.lsb().toString(),     "00000000000000001111000001011010");
        assertEquals(line2.opacity().toString(), "00000000000000000101111100001010");
        
        line2 = line.extractWrapped(32, 32);
        
        assertEquals(line2.msb().toString(),     "00000000000000001010010111110000");
        assertEquals(line2.lsb().toString(),     "00000000000000001111000001011010");
        assertEquals(line2.opacity().toString(), "00000000000000000101111100001010");
        
        line2 = line.extractWrapped(16, 32);
        
        assertEquals(line2.msb().toString(),     "10100101111100000000000000000000");
        assertEquals(line2.lsb().toString(),     "11110000010110100000000000000000");
        assertEquals(line2.opacity().toString(), "01011111000010100000000000000000");
        
        line2 = line.extractWrapped(12, 32);
                                                  
        assertEquals(line2.msb().toString(),     "01011111000000000000000000001010");
        assertEquals(line2.lsb().toString(),     "00000101101000000000000000001111");
        assertEquals(line2.opacity().toString(), "11110000101000000000000000000101");
        
        LcdImageLine line3 = line.below(line2);

        assertEquals(line3.msb().toString(),     "01010000000000001010010111110000");
        assertEquals(line3.lsb().toString(),     "00000000101000001111000001011111");
        assertEquals(line3.opacity().toString(), "11110000101000000101111100001111");
        
        msb     = new BitVector.Builder(64).setByte(0, 0b1111_0000).setByte(1, 0b1010_0101).setByte(2, 0b1010_0101).setByte(3, 0b1111_0000).setByte(4, 0b1111_0000).build();
        lsb     = new BitVector.Builder(64).setByte(0, 0b0101_1010).setByte(1, 0b1111_0000).setByte(2, 0b1111_0000).setByte(3, 0b1010_0101).setByte(4, 0b1111_0000).build();
        opacity = new BitVector.Builder(64).setByte(0, 0b0000_1010).setByte(1, 0b0101_1111).setByte(2, 0b1111_0000).setByte(3, 0b1111_0000).setByte(4, 0b1111_0000).build();
        
        line  = new LcdImageLine(msb,lsb,opacity);
        line2 = new LcdImageLine(lsb,msb,opacity);
        
        line3 = line.join(32, line2);

        assertEquals(line3.msb().toString(),     "1010010111110000111100000101101011110000101001011010010111110000");
        assertEquals(line3.lsb().toString(),     "1111000010100101101001011111000010100101111100001111000001011010");
        assertEquals(line3.opacity().toString(), "1111000011110000010111110000101011110000111100000101111100001010");   
       
    }

}

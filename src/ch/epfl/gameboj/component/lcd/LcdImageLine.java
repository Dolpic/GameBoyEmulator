package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * This class represents a line on the GameBoy LCD screen
 * 
 * @author Corentin Junod (283214)
 */
public final class LcdImageLine {
    
    /** The number of colors used by the Gameboy */
    public static final int NUMBER_OF_COLORS = 4;
    /** The palette that doesn't change the colors */
    public static final int IDENTITY_PALETTE = 0b11_10_01_00;
    
    private final BitVector msb;
    private final BitVector lsb;
    private final BitVector opacity; //0=transparent, 1=opaque
    
    /**
     * Create a new LCD line with it's colors and opacity
     * 
     * @param msb
     *            The color's most significant bits as a BitVector
     * @param lsb
     *            The color's least significant bits as a BitVector
     * @param opacity
     *            The opacity bits as a BitVector
     * @throws IllegalArgumentException
     *             if the three BitVectors are not the same size
     * @throws NullPointerException
     *             if one or more of the three BitVectors is null
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Objects.requireNonNull(msb);
        Objects.requireNonNull(lsb);
        Objects.requireNonNull(opacity);
        Preconditions.checkArgument(msb.size() == lsb.size() && msb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }
    
    /**
     * Returns the number of pixel of the line
     * 
     * @return the number of pixel of the line
     */
    public int size() {
        return msb.size();
    }
    
    /**
     * Returns the BitVector containing the most significant bit of each pixel
     * 
     * @return the msb BitVector
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Returns the BitVector containing the least significant bit of each pixel
     * 
     * @return the lsb BitVector
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Returns the BitVector containing the opacity of each pixel
     * 
     * @return the opacity BitVector
     */
    public BitVector opacity() {
        return opacity;
    }
    
    /**
     * Shift over a given distance the whole line
     * 
     * @param distance
     *            the distance of the shift, positive means to the right, negative means to the left
     * @return the calling LcdImageLine shifted over the given distance
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), 
                                lsb.shift(distance),
                                opacity.shift(distance));
    }
    
    /**
     * Extract the line starting with a given index of a given length with a wrapped-extension
     * 
     * @param startIndex
     *            The index of the first bit if the sub-line
     * @param size
     *            The size in pixels of the sub-line
     * @return The calling line but wrapped-extended with the given parameters
     * @throws IllegalArgumentException
     *             if size is negative or not a multiple of 32
     */
    public LcdImageLine extractWrapped(int startIndex, int size) {
        return new LcdImageLine(msb.extractWrapped(startIndex, size),
                                lsb.extractWrapped(startIndex, size),
                                opacity.extractWrapped(startIndex, size));
    }
    
    /**
     * Returns the calling line below a given line with a given opacity
     * 
     * @param line
     *            The line above the calling line
     * @param opacity
     *            The opacity applied to the above line's pixels
     * @return The calling line below the given line
     * @throws NullPointerException
     *             if the given line or opacity is null
     * @throws IllegalArgumentException
     *             if the given line or opacity is not the same size as the calling line
     */
    public LcdImageLine below(LcdImageLine line, BitVector opacity) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(opacity);
        BitVector newOpacity = this.opacity.or(opacity);
        BitVector newMsb = opacity.not().and(msb).or(opacity.and(line.msb));
        BitVector newLsb = opacity.not().and(lsb).or(opacity.and(line.lsb));
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * Returns the calling line below a given line with the given line opacity
     * 
     * @param line
     *            The line above the calling line
     * @return The calling line below the given line
     * @throws NullPointerException
     *             if the given line is null
     * @throws IllegalArgumentException
     *             if the given line is not the same size as the calling line
     */
    public LcdImageLine below(LcdImageLine line) {
        return below(line, line.opacity);
    }
    
    /**
     * Join two lines together at a given pixel
     * 
     * @param juncPixel
     *            From this pixel, the given line will be used
     * @param line
     *            The line used from the junction pixel
     * @return The calling line joined with the given line at the junction pixel
     * @throws NullPointerException
     *             if the given line is null
     * @throws IllegalArgumentException
     *             if the given line is not the same size as the calling line
     */
    public LcdImageLine join(int juncPixel, LcdImageLine line) {
        Objects.requireNonNull(line);
        BitVector mask = new BitVector(size(), true).shift(juncPixel).not();

        BitVector newOpacity = (opacity.and(mask)).or(line.opacity.shift(juncPixel));
        BitVector newMsb = (msb.and(mask)).or(line.msb.shift(juncPixel));
        BitVector newLsb = (lsb.and(mask)).or(line.lsb.shift(juncPixel));

        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }
    
    /**
     * Change the colors of the calling line following the given palette
     * 
     * @param palette
     *            The palette used to change the line colors
     * @return The calling line with it's colors changed following the given palette
     * @throws IllegalArgumentException
     *             if the given palette is not an 8 bits value
     */
    public LcdImageLine mapColor(int palette) {
        Preconditions.checkBits8(palette);

        if (palette == IDENTITY_PALETTE) {
            return new LcdImageLine(msb, lsb, opacity);
        } else {
            BitVector newMsb = new BitVector(size());
            BitVector newLsb = new BitVector(size());

            BitVector[] masks = new BitVector[]{
                msb.not().and(lsb.not()),
                msb.not().and(lsb),
                msb.and(lsb.not()),
                msb.and(lsb)
            };

            for (int i = 0; i < NUMBER_OF_COLORS; i++) {
                if (Bits.test(palette, 2 * i + 1)) 
                    newMsb = newMsb.or(masks[i]);
                if (Bits.test(palette, 2 * i)) 
                    newLsb = newLsb.or(masks[i]);
            }
            return new LcdImageLine(newMsb, newLsb, opacity);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof LcdImageLine) {
            LcdImageLine that0 = (LcdImageLine) that;
            return (msb.equals(that0.msb) && lsb.equals(that0.lsb) && opacity.equals(that0.opacity));
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }
    
    /** Builder **/
    
    /** Represents a Builder to build a LcdImageLine */
    public final static class Builder{
        
        private BitVector.Builder msbBuilder;
        private BitVector.Builder lsbBuilder;
        
        /**
         * Create a new LcdImageLine builder
         * 
         * @param width
         *            the width of the new line
         * @throws IllegalArgumentException
         *             if the given width is <= 0 or it is not a multiple of 32
         */
        public Builder(int width) {
            msbBuilder = new BitVector.Builder(width);
            lsbBuilder = new BitVector.Builder(width);
        }
        
        /**
         * Set 8 pixels at a given position to a given color
         * 
         * @param index
         *            The index of the 8 pixels to set
         * @param msbByte
         *            The most significant bits of the new colors
         * @param lsbByte
         *            The least significant bits of the new colors
         * @throws IndexOutOfBoundsException
         *             if the given index is negative or greater than the number
         *             of bytes in the line
         * @throws IllegalArgumentException if the given msb and lsb are not 8
         *         bits values
         */
        public void setBytes(int index, int msbByte, int lsbByte) {
            msbBuilder.setByte(index, msbByte);
            lsbBuilder.setByte(index, lsbByte);
        }
        
        /**
         * Build the new line
         * 
         * @return A new LcdImageLine as sets by the user
         */
        public LcdImageLine build() {
            BitVector msb = msbBuilder.build();
            BitVector lsb = lsbBuilder.build();
            return new LcdImageLine(msb, lsb, msb.or(lsb));
        }
    }
    
}

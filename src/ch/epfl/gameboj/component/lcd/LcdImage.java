package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * This class represents an image on the GameBoy LCD screen
 * 
 * @author Corentin Junod (283214)
 */
public final class LcdImage {
    
    private final List<LcdImageLine> listLines;
    private final int width;
    private final int height;

    /**
     * Create a new image of a given size made of a given list of lines
     * 
     * @param width
     *            The width of the new image
     * @param height
     *            The height of the new image
     * @param listLines
     *            The list of all the new image lines
     * @throws NullPointerException
     *             if the given list is null
     * @throws IllegalArgumentException
     *             if the width or the height is negative
     */
    public LcdImage(int width, int height, List<LcdImageLine> listLines) {
        Objects.requireNonNull(listLines);
        Preconditions.checkArgument(width > 0 && height > 0);
        
        this.width = width;
        this.height = height;
        this.listLines = Collections.unmodifiableList(new ArrayList<>(listLines));
    }
    
    /**
     * Returns the width of the image
     * 
     * @return the width of the image
     */
    public int width() {
        return width;
    }
    
    /**
     * Returns the height of the image
     * 
     * @return the height of the image
     */
    public int height() {
        return height;
    }
    
    /**
     * Returns the color of a given pixel on the image
     * 
     * @param x
     *            The x coordinate of the pixel to read
     * @param y
     *            The y coordinate of the pixel to read
     * @return The color of the given pixel
     * @throws IllegalArgumentException
     *             if one of the parameters is negative
     */
    public int get(int x, int y) {
        Preconditions.checkArgument(x >= 0 && y >= 0);
        return (listLines.get(y).msb().testBit(x) ? 0b10 : 0b00) 
             | (listLines.get(y).lsb().testBit(x) ? 0b01 : 0b00);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof LcdImage) {
            LcdImage that0 = (LcdImage) that;
            return (listLines.equals(that0.listLines) 
                   && height == that0.height
                   && width  == that0.width);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(listLines, width, height);
    }
    
    /** Builder **/
    
    /** Represents a Builder to build a LcdImage */
    public static final class Builder {
        
        private final List<LcdImageLine> listLines;
        private final int width;
        private final int height;
        
        /**
         * Create a new builder of ImageLine
         * 
         * @param width
         *            The width of the new image
         * @param height
         *            The height of the new image
         * @throws IllegalArgumentException
         *             if one of the parameters is null or negative, 
         *             or width is not a multiple of 32
         */
        public Builder(int width, int height) {
            Preconditions.checkArgument(width > 0 && height > 0 && width % Integer.SIZE == 0);

            this.width = width;
            this.height = height;
            listLines = new ArrayList<>(height);

            for (int i = 0; i < height; i++) {
                listLines.add(new LcdImageLine.Builder(width).build());
            }
        }
        
        /**
         * Set a new line of the image
         * 
         * @param index
         *            The index of the line to set
         * @param line
         *            The LcdImageLine used to set the line
         * @throws IndexOutOfBoundsException
         *             if the given index is negative or greater than the width
         *             of the image
         * @throws IllegalArgumentException
         *             if the given line is null, or it is not the same size as
         *             the image
         */
        public void setLine(int index, LcdImageLine line) {
            Objects.checkIndex(index, width);
            Objects.requireNonNull(line);
            Preconditions.checkArgument(line.size() == width);
            listLines.set(index, line);
        }
        
        /**
         * Build the new image
         * 
         * @return the new image
         */
        public LcdImage build() {
            return new LcdImage(width, height, listLines);
        }
    }
}

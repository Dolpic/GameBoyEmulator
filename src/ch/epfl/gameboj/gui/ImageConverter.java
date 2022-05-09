package ch.epfl.gameboj.gui;

import java.util.Objects;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * This class convert a LcdImage to a JavaFX Image
 * 
 * @author Corentin Junod (283214)
 */
public class ImageConverter {
    private ImageConverter() {}

    /**
     * Converts a LcdImage to a JavaFX Image
     * 
     * @param image
     *            The LcdImage to convert
     * @return A new JavaFX Image containing the same pixels as the given
     *         LcdImage
     * @throws IllegalArgumentException
     *             If the given image is not the same size as a Gameboy LCD
     *             screen
     * @throws NullPointerException
     *             if the given image is null
     */
    public static Image convert(LcdImage image) {
       return convert(image, GameBoy.DEFAULT_COLOR_MAP);
    }
    
    /**
     * Converts a LcdImage to a JavaFX Image with a given colorMap
     * 
     * @param image
     *            The LcdImage to convert
     * @param colorMap
     *            A table containing 
     * @return A new JavaFX Image containing the same pixels as the given
     *         LcdImage
     * @throws IllegalArgumentException
     *             If the given image is not the same size as a Gameboy LCD
     *             screen
     * @throws NullPointerException
     *             if the given image is null
     */
    public static Image convert(LcdImage image, int[] colorMap) {
        Objects.requireNonNull(image);
        Preconditions.checkArgument(image.height() == LcdController.LCD_HEIGHT);
        Preconditions.checkArgument(image.width() == LcdController.LCD_WIDTH);
        Preconditions.checkArgument(colorMap.length == GameBoy.DEFAULT_COLOR_MAP.length);
        WritableImage result = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
        
        for (int y = 0; y < LcdController.LCD_HEIGHT; y++) {
            for (int x = 0; x < LcdController.LCD_WIDTH; x++) {
                result.getPixelWriter().setArgb(x, y, colorMap[image.get(x, y)]);
            }
        }
        return result;
    }
}

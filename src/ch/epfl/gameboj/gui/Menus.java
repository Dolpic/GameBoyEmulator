package ch.epfl.gameboj.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class Menus {
    
    /** An alternative Color Map for the gameboy **/
    public final static int[] SEPIA_COLOR_MAP = new int[] { 
        0xFF_FE_E1_D3, 0xFF_BB_9E_90, 0xFF_7C_5F_51, 0xFF_37_1A_0C 
    };
    
    /** An alternative Color Map for the gameboy **/
    public final static int[] GREEN_COLOR_MAP = new int[] { 
        0xFF_E7_FF_42, 0xFF_9C_AD_29, 0xFF_7B_84_21, 0xFF_42_4A_21 
    };
    
    /** An alternative Color Map for the gameboy **/
    public final static int[] BLUE_COLOR_MAP = new int[] { 
        0xFF_E6_EB_FF, 0xFF_7E_90_FF, 0xFF_3F_48_7F, 0xFF_1F_24_40
    };
    
    private final Stage stage;
    private final ImageView imageView;
    private final GameBoy gameboy;
    
    private final MenuBar menuBar = new MenuBar();
    
    private final Menu file     = new Menu("Fichier");
    private final MenuItem open = new MenuItem("Ouvrir une ROM");
    private final MenuItem screenshot = new MenuItem("Faire une capture d'écran");
    private final MenuItem quit = new MenuItem("Quitter");
    
    private final Menu simulation = new Menu("Simulation");
    private final MenuItem speed  = new MenuItem("Vitesse");
    private final Menu colors     = new Menu("Couleurs");
    private final MenuItem defaultColor = new MenuItem("Noir et blanc");
    private final MenuItem green = new MenuItem("Vert");
    private final MenuItem blue  = new MenuItem("Bleu");
    private final MenuItem sepia = new MenuItem("Sepia");
    private final MenuItem size  = new MenuItem("Taille");
    
    private double currentScaleFactor;
    
    /**
     * Create a new Menu bar for the emulator
     * 
     * @param stage
     *            The main Stage of the application
     * @param gameboy
     *            The gameboy used in the application
     * @param imageView
     *            The imageView used to print the gameboy screen
     */
    public Menus(Stage stage, GameBoy gameboy, ImageView imageView){
        Objects.requireNonNull(stage);
        Objects.requireNonNull(gameboy);
        Objects.requireNonNull(imageView);
        
        this.stage = stage;
        this.gameboy = gameboy;
        this.imageView = imageView;
        
        currentScaleFactor = Main.DEFAULT_SCALE_FACTOR;
      
        file.getItems().addAll(open, screenshot ,quit);
        colors.getItems().addAll(defaultColor, green, blue, sepia);
        simulation.getItems().addAll(speed, colors, size);
        menuBar.getMenus().addAll(file, simulation);
        
        assignEvents();
    };
    
    /**
     * Return the generated menus bar
     * @return the generated menus bar
     */
    public MenuBar generate() {
        return menuBar;
    }
    
    private void assignEvents() {
        open.setOnAction(new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent t) {changeCartridge();} 
        });
        
        speed.setOnAction(new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent t) {changeSpeedSize();}
        });
        
        size.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {changeScreenSize();}
        });
        
        screenshot.setOnAction(new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent t) {screenShot();} 
        });
        
        quit.setOnAction(new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent t) {System.exit(0);} 
        });
        
        assignColorMapEvent(defaultColor, GameBoy.DEFAULT_COLOR_MAP);
        assignColorMapEvent(sepia, SEPIA_COLOR_MAP);
        assignColorMapEvent(green, GREEN_COLOR_MAP);
        assignColorMapEvent(blue,  BLUE_COLOR_MAP);
    }
    
    private void assignColorMapEvent(MenuItem item, int[] newColorMap){
        item.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {gameboy.setColorMap(newColorMap);}
        });
    }
    
    private void screenShot() {
        LcdImage image = gameboy.lcdController().currentImage();
        int[] colorMap = gameboy.currentColorMap();
        BufferedImage outputImage = new BufferedImage(image.width(), 
                                                      image.height(), 
                                                      BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.height(); y++) {
            for (int x = 0; x < image.width(); x++) {
                outputImage.setRGB(x, y, colorMap[image.get(x, y)]);
            }
        }
        
        try {
            ImageIO.write(outputImage, "png", new File("screenshot.png"));
        }catch(IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Une erreur est survenue lors de l'écriture du fichier");
            alert.showAndWait();
        }
    }
    
    private void changeCartridge() {
        try {
            File file = openFileChooser(stage);
            if(file != null)
                gameboy.start(Cartridge.ofFile(file), imageView);
        }catch(Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Fichier invalide!");
            alert.setContentText("Le fichier sélectionné n'est pas un fichier ROM supporté");
            alert.showAndWait();
        }
    }
    
    private void changeScreenSize() {
        currentScaleFactor = sliderDialog("Taille de l'écran",
                "Sélectionnez la taille de l'écran", 
                1, 5, currentScaleFactor);
        imageView.setFitHeight(LcdController.LCD_HEIGHT * currentScaleFactor);
        imageView.setFitWidth(LcdController.LCD_WIDTH * currentScaleFactor);
        stage.sizeToScene();
    }
    
    private void changeSpeedSize() {
        gameboy.setSpeed(
                sliderDialog(
                "Vitesse d'émulation",
                "Sélectionnez la vitesse d'émulation",
                0, 7,
                gameboy.currentSpeed())
        );
    }
    
    private double sliderDialog(String title, String text, double min, double max, double defaultValue) {
        Slider slider = new Slider();
        slider.setMin(min);
        slider.setMax(max);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setValue(defaultValue);
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(text);
        dialog.getDialogPane().setContent(slider);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.get() == ButtonType.OK) 
            return slider.getValue();
        else 
            return defaultValue;
    }
    
    private File openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez la ROM à démarrer");
        return fileChooser.showOpenDialog(stage);
    }
    
}

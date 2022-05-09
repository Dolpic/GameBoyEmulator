package ch.epfl.gameboj.gui;


import java.util.HashMap;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    public static final int DEFAULT_SCALE_FACTOR = 2;
    
    private GameBoy gameboy;
    private ImageView imageView;
    private Menus menus;
    
    private Map<String, Joypad.Key> keyMapText;
    private Map<KeyCode,Joypad.Key> keyMapCode;
    
    /**
     * The main function of the program. The program exits if more than one
     * argument is given
     * 
     * @param args
     *            Only one argument is accepted, it must be a path to the ROM to
     *            load
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /* (non-Javadoc)
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage stage) throws Exception {

        gameboy    = new GameBoy();
        imageView  = new ImageView();
        keyMapText = new HashMap<>();
        keyMapCode = new HashMap<>();
        menus      = new Menus(stage, gameboy, imageView);

        setKeyMap();
        buildGui(stage);
    }
    
    private void buildGui(Stage stage) {
        imageView.setFitHeight(LcdController.LCD_HEIGHT * DEFAULT_SCALE_FACTOR);
        imageView.setFitWidth(LcdController.LCD_WIDTH * DEFAULT_SCALE_FACTOR);

        imageView.setOnKeyPressed(e -> {
            dispatchEvent(e, true);
        });
        imageView.setOnKeyReleased(e -> {
            dispatchEvent(e, false);
        });

        BorderPane pane = new BorderPane();
        pane.setCenter(imageView);
        pane.setTop(menus.generate());

        stage.setTitle("GameBoj");
        stage.setScene(new Scene(pane));
        stage.sizeToScene();
        stage.show();
        imageView.requestFocus();
    }
    
    
    private void dispatchEvent(KeyEvent e, boolean isPressed) {
        if (keyMapText.containsKey(e.getText()))
            gameboy.joypad().setKey(keyMapText.get(e.getText()), isPressed);
        else if (keyMapCode.containsKey(e.getCode()))
            gameboy.joypad().setKey(keyMapCode.get(e.getCode()), isPressed);
    }
    
    private void setKeyMap() {
        keyMapText.put("a", Key.A);
        keyMapText.put("b", Key.B);
        keyMapText.put(" ", Key.SELECT);
        keyMapText.put("s", Key.START);
        keyMapCode.put(KeyCode.LEFT , Key.LEFT);
        keyMapCode.put(KeyCode.RIGHT, Key.RIGHT);
        keyMapCode.put(KeyCode.UP   , Key.UP);
        keyMapCode.put(KeyCode.DOWN , Key.DOWN);
    }

}

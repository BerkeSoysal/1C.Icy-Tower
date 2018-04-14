package view;

import controller.GameController;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import model.logic.ButtonManager;
import model.logic.CharacterManager;
import model.logic.GameEngine;
import model.logic.SoundManager;


public class GameFrame {

    /**
     * The instance of GameEngine that will be showed
     */
    private GameEngine gameEngine;
    /**
     * GameController instance that will take input from user
     */
    private GameController gameController;
    /**
     * Timeline will controll the time between each frame
     */
    Timeline timeline;
    /**
     * The Scene that will be shown is held in gameScene
     */
    private Scene gameScene;
    /**
     * MediaPlayer instance will play the musics in game
     */
    public MediaPlayer mediaplayer;

    /**
     * This is the default JavaFX method to run animations
     *
     */
    public Scene start() {

        playSong();
        KeyCode[] kc = createKeycode();


        gameEngine = new GameEngine();

        Image[] charIms = CharacterManager.getInstance().getCharacterImages();

        gameEngine.setCurrentCharactersImages(charIms);

        gameScene = new Scene(gameEngine.convertMapToPane(), 800, 600);

        gameController = new GameController(gameScene, kc, gameEngine);

        timeline = new Timeline(new KeyFrame(
                Duration.millis(50),
                ae -> updateFrame()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        return gameScene;
    }

    /**
     * This method gets the game buttons from the ButtonManager and pass them to GameEngine
     * @return KeyCode
     */
    private KeyCode[] createKeycode() {

        KeyCode[] kc = ButtonManager.getInstance().getButtons();
        return kc;
    }

    /**
     * This method gets the sound settings from SoundManager and plays them
     *
     */
    public void playSong(){

        Media media = SoundManager.getInstance().getSelectedSong();
        mediaplayer = new MediaPlayer(media);
        mediaplayer.setAutoPlay(true);
        mediaplayer.setVolume(SoundManager.getInstance().getVolume());
        mediaplayer.setOnEndOfMedia(() -> mediaplayer.seek(Duration.ZERO));
        mediaplayer.play();
    }

    /**
     * This method updates the frame at each timer instance
     *
     */
    private void updateFrame(){
        gameEngine.convertMapToPane();
        if(gameEngine.getMap().gameOver())
        {
            timeline.stop();
            mediaplayer.stop();
        }

    }
}

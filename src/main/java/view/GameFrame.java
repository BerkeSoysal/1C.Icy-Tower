package view;

import controller.GameController;
import javafx.animation.Animation;
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
import model.network.GameStatePacket;
import model.network.NetworkManager;
import model.entity.Map;
import model.entity.Character;
import java.util.Random;


public class GameFrame {
    private GameFrame instance;
    private GameFrame(){

    }
    public GameFrame getInstance(){
        if(instance == null)
        {
            instance = new GameFrame();
        }
        return  instance;
    }
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
    public static MediaPlayer mediaplayer;

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    private int difficulty;



    private int gameSpeed;
    private boolean seedReceived = false;
    private boolean waitingForSeed = false;


    public GameFrame(int difficulty){
        this.difficulty = difficulty;
        gameSpeed = 40;
        timeline = new Timeline(new KeyFrame(
                Duration.millis(gameSpeed),
                ae -> updateFrame()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    public Scene start() {

        // Handle Multiplayer Setup
        if (Map.getInstance().isMultiplayer()) {
            if (NetworkManager.getInstance().isHost()) {
                // Host sends seed immediately
                // We need to retrieve the seed we set in Map
                // Actually Map doesn't expose seed getter, but we set Random.
                // We should have passed the seed.
                // Let's modify Map to store seed or just send a new one and re-seed.
                // But Map is already initialized in MultiplayerMenuController.
                // Let's just generate a seed here if host, or assume it's set.
                // For simplicity, let's send a dummy seed or retrieve it from Map if possible.
                // The random object in Map is private.
                // Let's just send a new random long as seed, and reset Map with it.
                long seed = new Random().nextLong();
                // Ensure host resets map with the new seed so it matches what the client will generate
                // And regenerates platforms!
                GameEngine.getInstance().resetForMultiplayer(seed);
                NetworkManager.getInstance().sendState(new GameStatePacket(seed));
                timeline.play();
            } else {
                // Client waits for seed
                waitingForSeed = true;
                timeline.play(); // We play, but updateFrame handles waiting
            }
        } else {
            timeline.play();
        }

        playSong();
        KeyCode[] kc = createKeycode();

        Image[] charIms = CharacterManager.getInstance().getCharacterImages();

        GameEngine.init(difficulty);


        GameEngine.getInstance().loadCurrentCharactersImages(charIms);
        // Also load images for opponent if multiplayer
        if (Map.getInstance().isMultiplayer()) {
             Map.getInstance().addOpponentToGameObjects();
             // Just reuse same images for now
             if (Map.getInstance().getOpponentCharacter() != null) {
                 Map.getInstance().getOpponentCharacter().setImages(charIms);
             }
        }

        gameScene = new Scene(GameEngine.getInstance().convertMapToPane(), 800, 600);

        gameController = new GameController(gameScene, kc, GameEngine.getInstance());




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
    public static MediaPlayer  playSong(){

        Media media = SoundManager.getInstance().getSelectedSong();
        mediaplayer = new MediaPlayer(media);
        mediaplayer.setAutoPlay(true);
        mediaplayer.setVolume(SoundManager.getInstance().getVolume());
        mediaplayer.setOnEndOfMedia(() -> mediaplayer.seek(Duration.ZERO));
        mediaplayer.play();
        return mediaplayer;
    }

    /**
     * This method updates the frame at each timer instance
     *
     */
    private void updateFrame(){

        if (waitingForSeed) {
            GameStatePacket packet = NetworkManager.getInstance().getLatestPacket();
            if (packet != null && packet.isInit) {
                GameEngine.getInstance().resetForMultiplayer(packet.seed);
                waitingForSeed = false;
            } else {
                return; // Wait for seed
            }
        }

        if (Map.getInstance().isMultiplayer()) {
            NetworkManager nm = NetworkManager.getInstance();
            if (nm.isConnected()) {
                // Send Local State
                Character myChar = Map.getInstance().getGameCharacter();
                GameStatePacket myPacket = new GameStatePacket();
                myPacket.x = myChar.getPosX();
                myPacket.y = myChar.getPosY();
                myPacket.altitude = Map.getInstance().getAltitude();
                myPacket.vx = myChar.getHorizontalVelocity();
                myPacket.vy = myChar.getVerticalVelocity();
                myPacket.score = myChar.getScore();
                myPacket.movingLeft = myChar.isMovingLeft();
                myPacket.movingRight = myChar.isMovingRight();
                myPacket.standing = myChar.isStanding();
                myPacket.comboJumping = myChar.isComboJumping();
                myPacket.barExtendTaken = Map.getInstance().isBarExtendTaken();
                myPacket.barExtendTakenBar = Map.getInstance().getBarExtendTakenBar();
                // Check if I died
                if (GameEngine.getInstance().getMap().gameOver()) {
                    myPacket.isDead = true;
                }
                nm.sendState(myPacket);

                // Process Remote State
                GameStatePacket remotePacket = nm.getLatestPacket();
                while (remotePacket != null) {
                    if (!remotePacket.isInit) {
                        Map.getInstance().updateOpponent(remotePacket.x, remotePacket.y, remotePacket.altitude, remotePacket.isDead,
                                remotePacket.movingLeft, remotePacket.movingRight, remotePacket.standing, remotePacket.comboJumping);

                        if (remotePacket.barExtendTaken) {
                            if (!Map.getInstance().isBarExtendTaken() || remotePacket.barExtendTakenBar > Map.getInstance().getBarExtendTakenBar()) {
                                Map.getInstance().extendBar(remotePacket.barExtendTakenBar);
                            }
                        }

                        if (remotePacket.isDead) {
                            // Opponent died, we win!
                             System.out.println("Opponent died. You Win!");
                             // Trigger Game Over with Win logic?
                             // Current Game Over logic is based on local character dying.
                             // We can trigger a "Win" screen.
                             // For now, let's just stop the game or let it continue until we die too (high score contest).
                             // Requirement: "The game will end when one of the players lose."
                             timeline.stop();
                             mediaplayer.stop();
                             NetworkManager.getInstance().close();
                             // TODO: Show Victory Screen
                        }
                    }
                    remotePacket = nm.getLatestPacket();
                }
            }
        }

        GameEngine.getInstance().convertMapToPane();
        if(GameEngine.getInstance().getMap().gameOver())
        {
            timeline.stop();
            mediaplayer.stop();
            // If multiplayer, we should send one last packet saying we died (handled above)
            if (Map.getInstance().isMultiplayer()) {
                // Give a small delay or ensure the packet is sent before closing?
                // The network runs on separate thread, sending is blocking/synchronized usually?
                // sendState uses writeObject which is blocking.
                // We already sent the death state in the block above (if (GameEngine.getInstance().getMap().gameOver())).
                // So the opponent should receive it.
                // We should close the connection now.
                NetworkManager.getInstance().close();
            }
        }
        if(GameEngine.getInstance().isIncreaseGameSpeed())
        {
            timeline.setRate(timeline.getCurrentRate() * (1.25));
            GameEngine.getInstance().setIncreaseGameSpeed(false);
        }
        if(GameEngine.getInstance().isDecreaseGameSpeed())
        {
                timeline.setRate(timeline.getCurrentRate() * (0.8));
                GameEngine.getInstance().setDecreaseGameSpeed(false);
        }
        if(GameEngine.getInstance().deActivateIncraseGameSpeed())
        {
            timeline.setRate(timeline.getRate() * 0.8);
        }
        if(GameEngine.getInstance().deActivateDecreaseGameSpeed())
        {
            timeline.setRate(timeline.getRate() * 1.25);
        }
    }

    public static void changeVolume() {
        mediaplayer.setVolume(SoundManager.getInstance().getVolume());
    }

    public static void stopSong() {
        mediaplayer.stop();
    }


    public  void setRate(double rate){
        timeline.setRate(rate);
        System.out.println(timeline.getRate());
    }
}

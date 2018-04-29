package model.entity;

import javafx.scene.image.Image;

import java.nio.file.Paths;

public class TimeSqueezer extends Collectible {

    private final double SQUEEZE_RATE = 1.5; //game screen speed will be faster with the rate of 1.5

    /**
     * Constructor for Collectible item TimeSqueezer
     */
    public TimeSqueezer(){
        Image[] images = new Image[1];
        images[0] = new Image(Paths.get("./images/bonus/timesqueezer.png").toUri().toString());
        super.setImages(images);
    }
}

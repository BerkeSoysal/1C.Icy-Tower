package model.network;

import java.io.Serializable;

public class GameStatePacket implements Serializable {
    private static final long serialVersionUID = 1L;

    public int x, y;
    public int altitude;
    public double vx, vy;
    public int score;
    public boolean isDead;
    public boolean movingLeft, movingRight, standing, comboJumping;
    public boolean barExtendTaken;
    public int barExtendTakenBar;
    public long seed; // Only sent by host initially
    public boolean isInit; // Flag to check if it's init packet

    public GameStatePacket() {}

    public GameStatePacket(long seed) {
        this.seed = seed;
        this.isInit = true;
    }
}

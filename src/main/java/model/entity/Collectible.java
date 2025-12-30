package model.entity;

/**
 * Bonuses which are collectible game objects
 */
public class Collectible extends GameObject {

    private static int idCounter = 0;
    private int id;

    /**
     * default constructor for Collectibles
     */
    public Collectible() {
        this.id = idCounter++;
    }

    public int getId() {
        return id;
    }

    public static void resetId() {
        idCounter = 0;
    }
}

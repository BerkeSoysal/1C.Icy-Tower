package model.entity;

import javafx.scene.image.Image;

public class Bar extends GameObject {

	private int width;
	private int left;
	private int right;
	private int top;
	private int bottom;

	public Bar(){ }

    public Bar(Image[] images) {
        super(images);
    }

    public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getLeft(){return left; }

	public int getRight(){return right;}

	public int getTop(){return top;}

	public int getBottom(){return bottom;}
}
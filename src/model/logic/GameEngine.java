package model.logic;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Timer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;
import controller.MainController;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import model.entity.*;
import view.GameFrame;

public class GameEngine {

	private MapGenerator mapGenerator;
	private PauseManager pm;
	//private GameFrame gf;
	private CollisionManager cm;
	private Timer timer;
	private int currentAltitude;
	private int currentScore;
	private boolean gameFinished;
	private boolean gamePaused;
	private boolean firstTimeSettings;
	boolean firstTime;

	private Pane pane;
	int mapLevel;
	private static GameEngine instance;
	public static void init(int difficulty){
        Map.getInstance().setDifficulty(difficulty);
    }
	public static GameEngine getInstance(){
		if(instance == null)
		{
			instance = new GameEngine();

		}
		return instance;
	}
	private GameEngine() {
		currentAltitude = 0;
		pane = new Pane();
		BackgroundImage backgroundImage = new BackgroundImage(new Image(Paths.get( "./images/gameObject/gameBack.png").toUri().toString()), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		pane.setBackground(new Background(backgroundImage));
		mapGenerator = new MapGenerator(Map.getInstance());
		mapGenerator.createNextGameObjects();
		firstTime=true;
	}


	public Pane convertMapToPane(){
	    if(!gamePaused) {

			pane.getChildren().clear();
			Text textscore = new Text("score");
			textscore.setFont(Font.font("score", FontWeight.BOLD, 35));
			textscore.setTranslateY(40);
			textscore.setTranslateX(700);
			textscore.setFill(Color.ORANGE);
			Text tscore = new Text(""+Map.getInstance().getGameCharacter().getScore());
			tscore.setFont(Font.font("score", FontWeight.BOLD, 35));
			tscore.setTranslateY(77);
			tscore.setTranslateX(720);
			tscore.setFill(Color.ORANGE);

			pane.getChildren().addAll(textscore,tscore);
			for(GameObject g: Map.getInstance().getGameObjects()){
				int xsofar = 0;
				if(g instanceof model.entity.Character){
					ImageView add = new ImageView(((model.entity.Character) g).getCurrentImage());
					add.setTranslateX(g.getPosX() + xsofar);
					add.setTranslateY(500 - g.getPosY());
					pane.getChildren().add(add);
				}
				else {
					for (int i = 0; i < g.getImages().length; i++) {
						ImageView add = new ImageView(g.getImages()[i]);
						add.setTranslateX(g.getPosX() + xsofar);
						xsofar += g.getImages()[i].getWidth();
						add.setTranslateY(500 - g.getPosY());
						pane.getChildren().add(add);
						if (g instanceof Bar&& i == 0) {
							for (int j = 0; j < ((Bar) g).getWidth(); j++) {
								add = new ImageView(g.getImages()[1]);
								add.setTranslateX(g.getPosX() + xsofar);
								xsofar += g.getImages()[1].getWidth();
								add.setTranslateY(500 - g.getPosY());
								if(g instanceof HardlyVisible){
									add.setOpacity(0.3);
								}
								pane.getChildren().add(add);
							}
							i++;
						}
						else if(g instanceof Base && i == 0){
							for (int j = 0; j < ((Base) g).getWidth(); j++) {
								add = new ImageView(g.getImages()[1]);
								add.setTranslateX(g.getPosX() + xsofar);
								xsofar += g.getImages()[1].getWidth();
								add.setTranslateY(500 - g.getPosY());
								pane.getChildren().add(add);
							}
							i++;
						}

					}
				}
			}
			Map.getInstance().updateCharacter();
			Map.getInstance().updateObjects();
			if (Map.getInstance().getLevel() - (Map.getInstance().getAltitude() / 50) < 15) {
				mapGenerator.createNextGameObjects();
			}

		}
		else{
			if(firstTimeSettings)
				createSettingsPane();
		}
		//map.changeImages();
		if(Map.getInstance().gameOver()){
			createGameOverPane();
			return pane;
		}

		return pane;
	}

	private void createSettingsPane() {
		firstTimeSettings=false;
		Rectangle rectangle = new Rectangle();
		rectangle.setX(170);
		rectangle.setY(50);
		rectangle.setWidth(400);
		rectangle.setHeight(400);
		rectangle.setFill(Color.LIGHTSALMON);
		Text pausetext = new Text("Pause Menu");
		pausetext.setTranslateX(250);
		pausetext.setTranslateY(100);
		pausetext.setFont(Font.font("PauseMenu", FontWeight.BOLD, 35));
		Text resumetext = new Text(">>");
		resumetext.setTranslateX(200);
		resumetext.setTranslateY(300);
		resumetext.setFont(Font.font("resume", FontWeight.BOLD, 25));
		Button resume = new Button("Resume Game");
		resume.setTranslateX(260);
		resume.setTranslateY(290);
		resume.setMinSize(20, 20);
		resume.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                continueGame();
            }
        });
		Text voltext = new Text("Volume");
		voltext.setTranslateX(200);
		voltext.setTranslateY(155);
		voltext.setFont(Font.font("vol", FontWeight.BOLD, 15));
		Slider volume = new Slider();
		volume.setTranslateX(260);
		volume.setTranslateY(150);
		volume.setShowTickMarks(true);
		volume.setMin(0);
		volume.setMax(200);
		volume.setMajorTickUnit(50);
		volume.setValue((int)(SoundManager.getInstance().getVolume() * 200));
		volume.valueProperty().addListener((ov, old_val, new_val) -> {

            int newvolume = new_val.intValue();
            double volumeNormalized = newvolume / 200.0;
            SoundManager.getInstance().setVolume(volumeNormalized);
            GameFrame.changeVolume();
        });
		Text songtext = new Text("Songs");
		songtext.setTranslateX(200);
		songtext.setTranslateY(220);
		songtext.setFont(Font.font("song", FontWeight.BOLD, 15));
		ChoiceBox songlist = new ChoiceBox(FXCollections.observableArrayList("Requiem for a dream", "Never gonna give you up", "Light my fire"));
		songlist.setTranslateX(260);
		songlist.setTranslateY(210);
		songlist.getSelectionModel().select(SoundManager.getInstance().getSong());

		songlist.setOnAction(event -> {
			int songNo = songlist.getSelectionModel().getSelectedIndex();
			SoundManager.getInstance().setSong(songNo);
			GameFrame.stopSong();
			GameFrame.playSong();
		});


		pane.getChildren().addAll(rectangle, pausetext, resumetext, resume, volume, voltext, songtext, songlist);
	}

	private void createGameOverPane() {

		int score = Map.getInstance().getGameCharacter().getScore();

		Button backToMenu = new Button("Back to Menu");
		backToMenu.setTranslateY(550);
		backToMenu.setTranslateX(650);

		Text highScoreText = new Text("");
		highScoreText.setFont(Font.font("HighScore", FontWeight.BOLD, 30));
		highScoreText.setFill(Color.BLUE);
		highScoreText.setTranslateY(400);
		highScoreText.setTranslateX(-400);

		Button saveScore = new Button("Save score!");;
		TextField nameField = new TextField("Write Name");


		if(FileManager.getInstance().isHighScore(score) )
		{
			highScoreText.setText("New HighScore!!");


			nameField.setTranslateX(350);
			nameField.setTranslateY(500);

			saveScore.setTranslateX(350);
			saveScore.setTranslateY(520);
			pane.getChildren().addAll(nameField,saveScore);
		}

		saveScore.setOnMouseClicked(event ->  {
			try {
				FileManager.getInstance().saveNewHighScore(nameField.getText(),score);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Stage primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
			primaryStage.setScene(MainController.getInstance().getMainMenuScene());
			instance = null; // terminate the gameEngine for that game
			Map.setMapNull();
		});

		backToMenu.setOnMouseClicked(event -> {
            Stage primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            primaryStage.setScene(MainController.getInstance().getMainMenuScene());
            instance = null; // terminate the gameEngine for that game
			Map.setMapNull();
        });
		TextFlow textflow = new TextFlow();
		Text text1 = new Text("Game Over");
		text1.setFont(Font.font("Game Over", FontWeight.BOLD, 100));
		text1.setFill(Color.PALEGREEN);
		text1.setTranslateX(100);
		text1.setTranslateY(150);
		Text text2 = new Text("Score: "+ Map.getInstance().getGameCharacter().getScore());
		text2.setFont(Font.font("Score: ", FontWeight.BOLD, 60));
		text2.setFill(Color.PALETURQUOISE);
		text2.setTranslateX(-350);
		text2.setTranslateY(270);
		textflow.getChildren().addAll(text1,text2,highScoreText);
		pane.getChildren().addAll(backToMenu,textflow);
	}

	public GameEngine(int difficulty, KeyCode[] buttons) {

	}

	/**
	 * 
	 * @param images
	 */
	public void loadCurrentCharactersImages(Image[] images) {
		Map.getInstance().loadCurrentCharactersImages(CharacterManager.getInstance().getCharacterImages());
	}

	public void moveCharacterLeft(){

		Map.getInstance().moveLeft();
	}

	public void moveCharacterRight(){

		Map.getInstance().moveRight();
	}
	public void jumpCharacter() {

		Map.getInstance().jump();
	}


	public void stopMoveCharacterLeft(){

		Map.getInstance().stopMoveLeft();
	}

	public void stopMoveCharacterRight(){

		Map.getInstance().stopMoveRight();
	}

	public void stopJump(){

		Map.getInstance().stopMoveJump();
	}

	public void updateScore() {
		// TODO - implement GameEngine.updateScore
		throw new UnsupportedOperationException();
	}

	public boolean isGameOver() {
		// TODO - implement GameEngine.isGameOver
		throw new UnsupportedOperationException();
	}

	public boolean isGamePaused() {
		return this.gamePaused;
	}

	public void pauseGame() {
	    firstTimeSettings=true;
		gamePaused = !gamePaused;
	}

	public void continueGame() {
		gamePaused = false;
	}

	public Map getMap(){
		return Map.getInstance();
	}

}
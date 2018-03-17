package view;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.nio.file.Paths;

public class ButtonSettings extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Icy Tower");


        VBox creditslayout = new VBox(10);
        Button backbutton = new Button("Back to Menu");
        backbutton.setMinSize(30, 30);
        backbutton.setTranslateX(420);
        backbutton.setTranslateY(215);
        Text text = new Text("Button Settings");
        Text text2 = new Text("Ozan Kerem Devamlı \nBerke Soysal \nCansu Yıldırım");
        text.setTranslateX(250);
        text.setTranslateY(50);
        text.setFont(Font.font("T", FontWeight.BOLD, 40));
        text2.setFont(new Font(30));
        text2.setTranslateX(225);
        text2.setTranslateY(70);

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.add(backbutton, 300, 300);
        creditslayout.getChildren().addAll(text, text2, backbutton);
        pane.getChildren().add(creditslayout);
        pane.setAlignment(Pos.CENTER);

        Image creditsimage = new Image(Paths.get("images/background/backgrounds.png").toUri().toString(),true);
        Pane bigpane = new Pane();
        bigpane.getChildren().add(pane);
        Scene scene = new Scene(bigpane,700, 500);
        bigpane.setBackground(new Background(new BackgroundImage(creditsimage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(scene.getWidth(), scene.getHeight(), true, true, true, true))));


        primaryStage.setScene(scene);
        primaryStage.show();


    }
}



package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.entity.Map;
import model.logic.GameEngine;
import model.network.NetworkManager;
import view.GameFrame;
import java.util.Random;

public class MultiplayerMenuController {

    @FXML
    private TextField ipAddressField;

    public void hostGame(ActionEvent actionEvent) {
        System.out.println("Hosting game...");
        // Use a fixed port for simplicity, or ask user. Using 9999.
        NetworkManager.getInstance().startServer(9999, () -> {
            Platform.runLater(() -> {
                startGame(actionEvent, true);
            });
        });
    }

    public void joinGame(ActionEvent actionEvent) {
        String ip = ipAddressField.getText();
        if (ip == null || ip.isEmpty()) {
            // Default to localhost for testing if empty
            ip = "localhost";
        }
        System.out.println("Joining game at " + ip);
        NetworkManager.getInstance().startClient(ip, 9999, () -> {
             Platform.runLater(() -> {
                startGame(actionEvent, false);
            });
        });
    }

    private void startGame(ActionEvent event, boolean isHost) {
        // Reset Map
        Map.setMapNull();
        GameEngine.setEngineNull(); // Reset GameEngine so it picks up the new Map
        Map.getInstance(); // Re-init

        long seed;
        if (isHost) {
            seed = new Random().nextLong();
            Map.getInstance().setSeed(seed);
            // We will send this seed in GameFrame start
        } else {
            // Client will wait for seed in GameFrame
        }

        Map.getInstance().initMultiplayer(isHost);

        Stage primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        GameFrame gameFrame = new GameFrame((int)Map.getInstance().getDifficulty());

        // Pass seed info if needed, but Map is singleton so we set it there.
        // For client, we need to handle waiting for seed.
        // In GameFrame, we can add logic to wait or sync.

        primaryStage.setScene(gameFrame.start());
    }

    public void backToMainMenu(ActionEvent actionEvent) {
        // Ensure network is closed if we back out (though we don't start it until host/join)
        // But if we clicked host, it starts a server. If we back out then, we should close it.
        // Currently Host button transitions immediately to game?
        // Ah, Host button: startServer -> onConnect -> runLater -> startGame.
        // It waits for connection.
        // We don't have a "Waiting" screen implemented.
        // "Hosting game..." is printed. The UI stays on the menu.
        // So the user is stuck on the menu while the server socket is listening in a background thread.
        // If they click Back, that thread is still running and the port is bound.
        // We MUST close the network manager on back.
        NetworkManager.getInstance().close();

        Stage primaryStage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        primaryStage.setScene(MainController.getInstance().getMainMenuScene());
    }
}

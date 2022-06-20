package com.example.network_chat.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Optional;

public class ChatController {

    @FXML
    public TextField messageField;
    @FXML
    private TextArea messageArea;

    private final ChatClient client;

    public ChatController() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.OpenConnection();
                break;
            } catch (IOException e) {
                showNotification();
            }
        }
    }

    private void showNotification() {

        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Cannot connect to the server \n" +
                "please check if the server is started",
                new ButtonType("Try again?", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Leave the chat", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        alert.setTitle("Connection error");
        Optional<ButtonType> answer = alert.showAndWait();
        Boolean isExit = answer.map(select -> select.getButtonData().isCancelButton()).orElse(false);

        if (isExit) {
            System.exit(0);
        }


    }

    public void clickSendButton() {

        String message = messageField.getText();
        if (message.isBlank()) {
            return;
        }
        client.sendMessage(message);
        messageField.clear();
        messageArea.requestFocus();

    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n");

    }
}
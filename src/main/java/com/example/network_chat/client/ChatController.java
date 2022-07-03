package com.example.network_chat.client;

import com.example.network_chat.Command;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Optional;

public class ChatController {

    @FXML
    private Text timerBox;
    String selectedNick = null;
    @FXML
    private HBox authBox;
    @FXML
    private ListView<String> clientList;
    @FXML
    private HBox messageBox;
    @FXML
    private PasswordField passField;
    @FXML
    private TextField loginField;
    @FXML
    private TextField messageField;
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

    public void finalCoundown(String count) {
        timerBox.setText(count);
    }


    public void clickSendButton() {
        String message = messageField.getText();
        if (message.isBlank()) {
            return;
        }

        if (selectedNick != null) {
            client.sendMessage(Command.PRIVATE_MESSAGE, selectedNick, message);
            selectedNick = null;
        } else {
            client.sendMessage(Command.MESSAGE, message);
        }
        messageField.clear();
        messageArea.requestFocus();

    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n");

    }

    public void signinBthClc() {
        if (client.isConnected()) {
            client.sendMessage(Command.AUTH, loginField.getText(), passField.getText());
        }
    }

    public void setAuth(boolean success) {
        authBox.setVisible(!success);
        messageBox.setVisible(success);
        timerBox.setVisible(false);
    }

    public void showError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage,
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Error!!!");
        alert.showAndWait();

    }
    public void selectClient(MouseEvent mouseEvent) {

        if (mouseEvent.getClickCount() == 2) {
            selectedNick = clientList.getSelectionModel().getSelectedItem();
            if (selectedNick != null && !selectedNick.isEmpty()) {
                this.selectedNick = selectedNick;
            }
        }
    }

    public void updateClientsList(String[] clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);

    }

    public ChatClient getClient() {
        return client;
    }

    public void leaveTheChat(ActionEvent actionEvent) {
        client.sendMessage(Command.END);
    }
}


package com.example.network_chat.client;

import com.example.network_chat.Command;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ChatClient {

    private final int WAITING_FOR_AUTH_TIME = 20;
    private final ChatController controller;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isClientAuthorized;
    private boolean isSpyDetected;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void OpenConnection() throws IOException {

        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                waitForAuth();
                if (!isSpyDetected) {ReadMessages();}
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("Client disconnected");
                CloseConnection();
                System.out.println(socket.isClosed());
            }
        }).start();
    }

    private void waitForAuth() {

        new Thread(() -> {
            int count = WAITING_FOR_AUTH_TIME;
            while ((count != 0) && (!isClientAuthorized)) {
                String s = "Only " + count + " secs left";
                Platform.runLater(() -> controller.finalCoundown(s));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                count--;
                if (count == 0) {
                    String errorText = "Sorry, but you are identified as a spy \nContacting nearest KGB office ...";
                    Platform.runLater(() -> controller.showError(errorText));
                    isSpyDetected = true;
                    sendMessage(Command.END);
                }
            }
        }).start();

        while (true) {
            try {
                String s = in.readUTF();
                Command command = Command.getCommand(s);
                String[] params = command.parse(s);
                if (command == Command.AUTHOK) {
                    isClientAuthorized = true;
                    String nick = params[0];
                    controller.addMessage("Authorization is successful for " + nick);
                    controller.setAuth(true);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params[0]));
                    continue;
                }
                if (command == Command.END) {
                    System.out.println("End");
                    break;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void CloseConnection() {

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void ReadMessages() throws IOException {

        while (true) {
            String message = in.readUTF();
            Command command = Command.getCommand(message);
            String[] params = command.parse(message);
            if (command == Command.END) {
                controller.setAuth(false);
                break;
            }
            if (command == Command.ERROR) {
                String messageError = params[0];
                Platform.runLater(() -> controller.showError(messageError));
            }
            if (command == Command.MESSAGE) {
                Platform.runLater(() -> controller.addMessage(params[0]));
            }
            if (command == Command.CLIENTS) {
                Platform.runLater(() -> controller.updateClientsList(params));
            }
        }
    }

    private void sendMessage(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));

    }

    public boolean isConnected() {
        return (!socket.isClosed());
    }
}

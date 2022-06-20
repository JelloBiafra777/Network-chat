package com.example.network_chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private final ChatController controller;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;


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
                ReadMessages();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                CloseConnection();
            }
        }).start();
    }

    private void waitForAuth() {

        while (true) {
            try {
                String s = in.readUTF();
                if (s.startsWith("//Autorisation")) {
                    String[] split = s.split("\\s");
                    String nick = split[1];
                    controller.addMessage("Authorisation is succesfull for " + nick);
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
            if (message.equals("/end")) {
                break;
            }
            controller.addMessage(message);

        }

    }

    public void sendMessage(String s) {

        try {
            out.writeUTF(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

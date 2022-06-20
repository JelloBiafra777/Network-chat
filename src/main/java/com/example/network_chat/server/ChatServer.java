package com.example.network_chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Awaiting for connection ...");
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("The client is connected");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void broadcast(String s) {
        for (ClientHandler client : clients) {
            client.SendMessage(s);
        }

    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;

    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void sendPrivateMessage(String nick, String message) {

        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                client.SendMessage(message);
            }

        }

    }

    public boolean isReceiverExists(String messageReceiver) {
        for (ClientHandler client : clients) {
            if (messageReceiver.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

}

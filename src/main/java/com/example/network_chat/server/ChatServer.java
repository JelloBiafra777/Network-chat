package com.example.network_chat.server;

import com.example.network_chat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {

    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();
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

    public void broadcast(Command command, String s) {
        for (ClientHandler client : this.clients.values()) {
            client.SendMessage(command, s);
        }

    }

    public void subscribe(ClientHandler clientHandler) {
        clients.put(clientHandler.getNick(), clientHandler);
        broadcastClientList();
    }

    private void broadcastClientList() {
        final String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));
        broadcast(Command.CLIENTS, nicks);
    }

    public boolean isNickBusy(String nick) {
        return clients.get(nick) != null;
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler.getNick());
        broadcastClientList();
    }

    public void sendPrivateMessage(ClientHandler from, String to, String message) {
        ClientHandler clientTo = clients.get(to);
        if (clientTo == null) {
            from.SendMessage(Command.ERROR, "No such user exists");
            return;
        }
        clientTo.SendMessage(Command.MESSAGE, "Private message from " + from.getNick() + ": " + message);
        from.SendMessage(Command.MESSAGE, "Private message to " + to + ": " + message);
    }
}

package com.example.network_chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private ChatServer server;
    private String nick;
    private final AuthService authService;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {

        try {

            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    Authenticate();
                    ReadMessages();
                } finally {
                    CloseConnection();
                }
            }).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void Authenticate() {
        while (true) {
            try {
                String s = in.readUTF();
                if (s.startsWith("/auth")) {
                    String[] split = s.split("\\s");
                    String login = split[1];
                    String password = split[2];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            SendMessage("This user is already autorised");
                            continue;
                        }
                        SendMessage("//Autorisation " + nick + " is passed!");
                        this.nick = nick;
                        server.broadcast("User " + nick + " has entered the chat!");
                        server.subscribe(this);
                        break;
                    } else {
                        SendMessage("Login or password is not correct!");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
    }


    private void CloseConnection() {

        SendMessage("/end");

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
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void SendMessage(String s) {

        try {
            out.writeUTF(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ReadMessages() {

        while (true) {
            String message = null;
            try {
                message = in.readUTF();
                if (message.equals("/end")) {
                    break;
                }

                if (message.startsWith("/w ")) {
                    server.sendPrivateMessage(this.nick, message);
                    String[] split = message.split("\\s", 3);
                    String messageReceiver = split[1];
                    if (server.isReceiverExists(messageReceiver)) {
                        String s = "private message from " + this.nick + " to " + split[1] + "> " + split[2];
                        server.sendPrivateMessage(messageReceiver, s);
                    } else {
                        server.sendPrivateMessage(this.nick, messageReceiver + " No such user exist ");
                    }
                } else {
                    server.broadcast(nick + "> " + message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

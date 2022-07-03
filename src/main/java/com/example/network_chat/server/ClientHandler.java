package com.example.network_chat.server;


import com.example.network_chat.Command;

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
    private boolean isSpyDetected;

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
                    if (!isSpyDetected) ReadMessages();
                } finally {
                    System.out.println("Server disconnected");
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
                Command command = Command.getCommand(s);
                if (command == Command.END) {
                    System.out.println("$$$$");
                    isSpyDetected = true;
                    break;
                }
                if (command == Command.AUTH) {
                    String login = command.parse(s)[0];
                    String password = command.parse(s)[1];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            SendMessage(Command.ERROR, "This user is already authorized");
                            continue;
                        }
                        SendMessage(Command.AUTHOK, nick);
                        this.nick = nick;
                        server.broadcast(Command.MESSAGE, "User " + nick + " has entered the chat!");
                        server.subscribe(this);
                        break;
                    } else {
                        SendMessage(Command.ERROR, "Login or password is not correct!");
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
    }

    public void SendMessage(Command command, String... params) {
        SendMessage(command.collectMessage(params));
    }


    private void CloseConnection() {

        SendMessage(Command.END);

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

    private void SendMessage(String s) {

        try {
            out.writeUTF(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ReadMessages() {

        while (true) {
            try {
                String message = in.readUTF();
                Command command = Command.getCommand(message);
                if (command == Command.END) {
                    System.out.println("!!!!");
                    break;
                }
                if (command == Command.MESSAGE) {
                    server.broadcast(Command.MESSAGE, nick + "> " + command.parse(message)[0]);
                }

                if (command == Command.PRIVATE_MESSAGE) {
                    String[] params = command.parse(message);
                    server.sendPrivateMessage(this, params[0], params[1]);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

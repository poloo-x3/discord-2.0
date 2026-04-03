package chat.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;

public class ConnectionReceiver implements Runnable {
    final DataInputStream input;
    final Chat chat;
    final ClientConnection parent;
    private boolean loggedIn = false;
    private String selectedUser = null;

    public ConnectionReceiver(DataInputStream input, Chat chat, ClientConnection parent) {
        this.input = input;
        this.chat = chat;
        this.parent = parent;
    }

    @Override
    public void run() {
        String msg;
        boolean meow = true;

        try {
            sendMessage("Server: authorize or register");
            while (meow) {
                msg = input.readUTF();
                if (msg.startsWith("/")) {
                    String[] tokens = msg.split(" ");
                    switch (tokens[0]) {
                        case "/list" -> sendMessage("Server: " + chat.getActiveUserList(parent.username));
                        case "/auth" -> authenticateUser(tokens);
                        case "/registration" -> registerUser(tokens);
                        case "/chat" -> selectChat(tokens);
                        case "/exit" -> {
                            chat.disconnectUser(parent);
                            meow = false;
                        }
                        default -> parent.sendMessage("Server: incorrect command!");
                    }
                    continue;
                }

                if (!loggedIn) {
                    sendMessage("Server: you are not in the chat!");
                    continue;
                }

                if (selectedUser == null) {
                    sendMessage("Server: use /list command to choose a user to text!");
                    continue;
                }

                sendDirectMessage(selectedUser, msg);
            }
        } catch (IOException e) {
            System.out.println("meow mother fucker");
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        parent.sendMessage(message);
    }

    private void authenticateUser(String[] tokens) {
        if (tokens.length != 3) {
            parent.sendMessage("Server: incorrect command!");
            return;
        }

        if (!chat.isRegistered(tokens[1])) {
            sendMessage("Server: incorrect login!");
            return;
        }

        if (!chat.isCorrectPassword(tokens[1], tokens[2])) {
            sendMessage("Server: incorrect password!");
            return;
        }

        loggedIn = true;
        chat.connectUser(parent, tokens[1]);
        sendMessage("Server: you are authorized successfully!");
    }

    private void registerUser(String[] tokens) {
        if (tokens.length != 3) {
            sendMessage("Server: incorrect command!");
            return;
        }

        if (chat.isRegistered(tokens[1])) {
            sendMessage("Server: this login is already taken! Choose another one.");
            return;
        }

        if (tokens[2].length() < 8) {
            sendMessage("Server: the password is too short!");
            return;
        }

        chat.addNewUser(tokens[1], tokens[2]);
        sendMessage("Server: you are registered successfully!");

        loggedIn = true;
        chat.connectUser(parent, tokens[1]);
    }

    private void selectChat(String[] tokens) {
        if (!chat.isRegistered(tokens[1])) {
            sendMessage("Server: the user is not online!");
            return;
        }

        selectedUser = tokens[1];

        chat.sendAllPreviousDirectMessages(tokens[1], parent.username);
    }

    private void sendDirectMessage(String toUser, String message) {
        int chatHashCode = parent.getUsername().hashCode() * toUser.hashCode();

        if (Objects.equals(chat.getUser(toUser).connectionReciever.selectedUser, parent.username)) {
            chat.sendDirectMessage(toUser, parent.getUsername() + ": " + message);
            chat.addMessageToDatabase(chatHashCode, parent.getUsername() + ": " + message);
            sendMessage(parent.getUsername() + ": " + message);
            return;
        }

        chat.addMessageToDatabase(chatHashCode, "(new) " + parent.getUsername() + ": " + message);
        sendMessage(parent.getUsername() + ": " + message);
    }
}


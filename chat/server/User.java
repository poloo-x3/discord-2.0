package chat.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class User {
    Chat chat;
    Socket socket;
    ClientConnection output = new ClientConnection(new DataOutputStream(socket.getOutputStream()));
    ConnectionReceiver input = new ConnectionReceiver(this, new DataInputStream(socket.getInputStream()));

    private String username = null;
    private String selectedUser = null;
    Role role = Role.USER;

    User(Chat chat, Socket socket) throws IOException {
        this.chat = chat;
        this.socket = socket;
    }

    public void parseInput(String msg) {
        try {
            if (msg.startsWith("/")) {
                String[] tokens = msg.split(" ");
                switch (tokens[0]) {
                    case "/list" -> sendMessage("Server: " + chat.getActiveUserList(username));
                    case "/auth" -> authenticateUser(tokens);
                    case "/registration" -> registerUser(tokens);
                    case "/chat" -> selectChat(tokens);
                    case "/kick" -> kickUser(tokens);
                    case "/exit" -> chat.disconnect(parent);
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

        } catch (IOException e) {
            System.out.println("meow mother fucker");
        }
    }

    protected String getUsername() {
        return username;
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

        chat.registerNewUser(tokens[1], tokens[2]);
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

        if (Objects.equals(chat.getUser(toUser).connectionReceiver.selectedUser, parent.username)) {
            chat.sendDirectMessage(toUser, parent.getUsername() + ": " + message);
            chat.addMessageToDatabase(chatHashCode, parent.getUsername() + ": " + message);
            sendMessage(parent.getUsername() + ": " + message);
            return;
        }

        chat.addMessageToDatabase(chatHashCode, "(new) " + parent.getUsername() + ": " + message);
        sendMessage(parent.getUsername() + ": " + message);
    }

    private void kickUser(String[] tokens) {
        if (parent.getRole() == Role.USER) {
            sendMessage("Server: you are not a moderator or an admin!");
            return;
        }

        if (tokens[1].equals(parent.getUsername())) {
            sendMessage("Server: you can't kick yourself!");
            return;
        }

        chat.setUserRole(tokens[1], Role.KICKED);
        ClientConnection selectedUser = chat.getUser(tokens[1]);
        if (selectedUser != null) {
            chat.disconnect(selectedUser);
        }
    }

    private void exit() {

    }
}

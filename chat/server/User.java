package chat.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class User {
    Chat chat;
    Socket socket;
    ConnectionReceiver input;
    ClientConnection output;

    private String username = null;
    private String selectedUser = null;
    Role role = Role.USER;

    User(Chat chat, Socket socket) throws IOException {
        this.chat = chat;
        this.socket = socket;

        this.input = new ConnectionReceiver(this, new DataInputStream(socket.getInputStream()));
        this.output = new ClientConnection(new DataOutputStream(socket.getOutputStream()));

        new Thread(input).start();
        new Thread(output).start();
    }

    public void parseInput(String msg) {
        if (msg.startsWith("/")) {
            String[] tokens = msg.split(" ");
            switch (tokens[0]) {
                case "/list" -> sendMessage("Server: " + chat.getActiveUserList(username));
                case "/auth" -> authenticate(tokens);
                case "/registration" -> registerUser(tokens);
                case "/chat" -> selectChat(tokens);
                case "/kick" -> kickUser(tokens);
                case "/exit" -> disconnect();
                default -> sendMessage("Server: incorrect command!");
            }
            return;
        }

        if (username == null) {
            sendMessage("Server: you are not in the chat!");
            return;
        }

        if (selectedUser == null) {
            sendMessage("Server: use /list command to choose a user to text!");
            return;
        }

        sendDirectMessage(selectedUser, msg);
    }

    protected String getUsername() {
        return username;
    }

    protected void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    protected String getSelectedUser() {
        return selectedUser;
    }

    protected void sendMessage(String message) {
        output.sendMessage(message);
    }

    private void authenticate(String[] tokens) {
        if (tokens.length != 3) {
            sendMessage("Server: incorrect command!");
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

        chat.connectUser(this, tokens[1]);
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

        chat.connectUser(this, tokens[1]);
    }

    private void selectChat(String[] tokens) {
        if (!chat.isRegistered(tokens[1])) {
            sendMessage("Server: the user is not online!");
            return;
        }

        selectedUser = tokens[1];

        chat.sendAllPreviousDirectMessages(tokens[1], username);
    }

    private void sendDirectMessage(String toUser, String message) {
        int chatHashCode = username.hashCode() * toUser.hashCode();

        if (Objects.equals(chat.getUser(toUser).getSelectedUser(), username)) {
            chat.sendDirectMessage(toUser, username + ": " + message);
            chat.addMessageToDatabase(chatHashCode, username + ": " + message);
            sendMessage(username + ": " + message);
            return;
        }

        chat.addMessageToDatabase(chatHashCode, "(new) " + username + ": " + message);
        sendMessage(username + ": " + message);
    }

    private void kickUser(String[] tokens) {
        if (role == Role.USER) {
            sendMessage("Server: you are not a moderator or an admin!");
            return;
        }

        if (tokens[1].equals(username)) {
            sendMessage("Server: you can't kick yourself!");
            return;
        }

        chat.setUserRole(tokens[1], Role.KICKED);
        chat.setUsername(tokens[1], null);

        User selectedUser = chat.getUser(tokens[1]);
        if (selectedUser != null) {
            chat.disconnect(selectedUser);
        }
    }

    protected void disconnect() {
        output.disconnect();
        input.disconnect();
    }

    @Override
    public String toString() {
        return username;
    }
}

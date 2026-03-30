package chat.server;

import java.util.*;
import java.io.*;

public class Chat {
    ArrayList<ClientConnection> connectedPeople = new ArrayList<>();
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<String> usernames = new ArrayList<>(List.of(""));
    HashMap<String, String> users = new HashMap<>();


    public Chat() {
        File temp1 = new File("userdb.txt");
        try {
            FileWriter userdb = new FileWriter(temp1);
        } catch (IOException e) {
            throw new RuntimeException("Could not open ");
        }
    }

    protected String getUserList() {
        return users.keySet().toString();
    }

    protected void connectUser(ClientConnection client, String username) {
        if (!connectedPeople.contains(client)) {
            connectedPeople.add(client);
        }
        client.setUsername(username);
        usernames.add(username);
        if (!messages.isEmpty()) {
            client.sendMessage(getChat());
        }
    }

    protected void sendMessage(ClientConnection user, String message) {
        messages.add(String.format("%s: %s", user.getUsername(), message));
        for (ClientConnection otherUser: connectedPeople) {
            if (true) {
                otherUser.sendMessage(String.format("%s: %s", user.getUsername(), message));
            }
        }
    }

    protected void disconnectUser(ClientConnection client) {
        connectedPeople.remove(client);
    }

    protected synchronized String getChat() {
        StringBuilder stringBuilder = new StringBuilder();

        if (messages.size() < 10) {
            messages.forEach(message -> stringBuilder.append(message).append("\n"));
        } else {
            messages.subList(messages.size() - 10, messages.size()).forEach(message -> stringBuilder.append(message).append("\n"));
        }
        return stringBuilder.toString().strip();
    }

    protected boolean hasUsername(String username) {
        return usernames.contains(username);
    }
}

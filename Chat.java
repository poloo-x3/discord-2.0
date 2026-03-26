package chat.server;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    ArrayList<ClientConnection> connectedPeople = new ArrayList<>();
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<String> usernames = new ArrayList<>(List.of(""));

    protected void connectUser(ClientConnection client, String username) {
        if (!connectedPeople.contains(client)) {
            connectedPeople.add(client);
        }
        client.setUsername(username);
        usernames.add(username);
        if (!messages.isEmpty()) {
            client.getSender().sendMessage(getChat());
        }
    }

    protected void sendMessage(ClientConnection user, String message) {
        messages.add(String.format("%s: %s", user.getUsername(), message));
        for (ClientConnection otherUser: connectedPeople) {
            if (true) {
                otherUser.getSender().sendMessage(String.format("%s: %s", user.getUsername(), message));
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

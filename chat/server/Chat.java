package chat.server;

import java.util.*;
import java.io.*;

public class Chat {
    ArrayList<ClientConnection> connectedPeople = new ArrayList<>();
    HashMap<String, Integer> users = new HashMap<>();
    HashMap<Integer, ArrayList<String>> directMessages = new HashMap<>();
    File userdb;
    File messagedb;

    public Chat() {
        this.userdb = new File("usersdb.txt");
        loadUsersFromFile();

        initializeDirectMessages();

        this.messagedb = new File("messagedb.txt");
        loadMessagesFromFile();
    }

    protected void loadUsersFromFile() {
        try {
            userdb.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Scanner fileReader = new Scanner(new FileReader(userdb))) {
            while (fileReader.hasNextLine()) {
                String[] line = fileReader.nextLine().strip().split(": ");
                users.put(line[0], Integer.parseInt(line[1]));
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not open userdb.txt, aborting");
        }
    }

    protected void initializeDirectMessages() {
        ArrayList<String> setOfUsers = new ArrayList<>(users.keySet());

        for (int i = 0; i < setOfUsers.size(); i++) {
            for (int j = i + 1; j < setOfUsers.size(); j++) {
                directMessages.put(setOfUsers.get(i).hashCode() * setOfUsers.get(j).hashCode(), new ArrayList<>());
            }
        }
    }

    protected void loadMessagesFromFile() {
        try {
            messagedb.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Scanner fileReader = new Scanner(new FileReader(messagedb))) {
            while (fileReader.hasNextLine()) {
                String[] line = fileReader.nextLine().strip().split(";");
                directMessages.get(Integer.parseInt(line[0])).add(line[1]);
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not open userdb.txt, aborting");
        }
    }

    protected synchronized String getActiveUserList(String username) {
        if (connectedPeople.size() == 1) {
            return "no one online";
        }

        ArrayList<String> allUsernames = new ArrayList<>();
        connectedPeople.forEach(user -> allUsernames.add(user.getUsername()));
        allUsernames.remove(username);

        return "online: " + allUsernames.toString().substring(1, allUsernames.toString().length() - 1);
    }

    protected ClientConnection getUser(String name) {
        for (ClientConnection user: connectedPeople) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        System.out.println("Not able to find user");
        return null;
    }

    protected synchronized boolean isRegistered(String name) {
        return users.containsKey(name);
    }

    protected synchronized boolean isCorrectPassword(String username, String password) {
        return users.get(username) == username.hashCode() * password.hashCode();
    }

    protected synchronized void addNewUser(String username, String password) {
        users.put(username, username.hashCode() * password.hashCode());

        try (PrintWriter file = new PrintWriter(new FileWriter(userdb, true))) {
            file.println(String.format("%s: %d", username, username.hashCode() * password.hashCode()));
        } catch (IOException e) {
            System.out.println("Could not add user to database");
            return;
        }
        System.out.println("Username and password added to database");

        for (String user: users.keySet()) {
            directMessages.put(user.hashCode() * username.hashCode(), new ArrayList<>());
        }

    }


    protected synchronized void connectUser(ClientConnection client, String username) {
        if (!connectedPeople.contains(client)) {
            connectedPeople.add(client);
        }
        client.setUsername(username);
    }

    protected synchronized void disconnectUser(ClientConnection client) {
        connectedPeople.remove(client);
    }

    protected synchronized void addMessageToDatabase(int chatHashCode, String message) {
        directMessages.get(chatHashCode).add(message);

        try (PrintWriter file = new PrintWriter(new FileWriter(messagedb, true))) {
            file.println(String.format("%d;%s", chatHashCode, message));
        } catch (IOException e) {
            System.out.println("Could not add message to database");
        }
    }

    protected synchronized void sendDirectMessage(String toUser, String message) {
        getUser(toUser).sendMessage(message);
    }

    protected synchronized void sendAllPreviousDirectMessages(String fromUser, String toUser) {
        ArrayList<String> messages = directMessages.get(fromUser.hashCode() * toUser.hashCode());
        if (messages.size() < 10) {
            for (int i = 0; i < messages.size(); i++) {
                getUser(toUser).sendMessage(messages.get(i));
            }
        } else {
                ArrayList<String> latestMessages = new ArrayList<>(messages.subList(messages.size() - 10, messages.size()));
                for (String message: latestMessages) {
                    getUser(toUser).sendMessage(message);
            }
        }
        messages.replaceAll(message -> message.startsWith("(new) ") ? message.substring(6, message.length()) : message);
    }
}


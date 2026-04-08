package chat.server;

import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

public class Chat {
    ArrayList<User> connectedPeople = new ArrayList<>();
    HashMap<String, HashMap<String, String>> users = new HashMap<>();
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
            if (userdb.createNewFile()) {
                try (PrintWriter file = new PrintWriter(new FileWriter(userdb, true))) {
                    file.println("admin;;-966019908;;ADMIN");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        try (Scanner fileReader = new Scanner(new FileReader(userdb))) {
            while (fileReader.hasNextLine()) {
                String[] line = fileReader.nextLine().strip().split(";;");
                HashMap<String, String> user = new HashMap<>();
                users.put(line[0], user);
                user.put("Password", line[1]);
                user.put("Role", line[2]);
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

        ArrayList<User> allUsernames = new ArrayList<>(connectedPeople);
        allUsernames.remove(getUser(username));

        return "online: " + allUsernames.toString().substring(1, allUsernames.toString().length() - 1);
    }

    protected User getUser(String name) {
        for (User user: connectedPeople) {
            if (Objects.equals(user.getUsername(), name)) {
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
        return Objects.equals(users.get(username).get("Password"), Integer.toString(username.hashCode() * password.hashCode()));
    }

    protected synchronized void registerNewUser(String username, String password) {
        users.put(username, new HashMap<>());
        users.get(username).put("Password", Integer.toString(username.hashCode() * password.hashCode()));
        users.get(username).put("Role", Role.USER.name());

        try (PrintWriter file = new PrintWriter(new FileWriter(userdb, true))) {
            file.println(String.format("%s;;%d;;USER", username, username.hashCode() * password.hashCode()));
        } catch (IOException e) {
            System.out.println("Could not add user to database");
            return;
        }
        System.out.println("Username and password added to database");

        for (String user: users.keySet()) {
            directMessages.put(user.hashCode() * username.hashCode(), new ArrayList<>());
        }

    }

    protected synchronized void connectUser(User client, String username) {
        if (!connectedPeople.contains(client)) {
            connectedPeople.add(client);
        }
        client.setUsername(username);
        client.setRole(Role.valueOf(users.get(username).get("Role")));
    }

    protected synchronized void disconnect(User client) {
        client.disconnect();
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

    protected synchronized void setUserRole(String username, Role role) {
        users.get(username).put("Role", role.name());

        try (PrintWriter file = new PrintWriter(new FileWriter(userdb, false))) {
            for (String key: users.keySet()) {
                ArrayList<String> values = new ArrayList<>(users.get(key).values());
                file.println(String.format("%s;;%s;;%s", key, values.get(1), values.get(0)));
            }
        } catch (IOException e) {
            System.out.println("Could not add user to database");
        }
    }

    protected synchronized void setUsername(String username, String newUsername) {
        getUser(username).setUsername(newUsername);
    }
}


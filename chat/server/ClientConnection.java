package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection implements Runnable {
    final Chat chat;
    final Socket socket;
    String username;
    ConnectionReceiver connectionReciever;
    DataOutputStream output;
    final Scanner scanner = new Scanner(System.in);
    Role role = Role.USER;

    public ClientConnection(Chat chat, Socket socket) {
        this.chat = chat;
        this.socket = socket;
        try {
            this.output = new DataOutputStream(socket.getOutputStream());
            this.connectionReciever = new ConnectionReceiver(new DataInputStream(socket.getInputStream()), chat, this);
            new Thread(connectionReciever).start();

        } catch (IOException e) {
            System.out.println("Could not establish connection with Client");
        }
    }

    @Override
    public void run() {
        while (true) scanner.nextLine();
    }

    protected void setUsername(String username) {
        this.username = username;
    }

    protected String getUsername() {
        return username;
    }

    protected void setRole(Role role) {
        this.role = role;
    }

    protected Role getRole() {
        return role;
    }

    protected void sendMessage(String message) {
        try {
            output.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Failed to send message to client");
        }
    }

    @Override
    public String toString() {
        return username;
    }
}

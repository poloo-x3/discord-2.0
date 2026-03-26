package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {
    final Chat chat;
    final Socket socket;
    String username;
    ConnectionReciever connectionReciever = null;
    ConnectionSender connectionSender = null;

    public ClientConnection(Chat chat, Socket socket) {
        this.chat = chat;
        this.socket = socket;
        try {
            this.connectionSender = new ConnectionSender(new DataOutputStream(socket.getOutputStream()), chat, this);
            this.connectionReciever = new ConnectionReciever(new DataInputStream(socket.getInputStream()), chat, this);

            new Thread(connectionSender).start();
            new Thread(connectionReciever).start();
        } catch (IOException e) {
            System.out.println("Could not establish connection with Client");
        }
    }

    protected void setUsername(String username) {
        this.username = username;
    }

    protected String getUsername() {
        return username;
    }

    protected ConnectionSender getSender() {
        return connectionSender;
    }
}

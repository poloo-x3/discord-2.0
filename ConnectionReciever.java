package chat.server;

import chat.client.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionReciever implements Runnable {
    final DataInputStream input;
    final Chat chat;
    final ClientConnection parent;

    public ConnectionReciever(DataInputStream input, Chat chat, ClientConnection parent) {
        this.input = input;
        this.chat = chat;
        this.parent = parent;
    }

    @Override
    public void run() {
        try {
            String username;
            while (true) {
                username = input.readUTF();
                if (chat.hasUsername(username)) {
                    parent.connectionSender.sendMessage("Server: this name is already taken! Choose another one.");
                    continue;
                }
                break;
            }
                chat.connectUser(parent, username);
            while (true) {
                String message = input.readUTF();
                chat.sendMessage(parent, message);
            }
        } catch (Exception e) {
            chat.disconnectUser(parent);
            parent.getSender().isConnected = false;
        }
    }
}


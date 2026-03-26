package chat.server;

import java.io.DataOutputStream;
import java.io.IOException;

public class ConnectionSender implements Runnable{
    final DataOutputStream output;
    final Chat chat;
    final ClientConnection parent;
    boolean isConnected = true;

    public ConnectionSender(DataOutputStream output, Chat chat, ClientConnection parent) {
        this.output = output;
        this.chat = chat;
        this.parent = parent;
    }

    @Override
    public void run() {
        try {
            output.writeUTF("Server: write your name");
            while (isConnected) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            System.out.println("lol");
        }
    }

    protected void sendMessage(String message) {
        try {
            output.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Failed to send message to client");
        }
    }
}

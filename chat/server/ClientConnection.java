package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection implements Runnable {
    final Scanner scanner = new Scanner(System.in);
    final DataOutputStream output;

    public ClientConnection(DataOutputStream output) {
        this.output = output;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            scanner.nextLine();
        }
    }

    protected void disconnect() {
        receiverThread.interrupt();
        Thread.currentThread().interrupt();
    }

    protected void sendMessage(String message) {
        try {
            output.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Failed to send message to client");
        }
    }
}

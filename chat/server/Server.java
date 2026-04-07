package chat.server;

import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) {
        final int PORT = 6767;
        final Chat chat = new Chat();

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started!");

            while (!Thread.interrupted()) {
                Socket socket = server.accept();
                new Thread(new ClientConnection(chat, socket));
            }

        } catch (IOException e) {
            System.out.println("Could not establish connection with Client.");
        }
    }
}
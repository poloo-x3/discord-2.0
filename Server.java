package chat.server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    final static Scanner scanner = new Scanner(System.in);
    final static int PORT = 6767;
    final static Chat chat = new Chat();


    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT);) {
            server.setSoTimeout(10000);
            System.out.println("Server started!");

            while (!Thread.interrupted()) {
                Socket socket = server.accept();
                new ClientConnection(chat, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
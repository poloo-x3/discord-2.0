package chat.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    final static Scanner scanner = new Scanner(System.in);
    final static int PORT = 6767;

    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(100);

        try (Socket socket = new Socket("127.0.0.1", PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            System.out.println("Client started!");

            Thread thread = new Thread(new ClientReciver(input));
            thread.start();

            while (true) {
                output.writeUTF(scanner.nextLine());
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }

}

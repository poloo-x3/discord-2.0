package chat.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    final static Scanner scanner = new Scanner(System.in);
    final static int PORT = 6767;

    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(1000);

        try (Socket socket = new Socket("127.0.0.1", PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            System.out.println("Client started!");

            Thread thread = new Thread(new Listener(input));
            thread.start();

            while (true) {
                String message = scanner.nextLine();
                output.writeUTF(message);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}

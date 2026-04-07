package chat.client;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientReciver implements Runnable {
    final DataInputStream input;

    public ClientReciver(DataInputStream input) {
        this.input = input;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println(input.readUTF());
            }
        } catch (IOException e) {
        }

    }
}

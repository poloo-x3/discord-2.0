package chat.client;

import java.io.DataInputStream;
import java.io.IOException;

public class Listener implements Runnable {
    final DataInputStream input;

    public Listener(DataInputStream input) {
        this.input = input;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                System.out.println(input.readUTF());
            }
        } catch (IOException e) {
        }

    }

}

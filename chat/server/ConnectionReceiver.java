package chat.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;

public class ConnectionReceiver implements Runnable {
    final User user;
    final DataInputStream input;

    public ConnectionReceiver(User user, DataInputStream input) {
        this.user = user;
        this.input = input;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                user.run(input.readUTF());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


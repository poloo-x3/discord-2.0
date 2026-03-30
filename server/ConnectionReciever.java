package chat.server;

import java.io.DataInputStream;

public class ConnectionReciever implements Runnable {
    final DataInputStream input;
    final Chat chat;
    final ClientConnection parent;
    private boolean loggedIn = false;

    public ConnectionReciever(DataInputStream input, Chat chat, ClientConnection parent) {
        this.input = input;
        this.chat = chat;
        this.parent = parent;
    }

    @Override
    public void run() {
        String msg;

        try {
            while (!loggedIn) {
                msg = input.readUTF();
                if (!msg.startsWith("/")) {
                    parent.sendMessage("Server: you are not in the chat!");
                    continue;
                }

                String[] tokens = msg.split(" ");
                switch (tokens[0]) {
                    case "/list" -> parent.sendMessage(chat.getUserList());
                }
            }
        } catch (Exception e) {
            System.out.println("meow mother fucker");
        }
    }
}


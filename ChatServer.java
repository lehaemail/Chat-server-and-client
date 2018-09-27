

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ChatServer {
    public static final int DEFAULT_PORT = 4688;
    List<ChatServer.ClientHandler> clients = new ArrayList();
    int clientCount;

    public ChatServer() {
    }

    void run() {
        try {
            int var2 = 0;
            this.clientCount = 0;
            ServerSocket var3 = new ServerSocket(4688);
            var3.setReuseAddress(true);

            while(true) {
                Socket var4 = var3.accept();
                System.out.println("Spawning " + var2);
                ChatServer.ClientHandler var1 = new ChatServer.ClientHandler(var4, var2, this);
                Thread var5 = new Thread(var1);
                this.clients.add(var1);
                System.out.println(this.clients.size() + " clients in the list.");
                var5.start();
                ++var2;
                this.clientCount = var2;
            }
        } catch (IOException var6) {
            var6.printStackTrace();
        }
    }

    public void sendToAll(String var1, int var2) {
        Iterator var3 = this.clients.iterator();

        while(var3.hasNext()) {
            ChatServer.ClientHandler var4 = (ChatServer.ClientHandler)var3.next();
            if (var4.id != var2 && var4.isConnected()) {
                var4.send(var1);
            }
        }

    }

    public static void main(String[] var0) {
        ChatServer var1 = new ChatServer();
        var1.run();
    }

    class ClientHandler implements Runnable {
        private Socket incoming;
        int id;
        Scanner in;
        PrintWriter out;
        ChatServer server;
        String name;
        boolean connected;

        public boolean isConnected() {
            return this.connected;
        }

        public ClientHandler(Socket var2, int var3, ChatServer var4) {
            this.incoming = var2;
            this.id = var3;
            this.server = var4;
            System.out.println("ClientHandler socket local port is " + this.incoming.getLocalPort() + " remote port is " + this.incoming.getPort());
        }

        public void send(String var1) {
            if (this.connected) {
                this.out.println(var1);
            }

        }

        public void handleMsg(String var1) {
            String[] var2 = var1.split("\\s");
            System.out.println(this.id + ": " + var1);
            if (var2[0].equalsIgnoreCase("disconnect")) {
                this.disconnect();
            } else {
                this.server.sendToAll(this.name + ": " + var1, this.id);
            }
        }

        public void disconnect() {
            this.server.sendToAll(this.name + " has left the chat.", this.id);

            try {
                this.send("disconnected");
                this.connected = false;
                this.incoming.close();
                ChatServer.this.clients.remove(this);
            } catch (IOException var2) {
                var2.printStackTrace();
            }

        }

        public void getConnectMsg() {
            String var1 = this.in.nextLine();
            String[] var2 = var1.split("\\s");
            if (var2.length >= 2 && var2[0].equalsIgnoreCase("connect")) {
                this.name = var2[1];
                this.server.sendToAll(this.name + " has joined the chat.", this.id);
            } else {
                System.out.println("*" + var1 + "*");
                throw new RuntimeException("invalid connect string");
            }
        }

        public void run() {
            try {
                this.connected = true;
                InputStream var1 = this.incoming.getInputStream();
                OutputStream var2 = this.incoming.getOutputStream();
                this.in = new Scanner(var1);
                this.out = new PrintWriter(var2, true);
                this.send("Hello! Welcome to Oleksii Chat Server.");
                this.getConnectMsg();

                while(this.connected && this.in.hasNextLine()) {
                    this.handleMsg(this.in.nextLine());
                }
            } catch (IOException var3) {
                var3.printStackTrace();
            }

        }
    }
}

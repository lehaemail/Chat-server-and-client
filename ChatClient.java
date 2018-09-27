import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ChatClient extends JFrame {
    public static final int PORT = 4688;
    public static final String HOST = "127.0.0.1"; // localhost
    ChatClient.ChatMaker chatReader;
    JTextArea textArea;
    JScrollPane scrollPane;
    JTextField textField;
    Socket socket;
    Scanner scanner;
    PrintWriter printWriter;
    static String name = "anonimus";
    
    // Shows message on sender's display
    synchronized void showMsg(String msg) {
        this.textArea.append(msg);
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }

    public ChatClient(String username) {
        this.name = username; // Set username
        this.setTitle("Oleksii Chat Client: " + username); // Set window title
        this.setLayout(new BorderLayout()); // Set layaout
        this.textArea = new JTextArea(); // Set messaging area
        this.scrollPane = new JScrollPane(this.textArea); // Set scroll pane
        this.textField = new JTextField();
        this.add(this.scrollPane, "Center");
        this.add(this.textField, "South");
        InputMap msgIn = this.textField.getInputMap(0);
        ActionMap msgOut = this.textField.getActionMap();
        msgIn.put(KeyStroke.getKeyStroke(10, 0), "sendCommand");
        msgOut.put("sendCommand", new ChatClient.CommandSender("sendCommand"));
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
    }
    
    // Connects to the server
    void connect() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.scanner= new Scanner(this.socket.getInputStream());
            this.printWriter = new PrintWriter(this.socket.getOutputStream(), true);
            this.chatReader = new ChatClient.ChatMaker();
            this.chatReader.start();
            this.printWriter.println("connect " + this.name);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }
    
    // Disconnects from the server
    void disconnect() {
        this.printWriter.println("disconnect " + this.name);
        this.chatReader.disconnect();

        try {
            this.socket.close();
        } catch (IOException exp) {
            exp.printStackTrace();
        }

        System.exit(0);
    }
    
    // helper function for sendMessage()
    public void sendMsgHelper(String msg) {
        this.printWriter.println(msg);
    }
    
    // Send message to the server
    void sendMessage() {
        String msg = this.textField.getText();
        if (msg.equalsIgnoreCase("/q")) {
            this.disconnect();
        } else {
            this.showMsg(msg + "\n");
            this.sendMsgHelper(msg);
            this.textField.setText("");
        }
    }    

    public static void main(String[] args) {
        String uName = name; // User name
        if (args.length > 0) {
            uName = args[0];
        }

        ChatClient chatClient = new ChatClient(uName);
        chatClient.setSize(600, 400);
        chatClient.connect();
        chatClient.setVisible(true); // Initialize GUI
    }
    
    // Displays chat messages
    class ChatMaker extends Thread {
        boolean done = false;

        ChatMaker() {
        }

        public void disconnect() {
            this.done = true;
        }

        public void run() {
            while(!this.done) {
                String msg = this.read();
                if (msg != null) {
                    ChatClient.this.showMsg(msg + "\n");
                }
            }

        }

        String read() {
            return ChatClient.this.scanner.hasNextLine() ? ChatClient.this.scanner.nextLine() : null;
        }
    }
    
    
    // Sends commands to the server
    class CommandSender extends AbstractAction {
        String cmd;

        public CommandSender(String command) {
            this.cmd = command;
        }

        public String getCmd() {
            return this.cmd;
        }

        public void actionPerformed(ActionEvent actEvnt) {
            if (this.cmd.equals("sendCommand")) {
                ChatClient.this.sendMessage();
            }

        }
    }
}
 

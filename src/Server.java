// Java implementation of Server side 
// It contains two classes : Server and ClientHandler 

import java.io.*;
import java.util.*;
import java.net.*;

// Server class 
public class Server {

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();

    // counter for clients
    private static int i = 0;

    public static ArrayList<String> userNames = new ArrayList<>();

    public static void main(String[] args) {

        // You can't open a port below 1024, if you don't have root privileges
        // port number
        int portNumber;
        if (args.length == 0) {
            portNumber = 1025;
        } else if (Integer.parseInt(args[0]) < 1024) {
            portNumber = 1025;
        } else {
            portNumber = Integer.parseInt(args[0]);
        }
        System.out.println("Port number = " + portNumber);

        try {
            // server is listening on entered port number
            ServerSocket ss = new ServerSocket(portNumber);

            Socket s;


            // client request using infinite loop
            while (true) {
                // Accept the incoming request
                s = ss.accept();

                System.out.println("New client request received : " + s);


                // obtain input and output streams
                ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());

                // Create a new handler object for handling this request.
                System.out.println("Creating a new handler for this client...");
                ClientHandler mtch = new ClientHandler(s, "Client" + i, dis, dos);

                // Create a new Thread with this object.
                Thread t = new Thread(mtch);

                System.out.println("Adding this client to active client list");
                // add this client to active clients list
                ar.add(mtch);
                System.out.println("Added " + mtch.getName() + " to the active client list - " + ar.contains(mtch));

                // start the thread.
                t.start();

                // increment i for new client.
                // i is used for naming only, and can be replaced
                // by any naming scheme
                i++;
            }

        } catch (IOException e) { // Letting client know that the connection was Refused
            e.printStackTrace();
        }
    }
}

// ClientHandler class
class ClientHandler implements Runnable {
    private String name;
    private final ObjectInputStream dis;
    public final ObjectOutputStream dos;
    private Socket s;
    private boolean isloggedin;
    private Queue<Message> queue = new LinkedList<>();

    // constructor
    public ClientHandler(Socket s, String name,
                         ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin = true;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void addMessage(Message message) {
        queue.add(message);
    }

    @Override
    public void run() {

        Message received;
        while (true) {
            try {
                // receive the string
                received = (Message) dis.readObject();

                System.out.println(received);

                switch (received.getType()) {
                    case LOGOUT:
                        // Not closing connection to avoid reestablishing a connection for "reconnect"
                        this.isloggedin = false;
                        break;
                    case SETNAME:
                        // If we have time we need to implement that the name is completely unique

                        if (!checkName(received.getSender())) {
                            setName(received.getSender());
                            Server.userNames.add(received.getSender());
                            for (ClientHandler mc : Server.ar) {
                                mc.dos.writeObject(new Message(MessageType.ACTIVEUSERS, Server.userNames));
                            }
                        } else {
                            this.dos.writeObject(new Message(MessageType.BASIC));
                            quit();
                        }
                        break;
                    case RECONNECT:
                        this.isloggedin = true;
                        // Send stored messages to client
                        while (!queue.isEmpty()) {
                            this.dos.writeObject(queue.remove());
                        }
                        break;
                    case SENDMESSAGE:
                        for (ClientHandler mc : Server.ar) {
                            // if the recipient is found, write on its
                            // output stream
                            if (mc.getName().equals(received.getRecipient()) && mc.isloggedin) {
                                // Need to update client to handle message object
                                mc.dos.writeObject(received);
                                break;
                            } else if (!mc.isloggedin){
                                // Store messages while client is "logged out"
                                mc.addMessage(received);
                            }
                        }
                        break;
                    case QUIT:
                        // Graceful exit
                        quit();
                        break;
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Boolean checkName(String name) {
        Boolean r = false;
        for (ClientHandler mc : Server.ar) {
            if (name.equals(mc.getName())) {
                r = true;
            } else {
                r = true;
            }
        }
        return r;
    }

    private void quit() {
        try {
            this.dis.close();
            this.dos.close();
            this.s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.ar.remove(this);
    }
}


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

    public static transient ArrayList<String> userNames = new ArrayList<>();

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
                InputStream dis = s.getInputStream();
                OutputStream dos = s.getOutputStream();

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
    private ObjectInputStream dis;
    private ObjectOutputStream dos;
    private Socket s;
    private boolean isloggedin;
    private Queue<Message> queue = new LinkedList<>();
    private FileWriter csvWriter;
    private boolean quit = false;

    {
        try {
            csvWriter = new FileWriter("timeStamps.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // constructor
    public ClientHandler(Socket s, String name,
                         InputStream dis, OutputStream dos) {
        try {
            this.dos = new ObjectOutputStream(dos);
            this.dis = new ObjectInputStream(dis);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        while (!quit) {
            try {
                // receive the string
                received = (Message) dis.readObject();

                //System.out.println(received);

                switch (received.getType()) {
                    case LOGOUT:
                        // Not closing connection to avoid reestablishing a connection for "reconnect"
                        this.isloggedin = false;
                        System.out.println(this.name + " has logged out");
                        break;
                    case SETNAME:
                        if (checkName(received.getSender())) {
                            setName(received.getSender());
                            Server.userNames.add(received.getSender());
                            System.out.println("Sending active users to all clients.");
                            for (ClientHandler mc : Server.ar) {
                                if (!mc.getName().equals(this.getName())){
                                    mc.dos.writeObject(new Message(MessageType.ACTIVEUSERS, Server.userNames));
                                    mc.dos.reset();
                                    mc.dos.flush();
                                }
                            }
                        } else {
                            this.dos.writeObject(new Message(MessageType.BASIC));
                            this.dos.reset();
                            this.dos.flush();
                            System.out.println(this.name + " is already in use. Rejecting connection.");
                            quit();
                        }
                        break;
                    case RECONNECT:
                        this.isloggedin = true;
                        // Send stored messages to client
                        System.out.println(this.name + " has logged back in. Sending stored messages.");
                        while (!queue.isEmpty()) {
                            this.dos.writeObject(queue.remove());
                            this.dos.reset();
                            this.dos.flush();
                        }
                        break;
                    case SENDMESSAGE:
                        for (ClientHandler mc : Server.ar) {
                            // if the recipient is found, write on its
                            // output stream
                            if (received.getRecipient().equalsIgnoreCase("all") ||
                                    mc.getName().equals(received.getRecipient()) && mc.isloggedin) {
                                // Need to update client to handle message object
                                mc.dos.writeObject(received);
                                mc.dos.reset();
                                mc.dos.flush();
                                System.out.println(received.toString());
                                break;

                            } else if (!mc.isloggedin){
                                // Store messages while client is "logged out"
                                mc.addMessage(received);
                                System.out.println(mc.name + " is not logged in. " +
                                        "Message will send when the user logged back in.");
                            }
                        }
                        break;
                    case QUIT:
                        // Graceful exit
                        quit();
                        System.out.println(this.name + " has exited chatroom.");
                        break;
                    case RECEIPT:
                        String receipt = received.getTimeSent() + "," + received.getTimeReceived() + "\n";
                        writeToFile(receipt);
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
                r = false;
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
            quit = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.ar.remove(this);
    }

    private void writeToFile(String str) {
        try {
            System.out.println("Receipt Received");
            csvWriter.append(str);
            csvWriter.flush();
            System.out.println("Message written to TimeStamp.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


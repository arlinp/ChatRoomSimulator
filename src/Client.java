// Java implementation for multithreaded chat client
// Command-line Version 1.0

//import javafx.scene.text.Text;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Client {
    private static int ServerPort = 1025;

    //Saved client settings/info
    private static String username;
    private static String sendingTo;
    private static long timeSent;
    private static long timeReceived;
    private static int clientNum = 0;
    private static boolean loggedIn = true;
    private static boolean quit = false;

    //Saved lists
    private static ArrayList<String> activeUsers = new ArrayList<>();
    private static ArrayList<Message> savedMessages = new ArrayList<>();

    public static void main(String[] args) throws UnknownHostException, IOException {

        Scanner scn = new Scanner(System.in);

        InetAddress ip = InetAddress.getByName(args[0]);
        ServerPort = Integer.parseInt(args[1]);
        username = args[2];
//        InetAddress ip = InetAddress.getByName("localhost");
//        ServerPort = 1025;
//        username = "arlin2";

//         getting localhost ip
//        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // establish output streams
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        //send a hello message to register username with client
        Message hello = new Message(MessageType.SETNAME, username.toLowerCase());
        oos.writeObject(hello);
        oos.reset();
        oos.flush();

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!quit && scn.hasNextLine()) {
                    try {
                        // read the message to deliver.
                        String msg = scn.nextLine();

                        if (!msg.isEmpty()) {
                            Message newMsg;
                            StringTokenizer tkmsg = new StringTokenizer(msg, "#");
                            String command = tkmsg.nextToken();

                            // logging out
                            if (command.equalsIgnoreCase("logout")) {
                                loggedIn = false;
                                newMsg = new Message(MessageType.LOGOUT);
                                oos.writeObject(newMsg);
                                oos.reset();
                                oos.flush();
                                System.out.println("You have logged out");
                            }
                            if (command.equalsIgnoreCase("login")) {
                                loggedIn = true;
                                newMsg = new Message(MessageType.RECONNECT);
                                System.out.println("You have logged in " + username);
                                oos.writeObject(newMsg);
                                oos.reset();
                                oos.flush();
                            }
                            if(loggedIn){
                                //view all active users
                                if (command.equalsIgnoreCase("activeUsers")) {
                                    System.out.println("Here are the active users: ");
//
                                    if(activeUsers.isEmpty()) {
                                        System.out.println("You are alone in this server");}
                                    else {
                                        for (String user : activeUsers) {
                                            System.out.println(user);
                                        }
                                    }

                                }

                                else if (command.equalsIgnoreCase("quit")) {
                                    newMsg = new Message(MessageType.QUIT);
                                    oos.writeObject(newMsg);
                                    oos.reset();
                                    oos.flush();
                                    quit = true;
                                    System.exit(0);
                                }

                                // changing your username - send change to server
                                else if (command.equalsIgnoreCase("chgName")) {
                                    username = tkmsg.nextToken();
                                    newMsg = new Message(MessageType.SETNAME);
                                    newMsg.setSender(username);

                                    // set timeSent and write on the output stream
                                    System.out.println("You changed your name to " + username);
                                    newMsg.setTimeSent(System.currentTimeMillis());
                                    oos.writeObject(newMsg);
                                    oos.reset();
                                    oos.flush();
                                    savedMessages.add(newMsg);

                                } else if(tkmsg.hasMoreTokens()){
                                    //create message object
                                    sendingTo = command;
                                    newMsg = new Message(MessageType.SENDMESSAGE);
                                    newMsg.setSender(username);
                                    newMsg.setRecipient(sendingTo.toLowerCase());
                                    newMsg.setMessage(tkmsg.nextToken());

                                    // set timeSent and write on the output stream
                                    newMsg.setTimeSent(System.currentTimeMillis());
                                    oos.writeObject(newMsg);
                                    oos.reset();
                                    oos.flush();
                                    savedMessages.add(newMsg);
                                    System.out.println("SENT: " + "@" + newMsg.getRecipient() + " " +
                                            newMsg.getMessage());
                                }
                                else {
                                    System.out.println("Not an actual command. Try again");
                                }
                            }
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(() -> {
            Date date;
            Message msgReceived;

            while (!quit) {
                try {
                    // read the message sent to this client
                    msgReceived = (Message)ois.readObject();

                    // set time received stamp
                    msgReceived.setTimeReceived(System.currentTimeMillis());

                    //print message received
                    date = java.util.Calendar.getInstance().getTime();


                    //Send receipt to server
                    if(msgReceived.getType() == MessageType.SENDMESSAGE) {
                        Message receipt = msgReceived;
                        receipt.setType(MessageType.RECEIPT);
                        oos.writeObject(receipt);
                        oos.reset();
                        oos.flush();
                        System.out.println("RECEIVED: " + date + " " + "From: @" + msgReceived.getSender() + ": " +  " " +
                                msgReceived.getMessage());
                    }

                    if (msgReceived.getType() == MessageType.ACTIVEUSERS) {
                        activeUsers = (ArrayList<String>)msgReceived.getUsers();
                    }

                    if (msgReceived.getType() == MessageType.BASIC) {
                        System.out.println("Choose a different username");
                    }


                    //print message received
                    if (msgReceived == null) {
                        System.out.println("received empty message");
                    }



                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        });
        sendMessage.start();
        readMessage.start();
    }


    public Client(String ID) {
        this.username = ID;
    }

    private String getUsername() {
        return this.username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }
}

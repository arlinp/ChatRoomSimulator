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

    //Saved lists
    private static ArrayList<String> activeUsers;
    private static ArrayList<Message> savedMessages = new ArrayList<>();

    //Display aspects
    public static StringBuilder log = new StringBuilder();
    //private static Text logDisplay = new Text();

    public static void main(String[] args) throws UnknownHostException, IOException {

        Scanner scn = new Scanner(System.in);

//        InetAddress ip = InetAddress.getByName(args[0]);
//        ServerPort = Integer.parseInt(args[1]);
//        username = args[2];
        InetAddress ip = InetAddress.getByName("localhost");
        ServerPort = 1025;
        username = "arlin";

//         getting localhost ip
//        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // establish output streams
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        //send a hello message to register username with client
        Message hello = new Message(MessageType.SETNAME, username);
        oos.writeObject(hello);
        oos.reset();
        oos.flush();


        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
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
                                newMsg = new Message(MessageType.LOGIN);
                                System.out.println("You have logged in " + username);
                                oos.writeObject(newMsg);
                                oos.reset();
                                oos.flush();
                            }
                            if(loggedIn){
                                //view all active users
                                if (command.equalsIgnoreCase("activeUsers")) {
                                    System.out.println("Here are the active users: ");

                                    if(activeUsers.isEmpty()) {
                                        System.out.println("no users");}
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
                                    newMsg.setRecipient(sendingTo);
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
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                Date date;
                Message msgReceived;

                while (true) {
                    try {
                        // read the message sent to this client
                        // set time received stamp
                        msgReceived = (Message)ois.readObject();
                        System.out.println(msgReceived.getType());

                        msgReceived.setTimeReceived(System.currentTimeMillis());
                        //savedMessages.add(msgReceived);

                        //print message received
                        date = java.util.Calendar.getInstance().getTime();
                        System.out.println("RECEIVED: " + date + " " + "From: @" + msgReceived.getSender() + ": " +  " " +
                                msgReceived.getMessage());


                        //Send receipt to server
                        if(msgReceived.getType() == MessageType.SENDMESSAGE) {
                            Message receipt = msgReceived;
                            receipt.setType(MessageType.RECEIPT);
                            oos.writeObject(receipt);
                            oos.reset();
                            oos.flush();
                        }

                        if (msgReceived.getType() == MessageType.ACTIVEUSERS) {
                            System.out.println("Received active users list");
                            activeUsers = (ArrayList<String>)msgReceived.getUsers();
                            System.out.println(msgReceived.getType());
                        }

                        //print message received
                        if (msgReceived == null) {
                            System.out.println("received empty message");
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
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

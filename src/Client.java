// Java implementation for multithreaded chat client

import javafx.scene.text.Text;

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

    //Saved lists
    private static ArrayList<String> activeUsers = new ArrayList<>();
    private static ArrayList<Message> savedMessages = new ArrayList<>();

    //Display aspects
    public static StringBuilder log = new StringBuilder();
    private static Text logDisplay = new Text();

    public static void main(String[] args) throws UnknownHostException, IOException {

        Scanner scn = new Scanner(System.in);
        /**commenting out until I figure out a way separate this from output data stream**/

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

        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

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
                            StringTokenizer tkmsg = new StringTokenizer(msg);
                            String command = tkmsg.nextToken();

                            // logging out
                            if (command.equalsIgnoreCase("logout")) {
                                newMsg = new Message(MessageType.LOGOUT);
                                System.out.println("You have logged out");
                            }
                            //view all active users
                            if (command.equalsIgnoreCase("active")) {
                                System.out.println("Here are the active users: ");
                                for (String user: activeUsers) {
                                    System.out.println(user);
                                }
                            }
                            //changing your username - send change to server
                            if (command.equalsIgnoreCase("chgName")) {
                                username = tkmsg.nextToken();
                                newMsg = new Message(MessageType.SETNAME);
                                newMsg.setSender(username);
                                System.out.println("You changed your name to " + username);
                                // set timeSent and write on the output stream
                                newMsg.setTimeSent(System.currentTimeMillis());
                                oos.writeObject(newMsg);
                                oos.reset();
                                oos.flush();
                                savedMessages.add(newMsg);
                            } else {
                                //create message object
                                sendingTo = command;
                                newMsg = new Message(MessageType.SENDMESSAGE);
                                newMsg.setSender(username);
                                newMsg.setRecipient(sendingTo);
                                newMsg.setMessage(tkmsg.nextToken());

                                try {
                                    // set timeSent and write on the output stream
                                    newMsg.setTimeSent(System.currentTimeMillis());
                                    oos.writeObject(newMsg);
                                    oos.reset();
                                    oos.flush();
                                    savedMessages.add(newMsg);
                                    System.out.println("SENT: " + "@" + newMsg.getRecipient() + " " +
                                            newMsg.getMessage());
                                } catch (IOException e) {
                                    e.printStackTrace();
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

                while (true) {
                    try {
                        // read the message sent to this client
                        // set time received stamp
                        Message msgReceived = (Message) ois.readObject();
                        msgReceived.setTimeReceived(System.currentTimeMillis());
                        savedMessages.add(msgReceived);

                        date = java.util.Calendar.getInstance().getTime();

                        System.out.println("RECEIVED: " + date + " " + "From: @" + msgReceived.getSender() + ": " +  " " +
                                msgReceived.getMessage());

                        //Send receipt to server
                        Message receipt = msgReceived;
                        receipt.setType(MessageType.RECEIPT);
                        oos.writeObject(receipt);
                        oos.reset();
                        oos.flush();

                        //print message received
                        if (msgReceived == null) {
                            System.out.println("received empty message");
                        }
                        if (msgReceived.getType().equals(MessageType.ACTIVEUSERS)) {
                            System.out.println("Received active users list");
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

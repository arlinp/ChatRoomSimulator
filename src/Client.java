// Java implementation for multithreaded chat client
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.util.Date;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Client{
    private static int ServerPort = 1025;

    //Saved client settings/info
    private static String username;
    private static String sendingTo;
    private static long timeSent;
    private static long timeReceived;

    //Saved lists
    private static ArrayList<String> activeUsers = new ArrayList<>();
    private static ArrayList<Message> savedMessages = new ArrayList<>();

    //Display aspects
    public static StringBuilder log = new StringBuilder();
    private static Text logDisplay = new Text();


    public static void main(String[] args) throws UnknownHostException, IOException {


        Scanner scn = new Scanner(System.in);
        /**commenting out until I figure out a way separate this from output data stream**/

//        ip = InetAddress.getByName(args[0]);
        //       ServerPort = Integer.parseInt(args[1]);
        //       username = args[2];

//         getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        Message hello = new Message(MessageType.SETNAME, username);
        oos.writeObject(hello);


        Date date = java.util.Calendar.getInstance().getTime();
        log.append("\n" + date + " You" + " (" + username + ")" + " have entered the chat room.");

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {

                    // read the message to deliver.
                    String msg = scn.nextLine();

                    if(!msg.isEmpty()) {

                        //create message object
                        Message newMsg = new Message(MessageType.SENDMESSAGE);
                        newMsg.setSender(username);
                        newMsg.setRecipient(sendingTo);
                        newMsg.setMessage(msg);

                        try {
                            // set timeSent and write on the output stream
                            newMsg.setTimeSent(System.currentTimeMillis());
                            oos.writeObject(newMsg);
                            savedMessages.add(newMsg);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }



                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            /**when reuben sends me a message he sends
             * sendersname#msg
             */

            @Override
            public void run() {

                while (true) {
                    try {
                        // read the message sent to this client
                        // set time received stamp
                        Message msgReceived = (Message)ois.readObject();
                        msgReceived.setTimeReceived(System.currentTimeMillis());
                        savedMessages.add(msgReceived);

                        //Send receipt to server
                        Message receipt = msgReceived;
                        receipt.setType(MessageType.RECEIPT);
                        oos.writeObject(receipt);

                        //print message received
                        System.out.println(msgReceived.getMessage());

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


    public Client(String ID){
        this.username = ID;
    }

    private String getUsername(){
        return this.username;
    }

    public void setUsername(String newUsername){
        this.username = newUsername;
    }
}

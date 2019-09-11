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


public class ClientGUI extends Application {

    private static long timeSent;
    private static long timeReceived;
    private static int ServerPort = 1025;
    private static String username;
    private static InetAddress ip;
    private static Boolean sendFlag = false;
    private static TextArea textField = new TextArea("Enter text here...");
    private static TextField recipientField = new TextField("Recipient:");
    private static Text names = new Text(username);

    //Saved lists
    private static ArrayList<String> activeUsers = new ArrayList<>();
    private static StringBuilder log = new StringBuilder();
    private static ArrayList<Message> savedMessages = new ArrayList<>();

    //Display aspects
    private static String sendingTo;
    private static Text logDisplay = new Text();


    public static void main(String[] args) throws UnknownHostException, IOException {

        /**commenting out until I figure out a way separate this from output data stream**/

        username = "Candid";

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
                    System.out.println("in thread");
                    if (sendFlag) {
                        String msg = textField.getText();
                        System.out.println("worked!");
                        sendingTo = recipientField.getText();
                        log.append("\n" + date + " " + username + ": " + "@" + sendingTo + " " + msg);
                        logDisplay.setText(log.toString());
                        textField.setText("Enter text here...");
                        recipientField.setText("Recipient:");
                        sendFlag = false;

                        if (!msg.isEmpty()) {

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

        System.out.println("goddamnit amber");
        sendMessage.start();
        readMessage.start();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Server");

        VBox root = new VBox(5);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: lightgray");

        Pane glass = new Pane();
        glass.setStyle("-fx-background-color: transparent");
        root.getChildren().add(glass);

        Text logDisplay = new Text(log.toString());
        logDisplay.setWrappingWidth(670);
        logDisplay.setX(15);
        logDisplay.setStroke(Color.WHITE);

        Pane glass2 = new Pane();
        glass2.setStyle("-fx-background-color: transparent");
        glass2.getChildren().add(logDisplay);

        ScrollPane chatBox = new ScrollPane();
        chatBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatBox.setContent(glass2);
        chatBox.setMaxWidth(700);
        chatBox.setMaxHeight(450);
        chatBox.setMinHeight(450);
        chatBox.setTranslateY(50);
        chatBox.setTranslateX(50);
        chatBox.setStyle("-fx-background-color: black; -fx-control-inner-background: black;" +
                " -fx-background-insets:0; fx-border-color:blue; -fx-border-width:1; " +
                "-fx-background: black; -fx-border-insets:0;");

        root.getChildren().add(chatBox);

        Text header = new Text("On This Server:");
        header.setStroke(Color.NAVY);
        header.setTranslateX(-370);
        header.setTranslateY(-411);
        header.setFont(Font.font("Verdana", 16));

        names.setStroke(Color.DARKGOLDENROD);
        names.setX(7);
        names.setY(72);
        names.setFont(Font.font("Verdana", 13));

        root.getChildren().addAll(header);
        glass.getChildren().add(names);

        textField.setMaxWidth(700);
        textField.setMaxHeight(70);
        textField.setMinHeight(70);
        textField.setWrapText(true);
        textField.setTranslateY(30);
        textField.setTranslateX(50);
        textField.setStyle("-fx-control-inner-background: grey;" +
                "-fx-background: grey; -fx-border-insets:0;");

        recipientField.setMaxWidth(95);
        recipientField.setMinWidth(95);
        recipientField.setMaxHeight(15);
        recipientField.setTranslateY(-44);
        recipientField.setTranslateX(-370);
        recipientField.setStyle("-fx-control-inner-background: grey;" +
                "-fx-background: grey; -fx-border-insets:0;");


        Button buttonClear = new Button("Clear");
        buttonClear.setTranslateX(-395);
        buttonClear.setTranslateY(-35);
        buttonClear.setStyle("-fx-control-inner-background: grey; -fx-background-color: grey;"
                + "-fx-text-fill: black;");
        buttonClear.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                textField.clear();
                recipientField.clear();
            }
        });

        Button buttonSend = new Button("Send");
        buttonSend.setTranslateX(-345);
        buttonSend.setTranslateY(-65);
        buttonSend.setStyle("-fx-control-inner-background: grey; -fx-background-color: grey;"
                + "-fx-text-fill: black;");
        buttonSend.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendFlag = true;
                System.out.println(sendFlag);
            }
        });

        root.setPadding(new Insets(10));

        root.getChildren().addAll(textField, recipientField, buttonClear, buttonSend);

        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}

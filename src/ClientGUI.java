import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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


public class ClientGUI extends Application implements Serializable{

    private static long timeSent;
    private static long timeReceived;
    private static int ServerPort = 1025;
    private static String username;
    private static InetAddress ip;
    private static boolean sendFlag = false;
    private static boolean quit = false;

    //Saved lists
    private static ArrayList<String> activeUsers;
    private static StringBuilder log = new StringBuilder();
    private static ArrayList<Message> savedMessages = new ArrayList<>();

    //Display aspects
    private static String sendingTo;
    private static Text logDisplay = new Text();
    private static ScrollPane chatBox = new ScrollPane();
    private static VBox root = new VBox(5);
    private static Pane glass2 = new Pane();
    private static TextArea textField = new TextArea("Enter text here...");
    private static TextField recipientField = new TextField("Recipient:");
    private static Text names = new Text(username);

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

        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

        Message hello = new Message(MessageType.SETNAME, username.toLowerCase());
        oos.writeObject(hello);
        oos.reset();
        oos.flush();


        Date date = java.util.Calendar.getInstance().getTime();
        log.append("\n" + date + " You" + " (" + username + ")" + " have entered the chat room.");

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                Date date;
                while (!quit) {
                    // read the message to deliver.
                    if (sendFlag) {
                        //messageCount++;
                        date = java.util.Calendar.getInstance().getTime();
                        String msg = textField.getText();
                        sendingTo = recipientField.getText().toLowerCase();
                        log.append("\n" + date + " " + username + ": " + "@" + sendingTo + " " + msg);
                        sendFlag = false;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                logDisplay.setText(log.toString());
                                textField.setText("Enter text here...");
                                recipientField.setText("Recipient:");
                                glass2.getChildren().remove(logDisplay);
                                glass2.getChildren().add(logDisplay);
                            }
                        });

                        //create message object
                        Message newMsg = new Message(MessageType.SENDMESSAGE);
                        newMsg.setSender(username);
                        newMsg.setRecipient(sendingTo.toLowerCase());
                        newMsg.setMessage(msg);

                        try {
                            // set timeSent and write on the output stream
                            newMsg.setTimeSent(System.currentTimeMillis());
                            oos.writeObject(newMsg);
                            oos.reset();
                            oos.flush();
                            savedMessages.add(newMsg);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("User disconnected");
                        e.printStackTrace();
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {

            @Override
            public void run() {
                Date date;

                while (!quit) {
                    try {
                        // read the message sent to this client
                        // set time received stamp
                        Message msgReceived = (Message) ois.readObject();
                        msgReceived.setTimeReceived(System.currentTimeMillis());
                        savedMessages.add(msgReceived);

                        date = java.util.Calendar.getInstance().getTime();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                logDisplay.setText(log.toString());
                                glass2.getChildren().remove(logDisplay);
                                glass2.getChildren().add(logDisplay);
                            }
                        });

                        //print message received
                        if (msgReceived.getType().equals(MessageType.ACTIVEUSERS)) {
                            activeUsers = msgReceived.getUsers();
                            System.out.println("Updating active users list");
                            String newUserList = "";
                            for(String name : activeUsers){
                                 newUserList += (name + "\n");
                            }
                            System.out.println(newUserList);
                            names.setText(newUserList);
                        }

                        //Send receipt to server
                        if(msgReceived.getType() == MessageType.SENDMESSAGE) {
                            Message receipt = msgReceived;
                            receipt.setType(MessageType.RECEIPT);
                            oos.writeObject(receipt);
                            oos.reset();
                            oos.flush();
                            log.append("\n" + date + " " + msgReceived.getSender() + ": " + "@" +
                                    msgReceived.getRecipient() + " " + msgReceived.getMessage());

                        }

                        //Bad username
                        if (msgReceived.getType() == MessageType.BASIC) {
                            System.out.println("Choose a different username");
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

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Server");

        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: lightgray");

        Pane glass = new Pane();
        glass.setStyle("-fx-background-color: transparent");
        root.getChildren().add(glass);

        logDisplay = new Text(log.toString());
        logDisplay.setWrappingWidth(670);
        logDisplay.setX(15);
        logDisplay.setStroke(Color.WHITE);

        glass2.setStyle("-fx-background-color: transparent");
        glass2.getChildren().add(logDisplay);

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
        names.setText("\n" + username);
        names.setX(7);
        names.setY(header.getY() + 80);
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
        buttonSend.setTranslateX(-342);
        buttonSend.setTranslateY(-67);
        buttonSend.setStyle("-fx-control-inner-background: grey; -fx-background-color: grey;"
                + "-fx-text-fill: black;");
        buttonSend.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendFlag = true;
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

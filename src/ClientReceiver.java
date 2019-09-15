import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class ClientReceiver {

    //Saved client settings/info
    private static String username;
    private static boolean quit = false;

    //Saved lists
    private static ArrayList<String> activeUsers;
    private static ArrayList<Message> savedMessages = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        InetAddress ip = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);
        username = args[2];

        // establish the connection
        Socket s = new Socket(ip, serverPort);

        // establish output streams
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        //send a hello message to register username with client
        Message hello = new Message(MessageType.SETNAME, username);
        oos.writeObject(hello);
        oos.reset();
        oos.flush();

        // readMessage thread
        Thread readMessage = new Thread(() -> {
            Date date;
            Message msgReceived;

            while (!quit) {
                try {
                    // read the message sent to this client
                    msgReceived = (Message) ois.readObject();

                    // set time received stamp
                    msgReceived.setTimeReceived(System.currentTimeMillis());

                    //print message received
                    date = java.util.Calendar.getInstance().getTime();


                    //Send receipt to server
                    if (msgReceived.getType() == MessageType.SENDMESSAGE) {
                        msgReceived.setType(MessageType.RECEIPT);
                        oos.writeObject(msgReceived);
                        oos.reset();
                        oos.flush();
                        System.out.println("RECEIVED: " + date + " " + "From: @" + msgReceived.getSender() + ": " + " " +
                                msgReceived.getMessage());
                        msgReceived.setType(MessageType.QUIT);
                        oos.writeObject(msgReceived);
                        oos.reset();
                        oos.flush();
                        quit = true;
                    }

                    if (msgReceived.getType() == MessageType.ACTIVEUSERS) {
                        activeUsers = (ArrayList<String>) msgReceived.getUsers();
                    }

                    if (msgReceived.getType() == MessageType.BASIC) {
                        System.out.println("Choose a different username");
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        readMessage.start();
        if (quit) {System.exit(0);}
    }


    public ClientReceiver(String ID) {
        username = ID;
    }
}

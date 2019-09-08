// Java implementation for multithreaded chat client

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client
{
    private static int ServerPort = 1025;
    private static String username;
	private static InetAddress ip;
    private static long timeSent;
    private static Timestamp timeReceived;
    private static String sendingTo;
    private static ArrayList<String> activeUsers = new ArrayList<>();
    private static ArrayList<String> savedMessages = new ArrayList<>();

    public static void main(String args[]) throws UnknownHostException, IOException
    {
        Scanner scn = new Scanner(System.in);
            /**commenting out until I figure out a way separate this from output data stream**/

            ip = InetAddress.getByName(args[0]);
            ServerPort = Integer.parseInt(args[1]);
            username = args[2];

//         getting localhost ip
//        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        dos.writeUTF("setName#" + username);

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {

                    // read the message to deliver.
                    String msg = scn.nextLine();

                    //break message apart
                    StringTokenizer st = new StringTokenizer(msg, "#");

                    for (String user: activeUsers)
                    {
                        if(st.nextToken().equalsIgnoreCase(user)){
                            try {
                                // write on the output stream
                                dos.writeUTF(username + sendingTo + msg + timeSent);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            System.out.println("not allowed");
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
                        String msg = dis.readUTF();
                        StringTokenizer st = new StringTokenizer(msg, "#");
                        String MsgToSend = st.nextToken();
                        String recipient = st.nextToken();


                        //print message received
                        System.out.println(msg);

                    } catch (IOException e) {

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

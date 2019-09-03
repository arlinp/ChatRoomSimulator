// Java implementation for multithreaded chat client

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{
    final static int ServerPort = 1234;
    private String username;


    public static void main(String args[]) throws UnknownHostException, IOException
    {
        Scanner scn = new Scanner(System.in);

        System.out.println("Welcome to the chat server.  Please enter your username.");
        Client client = new Client(scn.next());

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {

                    // read the message to deliver.
                    String msg = scn.nextLine();

                    try {
                        // write on the output stream
                        dos.writeUTF(msg);
                    } catch (IOException e) {
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

                while (true) {
                    try {
                        // read the message sent to this client
                        String msg = dis.readUTF();
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

    public Client (String ID){
        this.username = ID;
    }

    private String getUsername(){
        return this.username;
    }

    private void setUsername(String newUsername){
        this.username = newUsername;
    }
}

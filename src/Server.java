// Java implementation of Server side 
// It contains two classes : Server and ClientHandler 

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.security.Timestamp;
import java.util.*;
import java.net.*;

// Server class 
public class Server {

	// Vector to store active clients
	static Vector<ClientHandler> ar = new Vector<>();

	// counter for clients
	static int i = 0;
	// port number
	static int portNumber;


	public static void main(String[] args) throws IOException {

	    // You can't open a port below 1024, if you don't have root privileges
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
				ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
				ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());

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

	/*public boolean checkName(String name) {

    }*/
}

// ClientHandler class
class ClientHandler implements Runnable
{
	Scanner scn = new Scanner(System.in);
	private String name;
	final ObjectInputStream dis;
	final ObjectOutputStream dos;
	Socket s;
	boolean isloggedin;

	// constructor
	public ClientHandler(Socket s, String name,
							ObjectInputStream dis, ObjectOutputStream dos) {
		this.dis = dis;
		this.dos = dos;
		this.name = name;
		this.s = s;
		this.isloggedin=true;
	}

	public String getName() {
	    return name;
    }

    public void setName(String name) {
	    this.name = name;
    }

	@Override
	public void run() {

		try {
			Message received;
			while (true) {
				// receive the string
				received = (Message) dis.readObject();

				System.out.println(received);

				switch (received.getType()) {
                    case LOGOUT:
                        this.isloggedin = false;
                        //this.s.close();
                        break;
                    case SETNAME:
                        // If we have time we need to implement that the check is completely unique
//                        for (ClientHandler mc : Server.ar) {
//                            if (mc.getName().equals(received.getSender())) {
//                                dos.writeObject(new Message(MessageType.BASIC, null, null, null, null));
//                                this.s.close();
//                                break;
//                            }
//                        }
                        if (!received.getRecipient().equals(this.name)) {
                            setName(received.getSender());
                        }
                        break;
                    case RECONNECT:
                        this.isloggedin = true;
                        break;
                    case SENDMESSAGE:
                        for (ClientHandler mc : Server.ar) {
                            // if the recipient is found, write on its
                            // output stream
                            if (mc.getName().equals(received.getRecipient()) && mc.isloggedin == true) {
                                //mc.dos.writeUTF(getName() + "#" + received); Need to update client to handle message object
                                break;
                            }
                        }
                        break;
                }

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try
		{
			// closing resources
			this.dis.close();
			this.dos.close();

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}


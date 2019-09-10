// Java implementation of Server side 
// It contains two classes : Server and ClientHandler 

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
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());

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
	final DataInputStream dis;
	final DataOutputStream dos;
	Socket s;
	boolean isloggedin;

	// constructor
	public ClientHandler(Socket s, String name,
							DataInputStream dis, DataOutputStream dos) {
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

		String received;
		while (true)
		{
			try
			{

				// receive the string
				received = dis.readUTF();

				System.out.println(received);

				if(received.equals("logout")) {
					this.isloggedin=false;
                    //this.s.close();
                    break;
				}

				if(received.equals("connect")) {
				    this.isloggedin = true;
				    break;
                }

                if(received.contains("#")) {
                    // break the string into message and recipient part
                    StringTokenizer st = new StringTokenizer(received, "#");
                    String sender = st.nextToken();
                    String recipient = st.nextToken();
                    String MsgToSend = st.nextToken();
                    Long timestamp = Long.parseLong(st.nextToken());

                    if (!sender.equals(name)) {
//                        for (ClientHandler mc : Server.ar) {
//                            if (mc.getName().equals(name)) {
//                                this.dos.writeUTF("basic");
//                                this.s.close();
//                                break;
//                            } else {
//                                continue;
//                            }
//                        }
                        setName(sender);
                    }

                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : Server.ar) {
                        // if the recipient is found, write on its
                        // output stream
                        if (mc.getName().equals(recipient) && mc.isloggedin == true) {
                            mc.dos.writeUTF(getName() + "#" + MsgToSend);
                            break;
                        }
//                        else if (mc.getName().equals()) {
//
//                        }
                    }
                }
			} catch (IOException e) {

				e.printStackTrace();
			}

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


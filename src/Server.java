// Java implementation of Server side 
// It contains two classes : Server and ClientHandler 

import java.io.*;
import java.util.*;
import java.net.*;

// Server class 
public class Server {

	// Vector to store active clients 
	static Vector<ClientHandler> ar = new Vector<>();

	// counter for clients 
	static int i = 0;
	//
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

				System.out.println("Creating a new handler for this client...");

				// Create a new handler object for handling this request.
				ClientHandler mtch = new ClientHandler(s, "client " + i, dis, dos);

				// Create a new Thread with this object.
				Thread t = new Thread(mtch);

				System.out.println("Adding this client to active client list");

				// add this client to active clients list
				ar.add(mtch);

				// start the thread.
				t.start();

				// increment i for new client.
				// i is used for naming only, and can be replaced
				// by any naming scheme
				i++;

			}
		} catch (IOException e) { // Letting client know that the connection was Refused
            System.out.println("Connection Refused");
		}
	} 
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
				
				if(received.equals("logout")){ 
					this.isloggedin=false; 
					this.s.close(); 
					break; 
				} 
				
				// break the string into message and recipient part 
				StringTokenizer st = new StringTokenizer(received, "#"); 
				String MsgToSend = st.nextToken(); 
				String recipient = st.nextToken(); 

				// search for the recipient in the connected devices list. 
				// ar is the vector storing client of active users 
				for (ClientHandler mc : Server.ar) 
				{ 
					// if the recipient is found, write on its 
					// output stream 
					if (mc.name.equals(recipient) && mc.isloggedin==true) 
					{ 
						mc.dos.writeUTF(this.name+" : "+MsgToSend); 
						break; 
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

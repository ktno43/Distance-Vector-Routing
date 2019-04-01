/*-
 ****************************************
 * Kyle Nguyen
 * Kodi Winterer
 * 
 * COMP 429
 * Spring 2019
 * Senhua Yu
 * Tuesday 7:00 PM - 9:45 PM
 * 
 * Programming Assignment 1:
 * Develop a simple chat application for 
 * message exchange among remote peers.
 * 
 * ServerThread.java
 * Version 2.0
 ****************************************/
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServerThread extends Thread {
	private int listeningPort; // Listening port of the current system
	protected Vector<ClientThreadOut> clientVectorOut; // Vector of all the connected clients's output
	protected Vector<ClientThreadIn> clientVectorIn; // Vector for all of the connected client's input

	public ServerThread(int listenPort) {
		this.listeningPort = listenPort; // Set the listening port
		this.clientVectorOut = new Vector<>(); // Create new vectors for the output & input
		this.clientVectorIn = new Vector<>();
	}

	@Override
	public void run() {
		try (ServerSocket ss = new ServerSocket(this.listeningPort)) { // Try to make a server socket for the current port
			// start the server...

			boolean listenFlag = true;
			
			System.out.println("Listening for connections on port: " + this.listeningPort + "\n");
			// and listen for connections
			while (listenFlag) { // While listening
				Socket sock = ss.accept(); // Accept incoming connections

				// got one!
				// Process the accepted connection
				System.out.println("\nNew Connection: " + sock.getInetAddress().toString());
				System.out.println("Port: " + sock.getPort() + "\n");

				// Create an input handle for the current connected client
				ClientThreadIn connectedClient = new ClientThreadIn(sock, this);

				clientVectorIn.add(connectedClient); // Add it to the vector list

				BufferedReader input = connectedClient.input; // Buffered reader for the connected client
				String remoteMessage = input.readLine(); // Read in the remote message(client's listening port)

				connectedClient.serverPort = Integer.parseInt(remoteMessage); // Set the connected client's listening port

				boolean breakout = false; // Flag for breakout

				for (ClientThreadOut cto : clientVectorOut) { // For all of the connected clients
					if (cto.getIp().equals(connectedClient.getIp()) && cto.getPort() == connectedClient.serverPort) { // If their IP and ports are the same
						connectedClient.start(); // Go ahead and star the client's input thread
						breakout = true; // toggle flag
					}
				}

				if (!breakout) { // If not breakout, keep listening for connections
					Socket s = new Socket(sock.getInetAddress().getHostAddress(), Integer.parseInt(remoteMessage)); // If it's a new IP, then create a new socket
					ClientThreadOut cto = new ClientThreadOut(s); // Create a new client output for the given socket
					clientVectorOut.add(cto); // Add the current client to the client's output vector
					cto.send(Integer.toString(listeningPort)); // Send the listening port of the current system to the connected client's output thread

					connectedClient.start(); // Start the client's input thread
				}

				checkConnection(); // Check the connections within the vector and continue to listen for connections
			}

		} catch (IOException ioe) {
			System.out.println("\nThere is already a Server Socket running on that port!");
			System.exit(0);
		}
	}

	/******************************************
	 * Add a connection
	 * 
	 * @param ip-
	 *            The IP of the client
	 * @param port-
	 *            The listening port of the client
	 ******************************************/
	protected void addConn(String ip, int port) {
		ClientThreadOut client = new ClientThreadOut(ip, port); // Try to create an output thread for the client

		if (client.isSuccess) { // If it was successful
			client.send(Integer.toString(listeningPort)); // Send the listening port
			this.clientVectorOut.add(client); // Add it to the vector
		}
	}

	/******************************************
	 * Check to see if the connection already
	 * exists
	 * 
	 * @param ip-
	 *            IP of the client
	 * @param port-
	 *            The listening port of the client
	 * @return - Whether they are connected/not
	 *******************************************/
	protected boolean isConnected(String ip, int port) {
		for (ClientThreadOut ct : this.clientVectorOut) {
			if (ct.getIp().equals(ip) && ct.listeningPort == port) {
				return true;
			}
		}

		return false;
	}

	/******************************************
	 * Print all the connections
	 ******************************************/
	protected void printClientList() {
		System.out.printf("%nID:\tIP Address\t\tPort No.%n");
		for (int i = 0; i < this.clientVectorOut.size(); i++) { // Print all the output connections in the vector

			System.out.printf("%d:\t%s\t\t%d%n", i + 1, this.clientVectorOut.get(i).getIp(),
					this.clientVectorOut.get(i).getPort()); // Display their IP and their listening port
		}

		System.out.println();
	}

	/******************************************
	 * Send a message to a client given the ID
	 * and the message
	 * 
	 * @param id-
	 *            The ID of the client to send the message to
	 * @param m-
	 *            The message to send to the client
	 ******************************************/
	protected void sendUserMessage(int id, String m) {
		if (id <= 0 || id > clientVectorOut.size())
			System.out.println("\nID is incorrect");

		else { // Valid ID
			ClientThreadOut cto = clientVectorOut.get(id - 1);

			cto.send(m);
		}
	}

	/******************************************
	 * Sends a message to every client
	 * (used for when a client exits)
	 ******************************************/
	protected void sendAllExitMsg() {
		for (int i = 0; i < clientVectorOut.size(); i++) {
			ClientThreadOut cto = clientVectorOut.get(i);
			cto.send("{EXIT}");
		}
	}

	/******************************************
	 * Terminate a client based off their ID
	 * 
	 * @param id-
	 *            ID of the client
	 * @throws IOException
	 ******************************************/
	protected void terminate(int id) throws IOException {

		if (id <= 0 || id > clientVectorOut.size())
			System.out.println("\nID is incorrect");

		else { // Valid ID
			ClientThreadOut cto = clientVectorOut.get(id - 1); // Get the client's output thread
			ClientThreadIn cti = clientVectorIn.get(id - 1); // Get the client's input thread

			cto.send("{TERMINATE}"); // Send a message to the client that they are terminated

			if (!clientVectorIn.isEmpty()) // As long as the input vector is not empty
				this.clientVectorIn.remove(id - 1); // Remove the client from the input vector

			if (!clientVectorOut.isEmpty()) // As long as the output vector is not empty
				this.clientVectorOut.remove(id - 1); // Remove the client from the output vector

			cto.clientSocket.close(); // Close the output socket
			cto.out.close(); // Close the output stream(PrintStream)

			cti.clientSocket.close(); // Close the input socket
			cti.input.close(); // Close the input stream(BufferedReader)
		}
	}

	/******************************************
	 * Checks to see if the connections are
	 * still connected
	 * If not, remove them from the list
	 ******************************************/
	protected void checkConnection() {
		for (int i = 0; i < clientVectorIn.size(); i++) { // Check all the client's input vector
			try {
				if (clientVectorIn.get(i).clientSocket.isClosed() || clientVectorOut.get(i).clientSocket.isClosed()
						|| clientVectorIn.get(i).clientSocket.getInputStream().read() == -1) { // If any of them are closed or if the read input is -1

					if (!clientVectorIn.isEmpty()) // If not empty remove it from the input vector
						this.clientVectorIn.remove(i);

					if (!clientVectorOut.isEmpty()) // If not empty remove it from the output vector
						this.clientVectorOut.remove(i);
				}

			} catch (IOException e) {
				if (!clientVectorIn.isEmpty()) // If not empty remove it from the input vector
					this.clientVectorIn.remove(i);

				if (!clientVectorOut.isEmpty()) // If not empty remove it from the output vector
					this.clientVectorOut.remove(i);
			}
		}
	}
}

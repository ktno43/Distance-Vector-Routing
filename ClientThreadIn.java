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
 * ClientThreadIn.java
 * Version 7.0
 ****************************************/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThreadIn extends Thread {
	protected Socket clientSocket; // Socket for the input
	protected BufferedReader input; // Buffered reader for the input
	protected int serverPort; // The listening port
	protected ServerThread st; // The associated server thread
	protected boolean firstMsg;
	protected boolean justConnected;

	ClientThreadIn(Socket sock, ServerThread serverThread) throws IOException { // Create a socket for the input and associate it to the serverthread
		this.clientSocket = sock;
		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Create new buffered reader
		this.st = serverThread;
		this.firstMsg = false;
		this.justConnected = true;
	}

	@Override
	public void run() { // Input thread for the client
		boolean flag = true;
		while (flag) {
			try {
				String remoteMessage = input.readLine(); // Read in remote message from the socket

				if (remoteMessage != null && !firstMsg) {
					StringBuilder sb = new StringBuilder(remoteMessage);
					if (remoteMessage.charAt(0) != '{')
						remoteMessage = sb.insert(0, '{').toString();

					firstMsg = true;
				}

				if (remoteMessage != null && remoteMessage.equals("{TERMINATE}")) { // Client was terminated
					System.out.println("\nSomeone has terminated you from their chat. . .\n");
					int index = this.st.clientVectorIn.indexOf(this);
					if (!this.st.clientVectorIn.isEmpty()) // As long as the input vector is not empty
						this.st.clientVectorIn.remove(index); // Remove the client from the input vector

					if (!this.st.clientVectorOut.isEmpty()) // As long as the output vector is not empty
						this.st.clientVectorOut.remove(index); // Remove the client from the output vector

					flag = false;
				}

				else if (remoteMessage != null && remoteMessage.equals("{EXIT}")) { // Someone left the chat
					System.out.println("\nSomeone has left the chat room. . .\n");
					int index = this.st.clientVectorIn.indexOf(this);
					if (!this.st.clientVectorIn.isEmpty()) // As long as the input vector is not empty
						this.st.clientVectorIn.remove(index); // Remove the client from the input vector

					if (!this.st.clientVectorOut.isEmpty()) // As long as the output vector is not empty
						this.st.clientVectorOut.remove(index); // Remove the client from the output vector
				}

				else if (remoteMessage != null && !remoteMessage.isEmpty()) { // Print the message if it isn't null or empty
					System.out.println("\nMessage received from " + this.clientSocket.getInetAddress().toString());
					System.out.println("Sender's Port: " + serverPort);
					System.out.println("Message: " + remoteMessage.substring(1));// Print to the local console
					System.out.println();

				}

			} catch (IOException e) {
				// Ignore error message, termination/exit already handled exceptions
			}
		}
	}

	/******************************************
	 * Return the IP of the socket
	 * 
	 * @return- Get the IP of the current client
	 ******************************************/
	protected String getIp() { // Return the IP of the socket
		return this.clientSocket.getInetAddress().getHostAddress();
	}
}

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
 * ClientThreadOut.java
 * Version 1.0
 ****************************************/

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ClientThreadOut {
	protected Socket clientSocket;
	protected int listeningPort;
	protected String clientMessage;
	protected String ip;
	protected PrintStream out;
	protected boolean isSuccess = true;

	ClientThreadOut(String ip, int listenPort) {
		try { // Try to create a socket for the given IP and listening port
			clientSocket = new Socket(ip, listenPort);

			this.listeningPort = listenPort;
			this.ip = ip;

			clientMessage = "";
			out = new PrintStream(clientSocket.getOutputStream()); // Create output stream for the client

		} catch (IOException e) { // Error when trying to create a socket
			System.out.println("Connection failed. . .\n");
			isSuccess = false;
		}
	}

	ClientThreadOut(Socket sock) throws IOException { // Assign a socket to the client's output
		this.clientSocket = sock;
		ip = sock.getInetAddress().getHostAddress();
		clientMessage = "";

		out = new PrintStream(clientSocket.getOutputStream());
	}

	/******************************************
	 * The message to the client
	 * 
	 * @param m-
	 *            The message to send to the client
	 ******************************************/
	public void send(String m) {
		clientMessage = m;
		out.println(clientMessage);
		out.flush();
	}

	/******************************************
	 * @return- The port the client is listening on
	 ******************************************/
	public int getPort() {
		return this.clientSocket.getPort();
	}

	/******************************************
	 * @return- The IP of the client
	 ******************************************/
	public String getIp() {
		return this.clientSocket.getInetAddress().getHostAddress();
	}
}

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
 * Chat.java
 * Version 6.0
 ****************************************/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Chat {
	static ServerThread st; // Server thread to start the chat

	public static synchronized void main(String[] args) throws IOException {
		int portListen = Integer.parseInt(args[0]); // Listening port read from command arguments

		st = new ServerThread(portListen); // Start the server thread to accept connections
		st.start();

		boolean flag = true; // Flag for user input
		String userInput; // Input string
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)); // Buffered reader to read from console

		String welcomeMessage = "Comp 429 Project #1";
		System.out.println(welcomeMessage);

		while (flag) { // while not done
			userInput = stdIn.readLine(); // Read in the input

			List<String> inputList = Arrays.asList(userInput.split(" ", 3)); // Split the read command by spaces
			// <command> <id> <message>

			switch (inputList.get(0)) { // Case for the entered command
			case "help": // Display help descriptions
				help();
				break;

			case "myip": // Display IP of current system
				myIp();
				break;

			case "myport": // Display the current listening port
				System.out.println("\nListening port: " + args[0] + "\n");
				break;

			case "connect": // The 2nd list element will be the client IP, and the 3rd list element will be their listening port
				connect(inputList.get(1), Integer.parseInt(inputList.get(2)));
				break;

			case "list": // Print the list of connections
				getList();
				break;

			case "terminate": // Get the 2nd list element and terminate that ID if it is within bounds
				if (inputList.size() > 1 && isNumeric(inputList.get(1))) {
					st.terminate(Integer.parseInt(inputList.get(1)));

					System.out.println();
				}

				else
					System.out.println("\nID is incorrect\n");

				break;

			case "send": // Get the 3rd list element (message) and send it to the 2nd list element (sender ID)
				if (inputList.size() > 2 && isNumeric(inputList.get(1))) {
					StringBuilder sb = new StringBuilder(inputList.get(2));
					st.sendUserMessage(Integer.parseInt(inputList.get(1)), sb.insert(0, "{").toString());
					System.out.println();
				}

				else
					System.out.println("\nID is incorrect\n");

				break;

			case "exit": // Exit the program
				flag = false;
				st.sendAllExitMsg(); // Send exit message to everyone

				break;

			default: // Not a valid command
				System.out.println("\n" + inputList.get(0) + " is not a command\n");
				break;
			}
		}

		System.exit(0);
	}

	/******************************************
	 * Check to see if input it is a number
	 * 
	 * @param str-
	 *            the string passed in
	 * @return is number or not
	 ******************************************/
	private static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/******************************************
	 * Display help menu
	 ******************************************/
	private static void help() {
		System.out.println("\nmyip :- \n\tDisplay the IP address of the current process\n");
		System.out.println("myport :- \n\tDisplay the IP address of the current process\n");
		System.out.println("connect <IP> <Listening Port>:-"
				+ " \n\tEstablish a new TCP connection to the specified destination at the specified port\n");
		System.out.println("list :- \n\tList all connections this process is apart of\n");
		System.out.println("terminate :- "
				+ "\n\tTerminate a connection listed under the specified ID when used in conjunction with list\n");
		System.out.println("send :- " + "\n\tSend a message to another client based on ID\n");
		System.out.println("exit :- \n\tClose all connections and terminate the current process\n");
	}

	/******************************************
	 * Display the IP of the current system
	 ******************************************/
	private static void myIp() {
		String systemIP = ""; // String for the IP

		try {
			URL urlName = new URL("http://bot.whatismyipaddress.com"); // Website to get the IP

			BufferedReader br = new BufferedReader(new InputStreamReader(urlName.openStream())); // Read from the website

			// Reads system IP Address
			systemIP = br.readLine().trim(); // Read into the string

		} catch (Exception e) {
			systemIP = "Cannot Execute Properly";
		}

		System.out.println("\nPublic IP Address: " + systemIP + "\n");
	}

	/******************************************
	 * Connect to a client given their valid
	 * IP & listening port
	 * 
	 * @param clientIP-
	 *            the client's IP
	 * @param clientListenPort-
	 *            the client's listening port
	 * @throws IOException
	 ******************************************/
	private static void connect(String clientIP, int clientListenPort) {
		if (!st.isConnected(clientIP, clientListenPort)) {
			st.addConn(clientIP, clientListenPort);
		}
	}

	/******************************************
	 * Print the connection list
	 ******************************************/
	private static void getList() {
		st.printClientList();
	}
}

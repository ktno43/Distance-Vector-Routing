import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class dvr {
	private static String myIP = "";
	private static int myID = Integer.MIN_VALUE;
	private static Node myNode = null;

	private static List<Socket> openChannelsList = new ArrayList<>();
	private static Selector read;
	private static Selector write;

	private static int routingInterval = 0;
	private static int numPacketsReceived = 0;
	private static List<Node> nodesList = new ArrayList<>();
	private static List<String> rtMessage = new ArrayList<>();
	private static Map<Node, Integer> rtMap = new HashMap<>();
	private static Set<Node> neighborsSet = new HashSet<>();
	private static Map<Node, Node> nextHopMap = new HashMap<>();

	public static void main(String[] args) throws Throwable {
		myIP = getMyLanIP();
		System.out.println(myIP);

		boolean isRunning = true;
		String userInput; // Input string
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)); // Buffered reader to read from console
		Timer timer = new Timer();

		while (isRunning) {
			userInput = stdIn.readLine(); // Read in the input

			List<String> inputList = Arrays.asList(userInput.split(" ", 4)); // Split the read command by spaces

			switch (inputList.get(0)) {

			case "server":
				if (inputList.size() < 3 || inputList.size() > 3)
					System.out.println("Incorrect use of command \"server\"");

				else {
					if (isNumeric(inputList.get(2)) && Integer.parseInt(inputList.get(2)) > 0) {
						String fileName = inputList.get(1);
						routingInterval = Integer.parseInt(inputList.get(2));
						readTopFile(fileName);
					}

					else {
						System.out.println("Routing-update interval is not a number");

					}
				}

				break;

			case "update":
				if (inputList.size() < 4 || inputList.size() > 4)
					System.out.println("Incorrect use of command \"update\"");

				else {
					if (isNumeric(inputList.get(1)) && isNumeric(inputList.get(2))
							&& (inputList.get(3).equals("inf") || isNumeric(inputList.get(3)))) {
						updateCost(Integer.parseInt(inputList.get(1)), Integer.parseInt(inputList.get(2)),
								inputList.get(3));
					}

					else {
						System.out.println("Server ID's specified or link cost may not be a number");
					}

				}

				break;

			case "step":
				break;

			case "packets":
				break;

			case "display":
				displayRt();
				break;

			case "disable":
				if (inputList.size() < 2 || inputList.size() > 2)
					System.out.println("Incorrect use of command \"disable\"");

				else {
					if (isNumeric(inputList.get(1))) {

					}

					else
						System.out.println("Server ID is incorrect");
				}

				break;

			case "crash":
				break;

			default:
				System.out.println("\n" + inputList.get(0) + " is not a command\n\n");
				break;
			}
		}
	}

	private static void displayRt() {
		System.out.println("\nSource Server ID\tNext Hop Server\t\tCost");
		for (Node node : nodesList) {
			int cost = rtMap.get(node);
			String costStr = "" + cost;

			if (cost == Integer.MAX_VALUE)
				costStr = "inf";

			if (cost != 0)
				System.out.print(node.getID() + "\t\t\t" + nextHopMap.get(node).getID() + "\t\t\t" + costStr + "\n");
		}
		System.out.println();
	}

	private static void updateCost(int srcID, int destID, String newCostStr) {
		if (srcID == myID || destID != myID) {
			Node destNode = getNodeByID(destID);

			if (newCostStr.equals("inf"))
				rtMap.put(destNode, Integer.MAX_VALUE);
			else
				rtMap.put(destNode, Integer.parseInt(newCostStr));
		}

		else {
			System.out.println("Wrong source?");
		}
	}

	private static void readTopFile(String fileName) throws Throwable {
		URL path = dvr.class.getResource(fileName);
		String filePath = path.getFile().substring(1);
		List<String> inputList = new ArrayList<>();

		try (Stream<String> inputStream = Files.lines(Paths.get(filePath), Charset.defaultCharset())) { // Attempt to read the text file given the path
			inputList = inputStream.collect((Collectors.toList())); // Convert the stream into a list

			createNodes(inputList);
			createNeighbors(inputList);

			System.out.println("\nReading topology file finished\n");

		} catch (IOException e) { // Error opening the file
			System.out.println("Error while trying to open the file");
		}

	}

	private static void createNodes(List<String> inputList) throws Throwable {
		int numServers = Integer.parseInt(inputList.get(0));

		for (int i = 0; i < numServers; i++) {
			String[] inputSplitArr = inputList.get(i + 2).split("\\s+");
			Node node = new Node(Integer.parseInt(inputSplitArr[0]), inputSplitArr[1],
					Integer.parseInt(inputSplitArr[2]));

			nodesList.add(node);

			int cost = Integer.MAX_VALUE;

			if (inputSplitArr[1].equals(myIP)) {
				myID = Integer.parseInt(inputSplitArr[0]);
				myNode = node;
				new Server(new InetSocketAddress(myIP, Integer.parseInt(inputSplitArr[2])), routingInterval);
				cost = 0;
				nextHopMap.put(node, myNode);
			}

			else
				nextHopMap.put(node, null);

			rtMap.put(node, cost);
			createEdge(inputSplitArr[1], Integer.parseInt(inputSplitArr[2]));
		}
	}

	private static void createEdge(String ip, int port) {
		try {
			if (!ip.equals(myIP)) {
				System.out.println("\nConnecting to ip:- " + ip);

				Socket clientSocket = new Socket(ip, port);
				OutputStream out = clientSocket.getOutputStream();
				out.write(55);
				out.write(54);
				out.write(34);
				out.write(23);
				out.write(2);
				out.write(7);
				out.flush();
				openChannelsList.add(clientSocket);
				System.out.println(".......");
				System.out.println("Connected to " + ip);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createNeighbors(List<String> inputList) {
		int numServers = Integer.parseInt(inputList.get(0));
		int numNeighbors = Integer.parseInt(inputList.get(1));
		int neighborsOffset = 2 + numServers;

		for (int i = 0; i < numNeighbors; i++) {
			String[] inputSplitArr = inputList.get(i + neighborsOffset).split("\\s+");
			int fromID = Integer.parseInt(inputSplitArr[0]);
			int toID = Integer.parseInt(inputSplitArr[1]);
			int cost = Integer.parseInt(inputSplitArr[2]);

			Node toNode = getNodeByID(toID);
			rtMap.put(toNode, cost);
			neighborsSet.add(toNode);
			nextHopMap.put(toNode, getNodeByID(fromID));
		}
	}

	private static Node getNodeByID(int ID) {
		for (Node node : nodesList) {
			if (node.getID() == ID)
				return node;
		}

		return null;
	}

	private static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
			return true;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static String getMyLanIP() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();

					final String IP = addr.getHostAddress();
					if (Inet4Address.class == addr.getClass())
						return IP;
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	private static void step() {

	}

}

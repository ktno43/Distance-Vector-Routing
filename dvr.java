import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class dvr {
	static ServerThread server;

	public static void main(String[] args) throws Throwable {
		System.out.println("COMP 429 Programming Assignment #2");
		boolean isRunning = true;
		String userInput; // Input string
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)); // Buffered reader to read from console

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
						int routingInterval = Integer.parseInt(inputList.get(2));
						readTopFile(fileName);

						Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
							server.step();

						}, routingInterval, routingInterval, TimeUnit.SECONDS);

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
				step();
				break;

			case "packets":
				System.out.println(
						"Number of packets received since the last invocation: " + server.getNumPackets() + "\n\n");
				server.setNumPackets(0);
				System.out.println("packets SUCCESS");
				break;

			case "display":
				displayRt();
				break;

			case "disable":
				if (inputList.size() < 2 || inputList.size() > 2)
					System.out.println("Incorrect use of command \"disable\"");

				else {
					if (isNumeric(inputList.get(1))) {
						disable(Integer.parseInt(inputList.get(1)));
					}

					else
						System.out.println("Server ID is incorrect");
				}

				break;

			case "crash":
				server.setCrash(true);
				isRunning = false;
				break;

			default:
				System.out.println("\n" + inputList.get(0) + " is not a command\n\n");
				break;
			}
		}

		System.exit(0);
	}

	private static void readTopFile(String fileName) throws Throwable {
		URL path = dvr.class.getResource(fileName);
		String filePath = path.getFile();
		List<String> inputList = new ArrayList<>();

		try (Stream<String> inputStream = Files.lines(Paths.get(filePath.substring(1)), Charset.defaultCharset())) { // Attempt to read the text file given the path
			inputList = inputStream.collect((Collectors.toList())); // Convert the stream into a list
			createNodes(inputList);

			System.out.println("\nReading topology file finished\n");

		} catch (IOException e) { // Error opening the file
			try (Stream<String> inputStream = Files.lines(Paths.get(filePath), Charset.defaultCharset())) { // Attempt to read the text file given the path
				inputList = inputStream.collect((Collectors.toList())); // Convert the stream into a list
				createNodes(inputList);

				System.out.println("\nReading topology file finished\n");

			} catch (IOException e2) {
				System.out.println("Error while trying to open the file");
			}
		}
	}

	private static void displayRt() {
		try {
			server.displayRt();
			System.out.println("display SUCCESS\n");
		} catch (NullPointerException e) {
			System.out.println(
					"display ERROR: Failure to display server table. Table is null. Check config file for mistakes.\n");
		}
	}

	private static void step() {
		if (server.step())
			System.out.println("step SUCCESS\n");

		else {
			System.out.println("step ERROR: One of your neighbors in the topology is not receiving messages\n");
		}
	}

	private static void disable(int id) {
		if (server.disable(id))
			System.out.println("disable SUCCESS\n");
	}

	private static void updateCost(int id1, int id2, String cost) {
		if (server.updateCost2(id1, id2, cost))
			System.out.println("update SUCCESS\n");
	}

	private static void createNodes(List<String> inputList) throws Throwable {
		int numServers = Integer.parseInt(inputList.get(0));
		String myIp = getMyLanIP();
		String myPubIp = myIp();
		boolean gotIp = true;
		for (int i = 0; i < numServers; i++) {
			String[] inputSplitArr = inputList.get(i + 2).split("\\s+");

			if ((inputSplitArr[1].equals(myIp) || inputSplitArr[1].equals(myPubIp)) && gotIp) {
				gotIp = false;
				int myID = Integer.parseInt(inputSplitArr[0]);
				int myPort = Integer.parseInt(inputSplitArr[2]);

				ServerThread st = new ServerThread(myPort, myID, inputSplitArr[1]);
				server = st;
				st.start();

				st.createNodes(inputList);
				st.createNeighbors(inputList);
			}
		}
	}

	private static String myIp() {
		String systemIP = ""; // String for the IP

		try {
			URL urlName = new URL("http://bot.whatismyipaddress.com"); // Website to get the IP

			BufferedReader br = new BufferedReader(new InputStreamReader(urlName.openStream())); // Read from the website

			// Reads system IP Address
			systemIP = br.readLine().trim(); // Read into the string

		} catch (Exception e) {
			systemIP = "Cannot Execute Properly";
		}

		return systemIP;
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
}
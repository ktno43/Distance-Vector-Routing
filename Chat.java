import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Chat {
	static ServerThread st;

	public static synchronized void main(String[] args) throws IOException {
		int portListen = Integer.parseInt(args[0]);

		st = new ServerThread(portListen);
		st.start();

		boolean flag = true;
		String userInput;
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		String welcomeMessage = "Comp 429 Project #1";
		System.out.println(welcomeMessage);

		while (flag) {
			userInput = stdIn.readLine();
			List<String> inputList = Arrays.asList(userInput.split(" ", 3));

			switch (inputList.get(0)) {
			case "help":
				help();
				break;

			case "myip":
				myIp();
				break;

			case "myport":
				System.out.println("\nListening port: " + args[0] + "\n");
				break;

			case "connect":
				connect(inputList.get(1), Integer.parseInt(inputList.get(2)));
				break;

			case "list":
				getList();
				break;

			case "terminate":
				if (inputList.size() > 1 && isNumeric(inputList.get(1))) {
					st.terminate(Integer.parseInt(inputList.get(1)));

					System.out.println();
				}

				else
					System.out.println("\nID is incorrect\n");

				break;

			case "send":
				if (inputList.size() > 2 && isNumeric(inputList.get(1))) {
					send(Integer.parseInt(inputList.get(1)), inputList.get(2));
					System.out.println();
				}

				else
					System.out.println("\nID is incorrect\n");

				break;

			case "exit":
				flag = false;
				st.sendAllExitMsg();

				break;

			default:
				System.out.println("\n" + inputList.get(0) + " is not a command\n");
				break;
			}
		}
		System.exit(0);
	}

	private static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

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

	private static void myIp() {
		String systemipaddress = "";
		try {
			URL url_name = new URL("http://bot.whatismyipaddress.com");

			BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

			// reads system IPAddress
			systemipaddress = sc.readLine().trim();
		} catch (Exception e) {
			systemipaddress = "Cannot Execute Properly";
		}

		System.out.println("\nPublic IP Address: " + systemipaddress + "\n");
	}

	private static void connect(String clientIP, int clientListenPort) throws IOException {
		if (!st.isConnected(clientIP, clientListenPort)) {
			st.addConn(clientIP, clientListenPort);
		}
	}

	private static void send(int id, String m) {
		StringBuilder sb = new StringBuilder(m);
		st.sendUserMessage(id, sb.insert(0, "{").toString());
	}

	private static void getList() {
		st.printClientList();
	}

}

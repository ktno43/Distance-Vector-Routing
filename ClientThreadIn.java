import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThreadIn extends Thread {

	int toPort;
	Socket clientSocket;
	int listeningPort;
	BufferedReader input;
	public int serverPort;
	ServerThread st;
	boolean firstMsg;
	boolean removedFromVector;
	boolean terminated;

	ClientThreadIn(Socket sock, ServerThread serverThread) throws IOException {
		this.clientSocket = sock;
		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		this.st = serverThread;
		this.firstMsg = false;
		this.removedFromVector = false;
		this.terminated = false;
	}

	@Override
	public void run() {
		while (this.clientSocket == null) {
			System.out.println("Socket Failed.. retry in 10 sec");
			try {// ..retry in 10 secs
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				ex.printStackTrace(System.err);
				Thread.currentThread().interrupt();
			}
		}

		// sock != null -> connection established
		try {
			while (true) {

				String remoteMessage = input.readLine();

				if (remoteMessage == null && !this.st.isConnected()) {
					terminated = true;
					break;
				}

				if (remoteMessage != null && !firstMsg) {
					StringBuilder sb = new StringBuilder(remoteMessage);
					if (remoteMessage != null && remoteMessage.charAt(0) != '{')
						remoteMessage = sb.insert(0, '{').toString();
					firstMsg = true;
				}

				if (remoteMessage != null && !remoteMessage.isEmpty() && isMessage(remoteMessage)) {

					System.out.println("\nMessage received from " + this.clientSocket.getInetAddress().toString());
					System.out.println("Sender's Port: " + serverPort);
					System.out.println("Message: " + remoteMessage.substring(1));// print to (local) console
					System.out.println();

				}

			}
		} catch (IOException io) {
			System.out.println("terminated?");
		}

		try {
			this.clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isMessage(String m) {
		return m.charAt(0) == '{';
	}

	public BufferedReader getReader() {
		return input;
	}

	public int getPort() {
		return this.clientSocket.getPort();
	}

	public int getListenPort() {
		return this.listeningPort;
	}

	public String getIp() {
		return this.clientSocket.getInetAddress().getHostAddress();
	}
}

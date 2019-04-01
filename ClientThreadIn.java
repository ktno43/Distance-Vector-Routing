import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThreadIn extends Thread {
	protected Socket clientSocket;
	protected BufferedReader input;
	protected int serverPort;
	protected ServerThread st;
	protected boolean firstMsg;
	protected boolean exited;

	ClientThreadIn(Socket sock, ServerThread serverThread) throws IOException {
		this.clientSocket = sock;
		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		this.st = serverThread;
		this.firstMsg = false;
		this.exited = false;
	}

	@Override
	public synchronized void run() {
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

		while (true) {
			try {
				String remoteMessage = input.readLine();

				if (remoteMessage != null && !firstMsg) {
					StringBuilder sb = new StringBuilder(remoteMessage);
					if (remoteMessage != null && remoteMessage.charAt(0) != '{')
						remoteMessage = sb.insert(0, '{').toString();
					firstMsg = true;
				}

				if (remoteMessage != null && remoteMessage.equals("{TERMINATE}")) {
					System.out.println("\nSomeone has terminated you from the chat. . .\n");
					this.clientSocket.close();
					this.input.close();
					this.st.isConnected();
					break;
				}

				else if (remoteMessage != null && remoteMessage.equals("{EXIT}")) {
					System.out.println("\nSomeone has left the chat. . .\n");
					this.st.isConnected();
					break;
				}

				else if (remoteMessage != null && !remoteMessage.isEmpty() && isMessage(remoteMessage)) {

					System.out.println("\nMessage received from " + this.clientSocket.getInetAddress().toString());
					System.out.println("Sender's Port: " + serverPort);
					System.out.println("Message: " + remoteMessage.substring(1));// print to (local) console
					System.out.println();

				}

			} catch (IOException e) {
				System.out.print("");
			}
		}

		try {
			this.clientSocket.close();
			this.input.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected boolean isMessage(String m) {
		return m.charAt(0) == '{';
	}

	protected BufferedReader getReader() {
		return input;
	}

	protected int getPort() {
		return this.clientSocket.getPort();
	}

	protected String getIp() {
		return this.clientSocket.getInetAddress().getHostAddress();
	}
}

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ClientThreadOut extends Thread {

	int toPort;
	Socket clientSocket;
	int listeningPort;
	boolean sendMessage;
	String clientMessage;
	String ip;
	final PrintStream out;
	boolean exited;

	ClientThreadOut(String ip, int listenPort) throws IOException {
		clientSocket = new Socket(ip, listenPort);
		this.listeningPort = listenPort;
		this.ip = ip;
		sendMessage = false;
		exited = false;

		clientMessage = "";
		out = new PrintStream(clientSocket.getOutputStream());
	}

	ClientThreadOut(Socket sock) throws IOException {
		this.clientSocket = sock;
		ip = sock.getInetAddress().getHostAddress();
		sendMessage = false;
		clientMessage = "";

		out = new PrintStream(clientSocket.getOutputStream());
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

		System.out.println("\nClient established\n");
	}

	public void send(String m) {
		sendMessage = true;
		clientMessage = m;
		if (!m.equals("{EXIT}")) {
			out.println(clientMessage);
			out.flush();
		}

		else {
			out.println("Someone has exited the chat");
			out.flush();
		}
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

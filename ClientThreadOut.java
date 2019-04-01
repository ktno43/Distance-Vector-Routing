import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ClientThreadOut extends Thread {
	protected Socket clientSocket;
	protected int listeningPort;
	protected String clientMessage;
	protected String ip;
	protected final PrintStream out;

	ClientThreadOut(String ip, int listenPort) throws IOException {
		clientSocket = new Socket(ip, listenPort);
		this.listeningPort = listenPort;
		this.ip = ip;

		clientMessage = "";
		out = new PrintStream(clientSocket.getOutputStream());
	}

	ClientThreadOut(Socket sock) throws IOException {
		this.clientSocket = sock;
		ip = sock.getInetAddress().getHostAddress();
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
		clientMessage = m;
		out.println(clientMessage);
		out.flush();
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

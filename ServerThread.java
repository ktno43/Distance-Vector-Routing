import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServerThread extends Thread {
	private int listeningPort;
	protected Vector<ClientThreadOut> clientVectorOut;
	protected Vector<ClientThreadIn> clientVectorIn;

	public ServerThread(int listenPort) {
		this.listeningPort = listenPort;
		this.clientVectorOut = new Vector<>();
		this.clientVectorIn = new Vector<>();
	}

	@Override
	public synchronized void run() {
		try (ServerSocket ss = new ServerSocket(this.listeningPort)) {
			// start the server...

			boolean listenFlag = true;
			System.out.println("Listening for connections on port: " + this.listeningPort + "\n");
			// and listen for connections
			while (listenFlag) {
				Socket sock = ss.accept();
				// got one!

				System.out.println("\nNew Connection: " + sock.getInetAddress().toString());
				System.out.println("Port: " + sock.getPort() + "\n");

				ClientThreadIn connectedClient = new ClientThreadIn(sock, this);

				clientVectorIn.add(connectedClient);
				BufferedReader input = connectedClient.getReader();
				String remoteMessage = input.readLine();// read remote message

				boolean flag = true;

				while (flag) {
					flag = false;
					connectedClient.serverPort = Integer.parseInt(remoteMessage);
					boolean breakout = false;
					for (ClientThreadOut cto : clientVectorOut) {
						if (cto.getIp().equals(connectedClient.getIp())
								&& cto.getPort() == connectedClient.serverPort) {
							flag = false;
							connectedClient.start();
							breakout = true;
							break;
						}
					}

					if (breakout)
						break;

					flag = false;
					Socket s = new Socket(sock.getInetAddress().getHostAddress(), Integer.parseInt(remoteMessage));
					ClientThreadOut cto = new ClientThreadOut(s);
					clientVectorOut.add(cto);
					cto.send(Integer.toString(listeningPort));
					cto.start();

					if (!connectedClient.isAlive())
						connectedClient.start();
				}
				isConnected();
			}
		} catch (

		IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace(System.err);
		}
	}

	protected void addConn(String ip, int port) throws IOException {
		ClientThreadOut client = new ClientThreadOut(ip, port);
		client.send(Integer.toString(listeningPort));
		client.start();
		this.clientVectorOut.add(client);
	}

	protected int getListeningPort() {
		return this.listeningPort;
	}

	protected boolean isConnected(String ip, int port) {
		for (ClientThreadOut ct : this.clientVectorOut) {
			if (ct.getIp().equals(ip) && ct.getListenPort() == port) {
				return true;
			}
		}

		return false;
	}

	protected void printClientList() {
		System.out.printf("%nID:\tIP Address\t\tPort No.%n");
		for (int i = 0; i < this.clientVectorOut.size(); i++) {

			System.out.printf("%d:\t%s\t\t%d%n", i + 1, this.clientVectorOut.get(i).getIp(),
					this.clientVectorOut.get(i).getPort());
		}
		System.out.println();
	}

	protected void sendUserMessage(int id, String m) {
		if (id <= 0 || id > clientVectorOut.size())
			System.out.println("\nID is incorrect");

		else {
			ClientThreadOut cto = clientVectorOut.get(id - 1);

			cto.send(m);
		}
	}

	protected void sendAllExitMsg() {
		for (int i = 0; i < clientVectorOut.size(); i++) {
			ClientThreadOut cto = clientVectorOut.get(i);
			cto.send("{EXIT}");
		}
	}

	protected void terminate(int id) throws IOException {

		if (id <= 0 || id > clientVectorOut.size())
			System.out.println("\nID is incorrect");

		else {
			ClientThreadOut cto = clientVectorOut.get(id - 1);
			cto.send("{TERMINATE}");

			if (!clientVectorIn.isEmpty())
				this.clientVectorIn.remove(id - 1);

			if (!clientVectorOut.isEmpty())
				this.clientVectorOut.remove(id - 1);

			cto.clientSocket.close();
			cto.out.close();
		}
	}

	protected boolean isConnected() {
		for (int i = 0; i < clientVectorIn.size(); i++) { // check for closed inPorts
			try {
				if (clientVectorIn.get(i).clientSocket.isClosed()
						|| clientVectorIn.get(i).clientSocket.getInputStream().read() == -1) {

					if (!clientVectorIn.isEmpty())
						this.clientVectorIn.remove(i);

					if (!clientVectorOut.isEmpty())
						this.clientVectorOut.remove(i);

					return false;
				}
			} catch (IOException e) {

			}
		}

		return true;
	}
}

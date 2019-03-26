import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class ServerThread extends Thread {
	private int listeningPort;
	static Vector<ClientThreadOut> clientVectorOut;
	static Vector<ClientThreadIn> clientVectorIn;
	boolean exited;

	public ServerThread(int listenPort) {
		this.listeningPort = listenPort;
		ServerThread.clientVectorOut = new Vector<>();
		ServerThread.clientVectorIn = new Vector<>();
		this.exited = false;
	}

	@Override
	public void run() {
		try (ServerSocket ss = new ServerSocket(this.listeningPort)) {
			// start the server...

			boolean flag1 = true;
			System.out.println("Listening for connections on port: " + this.listeningPort + "\n");
			// and listen for connections
			while (flag1) {
				final Socket sock = ss.accept();
				// got one!

				System.out.println("\nNew Connection: " + sock.getInetAddress().toString());
				System.out.println("Port: " + sock.getPort() + "\n");

				ClientThreadIn connectedClient = new ClientThreadIn(sock, this);

				clientVectorIn.add(connectedClient);
				BufferedReader input = connectedClient.getReader();

				boolean flag = true;

				while (flag) {
					String remoteMessage = input.readLine();// read remote message

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

					Socket s = new Socket(sock.getInetAddress().getHostAddress(), Integer.parseInt(remoteMessage));
					ClientThreadOut cto = new ClientThreadOut(s);
					clientVectorOut.add(cto);
					cto.send(Integer.toString(listeningPort));
					cto.start();
					if (!connectedClient.isAlive())
						connectedClient.start();

					isConnected();
					break;
				}
			}

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			ioe.printStackTrace(System.err);
		}
	}

	protected void addConn(String ip, int port) throws IOException {
		ClientThreadOut client = new ClientThreadOut(ip, port);
		client.send(Integer.toString(listeningPort));
		client.start();
		ServerThread.clientVectorOut.add(client);

	}

	protected int getListeningPort() {
		return this.listeningPort;
	}

	protected boolean isConnected(String ip, int port) {
		for (ClientThreadOut ct : ServerThread.clientVectorOut) {
			if (ct.getIp().equals(ip) && ct.getListenPort() == port) {
				return true;
			}
		}

		return false;
	}

	public void printClientList() {
		System.out.printf("%nID:\tIP Address\t\tPort No.%n");
		for (int i = 0; i < ServerThread.clientVectorOut.size(); i++) {

			System.out.printf("%d:\t%s\t\t%d%n", i + 1, ServerThread.clientVectorOut.get(i).getIp(),
					ServerThread.clientVectorOut.get(i).getPort());
		}
		System.out.println();
	}

	public void sendUserMessage(int id, String m) {
		ClientThreadOut cto = clientVectorOut.get(id - 1);

		cto.send(m);
	}

	public void sendAllExitMsg() {
		for (int i = 0; i < clientVectorOut.size(); i++) {
			ClientThreadOut cto = clientVectorOut.get(i);
			cto.send("{EXIT}");
		}
	}

	public void terminate(int id) throws IOException {

		// swapPos(id);
		ClientThreadOut deletedClient = clientVectorOut.remove(id - 1);

		if (!deletedClient.clientSocket.isClosed()) {
			deletedClient.clientSocket.close();
		}
		int deletedServerPort = deletedClient.getListenPort();

		for (int i = 0; i < clientVectorIn.size(); i++) {
			if (clientVectorIn.get(i).serverPort == deletedServerPort) {
				if (!clientVectorIn.get(i).clientSocket.isClosed()) {
					clientVectorIn.get(i).clientSocket.close();
				}
				clientVectorIn.remove(i);
				break;
			}
		}

	}

	public synchronized boolean isConnected() throws IOException {
		if (!clientVectorIn.isEmpty() && !clientVectorOut.isEmpty()) {
			for (int i = 0; i < clientVectorIn.size(); i++) { // check for closed inPorts

				if (clientVectorIn.get(i).clientSocket.getInputStream().read() == -1) {

					if (!clientVectorIn.get(i).clientSocket.isClosed()) {
						System.out.println("YOU HAVE BEEN TERMINATED :D?");
					}

					if (!clientVectorIn.isEmpty()) {
						ServerThread.clientVectorIn.remove(i);
					}

					if (!clientVectorOut.isEmpty())
						ServerThread.clientVectorOut.remove(i);

					return false;
				}
			}
		}
		return true;
	}

	private void swapPos(int start) {
		for (int i = start; i < ServerThread.clientVectorIn.size(); i++) {
			ClientThreadIn temp = ServerThread.clientVectorIn.get(i);
			ServerThread.clientVectorIn.set(i - 1, temp);
		}
		ServerThread.clientVectorIn.remove(ServerThread.clientVectorIn.size() - 1);

		for (int i = start; i < ServerThread.clientVectorOut.size(); i++) {
			ClientThreadOut temp = ServerThread.clientVectorOut.get(i);
			ServerThread.clientVectorOut.set(i - 1, temp);
		}
		ServerThread.clientVectorOut.remove(ServerThread.clientVectorOut.size() - 1);

	}
}

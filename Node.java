import java.util.Comparator;

public class Node implements Comparator<Node> {
	private Integer ID;
	private String IP;
	private Integer port;

	public Node(int ID, String IP, int port) {
		this.ID = ID;
		this.IP = IP;
		this.port = port;
	}

	@Override
	public int compare(Node n1, Node n2) {
		return n1.getID().compareTo(n2.getID());
	}

	protected Integer getID() {
		return this.ID;
	}

	protected String getIP() {
		return this.IP;
	}

	protected Integer getPort() {
		return this.port;
	}

}

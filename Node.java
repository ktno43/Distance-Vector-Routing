import java.util.Comparator;

public class Node implements Comparator<Node> {
	private Integer Id;
	private String Ip;
	private Integer port;
	private boolean receiveMsgs;

	public Node (int Id, String Ip, int port) {
		this.Id = Id;
		this.Ip = Ip;
		this.port = port;
		this.receiveMsgs = true;
	}

	protected boolean getReceiveMsgs () {
		return this.receiveMsgs;
	}

	protected void setReceiveMsgs (boolean flag) {
		this.receiveMsgs = flag;
	}

	@Override
	public int compare (Node n1, Node n2) {
		return n1.getID().compareTo(n2.getID());
	}

	protected Integer getID () {
		return this.Id;
	}

	protected String getIP () {
		return this.Ip;
	}

	protected Integer getPort () {
		return this.port;
	}

}
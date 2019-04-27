import java.util.List;
import java.util.Map;

public class Message {
	private String msg;
	private Integer ID;
	private String IP;
	private Integer port;

	public Message(int ID, String IP, int port, String msg) {
		this.ID = ID;
		this.IP = IP;
		this.port = port;
		this.msg = msg;
	}
	
	public static List<String> makeMessage(){
	List<String> msg = new ArrayList<>();
	
	for(Map.Entry<Node, Integer> entry : rtMap.
	}
}

/*-
 ****************************************
 * Kyle Nguyen
 * Kodi Winterer
 * 
 * COMP 429
 * Spring 2019
 * Senhua Yu
 * Tuesday 7:00 PM - 9:45 PM
 * 
 * Programming Assignment 2:
 * Implement a simplified version of the 
 * Distance Vector Routing Protocol.
 * 
 * Node.java
 * Version 7.0
 ****************************************/

public class Node {
	private Integer Id;
	private String Ip;
	private Integer port;
	private boolean receiveMsgs;

	public Node(int Id, String Ip, int port) {
		this.Id = Id;
		this.Ip = Ip;
		this.port = port;
		this.receiveMsgs = true;
	}

	protected boolean getReceiveMsgs() {
		return this.receiveMsgs;
	}

	protected void setReceiveMsgs(boolean flag) {
		this.receiveMsgs = flag;
	}

	protected Integer getID() {
		return this.Id;
	}

	protected String getIP() {
		return this.Ip;
	}

	protected Integer getPort() {
		return this.port;
	}
}
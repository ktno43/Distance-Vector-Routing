/*-
 ****************************************
 * Kyle Nguyen
 * Kodi Winterer
 * Michael Shi
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
 * Version 10.0
 ****************************************/

public class Node {
	private Integer Id; // Id from the topology file
	private String Ip; // Ip from the topology file
	private Integer port; // Port from the topology file
	private boolean receiveMsgs; // Able to receive messages or not

	public Node(int Id, String Ip, int port) {
		this.Id = Id;
		this.Ip = Ip;
		this.port = port;
		this.receiveMsgs = true;
	}

	/****************************************
	 * getReceiveMsgs- returns status of whether
	 * current node can receive messages
	 * 
	 * @return- status of receive messages
	 ****************************************/
	protected boolean getReceiveMsgs() {
		return this.receiveMsgs;
	}

	/****************************************
	 * setReceiveMsgs- sets status of receive
	 * msgs of the current node
	 * 
	 * @param flag-
	 *            set current receive msg based
	 *            on the flag
	 ****************************************/
	protected void setReceiveMsgs(boolean flag) {
		this.receiveMsgs = flag;
	}

	/****************************************
	 * getID- return the associated id
	 * 
	 * @return- id assigned on creation
	 ****************************************/
	protected Integer getID() {
		return this.Id;
	}

	/****************************************
	 * getIp- return the associated ip
	 * 
	 * @return- ip assigned on creation
	 ****************************************/
	protected String getIP() {
		return this.Ip;
	}

	/****************************************
	 * getPort- return the associated port
	 * 
	 * @return- port assigned on creation
	 ****************************************/
	protected Integer getPort() {
		return this.port;
	}
}
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
 * ServerThread.java
 * Version 7.0
 ****************************************/
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerThread extends Thread {
	private int listeningPort; // Listening port of the current system
	private int serverId;
	private List<Node> nodesList;
	private Node serverNode;
	private Map<Node, Integer> rtMap;
	private Set<Node> neighborsSet;
	private Map<Node, Node> hopMap;
	private String Ip;
	private int numPackets;
	private Long[] timeoutArr;
	public static final Long TIMEOUT = (long) 15000;
	private boolean crash;
	private boolean[] firstMsg;
	private DatagramSocket dgSocket;

	public ServerThread(int listenPort, int id, String ip) {
		this.listeningPort = listenPort; // Set the listening port
		this.serverId = id;
		this.nodesList = new ArrayList<>();
		this.serverNode = null;
		this.rtMap = new HashMap<>();
		this.neighborsSet = new HashSet<>();
		this.hopMap = new HashMap<>();
		this.Ip = ip;
		this.numPackets = 0;
		this.crash = false;
	}

	@Override
	public void run() {
		try { // Try to make a server socket for the current port
				 // start the server...
			dgSocket = new DatagramSocket(this.listeningPort);
			boolean listenFlag = true;

			System.out.println("Listening for connections on port: " + this.listeningPort + "\n");
			// and listen for connections

			while (listenFlag && !crash) { // While listening
				byte[] receiveData = new byte[5000];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				dgSocket.receive(receivePacket);
				read(receivePacket.getData(), receivePacket.getLength());
			}

		} catch (IOException ioe) {
			System.out.println("\nThere is already a Server Socket running on that port!");
			System.exit(0);

		}
	}

	protected void checkTimeStamps(int id) {
		long now = System.currentTimeMillis();
		if (now - timeoutArr[id] > TIMEOUT) {
			updateCost(this.serverId, id + 1, "inf");
			this.hopMap.put(getNodeByID(id + 1), null);

			for (Map.Entry<Node, Node> entry : hopMap.entrySet()) {
				Node keyNode = entry.getKey();
				Node valueNode = entry.getValue();
				if (valueNode != null && valueNode.getID().equals(getNodeByID(id + 1).getID())) {
					updateCost(keyNode.getID(), serverId, "inf");
					hopMap.put(keyNode, null);
				}
			}
		}
	}

	protected boolean step() {
		int packetsSentOut = 0;
		if (!crash) {
			try {
				int updateFields = nodesList.size();
				byte[] msgByte = new byte[8 + (updateFields * 10)];

				msgByte[0] = (byte) (updateFields & 0xFF);
				msgByte[1] = (byte) ((updateFields >> 8) & 0xFF);

				int serverPort = listeningPort;
				msgByte[2] = (byte) (serverPort & 0xFF);
				msgByte[3] = (byte) ((serverPort >> 8) & 0xFF);

				InetAddress serverIp = InetAddress.getByName(Ip);
				byte[] serverIpB = serverIp.getAddress();

				for (int i = 0; i < serverIpB.length; i++) {
					msgByte[4 + i] = serverIpB[i];
				}

				for (int j = 0; j < nodesList.size(); j++) {
					InetAddress serverIpN = InetAddress.getByName(nodesList.get(j).getIP());
					byte[] serverIpNB = serverIpN.getAddress();
					for (int k = 0; k < serverIpNB.length; k++)
						msgByte[8 + (j * 10) + k] = serverIpNB[k];

					int serverPortN = nodesList.get(j).getPort();
					msgByte[(j * 10) + 12] = (byte) (serverPortN & 0xFF);
					msgByte[(j * 10) + 13] = (byte) ((serverPortN >> 8) & 0xFF);

					int serverIdN = nodesList.get(j).getID();
					msgByte[(j * 10) + 14] = (byte) (serverIdN & 0xFF);
					msgByte[(j * 10) + 15] = (byte) ((serverIdN >> 8) & 0xFF);

					int serverCostN = rtMap.get(nodesList.get(j));
					msgByte[(j * 10) + 16] = (byte) (serverCostN & 0xFF);
					msgByte[(j * 10) + 17] = (byte) ((serverCostN >> 8) & 0xFF);

				}

				for (Node n : neighborsSet) {
					if (n.getReceiveMsgs()) {
						DatagramPacket msgPacket = new DatagramPacket(msgByte, msgByte.length,
								InetAddress.getByName(n.getIP()), n.getPort());
						dgSocket.send(msgPacket);
						packetsSentOut += 1;
					}
				}
			} catch (IOException e) {
				// do nothing
			}
		}

		return (neighborsSet.size() == packetsSentOut);
	}

	protected boolean disable(int id) {

		Node toDisableNode = getNodeByID(id);

		if (toDisableNode != null && neighborsSet.contains(toDisableNode)) {
			toDisableNode.setReceiveMsgs(false);
			updateCost(serverId, id, "inf");

			for (Node n : nodesList) {
				if (hopMap.get(n).getID() == id)
					updateCost(serverId, n.getID(), "inf");
			}

			return true;
		}

		else {
			System.out.println("disable ERROR: The ID entered is not one of your neighbors.\n");
			return false;
		}

	}

	protected void read(byte[] msg, int bytesRead) {
		try {

			byte[] messageByte = msg;

			int high = messageByte[1] >= 0 ? messageByte[1] : 256 + messageByte[1];
			int low = messageByte[0] >= 0 ? messageByte[0] : 256 + messageByte[0];
			int updateFields = low | (high << 8);

			high = messageByte[3] >= 0 ? messageByte[3] : 256 + messageByte[3];
			low = messageByte[2] >= 0 ? messageByte[2] : 256 + messageByte[2];
			int senderPort = low | (high << 8);

			String senderIp = InetAddress.getByAddress(Arrays.copyOfRange(messageByte, 4, 8)).getHostAddress();

			if (getNodeByIpAndPort(senderIp, senderPort).getReceiveMsgs()) {
				System.out.print("RECEIVED A MESSAGE FROM SERVER " + getNodeByIpAndPort(senderIp, senderPort).getID());
				this.numPackets += bytesRead;

				for (int j = 8; j < (10 * updateFields); j += 10) {
					String ipN = InetAddress.getByAddress(Arrays.copyOfRange(messageByte, j, j + 4)).getHostAddress();

					high = messageByte[j + 5] >= 0 ? messageByte[j + 5] : 256 + messageByte[j + 5];
					low = messageByte[j + 4] >= 0 ? messageByte[j + 4] : 256 + messageByte[j + 4];
					int nPort = low | (high << 8);

					high = messageByte[j + 7] >= 0 ? messageByte[j + 7] : 256 + messageByte[j + 7];
					low = messageByte[j + 6] >= 0 ? messageByte[j + 6] : 256 + messageByte[j + 6];
					int nID = low | (high << 8);

					high = messageByte[j + 9] >= 0 ? messageByte[j + 9] : 256 + messageByte[j + 9];
					low = messageByte[j + 8] >= 0 ? messageByte[j + 8] : 256 + messageByte[j + 8];

					int nCost = low | (high << 8);

					for (Node n : neighborsSet) {
						if (senderIp.equals(n.getIP()) && (senderPort == n.getPort()) && ipN.equals(serverNode.getIP())
								&& nPort == serverNode.getPort() && (nCost < rtMap.get(n))) {
							rtMap.put(n, nCost);
						}
					}

					for (int i = 0; i < rtMap.size(); i++) {
						if (!neighborsSet.contains(getNodeByIpAndPort(ipN, nPort))) {
							if (nCost == 65535) {
								updateCost(this.serverId, getNodeByIpAndPort(ipN, nPort).getID(), "inf");
								hopMap.put(getNodeByIpAndPort(ipN, nPort), null);
							}

							else if (nCost < rtMap.get(getNodeByIpAndPort(ipN, nPort))) {
								updateCost(this.serverId, getNodeByIpAndPort(ipN, nPort).getID(),
										Integer.toString(nCost));
								hopMap.put(getNodeByIpAndPort(ipN, nPort), getNodeByIpAndPort(senderIp, senderPort));
							}
						}
					}

					for (Node n : nodesList) {
						if (n.getIP().equals(senderIp) && n.getPort() == senderPort) {
							timeoutArr[n.getID() - 1] = System.currentTimeMillis();
						}
					}

					int senderID = getNodeByIpAndPort(senderIp, senderPort).getID();

					if (!firstMsg[senderID - 1]) {
						firstMsg[senderID - 1] = true;
						Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
							checkTimeStamps(senderID - 1);

						}, TIMEOUT, TIMEOUT, TimeUnit.MILLISECONDS);
					}
				}

				System.out.println("\n");
			}
		} catch (IOException e) {
			// do nothing?
		}

	}

	protected Node getNodeByIpAndPort(String ip, int port) {
		for (Node n : nodesList) {
			if (n.getIP().equals(ip) && n.getPort() == port)
				return n;
		}

		return null;
	}

	protected void createNodes(List<String> inputList) {
		int numServers = Integer.parseInt(inputList.get(0));

		for (int i = 0; i < numServers; i++) {
			String[] inputSplitArr = inputList.get(i + 2).split("\\s+");
			Node node = new Node(Integer.parseInt(inputSplitArr[0]), inputSplitArr[1],
					Integer.parseInt(inputSplitArr[2]));

			nodesList.add(node);

			int cost = Integer.MAX_VALUE;

			if (Integer.parseInt(inputSplitArr[0]) == serverId) {
				serverNode = node;
				cost = 0;
				hopMap.put(node, serverNode);
			}

			else
				hopMap.put(node, null);

			rtMap.put(node, cost);
		}
	}

	protected void createNeighbors(List<String> inputList) {
		int numServers = Integer.parseInt(inputList.get(0));
		int numNeighbors = Integer.parseInt(inputList.get(1));
		int neighborsOffset = 2 + numServers;

		for (int i = 0; i < numNeighbors; i++) {
			String[] inputSplitArr = inputList.get(i + neighborsOffset).split("\\s+");
			int fromID = Integer.parseInt(inputSplitArr[0]);
			int toID = Integer.parseInt(inputSplitArr[1]);
			int cost = Integer.parseInt(inputSplitArr[2]);

			Node toNode = getNodeByID(toID);
			rtMap.put(toNode, cost);
			this.timeoutArr = new Long[numNeighbors + 2];
			this.firstMsg = new boolean[numNeighbors + 2];

			for (int j = 0; j < timeoutArr.length; j++) {
				timeoutArr[j] = (long) -1;
				this.firstMsg[j] = false;
			}

			neighborsSet.add(toNode);
			hopMap.put(toNode, getNodeByID(fromID));
		}
	}

	protected void setNumPackets(int reset) {
		this.numPackets = reset;
	}

	protected int getNumPackets() {
		return this.numPackets;
	}

	protected void setCrash(boolean crash) {
		this.crash = crash;
	}

	private Node getNodeByID(int ID) {
		for (Node node : nodesList) {
			if (node.getID() == ID)
				return node;
		}

		return null;
	}

	protected boolean updateCost2(int id1, int id2, String newCost) {
		Node destNode = null;

		if ((id1 != id2) && (id1 == this.serverId || id2 == this.serverId)) {
			if ((id1 == this.serverId) && (id2 > 0) && (id2 <= this.nodesList.size()))
				destNode = getNodeByID(id2);

			else if ((id2 == this.serverId) && (id1 > 0) && (id1 <= this.nodesList.size()))
				destNode = getNodeByID(id1);

			if (neighborsSet.contains(destNode)) {
				if (newCost.equals("inf"))
					rtMap.put(destNode, Integer.MAX_VALUE);

				else
					rtMap.put(destNode, Integer.parseInt(newCost));

				return true;
			}

			else {
				System.out.println("update ERROR: The ID entered is not a neighbor.\n");
			}
		}

		else {
			System.out.println("update ERROR: Invalid ID.\n");
		}

		return false;
	}

	protected void updateCost(int id1, int id2, String newCost) {
		Node destNode = null;

		if ((id1 != id2) && (id1 == this.serverId || id2 == this.serverId)) {
			if ((id1 == this.serverId) && (id2 > 0) && (id2 <= this.nodesList.size()))
				destNode = getNodeByID(id2);

			else if ((id2 == this.serverId) && (id1 > 0) && (id1 <= this.nodesList.size()))
				destNode = getNodeByID(id1);

			if (newCost.equals("inf"))
				rtMap.put(destNode, Integer.MAX_VALUE);

			else
				rtMap.put(destNode, Integer.parseInt(newCost));

		}
	}

	protected void displayRt() {
		System.out.println("\nSource Server ID\tNext Hop Server\t\tCost");
		for (Node node : nodesList) {
			int cost = rtMap.get(node);
			String costStr = "" + cost;

			if (cost == Integer.MAX_VALUE || cost == 65535)
				costStr = "inf";

			if (hopMap.get(node) != null)
				System.out.print(node.getID() + "\t\t\t" + hopMap.get(node).getID() + "\t\t\t" + costStr + "\n");
			else
				System.out.print(node.getID() + "\t\t\t" + this.serverId + "\t\t\t" + costStr + "\n");
		}

		System.out.println();
	}
}
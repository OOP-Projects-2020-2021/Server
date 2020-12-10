/*
 * Lobby lobby = new Lobby(gameType);
 * 
 * if(lobby.initLobbyConnection()){
 * 
 * 	System.out.println("Lobby created successfully");
 *  lobby.start();
 * } else{
 * System.out.println("error");
 * }
 */

package tudorserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Lobby {

	private GameType gameType = GameType.ARCADE; // by default, it is an arcade game
	private boolean gameHasStarted = false;

	private InetAddress lobbyAddress = null;
	private int lobbyPort = -1;
	private Thread listenThread;
	private volatile boolean listening = false;
	private DatagramSocket socket; // helps us to receive packets of data

	private final int MAX_NR_PLAYERS_IN_LOBBY;
	private final int MAX_PACKET_SIZE = 1024;
	private byte[] receivedDataBuffer;
	private byte[] inGameInfoForAllOtherPlayers = new byte[10];

	private Set<ServerClient> clients = new HashSet<ServerClient>();
	private Queue<ServerClient> clientsQueue = new PriorityQueue<ServerClient>();
	// DataProcessor dataProcessor=new DataProcessor();

	public Lobby(GameType gameType, int lobbyPort) {

		this.gameType = gameType;
		this.lobbyPort = lobbyPort;

		switch (this.gameType) {

		case ARCADE:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
			break;

		case DEATH_MATCH:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
			break;

		case CAPTURE_THE_FLAG:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
			break;

		case ZOMBIE_INVASION:
			MAX_NR_PLAYERS_IN_LOBBY = 5;
			break;

		default:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
			break;
		}

		receivedDataBuffer = new byte[MAX_PACKET_SIZE * MAX_NR_PLAYERS_IN_LOBBY];
	}

	public void start() {

		try {
			socket = new DatagramSocket(lobbyPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		listening = true;

		listenThread = new Thread(() -> {
			listen();
		});
		listenThread.start();
	}

	public int getLobbyPort() {
		return lobbyPort;
	}

	private void listen() {

		DatagramPacket packet;

		while (listening) {

			packet = new DatagramPacket(receivedDataBuffer, MAX_PACKET_SIZE);
			System.out.println("aaaa");
			try {
				System.out.println("bbb");
				socket.receive(packet);
				System.out.println("ccc");
			} catch (IOException e) {
				System.out.println("ddd");
				e.printStackTrace();
				System.out.println("eee");
			}
			System.out.println("fff");
			if (gameHasStarted == false) {
				assignConnectionPacketToCorrectPlayer(packet);
				
				int id= new Byte(packet.getData()[0]).intValue();
				System.out.println(id);
			}

			// TODO send these information to all other players
			else {
				sendPacketToAllOtherPlayers(packet);
			}

			// processPacket(packet);
		}
	}

	private void assignConnectionPacketToCorrectPlayer(DatagramPacket packet) {

		for (ServerClient clientWaitingForApproval : clientsQueue) {

			if (clientSentBackCorrectConnectionPacket(clientWaitingForApproval, packet)) {

				this.clients.add(clientWaitingForApproval);
				this.clientsQueue.remove(clientWaitingForApproval);
			} else if (clientWaitingForApproval.getOutFromLobbyTimeInSeconds(System.nanoTime()) > 60) {

				// if it did not respond to connection test within 1 minute, we kick it out from
				// lobby
				clientsQueue.remove(clientWaitingForApproval);
			}
		}
	}

	private boolean clientSentBackCorrectConnectionPacket(ServerClient clientWaitingForApproval,
			DatagramPacket packet) {

		// checks if packet's address is the same with client's address
		// if packet's port is the same with client's port
		// if the message sent by the client matches his id

		return (packet.getAddress() == clientWaitingForApproval.getClientAddress()
				&& packet.getPort() == clientWaitingForApproval.getClientPort()
				&& new String(packet.getData()).equals(Integer.toString(clientWaitingForApproval.getPlayerID())));
	}

	private void sendPacketToAllOtherPlayers(DatagramPacket packet) {

		inGameInfoForAllOtherPlayers = packet.getData();
		InetAddress clientAddress = packet.getAddress();
		int clientPort = packet.getPort();

		for (ServerClient client : this.clients) {

			if (client.getClientAddress() == clientAddress && client.getClientPort() == clientPort) {
				// DO NOTHING
			} else {

				send(inGameInfoForAllOtherPlayers, clientAddress, clientPort);
			}
		}
	}

	private void processPacket(DatagramPacket packet) {

		byte[] data = packet.getData();

//		DataProcessor dataProcessor=new DataProcessor(data);
//		dataProcessor.processData();

		InetAddress clientAddress = packet.getAddress();
		int clientPort = packet.getPort();

		dumpPacket(packet);
	}

	private void dumpPacket(DatagramPacket packet) {

		byte[] data = packet.getData();
		InetAddress clientAddress = packet.getAddress();
		int clientPort = packet.getPort();

		System.out.println("------------------------------");
		System.out.println("PACKET:");
		System.out.println(
				"\t Host Address -> " + clientAddress.getHostAddress() + "  :  Client Port  ->  " + clientPort);
		System.out.println();
		System.out.println("\tContents:");
		System.out.println("\t\t" + new String(data));
		System.out.println("------------------------------");
	}

	public void send(byte[] data, InetAddress clientAddress, int clientPort) {

		assert (socket.isConnected());

		DatagramPacket packet = new DatagramPacket(data, 10, clientAddress, clientPort);

		try {
			socket.send(packet);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public boolean initLobbyConnection() {

		try {
			socket = new DatagramSocket(); // binds it to available port and address

			this.lobbyPort = socket.getPort();
			this.lobbyAddress = socket.getInetAddress();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(this.lobbyPort + "   " + this.lobbyAddress);
		return (this.lobbyPort != -1 && this.lobbyAddress != null);
	}

	public AddClientToLobbyQueueStatus addClient(ServerClient client) {

		if (this.clients.size() + 1 > this.MAX_NR_PLAYERS_IN_LOBBY) {
			// we won't add the player, the maximum size of the lobby is already reached
			// ERROR OVERFLOW

			return AddClientToLobbyQueueStatus.OVERFLOW;
		} else {

			client.timeItEnteredLobby = System.nanoTime();
			testConnection(client);
			clientsQueue.add(client);
			return AddClientToLobbyQueueStatus.SUCCESS;
		}
	}

//	public AddClientToLobbyQueueStatus addParty(Set<ServerClient> party) {
//
//		if (this.clients.size() + party.size() > this.MAX_NR_PLAYERS_IN_LOBBY) {
//			// we won't add the party
//			// ERROR OVERFLOW
//
//			return AddClientToLobbyQueueStatus.OVERFLOW;
//		} else {
//
//			for (ServerClient clientInParty : party) {
//				testConnection(clientInParty);
//			}
//
//			return AddClientToLobbyQueueStatus.SUCCESS;
//		}
//	}

	/*
	 * sends a connection packet to the user in order to connect to lobby, the user
	 * must send back a similar message
	 */

	private void testConnection(ServerClient client) {
		// TODO Auto-generated method stub
		byte[] testPacket = new StringBuilder().append("Test Client\nid = ").append(client.getPlayerID())
				.append("\nname = ").append(client.getPlayerName()).toString().getBytes();

		send(testPacket, client.getClientAddress(), client.getClientPort());
	}
}

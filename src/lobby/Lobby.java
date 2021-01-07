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

package lobby;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import game.*;
import tudorserver.AddClientToLobbyQueueStatus;
import tudorserver.PacketIntention;
import tudorserver.ServerClient;

public class Lobby {

	// THIS VARIABLE IS JUST FOR A SHORT PERIOD OF TIME
	private boolean team = false;

	public int lobbyID;
	private GameType gameType = GameType.ARCADE; // by default, it is an arcade game
	public boolean gameHasStarted = false;

	private InetAddress lobbyAddress = null;
	private int lobbyPort = -1;
	private Thread listenThread;
	private volatile boolean listening = false;
	private DatagramSocket socket; // helps us to receive packets of data

	private final int MAX_NR_PLAYERS_IN_LOBBY;
	private final int MAX_PACKET_SIZE = 1024;
	private byte[] receivedDataBuffer;
	private byte[] inGameInfoForAllOtherPlayers = new byte[10];
	Game gameProcesser;

	private Map<Integer, ServerClient> clients = new HashMap<Integer, ServerClient>();
	// DataProcessor dataProcessor=new DataProcessor();

	public Lobby(GameType gameType, int id) {
		// !!! provide lobby port; I will try to implement this without providing the
		// lobby port
		// now, I will socket

		this.lobbyID = id;
		this.gameType = gameType;

		switch (this.gameType) {

		case ARCADE:
			MAX_NR_PLAYERS_IN_LOBBY = 2;
			gameProcesser = new ArcadeGameProcesser(this.gameType);
			break;

		case BATTLE_ROYALE:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
			gameProcesser = new BattleRoyaleGameProcesser(this.gameType);
			break;

		case CAPTURE_THE_FLAG:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
			gameProcesser = new CaptureTheFlagGameProcesser(this.gameType);
			break;

		case ZOMBIE_INVASION:
			MAX_NR_PLAYERS_IN_LOBBY = 5;
			gameProcesser = new ZombieInvasionGameProcesser(this.gameType);
			break;

		default:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
			break;
		}

		receivedDataBuffer = new byte[MAX_PACKET_SIZE * MAX_NR_PLAYERS_IN_LOBBY];
	}

	public void start() {

		try {
			// Constructs a datagram socket and binds it to the specified port on the local
			// host machine.
			// The socket will be bound to the wild card address,an IP address chosen by the
			// kernel.

			// socket = new DatagramSocket(lobbyPort);

			// !!!!!!!! I try not to provide a port
			socket = new DatagramSocket();
			this.lobbyPort = socket.getPort();
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
		return this.lobbyPort;
	}

	public String getLobbyAddress() {
		return this.lobbyAddress.toString();
	}

	public GameType getGameType() {
		return this.gameType;
	}

	private void listen() {

		DatagramPacket packet;

		while (listening) {

			packet = new DatagramPacket(receivedDataBuffer, MAX_PACKET_SIZE);

			try {
				socket.receive(packet);
				inspectPacket(packet);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	private void inspectPacket(DatagramPacket packet) {

		processPacket(packet);

		String dataString = new String(packet.getData());
		String[] packetInfo = removeSpacesFromStrings(dataString.split(" ")); // every information will be splitted and
																				// placed in a String array

		PacketIntention packetIntention = getPacketIntention(packetInfo[0]); // collect packet intention
		// System.out.println(packetIntention);
		switch (packetIntention) {

		case JOIN_LOBBY: // request to join lobby

			if (gameHasStarted == false) {

				connectPlayer(packet, packetInfo);

				// announce other players that a new player is JOINING
				// sendPacketToAllOtherPlayers(packet);
			}

			break;

		case IN_GAME_INFO: // in game information

			// TODO send these information to all other players
			if (gameHasStarted == true) {

				/////////////////////////////////////////////////////////// gameProcesser.processReceivedData(packetInfo);
				/////////////////////////////////////////////////////////// put it back
				sendPacketToAllOtherPlayers(packet);

				if (gameProcesser.gameHasEnded()) {
					this.endGame();
					gameProcesser.retrieveEndGameStatistics();
					System.gc();
				}
			}

			break;

		case RESIDUAL:

			break;

		default: // residual
			break;
		}
	}

	private String[] removeSpacesFromStrings(String[] splitResult) {

		for (int i = 0; i < splitResult.length; i++) {
			splitResult[i].replace(" ", "");
		}

		return splitResult;
	}

	private PacketIntention getPacketIntention(String dataString) {
		// every packet sent by players MUST have an intention: an int which signifies
		// whether:
		// the user wants to log in (0)
		// the user wants to join the lobby - it is looking forward to connect to a
		// available game (1)
		// the user wants to send information about the game - its coordinates, swings
		// sword, etc(4)
		// also, it is possible that the server recived a residual packet which has
		// nothing to do with anything

		switch (dataString) {

		case "0":
			return PacketIntention.RECEIVE_ACCOUNT_UPON_LOG_IN;

		case "1":
			return PacketIntention.JOIN_LOBBY;

		case "4":
			return PacketIntention.IN_GAME_INFO;

		default:
			return PacketIntention.RESIDUAL;
		}
	}

	public AddClientToLobbyQueueStatus connectPlayer(DatagramPacket packet, String[] packetInfo) {

		if (this.clients.size() + 1 > this.MAX_NR_PLAYERS_IN_LOBBY) {
			// we won't add the player, the maximum size of the lobby is already reached
			// ERROR OVERFLOW
			System.out.println("ERROR " + this.clients.size() + 1);
			return AddClientToLobbyQueueStatus.OVERFLOW;

		} else {

			int playerID = -1;

			try {
				playerID = Integer.parseInt(packetInfo[1]);
			} catch (NumberFormatException nrFormatEx) {
				System.out.println(nrFormatEx);
				return AddClientToLobbyQueueStatus.WRONG_ID;
			}

			// check if this player
			/*
			 * 1. exists 2. is not a duplicate (hasn't already been introduced in the lobby)
			 * 3. retrieve player's username 4. optionally: not big level difference, same
			 * rank etc
			 */
			this.clients.put(playerID, new ServerClient(playerID, packet.getAddress(), packet.getPort()));

			if (team) {
				send(new String("2 0 STOP").getBytes(), packet.getAddress(), packet.getPort()); // 2 means, for the
																								// client,
				// CONNECTED TO LOBBY

				team = !team;
			} else {
				send(new String("2 1 STOP").getBytes(), packet.getAddress(), packet.getPort()); // 2 means, for the
				// client,
				// CONNECTED TO LOBBY
				team = !team;
			}

			if (this.clients.size() == this.MAX_NR_PLAYERS_IN_LOBBY) {
				gameHasStarted = true;
			}

			return AddClientToLobbyQueueStatus.SUCCESS;
		}
	}

	private void sendPacketToAllOtherPlayers(DatagramPacket packet) {

		inGameInfoForAllOtherPlayers = packet.getData();
		InetAddress receiverClientAddress = packet.getAddress();
		int receiverClientPort = packet.getPort();

		// now iterate through all clients and send them the information

		for (ServerClient client : clients.values()) {

			if (client.getClientAddress() == receiverClientAddress && client.getClientPort() == receiverClientPort) {
				// DO NOTHING
			} else { // for all others players

				send(inGameInfoForAllOtherPlayers, client.getClientAddress(), client.getClientPort()); // send
																										// information
			}
		}
	}

	private void processPacket(DatagramPacket packet) {

		byte[] data = packet.getData();

//		DataProcessor dataProcessor=new DataProcessor(data);
//		dataProcessor.processData();

		InetAddress clientAddress = packet.getAddress();
		int clientPort = packet.getPort();

		// dumpPacket(packet);
	}

	private void dumpPacket(DatagramPacket packet) {
		System.out.println("dumping packet");
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

		DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);

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

		return (this.lobbyPort != -1 && this.lobbyAddress != null);
	}

	private void endGame() {
		// a method which ends the lobby and the listening thread

		this.listening = false;
		this.listenThread.interrupt(); // interrupts the thread

		for (ServerClient client : clients.values()) { // send a message to all players - game ended

			send(new String("END").getBytes(), client.getClientAddress(), client.getClientPort());
		}
	}
}

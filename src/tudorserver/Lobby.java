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
import java.util.HashMap;
import java.util.Map;

import game.*;

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
	Game gameProcesser;

	private Map<Integer, ServerClient> clients = new HashMap<Integer, ServerClient>();
	// DataProcessor dataProcessor=new DataProcessor();

	public Lobby(GameType gameType, int lobbyPort) {
		// !!! provide lobby port; I will try to implement this without providing the lobby port
		// now, I will socket

		this.gameType = gameType;
		this.lobbyPort = lobbyPort;

		switch (this.gameType) {

		case ARCADE:
			MAX_NR_PLAYERS_IN_LOBBY = 10;
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

		PacketIntention packetIntention = getPacketIntention(dataString); // collect packet intention
		packet.setData(dataString.substring(dataString.indexOf(' ') + 1, dataString.toString().length()).getBytes());
		// now, since we have collected the packet intention, we shrink the packet's
		// data, by ignoring the intention information

		switch (packetIntention) {

		case JOIN_LOBBY: // request to join lobby

			if (gameHasStarted == false) {

				connectPlayer(packet);
			}

			break;

		case IN_GAME_INFO: // in game information

			// TODO send these information to all other players
			if (gameHasStarted == true) {
				gameProcesser.processReceivedData(packet.getData());
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

	private PacketIntention getPacketIntention(String dataString) {
		// every packet sent by players MUST have an intention: an int which signifies
		// whether:
		// the user wants to log in (0)
		// the user wants to join the lobby - it is looking forward to connect to a
		// available game (1)
		// the user wants to send information about the game - its coordinates, swings
		// sword, etc(2)
		// also, it is possible that the server recived a residual packet which has
		// nothing to do with anything

		switch (dataString.substring(0, dataString.indexOf(' '))) {

		case "0":
			return PacketIntention.RECEIVE_ACCOUNT_UPON_LOG_IN;

		case "1":
			return PacketIntention.JOIN_LOBBY;

		case "2":
			return PacketIntention.IN_GAME_INFO;

		default:
			return PacketIntention.RESIDUAL;
		}
	}

	private AddClientToLobbyQueueStatus connectPlayer(DatagramPacket packet) {

		if (this.clients.size() + 1 > this.MAX_NR_PLAYERS_IN_LOBBY) {
			// we won't add the player, the maximum size of the lobby is already reached
			// ERROR OVERFLOW
			System.out.println("ERROR " + this.clients.size() + 1);
			return AddClientToLobbyQueueStatus.OVERFLOW;
		} else {
			System.out.println("HERE");
			int playerID = Integer.parseInt(new String(packet.getData()));

			this.clients.put(playerID, new ServerClient(playerID, packet.getAddress(), packet.getPort()));

			// TO DO -> SEND BACK TO THE CLIENT A CONNECTED TO LOBBY MESSAGE
			send(new String("CONNECTED TO LOBBY").getBytes(), packet.getAddress(), packet.getPort());
			System.out.println("CONNECTED TO LOBBY");
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

	private void endGame() {
		// a method which ends the lobby and the listening thread
		//

		this.listening = false;
		this.listenThread.interrupt(); // interrupts the thread

		for (ServerClient client : clients.values()) { // send a message to all players - game ended

			send(new String("END").getBytes(), client.getClientAddress(), client.getClientPort());
		}
	}
}

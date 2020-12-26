package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import database.AppData;
import database.Database;
import lobby.Lobby;
import server.DataProcessor.ConnectionData;

public class Server {

	private int serverPort;
	private Thread listenThread;
	private volatile boolean listening = false;
	private DatagramSocket socket;

	private final int MAX_NR_PACKETS_RECEIVED_AT_ONE_MOMENT = 100;
	private final int MAX_PACKET_SIZE = 64;
	private byte[] receivedDataBuffer = new byte[MAX_PACKET_SIZE * MAX_NR_PACKETS_RECEIVED_AT_ONE_MOMENT];

	private AppData appData;

	private int lastLobbyID = 0;
	private ArrayList<Lobby> lobby = new ArrayList<Lobby>();

	public Server(int serverPort) {

		this.serverPort = serverPort;

		Database database = new Database();
		this.appData = database.retrieveAppData();
	}

	public void start() {

		try {
			socket = new DatagramSocket(serverPort);
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

	public int getServerPort() {
		return serverPort;
	}

	private void listen() {

		while (listening) {
			DatagramPacket packet = new DatagramPacket(receivedDataBuffer, MAX_PACKET_SIZE);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			processPacket(packet);
		}
	}

	private void processPacket(DatagramPacket packet) {

		System.out.println("processing packet");
		DataProcessor dataProcessor = new DataProcessor(packet);
		ConnectionData connectionData = dataProcessor.processData(lobby);

		if (intentionIsJoinGame(connectionData.intention)) {

			if (connectionData.availableLobbyExists == false) {
				// we are going to create a new lobby and the player is going to join the lobby
				lobby.add(new Lobby(connectionData.gameType, this.lastLobbyID++)); // create the lobby
				lobby.get(lobby.size() - 1).start(); // start the lobby
				lobby.get(lobby.size() - 1).connectPlayer(packet, dataProcessor.getPacketInfo()); // connect the player
																									// to the lobby
			} else { // the player will join the lobby

				lobby.get(connectionData.availableLobbyIndex).connectPlayer(packet, dataProcessor.getPacketInfo()); // connect the player to the lobby
			}
		} else if (connectionData.intention == ClientToServerPacketIntention.BUY) {

		} else if (connectionData.intention == ClientToServerPacketIntention.SIGN_IN) {

		} else if (connectionData.intention == ClientToServerPacketIntention.SIGN_UP) {

		} else if (connectionData.intention == ClientToServerPacketIntention.RESIDUAL) {

		}
	}

	private boolean intentionIsJoinGame(ClientToServerPacketIntention intention) {

		return (intention == ClientToServerPacketIntention.CONNECT_TO_ARCADE_LOBBY
				|| intention == ClientToServerPacketIntention.CONNECT_TO_BATTLE_ROYALE_LOBBY
				|| intention == ClientToServerPacketIntention.CONNECT_TO_CAPTURE_THE_FLAG_LOBBY
				|| intention == ClientToServerPacketIntention.CONNECT_TO_RANDOM_LOBBY
				|| intention == ClientToServerPacketIntention.CONNECT_TO_ZOMBIE_INVASION_LOBBY);
	}

	public void send(byte[] data, InetAddress address, int port) {

		assert (socket.isConnected());

		DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}

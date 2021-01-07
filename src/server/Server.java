package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

	private int serverPort = 8192;
	private Thread listenThread;
	private volatile boolean listening = false;
	private DatagramSocket socket;

	private final int MAX_NR_PACKETS_RECEIVED_AT_ONE_MOMENT = 100;
	private final int MAX_PACKET_SIZE = 64;
	private byte[] receivedDataBuffer = new byte[MAX_PACKET_SIZE * MAX_NR_PACKETS_RECEIVED_AT_ONE_MOMENT];

	private AppData appData;
	private Database database = new Database();

	private int lastLobbyID = 0;
	private ArrayList<Lobby> lobby = new ArrayList<Lobby>();

	public Server(int serverPort) {

		//////////////////// this.serverPort = serverPort;
		this.appData = database.retrieveAppData();
	}

	public void start() {

		try {
			try {
				socket = new DatagramSocket(serverPort, InetAddress.getByName("192.168.0.109"));
			} catch (UnknownHostException e) {

				e.printStackTrace();
			}
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
				processPacket(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void processPacket(DatagramPacket packet) {

		System.out.println("server is processing the packet");
		System.out.println(new String(packet.getData()));
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

				lobby.get(connectionData.availableLobbyIndex).connectPlayer(packet, dataProcessor.getPacketInfo());
				// connect the player to the lobby
			}
		} else if (connectionData.intention == ClientToServerPacketIntention.BUY) {

		} else if (connectionData.intention == ClientToServerPacketIntention.SIGN_IN) {
			if (this.appData.playerExists(connectionData.username, connectionData.password)) {
				// 1 means to the player -> sign in
				send("1 STOP".getBytes(), packet.getAddress(), packet.getPort());
			}
		} else if (connectionData.intention == ClientToServerPacketIntention.SIGN_UP) {

			this.appData.addPlayer(connectionData.username, connectionData.password);
			// 0 means to the player -> sign up
			send("0 STOP".getBytes(), packet.getAddress(), packet.getPort());

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

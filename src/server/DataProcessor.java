package server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

import game.GameType;
import lobby.Lobby;

public class DataProcessor {

	public class ConnectionData {

		private String[] dataForClient = null;
		public String username = "";
		public String password = "";
		public ClientToServerPacketIntention intention = ClientToServerPacketIntention.RESIDUAL;

		public boolean availableLobbyExists = false;
		public int availableLobbyIndex = -1;
		public GameType gameType = GameType.ARCADE; //////////////////////////////////// era initial GameType.RANDOM

		public boolean signIn = false;

		public boolean signUp = false;

		public boolean buy = false;
		public int itemID = -1;

		public ConnectionData(ClientToServerPacketIntention intention, String[] dataForClient,
				boolean availableLobbyExists, GameType gameType, int availableLobbyIndex) { // constructor for joining
																							// lobby ; i think ->
																							// currently unused

			this.intention = intention;
			this.dataForClient = dataForClient;
			this.availableLobbyExists = availableLobbyExists;
			this.gameType = gameType;
			this.availableLobbyIndex = availableLobbyIndex;

			if (this.gameType == GameType.RANDOM) { // if the player selects a random game mode, we will assign a random
													// game mode for it
				this.gameType = getRandomGameType();
			}
		}

		public ConnectionData(ClientToServerPacketIntention intention, boolean signIn, boolean signUp, String username,
				String password) {
			this.signUp=signUp;
			this.signIn = signIn;
			this.intention = intention;
			this.username = username;
			this.password = password;
		}

		// TODO - implement all other constructors for signing in/ signing up, buy, etc

		public String[] getDataForClient() {
			return dataForClient;
		}

		private GameType getRandomGameType() {
			// assigning a random game type for the player

			Random rand = new Random();
			int randomGameType = rand.nextInt(4);

			switch (randomGameType) {

			case 0:
				return GameType.CAPTURE_THE_FLAG;

			case 1:
				return GameType.BATTLE_ROYALE;

			case 2:
				return GameType.ARCADE;

			case 3:
				return GameType.ZOMBIE_INVASION;

			default:
				return GameType.ARCADE;

			}
		}
	}

	public InetAddress clientAddress;
	public int clientPort;
	private String[] packetInfo;
	private ClientToServerPacketIntention packetIntention;

	public DataProcessor(DatagramPacket packet) {

		this.clientAddress = packet.getAddress();
		this.clientPort = packet.getPort();
		this.packetInfo = new String(packet.getData()).split(" ");
		processPacketIntention(this.packetInfo[0]); // packetIntention is always the first integer in the packet
	}

	public ConnectionData processData(ArrayList<Lobby> lobby) {
		// no matter what the intention of the packet is, we will return a packet for
		// the player
		ConnectionData connectionData = null;
		switch (this.packetIntention) {

		case SIGN_UP: // 0
			return new ConnectionData(ClientToServerPacketIntention.SIGN_UP, false, true, packetInfo[1], packetInfo[2]);

		case SIGN_IN: // 1
			return new ConnectionData(ClientToServerPacketIntention.SIGN_IN, true, false, packetInfo[1], packetInfo[2]);

		case BUY: // 2

			return null;

		case CONNECT_TO_RANDOM_LOBBY: // 3

			connectionData = searchForAvailableLobby(GameType.RANDOM, lobby);

			if (connectionData != null) {
				return connectionData;
			}

			// else create a lobby
			return createLobby(GameType.RANDOM);

		case CONNECT_TO_BATTLE_ROYALE_LOBBY: // 4

			connectionData = searchForAvailableLobby(GameType.BATTLE_ROYALE, lobby);

			if (connectionData != null) {
				return connectionData;
			}

			// else create a lobby
			return createLobby(GameType.BATTLE_ROYALE);

		case CONNECT_TO_CAPTURE_THE_FLAG_LOBBY: // 5

			connectionData = searchForAvailableLobby(GameType.CAPTURE_THE_FLAG, lobby);

			if (connectionData != null) {
				return connectionData;
			}

			// else create a lobby
			return createLobby(GameType.CAPTURE_THE_FLAG);

		case CONNECT_TO_ARCADE_LOBBY: // 6

			connectionData = searchForAvailableLobby(GameType.ARCADE, lobby);

			if (connectionData != null) {
				return connectionData;
			}

			// else create a lobby
			return createLobby(GameType.ARCADE);

		case CONNECT_TO_ZOMBIE_INVASION_LOBBY: // 7

			connectionData = searchForAvailableLobby(GameType.ZOMBIE_INVASION, lobby);

			if (connectionData != null) {
				return connectionData;
			}

			// else create a lobby
			return createLobby(GameType.ZOMBIE_INVASION);

		default: // anything else is residual
			return null;
		}
	}

	private void processPacketIntention(String packetIntentionString) {

		int packetIntentionInt = Integer.parseInt(packetIntentionString);

		switch (packetIntentionInt) {

		case 0: // SIGN_UP
			this.packetIntention = ClientToServerPacketIntention.SIGN_UP;
			break;

		case 1: // SIGN_IN
			this.packetIntention = ClientToServerPacketIntention.SIGN_IN;
			break;

		case 2: // BUY
			this.packetIntention = ClientToServerPacketIntention.BUY;
			break;

		case 3: // CONNECT_TO_RANDOM_LOBBY
			this.packetIntention = ClientToServerPacketIntention.CONNECT_TO_RANDOM_LOBBY;
			break;

		case 4: // CONNECT_TO_BATTLE_ROYALE_LOBBY
			this.packetIntention = ClientToServerPacketIntention.CONNECT_TO_BATTLE_ROYALE_LOBBY;
			break;

		case 5: // CONNECT_TO_CAPTURE_THE_FLAG_LOBBY
			this.packetIntention = ClientToServerPacketIntention.CONNECT_TO_CAPTURE_THE_FLAG_LOBBY;
			break;

		case 6: // CONNECT_TO_ARCADE_LOBBY
			this.packetIntention = ClientToServerPacketIntention.CONNECT_TO_ARCADE_LOBBY;
			break;

		case 7: // CONNECT_TO_ZOMBIE_INVASION_LOBBY
			this.packetIntention = ClientToServerPacketIntention.CONNECT_TO_ZOMBIE_INVASION_LOBBY;
			break;

		default: // anything else
			this.packetIntention = ClientToServerPacketIntention.RESIDUAL;
			break;
		}
	}

	private ConnectionData searchForAvailableLobby(GameType gameType, ArrayList<Lobby> lobby) {
		// TODO
		// iterate through all lobbies and find whether is there one lobby which has not
		// started and has the appropriate game type or not

		try {
			for (Lobby currentLobby : lobby) {

				if (!currentLobby.gameHasStarted
						&& (gameType == GameType.RANDOM || currentLobby.getGameType() == gameType)) {

					byte[] data = createConnectionToLobbyDataPacket();

					String[] connectionDataToClient = new String[10];
					connectionDataToClient[0] = new StringBuilder("CONNECTED TO LOBBY ").append(gameType.toString())
							.toString();

					return new ConnectionData(this.packetIntention, connectionDataToClient, true, gameType,
							currentLobby.lobbyID);
				}
			}
		} catch (NullPointerException npe) {
			return null;
		}

		return null;
	}

	private ConnectionData createLobby(GameType gameType) {

		return new ConnectionData(this.packetIntention, null, false, gameType, -1);
	}

	private byte[] createConnectionToLobbyDataPacket() {
		// TODO
		// packet intention
		////////// return (new StringBuilder(""));
		return null;
	}

	public String[] getPacketInfo() {
		return this.packetInfo;
	}

	private void dumpPacket() {

		System.out.println("------------------------------");
		System.out.println("PACKET:");
		System.out.println("\t Host Address -> " + clientAddress + "  :  Client Port  ->  " + clientPort);
		System.out.println();
		System.out.println("\tContents:");
		for (int i = 0; i < packetInfo.length; i++) {
			System.out.println("\t\t" + packetInfo[i]);
		}
		System.out.println("------------------------------");
	}

}

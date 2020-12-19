package server;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class DataProcessor {

	private InetAddress clientAddress;
	private int clientPort;
	private String[] packetInfo;
	private ClientToServerPacketIntention packetIntention;

	public DataProcessor(DatagramPacket packet) {

		this.clientAddress = packet.getAddress();
		this.clientPort = packet.getPort();
		this.packetInfo = new String(packet.getData()).split(" ");
		processPacketIntention(this.packetInfo[0]); // packetIntention is always the first integer in the packet
	}

	public void processData() {

		switch (this.packetIntention) {

		case SIGN_IN: // 0

			break;

		case SIGN_UP: // 1

			break;

		case BUY: // 2

			break;

		case CONNECT_TO_RANDOM_LOBBY: // 3
			connectToRandomLobby();
			break;

		case CONNECT_TO_BATTLE_ROYALE_LOBBY: // 4

			break;

		case CONNECT_TO_CAPTURE_THE_FLAG_LOBBY: // 5

			break;

		case CONNECT_TO_ARCADE_LOBBY: // 6

			break;

		case CONNECT_TO_ZOMBIE_INVASION_LOBBY: // 7

			break;

		default: // anything else

			break;
		}
	}

	private void processPacketIntention(String packetIntentionString) {

		int packetIntentionInt = Integer.parseInt(packetIntentionString);

		switch (packetIntentionInt) {

		case 0: // SIGN_IN
			this.packetIntention = ClientToServerPacketIntention.SIGN_IN;
			break;

		case 1: // SIGN_UP
			this.packetIntention = ClientToServerPacketIntention.SIGN_UP;
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

	private void connectToRandomLobby() {
		/*
		 * within this method, we will:
		 * first -> search for an available lobby and return lobby's address and port
		 * second -> if there is no available lobby, create a lobby
		 */
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

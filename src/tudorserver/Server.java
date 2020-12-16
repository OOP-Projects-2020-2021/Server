package tudorserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import game.DataProcessor;

public class Server { // 1 server, 1 lobby

	private int serverPort;
	private Thread listenThread;
	private volatile boolean listening = false;
	private DatagramSocket socket;

	private int nrPlayersInLobby = 10;
	private final int MAX_PACKET_SIZE = 1024;
	private byte[] receivedDataBuffer = new byte[MAX_PACKET_SIZE * nrPlayersInLobby];
	
	private Set <ServerClient> clients = new HashSet<ServerClient>();
	// DataProcessor dataProcessor=new DataProcessor();
	
	public Server(int serverPort) {

		this.serverPort = serverPort;
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
		
		byte[] data = packet.getData();
		
		DataProcessor dataProcessor=new DataProcessor(data);
		dataProcessor.processData();
		
		InetAddress address=packet.getAddress();
		int clientPort = packet.getPort();
		
		// TODO send these information to all other players
		
		dumpPacket(packet);
	}

	private void dumpPacket(DatagramPacket packet) {

		byte[] data = packet.getData();
		InetAddress address=packet.getAddress();
		int clientPort = packet.getPort();
		
//		System.out.println("------------------------------");
//		System.out.println("PACKET:");
//		System.out.println("\t Host Address -> " + address.getHostAddress() + "  :  Client Port  ->  " + clientPort);
//		System.out.println();
//		System.out.println("\tContents:");
//		System.out.println("\t\t" + new String(data));
//		System.out.println("------------------------------");
		
		// System.out.println("\t\t" + new String(data));	
		System.out.println("\t\t" + data[2]+" " + data[1] +" "+ data[0]);	
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

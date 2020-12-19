package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import database.AppData;
import database.Database;

public class Server {

	private int serverPort;
	private Thread listenThread;
	private volatile boolean listening = false;
	private DatagramSocket socket;

	private final int MAX_NR_PACKETS_RECEIVED_AT_ONE_MOMENT = 100;
	private final int MAX_PACKET_SIZE = 64;
	private byte[] receivedDataBuffer = new byte[MAX_PACKET_SIZE * MAX_NR_PACKETS_RECEIVED_AT_ONE_MOMENT];
	
	private AppData appData;

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

		DataProcessor dataProcessor = new DataProcessor(packet);
		dataProcessor.processData();
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

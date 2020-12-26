package tudorserver;

import java.net.InetAddress;
import java.net.UnknownHostException;

import game.GameType;
import lobby.Lobby;
import server.Server;

public class TudorServer {

	public TudorServer() {
		Server server = new Server(8192); // http://127.0.0.1 (localhost) /// 8192
		server.start();

		InetAddress address = null;
		try {
			address = InetAddress.getByName("192.168.0.109");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// int port = 8192;
		// server.send(new byte[] { 0, 1, 2 }, address, port);
	}

	public TudorServer(int a) {

		Lobby lobby = new Lobby(GameType.ARCADE, 1);
		lobby.start();
//		if (lobby.initLobbyConnection()) {
//
//			System.out.println("Lobby created successfully");
//			lobby.start();
//		} else {
//			System.out.println("error");
//		}
	}

	public static void main(String[] args) {
		new TudorServer();
	}
}

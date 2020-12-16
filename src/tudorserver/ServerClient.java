package tudorserver;

import java.net.InetAddress;

import database.AppInfo;

public class ServerClient {

	private int playerID;
	private String playerName;
	private int playerX, playerY;

	private InetAddress clientAddress;
	private final int clientPort;
	public OnlineStatus status = OnlineStatus.ONLINE; // is connected
	public OnlineStatus onlineStatus = OnlineStatus.IN_GAME;
	public long timeItEnteredLobby;

	public ServerClient(int playerID, InetAddress clientAddress, int clientPort) {

		this.playerID = playerID;
		this.playerName = AppInfo.getPlayerNameByID(playerID);
		this.playerX = this.playerY = 100;

		this.clientAddress = clientAddress;
		this.clientPort = clientPort;
		this.status = OnlineStatus.ONLINE;
		this.onlineStatus = OnlineStatus.IN_GAME;
	}

	public void setOnlineStatus(OnlineStatus status) {
		this.onlineStatus = status;
	}

	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public int getClientPort() {
		return clientPort;
	}

	public int getPlayerID() {
		return playerID;
	}

	public String getPlayerName() {
		return playerName;
	}
}

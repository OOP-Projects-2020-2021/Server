package tudorserver;

import java.net.InetAddress;

import database.AppData;

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
		try {

			///////////// this.playerName = AppData.getPlayerNameByID(playerID);
			this.playerName = "Tudoreeeeel";
			
		} catch (NullPointerException npe) { // !!! it should not get here. this is just for development

			this.playerName = "dummy name";
		}
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

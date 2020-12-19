package database;

import java.beans.Statement;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import game.Player;

public class AppData {
	
	public Map<Integer, Player> players = new HashMap<Integer, Player>();
	public Connection connection;
	public Statement statement;

	public AppData() {
		
	}

	public String getPlayerNameByID(int playerID) {
		
		return (players.get(playerID).playerName);
	}
}

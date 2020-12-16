package database;

import java.util.HashMap;
import java.util.Map;

import game.Player;

public class AppInfo {
	
	private static Map<Integer, Player> players = new HashMap<Integer, Player>();

	public AppInfo() {
		
	}

	public static String getPlayerNameByID(int playerID) {
		
		return (players.get(playerID).playerName);
	}
	
}

package database;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.omg.CORBA.portable.ValueBase;

import server.DataProcessor.ConnectionData;

public class AppData {

	public Map<Integer, Player> players = new HashMap<Integer, Player>();
	public Connection connection;
	public Statement statement;

	public AppData(Connection connection, Statement statement, Map<Integer, Player> players) {
		this.connection = connection;
		this.statement = statement;
		this.players = players;
	}

	public String getPlayerNameByID(int playerID) {

		return (players.get(playerID).getUsername());
	}

	public boolean playerExists(String username, String password) {

		Iterator hmIterator = players.entrySet().iterator();

		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry) hmIterator.next();
			if (((Player) mapElement.getValue()).getUsername().equals(username)
					&& ((Player) mapElement.getValue()).getPassword().equals(password)) {
				return true;
			}
		}
		return false;
	}

	public void addPlayer(String username, String password) {

		Random rand = new Random();
		int id = rand.nextInt(Integer.MAX_VALUE);

		this.players.put(id, new Player(username, password, id));

		try {
			this.statement.executeUpdate(
					new StringBuilder("INSERT INTO swordingarounddata.user (Id,Username,Password) VALUES (").append(id)
							.append(",").append('"').append(username).append('"').append(",").append('"')
							.append(password).append('"').append(")").toString());

		} catch (SQLException sqlex) {

		}
	}
}

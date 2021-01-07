package database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Database {

	private CreateQuery queryMaker = new CreateQuery();
	private Connection connect;
	private Statement statement;

	public Database() {
		// TODO
		/*
		 * connect to database upon external calling public AppData retrieveAppData(),
		 * make several SELECTs from database and encapsulate every result in an AppData
		 * object moreover, pass the Connection and Statement objects into the AppData
		 * object, since we will need to further use INSERT and SELECT queries outside
		 * this class ex: if somebody makes a purchase, we will insert the
		 * skin/character/map into the database ex: if somebody signs up, we will
		 * introduce it into the database
		 */

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");// Conexiune
			String url = "jdbc:mysql://localhost:3306/swordingarounddata";// Link la baza de date, export jar extern
			String user = "root";
			String password = "";

			this.connect = DriverManager.getConnection(url, user, password);
			this.statement = connect.createStatement();

		} catch (Exception ex) {
			String className = this.getClass().getSimpleName();
			System.out.println(className + " DatabaseConnect " + " : " + ex);
			javax.swing.JOptionPane.showMessageDialog(null, "An error occured. App doesn't work");
		}
	}

	public boolean isNameValid(String username) {
		try {
			System.out.println(statement == null);
			statement.executeQuery(queryMaker.verifyIfValidNameQuery(username));
			if (statement.getResultSet().next())
				return false;
			else
				return true;
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return true;
	}

	public boolean signInValid(String username, String password) {
		try {
			statement.executeQuery(queryMaker.verifyIfUserQuery(username, password));
			if (statement.getResultSet().next())
				return false;
			else
				return true;
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return true;
	}

	public boolean insertData(String username, String password) { // Pentru a atentiona user-ul ca datele lui nu sunt
																	// bune metoda este boolean.
		try {
			if (signInValid(username, password)) {
				statement.executeUpdate(queryMaker.insertSignUp(username, password));
				return true;
			} else
				return false;
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return false;

	}

	public AppData retrieveAppData() {
		// Returnez un obiect al clasei app data cu toti playeri+connection and statment

		Map<Integer, Player> players = new HashMap<Integer, Player>();
		try {
			ResultSet usersRetrieved = statement.executeQuery(queryMaker.retrieveAllUsers());
			
			while (usersRetrieved.next()) {
				players.put(usersRetrieved.getInt("Id"), new Player(usersRetrieved.getString("Username"),
						usersRetrieved.getString("Password"), usersRetrieved.getInt("Id")));

			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		AppData users = new AppData(connect, statement, players);
		return users;
	}

}

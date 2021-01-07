package database;

public class CreateQuery {
	public CreateQuery() {

	}

	public String verifyIfUserQuery(String name, String password) { // If it will be 0 results => the username or the
																	// password are wrong;
		String sql = "SELECT Id FROM user WHERE Username LIKE '" + name + "' AND password LIKE '" + password + "';";
		return sql;
	}

	public String verifyIfValidNameQuery(String name) { // If it will be 0 results => the name is valid;
		String sql = "SELECT Id FROM user WHERE Username LIKE '" + name + "';";
		return sql;
	}

	public String getUsernameAndPasswordQuery(int index) {
		String sql = "SELECT Usename, Password FROM user WHERE Id = " + index + ";";
		return sql;
	}

	public String getIndexQuery(String name) {
		String sql = "SELECT Id FROM user WHERE Username LIKE'" + name + "';";
		return sql;
	}

	public String insertSignUp(String name, String password) {
		String sql = "INSERT INTO user (Id, Username, Password) VALUES (NULL, '" + name + "','" + password + "');";
		return sql;
	}

	public String retrieveAllUsers() {
		String sql = "SELECT * FROM user";
		return sql;
	}

}

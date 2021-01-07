package database;

public class Player {

	private String username = "";
	private String password = "";
	private int id = -1;

	public Player(String name, String password, int id) {
		this.username = name;
		this.password = password;
		this.id = id;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public int getID() {
		return this.id;
	}
}
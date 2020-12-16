package game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import Control.Keyboard;
import game_package.GameType;
import tudorserver.OnlineStatus;

public class Player {

	private int playerId;
	public String playerName;
	private String password;
	private int x;
	private int y;
	public int hoursPlayed;
	public OnlineStatus status;
	private ArrayList<Skin> skin;
	private Skin activeSkin;
	private Set<Player> friendsList;
	public Set<Pair<GameType, Statistics>> statistics; // for every gametype, you have a statistic
	public Keyboard gameplayKeyboardMode;

	public Player() {
		// TODO Auto-generated constructor stub
		this.statistics = new HashSet<Pair<GameType, Statistics>>();

		for (GameType gameType : GameType.values()) {
			
			this.statistics.add(new Pair(gameType,new Statistics()));
		}
	}

	public void changeName(String newName) {
		this.playerName = newName;
	}

	public Skin getActiveSkin() {
		return activeSkin;
	}

	public void addFriend(Player player) {
		friendsList.add(player);
	}

	public void removeFriend(Player player) {
		friendsList.remove(player);
	}
}

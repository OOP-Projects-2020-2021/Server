package game;

import tudorserver.GameType;

public abstract class Game {

	public GameType gameType;

	public abstract void processReceivedData(byte [] data);
	public abstract boolean gameHasEnded();
	public abstract Statistics retrieveEndGameStatistics();

	public Game(GameType gameType) {
		this.gameType = gameType;
	}
}
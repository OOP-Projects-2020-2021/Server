package game;

public abstract class Game {

	public GameType gameType;

	public abstract void processReceivedData(String [] data);
	public abstract boolean gameHasEnded();
	public abstract Statistics retrieveEndGameStatistics();

	public Game(GameType gameType) {
		this.gameType = gameType;
	}
}
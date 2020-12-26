package game;

public class ArcadeGameProcesser extends Game {

	private int team0Kills = 0;
	private int team1Kills = 0;
	private final int OBJECTIVE_NR_KILLS = 50;
	Statistics statistics = new ArcadeStatistics();

	public ArcadeGameProcesser(GameType gameType) {

		super(gameType);
	}

	@Override
	public void processReceivedData(String[] data) {
		// TODO Auto-generated method stub
		// this implementation is just for orientation !!!!!!
		int playersTeam = getTeamFromData(data.toString());

		if (playersTeam == 0) {
			
			if(itKilledSomething(data.toString())) {
				team0Kills++;
			}

		} else {
			
			if(itKilledSomething(data.toString())) {
				team1Kills++;
			}
		}
	}

	private boolean itKilledSomething(String data) {
		// this method detects if, according to the data sent by the player, the player killed someone
		// TODO Auto-generated method stub
		return false;
	}

	private int getTeamFromData(String data) {

		// for an arcade game, we must identify in which team does the player compete
		// thus, for an arcade game, the first information sent by the player will be
		// its team (0 or 1)

		return (Integer.parseInt(data.substring(0, data.indexOf(" "))));
	}

	@Override
	public boolean gameHasEnded() {
		
		return (this.team0Kills == OBJECTIVE_NR_KILLS || this.team1Kills == OBJECTIVE_NR_KILLS);
	}

	@Override
	public Statistics retrieveEndGameStatistics() {
		// TODO Auto-generated method stub
		System.out.println("Arcade game ended");
		return null;
	}

}

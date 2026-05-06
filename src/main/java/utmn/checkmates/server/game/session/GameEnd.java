package utmn.checkmates.server.game.session;

public class GameEnd {
    private final GameEndType type;
    private final int winner;

    public GameEnd(GameEndType type, int winnerColor) {
        this.type = type;
        this.winner = winnerColor;
    }

    public GameEndType getType() {
        return type;
    }

    public int getWinnerIdx() {
        return (winner < 0 || winner > 1) ? -1 : winner;
    }
}

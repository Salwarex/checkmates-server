package utmn.checkmates.server.game.session;

import utmn.checkmates.server.utility.logger.Logger;

import java.util.UUID;

public class Player {
    private String playerName;
    private boolean ready;
    private int color = -1;

    public Player(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        Logger.log("Player", "setReady", "Изменен статус готовности на %b".formatted(ready));
        this.ready = ready;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}

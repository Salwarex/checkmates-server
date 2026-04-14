package utmn.checkmates.server.network.packet.input;

public class ServerConnectionPacket extends InputPacket implements SessionPacket{
    private String playerName;

    public ServerConnectionPacket(int sessionId, String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public int getSessionId() {
        return 0;
    }

    @Override
    public void setSessionId(int id) {

    }
}

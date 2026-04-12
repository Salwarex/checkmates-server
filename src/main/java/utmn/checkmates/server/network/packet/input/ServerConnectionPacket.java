package utmn.checkmates.server.network.packet.input;

public class ServerConnectionPacket extends SessionPacket{
    private String playerName;

    public ServerConnectionPacket(int sessionId, String playerName) {
        super(sessionId);
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

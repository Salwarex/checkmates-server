package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class ServerConnectionPacket extends InputPacket implements SessionPacket{
    private int sessionId;
    private String playerName;

    public ServerConnectionPacket(InetAddress sourceAddress, int sessionId, String playerName) {
        super(sourceAddress);
        this.sessionId = sessionId;
        this.playerName = playerName;
    }

    public ServerConnectionPacket(InetAddress sourceAddress) {
        super(sourceAddress);
    }

    public ServerConnectionPacket() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(int id) {
        this.sessionId = id;
    }
}

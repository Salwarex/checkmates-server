package utmn.checkmates.server.network.packet.input;

public abstract class SessionPacket implements InputPacket{
    private int sessionId;

    public SessionPacket(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}

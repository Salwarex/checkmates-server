package utmn.checkmates.server.network.packet.input;

public class PlayerUpdatePacket extends SessionPacket{
    public PlayerUpdatePacket(int sessionId) {
        super(sessionId);
    }
}

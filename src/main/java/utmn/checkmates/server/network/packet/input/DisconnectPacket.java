package utmn.checkmates.server.network.packet.input;

public class DisconnectPacket extends SessionPacket{
    public DisconnectPacket(int sessionId) {
        super(sessionId);
    }
}

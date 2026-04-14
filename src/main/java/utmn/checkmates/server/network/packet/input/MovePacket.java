package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class MovePacket extends ClientSessionImplPacket{
    private byte from;
    private byte to;

    public MovePacket(InetAddress sourceAddress, int sessionId, int clientId, byte from, byte to) {
        super(sourceAddress, sessionId, clientId);
        this.from = from;
        this.to = to;
    }

    public MovePacket() {
    }

    public byte getFrom() {
        return from;
    }

    public void setFrom(byte from) {
        this.from = from;
    }

    public byte getTo() {
        return to;
    }

    public void setTo(byte to) {
        this.to = to;
    }
}

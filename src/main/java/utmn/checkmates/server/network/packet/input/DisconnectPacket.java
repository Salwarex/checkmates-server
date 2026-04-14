package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class DisconnectPacket extends ClientSessionImplPacket{
    public DisconnectPacket(InetAddress sourceAddress, int sessionId, int clientId) {
        super(sourceAddress, sessionId, clientId);
    }

    public DisconnectPacket() {
    }
}

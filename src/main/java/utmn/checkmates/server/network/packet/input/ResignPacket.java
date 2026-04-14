package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class ResignPacket extends ClientSessionImplPacket{
    public ResignPacket(InetAddress sourceAddress, int sessionId, int clientId) {
        super(sourceAddress, sessionId, clientId);
    }

    public ResignPacket() {
    }
}

package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class DrawOfferPacket extends ClientSessionImplPacket{
    public DrawOfferPacket(InetAddress sourceAddress, int sessionId, int clientId) {
        super(sourceAddress, sessionId, clientId);
    }

    public DrawOfferPacket() {
    }
}

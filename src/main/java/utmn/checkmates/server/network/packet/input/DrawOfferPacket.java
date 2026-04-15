package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public class DrawOfferPacket extends ClientSessionImplPacket{
    public DrawOfferPacket(Socket sourceAddress, int sessionId, int clientId) {
        super(sourceAddress, sessionId, clientId);
    }

    public DrawOfferPacket() {
    }
}

package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public class ResignPacket extends ClientSessionImplPacket{
    public ResignPacket(Socket sourceAddress, int sessionId, int clientId) {
        super(sourceAddress, sessionId, clientId);
    }

    public ResignPacket() {
    }
}

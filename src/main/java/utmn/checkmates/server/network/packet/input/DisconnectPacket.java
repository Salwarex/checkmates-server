package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public class DisconnectPacket extends ClientSessionImplPacket{
    public DisconnectPacket(Socket sourceAddress, int sessionId, int clientId) {
        super(sourceAddress, sessionId, clientId);
    }

    public DisconnectPacket() {
    }
}

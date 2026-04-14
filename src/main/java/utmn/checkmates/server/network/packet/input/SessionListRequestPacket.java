package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class SessionListRequestPacket extends InputPacket{
    public SessionListRequestPacket(InetAddress sourceAddress) {
        super(sourceAddress);
    }

    public SessionListRequestPacket() {
    }
}

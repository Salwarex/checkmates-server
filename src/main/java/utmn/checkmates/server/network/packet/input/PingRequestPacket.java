package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class PingRequestPacket extends InputPacket{
    public PingRequestPacket(InetAddress sourceAddress) {
        super(sourceAddress);
    }

    public PingRequestPacket() {
    }
}

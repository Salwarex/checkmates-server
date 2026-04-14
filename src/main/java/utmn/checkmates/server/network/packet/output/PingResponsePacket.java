package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.util.List;

public class PingResponsePacket extends OutputPacket{
    public PingResponsePacket(List<InetAddress> destinationAddress) {
        super(destinationAddress);
    }

    public PingResponsePacket() {
    }
}

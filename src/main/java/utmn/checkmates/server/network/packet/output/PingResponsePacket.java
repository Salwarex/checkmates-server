package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class PingResponsePacket extends OutputPacket{
    public PingResponsePacket(List<Socket> destinationAddress) {
        super(destinationAddress);
    }

    public PingResponsePacket() {
    }
}

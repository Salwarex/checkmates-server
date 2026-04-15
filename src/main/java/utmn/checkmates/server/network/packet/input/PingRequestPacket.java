package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public class PingRequestPacket extends InputPacket{
    public PingRequestPacket(Socket sourceAddress) {
        super(sourceAddress);
    }

    public PingRequestPacket() {
    }
}

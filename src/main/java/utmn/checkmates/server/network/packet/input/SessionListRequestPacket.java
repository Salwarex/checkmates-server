package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public class SessionListRequestPacket extends InputPacket{
    public SessionListRequestPacket(Socket sourceAddress) {
        super(sourceAddress);
    }

    public SessionListRequestPacket() {
    }
}

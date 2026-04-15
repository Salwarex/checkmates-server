package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public class CreateSessionPacket extends InputPacket{
    public CreateSessionPacket(Socket sourceAddress) {
        super(sourceAddress);
    }

    public CreateSessionPacket() {
    }
}

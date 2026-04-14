package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class CreateSessionPacket extends InputPacket{
    public CreateSessionPacket(InetAddress sourceAddress) {
        super(sourceAddress);
    }

    public CreateSessionPacket() {
    }
}

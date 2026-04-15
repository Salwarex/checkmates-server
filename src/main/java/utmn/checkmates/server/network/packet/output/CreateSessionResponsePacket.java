package utmn.checkmates.server.network.packet.output;

import utmn.checkmates.server.game.session.SessionDto;

import java.net.InetAddress;
import java.util.List;

public class CreateSessionResponsePacket extends OutputPacket{
    private SessionDto session;

    public CreateSessionResponsePacket(List<InetAddress> destinationAddress, SessionDto session) {
        super(destinationAddress);
        this.session = session;
    }

    public CreateSessionResponsePacket() {
    }

    public SessionDto getSession() {
        return session;
    }

    public void setSession(SessionDto session) {
        this.session = session;
    }
}

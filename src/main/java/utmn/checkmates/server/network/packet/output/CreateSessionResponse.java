package utmn.checkmates.server.network.packet.output;

import utmn.checkmates.server.game.session.SessionDto;

import java.net.InetAddress;
import java.util.List;

public class CreateSessionResponse extends OutputPacket{
    private SessionDto session;

    public CreateSessionResponse(List<InetAddress> destinationAddress, SessionDto session) {
        super(destinationAddress);
        this.session = session;
    }

    public CreateSessionResponse() {
    }

    public SessionDto getSession() {
        return session;
    }

    public void setSession(SessionDto session) {
        this.session = session;
    }
}

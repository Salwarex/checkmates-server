package utmn.checkmates.server.network.packet.output;

import utmn.checkmates.server.game.session.SessionDto;

import java.net.InetAddress;
import java.util.List;

public class SessionListResponsePacket extends OutputPacket{
    private List<SessionDto> sessions;

    public SessionListResponsePacket(List<InetAddress> destinationAddresses, List<SessionDto> sessions) {
        super(destinationAddresses);
        this.sessions = sessions;
    }

    public SessionListResponsePacket() {
    }

    public List<SessionDto> getSessions() {
        return sessions;
    }

    public void setSessions(List<SessionDto> sessions) {
        this.sessions = sessions;
    }
}

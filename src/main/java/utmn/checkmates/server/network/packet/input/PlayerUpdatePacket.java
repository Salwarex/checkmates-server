package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class PlayerUpdatePacket extends ClientSessionImplPacket{
    private boolean ready;

    public PlayerUpdatePacket(InetAddress sourceAddress, int sessionId, int clientId, boolean ready) {
        super(sourceAddress, sessionId, clientId);
        this.ready = ready;
    }

    public PlayerUpdatePacket() {
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}

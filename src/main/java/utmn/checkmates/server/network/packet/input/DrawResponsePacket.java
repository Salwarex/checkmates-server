package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;

public class DrawResponsePacket extends ClientSessionImplPacket{
    private boolean agree;

    public DrawResponsePacket(InetAddress sourceAddress, int sessionId, int clientId, boolean agree) {
        super(sourceAddress, sessionId, clientId);
        this.agree = agree;
    }

    public DrawResponsePacket() {
    }

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
    }
}

package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public class DrawResponsePacket extends ClientSessionImplPacket{
    private boolean agree;

    public DrawResponsePacket(Socket sourceAddress, int sessionId, int clientId, boolean agree) {
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

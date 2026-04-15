package utmn.checkmates.server.network.packet.input;

import java.net.InetAddress;
import java.net.Socket;

public abstract class ClientSessionImplPacket extends InputPacket implements SessionPacket, ClientRecognizePacket {
    private int sessionId;
    private int clientId;

    public ClientSessionImplPacket(Socket sourceAddress, int sessionId, int clientId) {
        super(sourceAddress);
        this.sessionId = sessionId;
        this.clientId = clientId;
    }

    public ClientSessionImplPacket() {
    }

    @Override
    public int getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(int id) {
        this.clientId = id;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(int id) {
        this.sessionId = id;
    }
}

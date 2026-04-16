package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class ClientConnectionPacket extends OutputPacket{
    private int clientId;
    private byte color;

    public ClientConnectionPacket(List<Socket> destinationAddress, int clientId, byte color) {
        super(destinationAddress);
        this.clientId = clientId;
        this.color = color;
    }

    public ClientConnectionPacket() {
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public byte getColor() {
        return color;
    }

    public void setColor(byte color) {
        this.color = color;
    }
}

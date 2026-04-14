package utmn.checkmates.server.network.packet;

import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketAddress;

public abstract class Packet {
    private SocketAddress senderAddress;
    private SocketAddress destinationAddress;

    public Packet(SocketAddress senderAddress, SocketAddress destinationAddress) {
        this.senderAddress = senderAddress;
        this.destinationAddress = destinationAddress;
    }

    public SocketAddress getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(SocketAddress senderAddress) {
        this.senderAddress = senderAddress;
    }

    public SocketAddress getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(SocketAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
}

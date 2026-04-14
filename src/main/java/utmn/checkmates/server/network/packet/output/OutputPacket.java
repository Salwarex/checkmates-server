package utmn.checkmates.server.network.packet.output;

import com.google.gson.annotations.Expose;
import utmn.checkmates.server.network.packet.Packet;

import java.net.InetAddress;
import java.util.List;

public abstract class OutputPacket extends Packet {
    @Expose(serialize = false, deserialize = false)
    private List<InetAddress> destinationAddresses;

    public OutputPacket(List<InetAddress> destinationAddresses) {
        this.destinationAddresses = destinationAddresses;
    }

    public OutputPacket() {
    }

    public List<InetAddress> getDestinationAddresses() {
        return destinationAddresses;
    }

    public void setDestinationAddresses(List<InetAddress> destinationAddresses) {
        this.destinationAddresses = destinationAddresses;
    }
}

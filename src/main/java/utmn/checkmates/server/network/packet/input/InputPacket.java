package utmn.checkmates.server.network.packet.input;

import com.google.gson.annotations.Expose;
import utmn.checkmates.server.network.packet.Packet;

import java.net.InetAddress;

public abstract class InputPacket extends Packet {
    //@Expose(serialize = false, deserialize = false)
    private transient InetAddress sourceAddress;

    public InputPacket(InetAddress sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public InputPacket() {
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(InetAddress sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
}

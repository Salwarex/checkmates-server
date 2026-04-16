package utmn.checkmates.server.network.packet.output;

import utmn.checkmates.server.network.packet.Packet;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public abstract class OutputPacket extends Packet {
    //@Expose(serialize = false, deserialize = false)
    private transient List<Socket> dest;

    public OutputPacket(List<Socket> dest) {
        this.dest = dest;
    }

    public OutputPacket() {
    }

    public List<Socket> getDest() {
        return dest;
    }

    public void setDest(List<Socket> dest) {
        this.dest = dest;
    }
}

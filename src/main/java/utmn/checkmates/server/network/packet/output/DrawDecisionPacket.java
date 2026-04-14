package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.util.List;

public class DrawDecisionPacket extends OutputPacket{
    private int type;

    public DrawDecisionPacket(List<InetAddress> destinationAddress, int type) {
        super(destinationAddress);
        this.type = type;
    }

    public DrawDecisionPacket() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

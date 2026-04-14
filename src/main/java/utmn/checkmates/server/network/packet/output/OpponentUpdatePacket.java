package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.util.List;

public class OpponentUpdatePacket extends OutputPacket{
    private boolean ready;
    private boolean online;

    public OpponentUpdatePacket(List<InetAddress> destinationAddress, boolean ready, boolean online) {
        super(destinationAddress);
        this.ready = ready;
        this.online = online;
    }

    public OpponentUpdatePacket() {
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}

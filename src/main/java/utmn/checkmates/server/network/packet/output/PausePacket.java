package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class PausePacket extends OutputPacket{
    private boolean status;

    public PausePacket(List<Socket> destinationAddress, boolean status) {
        super(destinationAddress);
        this.status = status;
    }

    public PausePacket() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}

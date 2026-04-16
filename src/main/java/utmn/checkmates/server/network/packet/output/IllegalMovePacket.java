package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class IllegalMovePacket extends OutputPacket{
    private String reason;

    public IllegalMovePacket(List<Socket> destinationAddress, String reason) {
        super(destinationAddress);
        this.reason = reason;
    }

    public IllegalMovePacket() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

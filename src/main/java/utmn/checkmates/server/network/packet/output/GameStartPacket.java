package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class GameStartPacket extends OutputPacket{
    private int time;
    public GameStartPacket(List<Socket> destinationAddress, int time) {
        super(destinationAddress);
        this.time = time;
    }

    public GameStartPacket() {
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}

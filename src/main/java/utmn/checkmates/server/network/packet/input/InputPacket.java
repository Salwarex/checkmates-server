package utmn.checkmates.server.network.packet.input;

import utmn.checkmates.server.network.packet.Packet;

import java.net.InetAddress;
import java.net.Socket;

public abstract class InputPacket extends Packet {
    //@Expose(serialize = false, deserialize = false)
    private transient Socket socket;

    public InputPacket(Socket socket) {
        this.socket = socket;
    }

    public InputPacket() {
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

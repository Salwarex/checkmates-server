package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.util.List;

public class ExceptionPacket extends OutputPacket{
    private int code;
    private String text;

    public ExceptionPacket(List<InetAddress> destinationAddress, int code, String text) {
        super(destinationAddress);
        this.code = code;
        this.text = text;
    }

    public ExceptionPacket() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

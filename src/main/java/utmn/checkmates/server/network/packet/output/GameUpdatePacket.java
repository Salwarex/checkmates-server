package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class GameUpdatePacket extends OutputPacket{
    private String fen;
    private boolean check;
    private long timeWhite;
    private long timeBlack;

    public GameUpdatePacket(List<Socket> destinationAddress, String fen, boolean check, long timeWhite, long timeBlack) {
        super(destinationAddress);
        this.fen = fen;
        this.check = check;
        this.timeWhite = timeWhite;
        this.timeBlack = timeBlack;
    }

    public GameUpdatePacket() {
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public long getTimeWhite() {
        return timeWhite;
    }

    public void setTimeWhite(long timeWhite) {
        this.timeWhite = timeWhite;
    }

    public long getTimeBlack() {
        return timeBlack;
    }

    public void setTimeBlack(long timeBlack) {
        this.timeBlack = timeBlack;
    }
}

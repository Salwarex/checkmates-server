package utmn.checkmates.server.network.packet.output;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class GameOverPacket extends OutputPacket{
    private int result;
    private int winner;

    public GameOverPacket(List<Socket> destinationAddress, int result, int winner) {
        super(destinationAddress);
        this.result = result;
        this.winner = winner;
    }

    public GameOverPacket() {
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }
}

package utmn.checkmates.server.network.packet.input;

public class DrawResponsePacket extends InputPacket implements ClientRecognizePacket{
    public DrawResponsePacket(int sessionId) {

    }

    @Override
    public int getClientId() {
        return 0;
    }

    @Override
    public void setClientId(int id) {

    }

    @Override
    public int getSessionId() {
        return 0;
    }

    @Override
    public void setSessionId(int id) {

    }
}

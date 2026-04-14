package utmn.checkmates.server.network.packet.input;

public class ResignPacket extends InputPacket implements ClientRecognizePacket{
    public ResignPacket(int sessionId) {
    }

    @Override
    public int getSessionId() {
        return 0;
    }

    @Override
    public void setSessionId(int id) {

    }

    @Override
    public int getClientId() {
        return 0;
    }

    @Override
    public void setClientId(int id) {

    }
}

package utmn.checkmates.server.network.packet.input;

public interface ClientRecognizePacket extends SessionPacket{
    int getClientId();
    void setClientId(int id);
}

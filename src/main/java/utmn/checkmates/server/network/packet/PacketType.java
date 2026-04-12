package utmn.checkmates.server.network.packet;

import utmn.checkmates.server.network.packet.input.*;
import utmn.checkmates.server.network.packet.output.*;

public enum PacketType {
    S0000(PingRequestPacket.class),
    S0001(ServerConnectionPacket.class),
    S0010(PlayerUpdatePacket.class),
    S0011(null),
    S0100(MovePacket.class),
    S0101(ResignPacket.class),
    S0110(DrawOfferPacket.class),
    S0111(DrawResponsePacket.class),
    S1000(DisconnectPacket.class),
    S1001(SessionListRequestPacket.class),
    S1010(CreateSessionPacket.class),
    S1011(null),
    S1100(null),
    S1101(null),
    S1110(null),
    S1111(null),
    C0000(PingResponsePacket.class),
    C0001(ClientConnectionPacket.class),
    C0010(OpponentUpdatePacket.class),
    C0011(GameStartPacket.class),
    C0100(GameUpdatePacket.class),
    C0101(IllegalMovePacket.class),
    C0110(DrawDecisionPacket.class),
    C0111(GameOverPacket.class),
    C1000(PausePacket.class),
    C1001(SessionListResponsePacket.class),
    C1010(null),
    C1011(null),
    C1100(null),
    C1101(null),
    C1110(null),
    C1111(ExceptionPacket.class);

    private final Class<?> clazz;

    PacketType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public static PacketType get(boolean input, Byte value){
        if(value > 0b1111 || value < 0b0000) throw new IllegalArgumentException("value не может выходить за [0, 15]");
        String name = (input ? "S" : "C") + value;
        return PacketType.valueOf(name);
    }
}

package utmn.checkmates.server.network.packet;

import utmn.checkmates.server.network.packet.input.*;
import utmn.checkmates.server.network.packet.output.*;

public enum PacketType {

    //ОБРАЩЕНИЯ К СЕРВЕРУ (ОБРАБАТЫВАЕМЫЕ)
    S0000((byte) 0b0000, PingRequestPacket.class, packet -> {
        if(!(packet instanceof PingRequestPacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S0001((byte) 0b0001, ServerConnectionPacket.class, packet -> {
        if(!(packet instanceof ServerConnectionPacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S0010((byte) 0b0010, PlayerUpdatePacket.class, packet -> {
        if(!(packet instanceof PlayerUpdatePacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S0011((byte) 0b0011,null, packet -> {return null;}),
    S0100((byte) 0b0100,MovePacket.class, packet -> {
        if(!(packet instanceof MovePacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S0101((byte) 0b0101,ResignPacket.class, packet -> {
        if(!(packet instanceof ResignPacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S0110((byte) 0b0110,DrawOfferPacket.class, packet -> {
        if(!(packet instanceof DrawOfferPacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S0111((byte) 0b0111,DrawResponsePacket.class, packet -> {
        if(!(packet instanceof DrawResponsePacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S1000((byte) 0b1000,DisconnectPacket.class, packet -> {
        if(!(packet instanceof DisconnectPacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S1001((byte) 0b1001,SessionListRequestPacket.class, packet -> {
        if(!(packet instanceof SessionListRequestPacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S1010((byte) 0b1010,CreateSessionPacket.class, packet -> {
        if(!(packet instanceof CreateSessionPacket))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return null;
    }),
    S1011((byte) 0b1011,null, packet -> {return null;}),
    S1100((byte) 0b1100,null, packet -> {return null;}),
    S1101((byte) 0b1101,null, packet -> {return null;}),
    S1110((byte) 0b1110,null, packet -> {return null;}),
    S1111((byte) 0b1111,null, packet -> {return null;}),

    //ОБРАЩЕНИЯ К КЛИЕНТУ
    C0000((byte) 0b0000,PingResponsePacket.class, packet -> {return null;}),
    C0001((byte) 0b0001,ClientConnectionPacket.class, packet -> {return null;}),
    C0010((byte) 0b0010,OpponentUpdatePacket.class, packet -> {return null;}),
    C0011((byte) 0b0011,GameStartPacket.class, packet -> {return null;}),
    C0100((byte) 0b0100,GameUpdatePacket.class, packet -> {return null;}),
    C0101((byte) 0b0101,IllegalMovePacket.class, packet -> {return null;}),
    C0110((byte) 0b0110,DrawDecisionPacket.class, packet -> {return null;}),
    C0111((byte) 0b0111,GameOverPacket.class, packet -> {return null;}),
    C1000((byte) 0b1000,PausePacket.class, packet -> {return null;}),
    C1001((byte) 0b1001,SessionListResponsePacket.class, packet -> {return null;}),
    C1010((byte) 0b1010,null, packet -> {return null;}),
    C1011((byte) 0b1011,null, packet -> {return null;}),
    C1100((byte) 0b1100,null, packet -> {return null;}),
    C1101((byte) 0b1101,null, packet -> {return null;}),
    C1110((byte) 0b1110,null, packet -> {return null;}),
    C1111((byte) 0b1111,ExceptionPacket.class, packet -> {return null;});

    private final byte value;
    private final Class<? extends Packet> clazz;
    private final HandlingFunc func;

    PacketType(byte value, Class<? extends Packet> clazz, HandlingFunc func) {
        this.value = value;
        this.clazz = clazz;
        this.func = func;
    }

    public Class<? extends Packet> getClazz() {
        return clazz;
    }

    public HandlingFunc getFunc() {
        return func;
    }

    public byte getValue() {
        return value;
    }

    public static PacketType get(boolean input, Byte value){
        if(value > 0b1111 || value < 0b0000) throw new IllegalArgumentException("value не может выходить за [0, 15]");
        String name = (input ? "S" : "C") + value;
        return PacketType.valueOf(name);
    }

    public static PacketType getByClass(Class<? extends Packet> clazz){
        for(PacketType type : PacketType.values()){
            if(type.clazz.equals(clazz)) return type;
        }
        return null;
    }
}

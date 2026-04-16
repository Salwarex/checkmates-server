package utmn.checkmates.server.network.packet;

import utmn.checkmates.server.Application;
import utmn.checkmates.server.game.session.Session;
import utmn.checkmates.server.network.packet.input.*;
import utmn.checkmates.server.network.packet.output.*;
import utmn.checkmates.server.network.tcp.SessionConnection;
import utmn.checkmates.server.network.tcp.SessionConnectionsManager;
import utmn.checkmates.server.utility.logger.Logger;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public enum PacketType {
    //ОБРАЩЕНИЯ К СЕРВЕРУ (ОБРАБАТЫВАЕМЫЕ)
    //пинг
    S0000((byte) 0b0000, PingRequestPacket.class, packet -> {
        if(!(packet instanceof PingRequestPacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        Socket socket = input.getSocket();
        List<Socket> response = List.of(socket);
        boolean result = Application.getServer().getConnectionsManager().ping(input.getSocket());

        return List.of(new PingResponsePacket(response)); //todo: заменить на сокеты;(
    }),
    
    //подключение к серверу
    S0001((byte) 0b0001, ServerConnectionPacket.class, packet -> {
        if(!(packet instanceof ServerConnectionPacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        Socket socket = input.getSocket();
        List<Socket> response = List.of(socket);

        SessionConnectionsManager manager = Application.getServer().getConnectionsManager();
        Session session = manager.getSessions().get(input.getSessionId());

        return List.of(new ClientConnectionPacket(response, -1, (byte) 0),
                new OpponentUpdatePacket(List.of(), false, true));
    }),
    
    //обновление статуса игрока
    S0010((byte) 0b0010, PlayerUpdatePacket.class, packet -> {
        if(!(packet instanceof PlayerUpdatePacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return unknownHandlerError(packet.getSocket());
    }),
    
    //зарезервирован
    S0011((byte) 0b0011,null, packet -> unknownHandlerError(packet.getSocket())),
    
    //движение фигуры
    S0100((byte) 0b0100,MovePacket.class, packet -> {
        if(!(packet instanceof MovePacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return unknownHandlerError(packet.getSocket());
    }),
    
    //сдача игрока
    S0101((byte) 0b0101,ResignPacket.class, packet -> {
        if(!(packet instanceof ResignPacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return unknownHandlerError(packet.getSocket());
    }),
    
    //предложение ничьей
    S0110((byte) 0b0110,DrawOfferPacket.class, packet -> {
        if(!(packet instanceof DrawOfferPacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return unknownHandlerError(packet.getSocket());
    }),
    
    //ответ на предложение ничьей
    S0111((byte) 0b0111,DrawResponsePacket.class, packet -> {
        if(!(packet instanceof DrawResponsePacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        return unknownHandlerError(packet.getSocket());
    }),
    
    //"мягкое" отключение
    S1000((byte) 0b1000,DisconnectPacket.class, packet -> {
        if(!(packet instanceof DisconnectPacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");

        Socket socket = input.getSocket();
        List<Socket> response = List.of();

        SessionConnectionsManager scm = Application.getServer().getConnectionsManager();
        //SessionConnection connection = scm.getSessions().get(input.getSessionId()).getById(input.getClientId());
        scm.closeSessionConnection(socket);
        //todo: потом сделать так, чтобы был реконнект адекватный

        return List.of(new OpponentUpdatePacket(response, false, false));
    }),
    
    //запрос списка активных сессий
    S1001((byte) 0b1001,SessionListRequestPacket.class, packet -> {
        if(!(packet instanceof SessionListRequestPacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");

        Socket socket = input.getSocket();
        List<Socket> response = List.of(socket);

        return List.of(new SessionListResponsePacket(response,
                Application.getServer().getConnectionsManager().getSessions().values().stream().map(Session::getDto).toList())
        );
    }),
    
    //создание сессии
    S1010((byte) 0b1010, CreateSessionPacket.class, packet -> {
        if(!(packet instanceof CreateSessionPacket input))
            throw new HandlingException("Представленный пакет не соответствует необходимому типу!");
        Socket socket = input.getSocket();
        List<Socket> response = List.of(socket);

        Session session = Application.getServer().getConnectionsManager().createSession();

        return List.of(new CreateSessionResponsePacket(response, session.getDto()));
    }),
    
    //зарезервирован
    S1011((byte) 0b1011,null, packet ->  unknownHandlerError(packet.getSocket())),

    //зарезервирован
    S1100((byte) 0b1100,null, packet -> unknownHandlerError(packet.getSocket())),

    //зарезервирован
    S1101((byte) 0b1101,null, packet -> unknownHandlerError(packet.getSocket())),

    //зарезервирован
    S1110((byte) 0b1110,null, packet -> unknownHandlerError(packet.getSocket())),

    //зарезервирован
    S1111((byte) 0b1111,null, packet -> unknownHandlerError(packet.getSocket())),

    //ОБРАЩЕНИЯ К КЛИЕНТУ
    //ответ на пинг
    C0000((byte) 0b0000,PingResponsePacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //подключение к клиенту
    C0001((byte) 0b0001,ClientConnectionPacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //обновление оппонента
    C0010((byte) 0b0010,OpponentUpdatePacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //статус запуска игры
    C0011((byte) 0b0011,GameStartPacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //обновлене игры
    C0100((byte) 0b0100,GameUpdatePacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //недопустимый ход
    C0101((byte) 0b0101,IllegalMovePacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //промежуточный пакет о решении по ничьей
    C0110((byte) 0b0110,DrawDecisionPacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //завершение игры
    C0111((byte) 0b0111,GameOverPacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //пауза
    C1000((byte) 0b1000,PausePacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //получение списка сессий на клиенте
    C1001((byte) 0b1001,SessionListResponsePacket.class, packet -> unknownHandlerError(packet.getSocket())),
    
    //Ответное сообщение на создание сессии
    C1010((byte) 0b1010,CreateSessionResponsePacket.class, packet -> unknownHandlerError(packet.getSocket())),

    //зарезервирован
    C1011((byte) 0b1011,null, packet -> unknownHandlerError(packet.getSocket())),

    //зарезервирован
    C1100((byte) 0b1100,null, packet -> unknownHandlerError(packet.getSocket())),

    //зарезервирован
    C1101((byte) 0b1101,null, packet -> unknownHandlerError(packet.getSocket())),

    //зарезервирован
    C1110((byte) 0b1110,null, packet -> unknownHandlerError(packet.getSocket())),

    //сообщение об ошибке
    C1111((byte) 0b1111,ExceptionPacket.class, packet -> unknownHandlerError(packet.getSocket()));

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
        String name = (input ? "S" : "C") + String.format("%4s", Integer.toBinaryString(value & 0b1111)).replace(' ', '0');
        return PacketType.valueOf(name);
    }

    public static PacketType getByClass(Class<? extends Packet> clazz){
        Logger.log("PacketType", "getByClass",
                "Поиск подходящего типа для класса: %s".formatted(clazz));

        for(PacketType type : PacketType.values()){
            if(clazz.equals(type.clazz)) return type;
        }
        return null;
    }
    
    private static List<OutputPacket> unknownHandlerError(Socket socket){
        return List.of(new ExceptionPacket(
                List.of(socket),
                4,
                "Обработчик для данного пакета не предусмотрен"
        ));
    }
}

package utmn.checkmates.server.network.packet;

import utmn.checkmates.server.Application;
import utmn.checkmates.server.network.packet.input.InputPacket;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.utility.logger.Logger;

import java.net.InetAddress;

public class PacketHandler {
    public static OutputPacket handle(InetAddress sourceAddress, InetAddress destinationAddress, byte messageType, String json)
            throws HandlingException{
        PacketType type = PacketType.get(true, messageType);

        InputPacket inputPacket = (InputPacket) Application.getGson().fromJson(json, type.getClazz());
        Logger.log("PacketHandler",
                "handle",
                "Получен входной пакет: %s%s".formatted(inputPacket.getClass().getSimpleName(), inputPacket.toJson())
        );

        OutputPacket outPacket = type.getFunc().handle(inputPacket);

        Logger.log("PacketHandler",
                "handle",
                "Создан ответный пакет: %s%s".formatted(outPacket.getClass().getSimpleName(), outPacket.toJson())
        );

        return outPacket;
    }
}

package utmn.checkmates.server.network.packet;

import utmn.checkmates.server.Application;
import utmn.checkmates.server.network.packet.input.InputPacket;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.utility.FormatUtils;
import utmn.checkmates.server.utility.logger.Logger;

import java.net.InetAddress;
import java.util.List;

public class PacketHandler {
    public static PacketSet handle(InetAddress sourceAddress, byte messageType, String json)
            throws HandlingException{
        PacketType type = PacketType.get(true, messageType);
        InputPacket inputPacket = (InputPacket) Application.getGson().fromJson(json, type.getClazz());
        inputPacket.setSourceAddress(sourceAddress);
        Logger.log("PacketHandler",
                "handle",
                "Получен входной пакет: %s%s".formatted(inputPacket.getClass().getSimpleName(), inputPacket.toJson())
        );

        List<OutputPacket> outPacket = type.getFunc().handle(inputPacket);

        Logger.log("PacketHandler",
                "handle",
                "Пакет успешно обработан. Созданы ответный пакет: %s".formatted(FormatUtils.listOutputs(outPacket))
        );

        return new PacketSet(inputPacket, outPacket);
    }

    public static class PacketSet{
        private final InputPacket input;
        private final List<OutputPacket> output;

        public PacketSet(InputPacket input, List<OutputPacket> output) {
            this.input = input;
            this.output = output;
        }

        public InputPacket getInput() {
            return input;
        }

        public List<OutputPacket> getOutput() {
            return output;
        }
    }
}

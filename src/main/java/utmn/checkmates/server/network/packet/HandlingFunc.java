package utmn.checkmates.server.network.packet;

import utmn.checkmates.server.network.packet.input.InputPacket;
import utmn.checkmates.server.network.packet.output.OutputPacket;

import java.util.List;

@FunctionalInterface
public interface HandlingFunc {
    List<OutputPacket> handle(InputPacket packet) throws HandlingException;
}
